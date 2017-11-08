package com.android.contacts.model;

import android.content.AsyncTaskLoader;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader.ForceLoadContentObserver;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Directory;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import com.android.contacts.GeoUtil;
import com.android.contacts.GroupMetaData;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.numbermark.YellowPageContactUtil;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountTypeWithDataSet;
import com.android.contacts.model.dataitem.DataItem;
import com.android.contacts.model.dataitem.PhoneDataItem;
import com.android.contacts.model.dataitem.PhotoDataItem;
import com.android.contacts.util.ContactLoaderUtils;
import com.android.contacts.util.DataStatus;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.UriUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.huawei.cspcommon.performance.PLog;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContactLoader extends AsyncTaskLoader<Contact> {
    private static final String TAG = ContactLoader.class.getSimpleName();
    private static Contact sCachedResult = null;
    private boolean mComputeFormattedPhoneNumber;
    private Contact mContact;
    private ContactLoadedListener mContactLoadedListener;
    private boolean mFromEditor;
    private boolean mLoadGroupMetaData;
    private boolean mLoadInvitableAccountTypes;
    private Uri mLookupUri;
    private final Set<Long> mNotifiedRawContactIds;
    private ForceLoadContentObserver mObserver;
    private boolean mPostViewNotification;
    private final Uri mRequestedUri;

    public interface ContactLoadedListener {
        void onContactLoaded(Contact contact);
    }

    private static class DirectoryQuery {
        static final String[] COLUMNS = new String[]{"displayName", "packageName", "typeResourceId", "accountType", "accountName", "exportSupport"};

        private DirectoryQuery() {
        }
    }

    private static class GroupQuery {
        static final String[] COLUMNS = new String[]{"account_name", "account_type", "data_set", "account_type_and_data_set", "_id", "title", "auto_add", "favorites", "sync4", "title_res", "res_package", "sync1"};

        private GroupQuery() {
        }
    }

    public void setContactLoadedListener(ContactLoadedListener listener) {
        this.mContactLoadedListener = listener;
    }

    public ContactLoader(Context context, Uri lookupUri, boolean postViewNotification) {
        this(context, lookupUri, false, false, postViewNotification, false);
    }

    public ContactLoader(Context context, Uri lookupUri, boolean loadGroupMetaData, boolean loadInvitableAccountTypes, boolean postViewNotification, boolean computeFormattedPhoneNumber) {
        super(context);
        this.mContactLoadedListener = null;
        this.mNotifiedRawContactIds = Sets.newHashSet();
        this.mLookupUri = lookupUri;
        this.mRequestedUri = lookupUri;
        this.mLoadGroupMetaData = loadGroupMetaData;
        this.mLoadInvitableAccountTypes = loadInvitableAccountTypes;
        this.mPostViewNotification = postViewNotification;
        this.mComputeFormattedPhoneNumber = computeFormattedPhoneNumber;
    }

    public ContactLoader(Context context, Uri lookupUri, boolean postViewNotification, boolean fromEditor) {
        this(context, lookupUri, false, false, postViewNotification, false);
        this.mFromEditor = fromEditor;
    }

    public Contact loadInBackground() {
        long detialTime = System.currentTimeMillis();
        try {
            ContentResolver resolver = getContext().getContentResolver();
            Uri uriCurrentFormat = ContactLoaderUtils.ensureIsContactUri(resolver, this.mLookupUri);
            Contact cachedResult = sCachedResult;
            resetCacheResult();
            if (uriCurrentFormat == null) {
                return Contact.forNotFound(null);
            }
            Contact result;
            boolean resultIsCached;
            if (cachedResult == null || !UriUtils.areEqual(cachedResult.getLookupUri(), this.mLookupUri)) {
                result = loadContactEntity(resolver, uriCurrentFormat);
                resultIsCached = false;
            } else {
                result = new Contact(this.mRequestedUri, cachedResult);
                resultIsCached = true;
            }
            if (result.isLoaded()) {
                if (result.isDirectoryEntry()) {
                    if (!resultIsCached) {
                        loadDirectoryMetaData(result);
                    }
                } else if (this.mLoadGroupMetaData && result.getGroupMetaData() == null) {
                    loadGroupMetaData(result);
                }
                if (this.mComputeFormattedPhoneNumber) {
                    computeFormattedPhoneNumbers(result);
                }
                if (!resultIsCached) {
                    if (YellowPageContactUtil.isYellowPageUri(this.mLookupUri)) {
                        YellowPageContactUtil.loadPhotoBinaryData(result);
                    } else {
                        loadPhotoBinaryData(result);
                    }
                }
                if (this.mLoadInvitableAccountTypes && result.getInvitableAccountTypes() == null) {
                    loadInvitableAccountTypes(result);
                }
            }
            if (this.mContactLoadedListener != null) {
                this.mContactLoadedListener.onContactLoaded(result);
            }
            if (PLog.DEBUG) {
                PLog.d(0, "ContactLoader loadInBackground, cost = " + (System.currentTimeMillis() - detialTime));
            }
            return result;
        } catch (Exception e) {
            HwLog.e(TAG, "Error loading the contact: " + this.mLookupUri, e);
            return Contact.forError(this.mRequestedUri, e);
        }
    }

    private Contact loadContactEntity(ContentResolver resolver, Uri contactUri) {
        if (YellowPageContactUtil.isYellowPageUri(contactUri)) {
            return YellowPageContactUtil.loadContactEntityFromYellowPage(resolver, contactUri);
        }
        return loadContactEntityFromContactsProvider(resolver, contactUri);
    }

    private Contact loadContactEntityFromContactsProvider(ContentResolver resolver, Uri contactUri) {
        String[] projection;
        Uri entityUri = Uri.withAppendedPath(contactUri, "entities");
        String[] lTempProjection = EmuiFeatureManager.isPrivacyFeatureEnabled() ? ContactQuery.getColumnPrivate() : ContactQuery.getColumns();
        if (QueryUtil.isHAPProviderInstalled()) {
            projection = new String[(lTempProjection.length + 1)];
            System.arraycopy(lTempProjection, 0, projection, 0, lTempProjection.length);
            projection[projection.length - 1] = "raw_contact_custom_ringtone";
        } else {
            projection = lTempProjection;
        }
        Cursor cursor = resolver.query(entityUri, projection, null, null, "raw_contact_id");
        if (cursor == null) {
            HwLog.e(TAG, "No cursor returned in loadContactEntity");
            return Contact.forNotFound(this.mRequestedUri);
        }
        try {
            if (cursor.moveToFirst()) {
                Contact contact = loadContactHeaderData(cursor, contactUri);
                long currentRawContactId = -1;
                RawContact rawContact = null;
                Builder<RawContact> rawContactsBuilder = new Builder();
                ImmutableMap.Builder<Long, DataStatus> statusesBuilder = new ImmutableMap.Builder();
                while (true) {
                    long rawContactId = cursor.getLong(14);
                    if (rawContactId != currentRawContactId) {
                        currentRawContactId = rawContactId;
                        RawContact rawContact2 = new RawContact(loadRawContactValues(cursor));
                        rawContactsBuilder.add((Object) rawContact2);
                    }
                    if (!cursor.isNull(27)) {
                        ContentValues data = loadDataValues(cursor);
                        if (rawContact != null) {
                            rawContact.addDataItemValues(data);
                        }
                        if (!(cursor.isNull(53) && cursor.isNull(55))) {
                            statusesBuilder.put(Long.valueOf(cursor.getLong(27)), new DataStatus(cursor));
                        }
                    }
                    if (!cursor.moveToNext()) {
                        contact.setRawContacts(rawContactsBuilder.build());
                        contact.setStatuses(statusesBuilder.build());
                        cursor.close();
                        return contact;
                    }
                }
            }
            Contact forNotFound = Contact.forNotFound(this.mRequestedUri);
            return forNotFound;
        } finally {
            cursor.close();
        }
    }

    private void loadPhotoBinaryData(Contact contactData) {
        String photoUri = contactData.getPhotoUri();
        if (photoUri != null) {
            AssetFileDescriptor fd;
            FileInputStream fis;
            try {
                fd = getContext().getContentResolver().openAssetFileDescriptor(Uri.parse(photoUri), "r");
                if (fd != null) {
                    byte[] buffer = new byte[16384];
                    fis = fd.createInputStream();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    while (true) {
                        int size = fis.read(buffer);
                        if (size != -1) {
                            baos.write(buffer, 0, size);
                        } else {
                            contactData.setPhotoBinaryData(baos.toByteArray());
                            fis.close();
                            fd.close();
                            return;
                        }
                    }
                }
                return;
            } catch (IOException e) {
            } catch (Throwable th) {
                fis.close();
                fd.close();
            }
        }
        long photoId = contactData.getPhotoId();
        if (photoId > 0) {
            for (RawContact rawContact : contactData.getRawContacts()) {
                for (DataItem dataItem : rawContact.getDataItems()) {
                    if (dataItem.getId() == photoId) {
                        if (dataItem instanceof PhotoDataItem) {
                            contactData.setPhotoBinaryData(((PhotoDataItem) dataItem).getPhoto());
                        }
                    }
                }
            }
        }
    }

    private void loadInvitableAccountTypes(Contact contactData) {
        Builder<AccountType> resultListBuilder = new Builder();
        if (!contactData.isUserProfile()) {
            Map<AccountTypeWithDataSet, AccountType> invitables = AccountTypeManager.getInstance(getContext()).getUsableInvitableAccountTypes();
            if (!invitables.isEmpty()) {
                Map<AccountTypeWithDataSet, AccountType> resultMap = Maps.newHashMap(invitables);
                for (RawContact rawContact : contactData.getRawContacts()) {
                    resultMap.remove(AccountTypeWithDataSet.get(rawContact.getAccountTypeString(), rawContact.getDataSet()));
                }
                resultListBuilder.addAll(resultMap.values());
            }
        }
        contactData.setInvitableAccountTypes(resultListBuilder.build());
    }

    private Contact loadContactHeaderData(Cursor cursor, Uri contactUri) {
        long directoryId;
        Integer num;
        Uri lookupUri;
        String customRingtone;
        String directoryParameter = contactUri.getQueryParameter("directory");
        if (directoryParameter == null) {
            directoryId = 0;
        } else {
            directoryId = Long.parseLong(directoryParameter);
        }
        long contactId = cursor.getLong(13);
        String lookupKey = cursor.getString(2);
        long nameRawContactId = cursor.getLong(0);
        int displayNameSource = cursor.getInt(1);
        String displayName = cursor.getString(3);
        String altDisplayName = cursor.getString(4);
        String phoneticName = cursor.getString(5);
        long photoId = cursor.getLong(6);
        String photoUri = cursor.getString(60);
        boolean starred = cursor.getInt(7) != 0;
        if (cursor.isNull(8)) {
            num = null;
        } else {
            num = Integer.valueOf(cursor.getInt(8));
        }
        boolean sendToVoicemail = cursor.getInt(61) == 1;
        boolean isUserProfile = cursor.getInt(63) == 1;
        String sortkey = cursor.getString(64);
        if (directoryId == 0 || directoryId == 1) {
            lookupUri = ContentUris.withAppendedId(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey), contactId);
        } else {
            lookupUri = contactUri;
        }
        boolean isPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            isPrivateContact = cursor.getInt(65) == 1;
        }
        int lRingtoneColumn = cursor.getColumnIndex("raw_contact_custom_ringtone");
        if (lRingtoneColumn <= -1 || lRingtoneColumn >= cursor.getColumnCount()) {
            customRingtone = cursor.getString(62);
        } else {
            customRingtone = cursor.getString(lRingtoneColumn);
            while (TextUtils.isEmpty(customRingtone) && cursor.moveToNext()) {
                customRingtone = cursor.getString(lRingtoneColumn);
            }
            cursor.moveToFirst();
        }
        return new Contact(this.mRequestedUri, contactUri, lookupUri, directoryId, lookupKey, contactId, nameRawContactId, displayNameSource, photoId, photoUri, displayName, altDisplayName, phoneticName, starred, num, sendToVoicemail, customRingtone, isUserProfile, isPrivateContact, sortkey);
    }

    private ContentValues loadRawContactValues(Cursor cursor) {
        ContentValues cv = new ContentValues();
        cv.put("_id", Long.valueOf(cursor.getLong(14)));
        cursorColumnToContentValues(cursor, cv, 15);
        cursorColumnToContentValues(cursor, cv, 16);
        cursorColumnToContentValues(cursor, cv, 17);
        cursorColumnToContentValues(cursor, cv, 18);
        cursorColumnToContentValues(cursor, cv, 19);
        cursorColumnToContentValues(cursor, cv, 20);
        cursorColumnToContentValues(cursor, cv, 21);
        cursorColumnToContentValues(cursor, cv, 22);
        cursorColumnToContentValues(cursor, cv, 23);
        cursorColumnToContentValues(cursor, cv, 24);
        cursorColumnToContentValues(cursor, cv, 25);
        cursorColumnToContentValues(cursor, cv, 26);
        cursorColumnToContentValues(cursor, cv, 13);
        cursorColumnToContentValues(cursor, cv, 7);
        cursorColumnToContentValues(cursor, cv, 64);
        int lRingtoneColumn = cursor.getColumnIndex("raw_contact_custom_ringtone");
        if (lRingtoneColumn > -1 && lRingtoneColumn < cursor.getColumnCount()) {
            cv.put("custom_ringtone", cursor.getString(lRingtoneColumn));
        }
        if (this.mFromEditor) {
            Cursor lDisplayNameCursor = getContext().getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"display_name"}, "_id=?", new String[]{cursor.getLong(14) + ""}, null);
            if (lDisplayNameCursor != null) {
                try {
                    if (lDisplayNameCursor.moveToFirst()) {
                        cv.put("raw_contact_display_name", lDisplayNameCursor.getString(lDisplayNameCursor.getColumnIndex("display_name")));
                    }
                    lDisplayNameCursor.close();
                } catch (Throwable th) {
                    lDisplayNameCursor.close();
                }
            }
        }
        return cv;
    }

    private ContentValues loadDataValues(Cursor cursor) {
        ContentValues cv = new ContentValues();
        cv.put("_id", Long.valueOf(cursor.getLong(27)));
        cursorColumnToContentValues(cursor, cv, 28);
        cursorColumnToContentValues(cursor, cv, 29);
        cursorColumnToContentValues(cursor, cv, 30);
        cursorColumnToContentValues(cursor, cv, 31);
        cursorColumnToContentValues(cursor, cv, 32);
        cursorColumnToContentValues(cursor, cv, 33);
        cursorColumnToContentValues(cursor, cv, 34);
        cursorColumnToContentValues(cursor, cv, 35);
        cursorColumnToContentValues(cursor, cv, 36);
        cursorColumnToContentValues(cursor, cv, 37);
        cursorColumnToContentValues(cursor, cv, 38);
        cursorColumnToContentValues(cursor, cv, 39);
        cursorColumnToContentValues(cursor, cv, 40);
        cursorColumnToContentValues(cursor, cv, 41);
        cursorColumnToContentValues(cursor, cv, 42);
        cursorColumnToContentValues(cursor, cv, 43);
        cursorColumnToContentValues(cursor, cv, 44);
        cursorColumnToContentValues(cursor, cv, 45);
        cursorColumnToContentValues(cursor, cv, 46);
        cursorColumnToContentValues(cursor, cv, 47);
        cursorColumnToContentValues(cursor, cv, 48);
        cursorColumnToContentValues(cursor, cv, 49);
        cursorColumnToContentValues(cursor, cv, 50);
        cursorColumnToContentValues(cursor, cv, 51);
        cursorColumnToContentValues(cursor, cv, 52);
        cursorColumnToContentValues(cursor, cv, 54);
        return cv;
    }

    private void cursorColumnToContentValues(Cursor cursor, ContentValues values, int index) {
        switch (cursor.getType(index)) {
            case 0:
                return;
            case 1:
                values.put(ContactQuery.getColumns()[index], Long.valueOf(cursor.getLong(index)));
                return;
            case 3:
                values.put(ContactQuery.getColumns()[index], cursor.getString(index));
                return;
            case 4:
                values.put(ContactQuery.getColumns()[index], cursor.getBlob(index));
                return;
            default:
                throw new IllegalStateException("Invalid or unhandled data type");
        }
    }

    private void loadDirectoryMetaData(Contact result) {
        Cursor cursor = getContext().getContentResolver().query(ContentUris.withAppendedId(Directory.CONTENT_URI, result.getDirectoryId()), DirectoryQuery.COLUMNS, null, null, null);
        if (cursor != null) {
            String packageName;
            int typeResourceId;
            try {
                if (cursor.moveToFirst()) {
                    String displayName = cursor.getString(0);
                    packageName = cursor.getString(1);
                    typeResourceId = cursor.getInt(2);
                    String accountType = cursor.getString(3);
                    String accountName = cursor.getString(4);
                    int exportSupport = cursor.getInt(5);
                    String directoryType = null;
                    if (!TextUtils.isEmpty(packageName)) {
                        directoryType = getContext().getPackageManager().getResourcesForApplication(packageName).getString(typeResourceId);
                    }
                    result.setDirectoryMetaData(displayName, directoryType, accountType, accountName, exportSupport);
                }
            } catch (NameNotFoundException e) {
                HwLog.w(TAG, "Contact directory resource not found: " + packageName + "." + typeResourceId);
            } catch (Throwable th) {
                cursor.close();
            }
            cursor.close();
        }
    }

    private void loadGroupMetaData(Contact result) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList();
        for (RawContact rawContact : result.getRawContacts()) {
            String accountName = rawContact.getAccountName();
            String accountType = rawContact.getAccountTypeString();
            String dataSet = rawContact.getDataSet();
            if (!(accountName == null || accountType == null)) {
                if (selection.length() != 0) {
                    selection.append(" OR ");
                }
                selection.append("(account_name=? AND account_type=?");
                selectionArgs.add(accountName);
                selectionArgs.add(accountType);
                if (dataSet != null) {
                    selection.append(" AND data_set=?");
                    selectionArgs.add(dataSet);
                } else {
                    selection.append(" AND data_set IS NULL");
                }
                selection.append(" AND ").append("deleted").append(" = 0");
                selection.append(")");
            }
        }
        Builder<GroupMetaData> groupListBuilder = new Builder();
        Cursor cursor = getContext().getContentResolver().query(Groups.CONTENT_URI, GroupQuery.COLUMNS, selection.toString(), (String[]) selectionArgs.toArray(new String[0]), null);
        while (cursor.moveToNext()) {
            boolean defaultGroup;
            accountName = cursor.getString(0);
            accountType = cursor.getString(1);
            dataSet = cursor.getString(2);
            long groupId = cursor.getLong(4);
            String title = cursor.getString(5);
            if (cursor.isNull(6)) {
                defaultGroup = false;
            } else {
                try {
                    defaultGroup = cursor.getInt(6) != 0;
                } finally {
                    cursor.close();
                }
            }
            boolean favorites = cursor.isNull(7) ? false : cursor.getInt(7) != 0;
            groupListBuilder.add(new GroupMetaData(accountName, accountType, dataSet, groupId, title, defaultGroup, favorites, cursor.getString(11), cursor.getString(8), cursor.getInt(9), cursor.getString(10)));
        }
        result.setGroupMetaData(groupListBuilder.build());
    }

    private void computeFormattedPhoneNumbers(Contact contactData) {
        String countryIso = GeoUtil.getCurrentCountryIso(getContext());
        ImmutableList<RawContact> rawContacts = contactData.getRawContacts();
        int rawContactCount = rawContacts.size();
        for (int rawContactIndex = 0; rawContactIndex < rawContactCount; rawContactIndex++) {
            List<DataItem> dataItems = ((RawContact) rawContacts.get(rawContactIndex)).getDataItems();
            int dataCount = dataItems.size();
            for (int dataIndex = 0; dataIndex < dataCount; dataIndex++) {
                DataItem dataItem = (DataItem) dataItems.get(dataIndex);
                if (dataItem instanceof PhoneDataItem) {
                    ((PhoneDataItem) dataItem).computeFormattedPhoneNumber(countryIso);
                }
            }
        }
    }

    public void deliverResult(Contact result) {
        unregisterObserver();
        if (!isReset() && result != null) {
            this.mContact = result;
            if (result.isLoaded()) {
                this.mLookupUri = result.getLookupUri();
                if (!result.isDirectoryEntry()) {
                    HwLog.i(TAG, "Registering content observer for " + this.mLookupUri);
                    if (this.mObserver == null) {
                        this.mObserver = new ForceLoadContentObserver(this);
                    }
                    getContext().getContentResolver().registerContentObserver(this.mLookupUri, true, this.mObserver);
                }
                if (this.mPostViewNotification) {
                    postViewNotificationToSyncAdapter();
                }
            }
            super.deliverResult(this.mContact);
        }
    }

    private void postViewNotificationToSyncAdapter() {
        Context context = getContext();
        for (RawContact rawContact : this.mContact.getRawContacts()) {
            long rawContactId = rawContact.getId().longValue();
            if (!this.mNotifiedRawContactIds.contains(Long.valueOf(rawContactId))) {
                this.mNotifiedRawContactIds.add(Long.valueOf(rawContactId));
                AccountType accountType = rawContact.getAccountType(context);
                String serviceName = accountType.getViewContactNotifyServiceClassName();
                String servicePackageName = accountType.getViewContactNotifyServicePackageName();
                if (!(TextUtils.isEmpty(serviceName) || TextUtils.isEmpty(servicePackageName))) {
                    Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
                    Intent intent = new Intent();
                    intent.setClassName(servicePackageName, serviceName);
                    intent.setAction("android.intent.action.VIEW");
                    intent.setDataAndType(uri, "vnd.android.cursor.item/raw_contact");
                    try {
                        context.startService(intent);
                    } catch (Exception e) {
                        HwLog.e(TAG, "Error sending message to source-app", e);
                    }
                }
            }
        }
    }

    private void unregisterObserver() {
        if (this.mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(this.mObserver);
            this.mObserver = null;
        }
    }

    public Uri getLookupUri() {
        return this.mLookupUri;
    }

    protected void onStartLoading() {
        if (this.mContact != null) {
            deliverResult(this.mContact);
        }
        if (takeContentChanged() || this.mContact == null) {
            forceLoad();
        }
    }

    protected void onStopLoading() {
        cancelLoad();
    }

    protected void onReset() {
        super.onReset();
        cancelLoad();
        unregisterObserver();
        this.mContact = null;
    }

    private synchronized void resetCacheResult() {
        sCachedResult = null;
    }
}
