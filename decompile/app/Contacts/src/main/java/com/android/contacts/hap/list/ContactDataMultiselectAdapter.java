package com.android.contacts.hap.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.Contactables;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.compatibility.PhoneCompat;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.hwsearch.HwSearchCursor;
import com.android.contacts.hap.rcs.list.RcsContactDataMultiselectAdapter;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.list.MultiCursor;
import com.android.contacts.preference.ContactsPreferences;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.SearchContract$ContactsSearch;
import com.huawei.cspcommon.util.SearchContract$DataQuery;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ContactDataMultiselectAdapter extends DataListAdapter {
    private static int CHINA_PHONE_NUMBER_LENGTH = 11;
    private ContactMultiSelectionActivity localActivityRef;
    private Context mContext;
    private Cursor mCursor;
    private HwCustContactDataMultiselectAdapter mCust = null;
    private DataListFilter mDataFilter;
    private boolean mIsCountGroupMembers;
    private ListView mListView;
    private int mPredefinedSmartGroupType = -1;
    private RcsContactDataMultiselectAdapter mRcsCust = null;
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private String mSmartGroupType = null;
    private String mTitle = null;
    Uri mUri;

    public ContactDataMultiselectAdapter(Context aContext, ListView aListView) {
        super(aContext);
        this.mContext = aContext;
        this.mListView = aListView;
        this.localActivityRef = (ContactMultiSelectionActivity) this.mContext;
        CommonUtilMethods.addFootEmptyViewPortrait(this.mListView, this.mContext);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustContactDataMultiselectAdapter) HwCustUtils.createObj(HwCustContactDataMultiselectAdapter.class, new Object[]{this.mContext});
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsContactDataMultiselectAdapter(this.mContext);
            this.mRcsCust.initService();
        }
    }

    public void configureLoader(CursorLoader aLoader, long aDirectoryId) {
        this.mIsCountGroupMembers = false;
        if (!isSearchMode()) {
            configureUri(aLoader, aDirectoryId);
            configureProjection(aLoader, aDirectoryId);
            configureSelection(aLoader, aDirectoryId);
        } else if (!configureLoaderForSearch(aLoader, aDirectoryId)) {
            if (QueryUtil.isProviderSupportHwSeniorSearch()) {
                configHwSearchLoader(aLoader, aDirectoryId);
                return;
            }
            configureUri(aLoader, aDirectoryId);
            configureProjection(aLoader, aDirectoryId);
            configureSelection(aLoader, aDirectoryId);
        }
        if (1 == new ContactsPreferences(this.mContext).getSortOrder()) {
            aLoader.setSortOrder("sort_key , is_primary DESC , raw_contact_id");
        } else {
            aLoader.setSortOrder("sort_key_alt , is_primary DESC , raw_contact_id");
        }
        configureGroupby(aLoader, aDirectoryId);
    }

    private boolean configureLoaderForSearch(CursorLoader loader, long directoryId) {
        boolean canSearch = false;
        String query = getQueryString();
        if (query == null) {
            query = "";
        }
        query = query.trim();
        if (TextUtils.isEmpty(query)) {
            loader.setUri(Data.CONTENT_URI);
            loader.setProjection(PROJECTION_DATA);
            loader.setSelection("0");
        } else {
            Uri uri = Contactables.CONTENT_FILTER_URI;
            switch (this.mDataFilter.filterType) {
                case -13:
                case -4:
                case -2:
                    uri = Email.CONTENT_FILTER_URI;
                    canSearch = true;
                    break;
                case -12:
                case -3:
                case -1:
                    uri = Phone.CONTENT_FILTER_URI;
                    canSearch = true;
                    break;
                case -5:
                    uri = Contactables.CONTENT_FILTER_URI;
                    canSearch = true;
                    break;
                default:
                    canSearch = false;
                    break;
            }
            if (canSearch) {
                Builder builder = uri.buildUpon();
                builder.appendPath(query);
                builder.appendQueryParameter("directory", String.valueOf(directoryId));
                if (!(directoryId == 0 || directoryId == 1)) {
                    builder.appendQueryParameter("limit", String.valueOf(getDirectoryResultLimit()));
                }
                loader.setUri(builder.build());
                loader.setProjection(PROJECTION_DATA);
                configureSelection(loader, directoryId);
            }
        }
        return canSearch;
    }

    protected void bindView(View aItemView, int aPartition, Cursor aCursor, int aPosition) {
        super.bindView(aItemView, aPartition, aCursor, aPosition);
        ContactListItemView view = (ContactListItemView) aItemView;
        if (isSearchMode()) {
            view.setSearchMatchType(aCursor);
        }
        view.setHighlightedPrefix(isSearchMode() ? getLowerCaseQueryString() : null);
        int prevPos = aPosition - 1;
        boolean isFirstEntry = true;
        long currentContactId = aCursor.getLong(0);
        if (prevPos >= 0 && aCursor.moveToPosition(prevPos) && currentContactId == aCursor.getLong(0)) {
            isFirstEntry = false;
        }
        aCursor.moveToPosition(aPosition);
        bindSectionHeaderAndDivider(view, aPosition, aCursor);
        if (isFirstEntry) {
            bindName(view, aCursor);
            bindPhoto(view, aPartition, aCursor);
        } else {
            view.hideDisplayName();
            if (ContactDisplayUtils.isSimpleDisplayMode()) {
                view.removePhotoView();
            } else {
                view.removePhotoView(true, false);
            }
        }
        bindCheckBox(view);
        if (this.mDataFilter.filterType != -11) {
            this.mListView.setItemChecked((isSearchMode() ? 1 : 0) + aPosition, this.localActivityRef.mSelectedDataUris.contains(Uri.withAppendedPath(Data.CONTENT_URI, aCursor.getString(1))));
            bindSnippet(view, aCursor);
        } else {
            this.mListView.setItemChecked((isSearchMode() ? 1 : 0) + aPosition, this.localActivityRef.mSelectedDataUris.contains(Uri.withAppendedPath(Contacts.CONTENT_URI, aCursor.getString(0))));
            if (isSearchMode()) {
                bindSearchSnippetInfo(view, aCursor);
            } else {
                view.setSnippet(null);
            }
        }
        if (isSearchMode()) {
            view.showCompany(aCursor, 12, -1);
        } else {
            view.setCompany(null);
        }
    }

    private void configureGroupby(CursorLoader aLoader, long aDirectoryId) {
        switch (this.mDataFilter.filterType) {
            case -11:
                this.mIsCountGroupMembers = true;
                return;
            case -2:
            case -1:
                if (aLoader instanceof ContactMultiselectListLoader) {
                    aLoader.setSortOrder(aLoader.getSortOrder() + ", " + "data1");
                }
                this.mIsCountGroupMembers = true;
                return;
            default:
                return;
        }
    }

    private void configureUri(CursorLoader aLoader, long aDirectoryId) {
        if (this.mDataFilter == null) {
            aLoader.setUri(getLoaderUri(aLoader, aDirectoryId, -10));
        } else if (this.mDataFilter.filterType == -5) {
            aLoader.setUri(getLoaderUri(aLoader, aDirectoryId, -3));
        } else {
            aLoader.setUri(getLoaderUri(aLoader, aDirectoryId, this.mDataFilter.filterType));
        }
    }

    private Uri getLoaderUri(CursorLoader aLoader, long aDirectoryId, int filterType) {
        Uri uri = Data.CONTENT_URI;
        if (aDirectoryId == 0) {
            uri = DataListAdapter.buildSectionIndexerUri(uri);
        }
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext)) {
            uri = uri.buildUpon().appendQueryParameter("directory", String.valueOf(0)).build();
        }
        if (this.mDataFilter == null) {
            return uri;
        }
        StringBuffer sb;
        switch (this.mDataFilter.filterType) {
            case -50:
                if (this.mRcsCust == null) {
                    return uri;
                }
                sb = new StringBuffer();
                sb.append("contact_id").append(", replace(replace(").append("data1").append(", '-', ''), ' ', '')");
                return uri.buildUpon().appendQueryParameter("group_by", sb.toString()).build();
            case -11:
                sb = new StringBuffer();
                sb.append("contact_id");
                return uri.buildUpon().appendQueryParameter("group_by", sb.toString()).build();
            case -5:
            case -3:
            case -1:
                sb = new StringBuffer();
                sb.append("contact_id").append(", replace(replace(").append("data1").append(", '-', ''), ' ', '')");
                return uri.buildUpon().appendQueryParameter("group_by", sb.toString()).build();
            case -4:
            case -2:
                sb = new StringBuffer();
                sb.append("contact_id").append(',').append("data1");
                return uri.buildUpon().appendQueryParameter("group_by", sb.toString()).build();
            default:
                return uri;
        }
    }

    public void setQueryString(String queryString) {
        super.setQueryString(queryString);
    }

    private void configureProjection(CursorLoader aLoader, long aDirectoryId) {
        aLoader.setProjection(PROJECTION_DATA);
    }

    protected void configureSelection(CursorLoader aLoader, long aDirectoryId) {
        if (this.mDataFilter != null && aDirectoryId == 0) {
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            switch (this.mDataFilter.filterType) {
                case -50:
                    if (this.mRcsCust != null) {
                        this.mRcsCust.setSelectionAndSelectionArgsForCustomizations(this.mDataFilter.filterType, selection, selectionArgs);
                        break;
                    }
                    break;
                case -13:
                case -12:
                    setSelectionAndSelectionArgsForCCGroupMessage(selection, selectionArgs);
                    break;
                case -11:
                    setSelectionAndSelectionArgsForRemoveGroupMembers(selection, selectionArgs);
                    break;
                case -9:
                case -8:
                case -7:
                case -6:
                    setSelectionAndSelectionArgsForMessagePlusOrRcse(selection, selectionArgs);
                    break;
                case -5:
                case -4:
                case -3:
                    setSelectionAndSelectionArgsForMessaging(selection, selectionArgs);
                    break;
                case -2:
                case -1:
                    setSelectionAndSelectionArgsForGroupMessage(selection, selectionArgs);
                    break;
            }
            aLoader.setSelection(selection.toString());
            aLoader.setSelectionArgs((String[]) selectionArgs.toArray(new String[0]));
        }
    }

    protected void bindPhoto(ContactListItemView view, int partitionIndex, Cursor cursor) {
        int i = 4;
        if (!isPhotoSupported(partitionIndex) || ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId = cursor.getLong(4);
        boolean lIsPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            lIsPrivateContact = CommonUtilMethods.isPrivateContact(cursor);
        }
        DefaultImageRequest request = null;
        if (photoId <= 0) {
            this.mRequest.displayName = cursor.getString(2);
            this.mRequest.identifier = cursor.getString(0);
            this.mRequest.isCircular = true;
            request = this.mRequest;
        }
        ContactPhotoManager photoLoader = getPhotoLoader();
        ImageView photoView = view.getPhotoView(photoId);
        long j = cursor.getLong(0);
        if (!lIsPrivateContact) {
            i = 0;
        }
        photoLoader.loadThumbnail(photoView, photoId, false, request, j, i);
    }

    protected void bindSearchSnippetInfo(ContactListItemView view, Cursor cursor) {
        view.showSnippetInfo(cursor);
    }

    protected void bindSnippet(ContactListItemView aView, Cursor aCursor) {
        if ("vnd.android.cursor.item/himessage".equalsIgnoreCase(aCursor.getString(10)) || "vnd.android.cursor.item/rcs".equalsIgnoreCase(aCursor.getString(10))) {
            bindSnippetForMessagePlusOrRcse(aView, aCursor);
            return;
        }
        boolean z;
        aView.setAccountIcons(null);
        String primaryData = aCursor.getString(7);
        int type = aCursor.getInt(8);
        String label = aCursor.getString(9);
        if (aCursor instanceof MultiCursor) {
            z = ((MultiCursor) aCursor).getCurrentCursor() instanceof HwSearchCursor;
        } else {
            z = false;
        }
        String typeLabel = "";
        String defaultString = "";
        if (z && aCursor.getInt(11) == 1) {
            defaultString = aView.getResources().getString(R.string.contacts_default);
        } else if (aCursor.getInt(11) == 1 && aCursor.getInt(13) == 1) {
            defaultString = aView.getResources().getString(R.string.contacts_default);
        }
        if (this.mDataFilter == null || !(this.mDataFilter.filterType == -4 || this.mDataFilter.filterType == -2 || this.mDataFilter.filterType == -13)) {
            typeLabel = Phone.getTypeLabel(this.mContext.getResources(), type, label).toString();
        } else if (type == 0 && TextUtils.isEmpty(label)) {
            typeLabel = this.mContext.getResources().getString(Email.getTypeLabelResource(3));
        } else {
            typeLabel = Email.getTypeLabel(this.mContext.getResources(), type, label).toString();
        }
        if (!TextUtils.isEmpty(defaultString)) {
            typeLabel = typeLabel + HwCustPreloadContacts.EMPTY_STRING + defaultString;
        }
        aView.setSnippet(typeLabel, primaryData);
    }

    private void bindSnippetForMessagePlusOrRcse(ContactListItemView aView, Cursor aCursor) {
        aView.setSnippet(aCursor.getString(7));
        Bitmap lBitmap = null;
        if (getContext().getResources().getBoolean(R.bool.show_account_icons)) {
            if ("vnd.android.cursor.item/himessage".equalsIgnoreCase(aCursor.getString(10))) {
                lBitmap = AccountsDataManager.getInstance(this.mContext).getAccountIcon("com.huawei.himessage");
            } else if ("vnd.android.cursor.item/rcs".equalsIgnoreCase(aCursor.getString(10))) {
                lBitmap = AccountsDataManager.getInstance(this.mContext).getAccountIcon("com.huawei.rcse");
            }
            if (lBitmap != null) {
                aView.setAccountIcons(new Bitmap[]{lBitmap});
            }
        }
    }

    public void changeCursor(int aPartitionIndex, Cursor aCursor) {
        super.changeCursor(aPartitionIndex, aCursor);
        this.mCursor = aCursor;
    }

    public Uri getSelectedDataUri(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return null;
        }
        if (this.mDataFilter.filterType != -11) {
            return Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(this.mCursor.getLong(1)));
        }
        return Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(this.mCursor.getLong(0)));
    }

    public String getSelectedData(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return null;
        }
        return this.mCursor.getString(7);
    }

    public long getContactId(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        return this.mCursor.getLong(0);
    }

    public long getSelectedDataId(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        return this.mCursor.getLong(1);
    }

    public int getDataType(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        return this.mCursor.getInt(8);
    }

    public int getDataTypeByNum(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        if (!EmuiFeatureManager.isChinaArea()) {
            return this.mCursor.getInt(8);
        }
        String number = ContactsUtils.removeDashesAndBlanks(this.mCursor.getString(7));
        if (number.length() < CHINA_PHONE_NUMBER_LENGTH || !number.matches("^((\\+86)|(86)|(0086))?(1)\\d{10}$")) {
            return -1;
        }
        return 2;
    }

    public int getDataPrimary(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return -1;
        }
        return this.mCursor.getInt(11);
    }

    public void setDataFilter(DataListFilter aFilter) {
        this.mDataFilter = aFilter;
    }

    protected DataListFilter getDataFilter() {
        return this.mDataFilter;
    }

    private void setSelectionAndSelectionArgsForGroupMessage(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (this.mDataFilter.filterType == -2) {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/email_v2");
        } else {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
        aSelectionBuilder.append(" AND contact_id IN (");
        if (this.mDataFilter.groupId == -1) {
            aSelectionBuilder.append("SELECT _id FROM view_contacts WHERE starred = 1 )");
        } else if (this.mDataFilter.groupId == -2) {
            setSelectionAndSelectionArgsForSmartGroupMessage(aSelectionBuilder, aSelectionArgs);
        } else {
            aSelectionBuilder.append("SELECT contact_id FROM view_data WHERE mimetype=? AND data1 = ?");
            aSelectionBuilder.append(")");
            aSelectionArgs.add("vnd.android.cursor.item/group_membership");
            aSelectionArgs.add(String.valueOf(this.mDataFilter.groupId));
        }
    }

    private void setSelectionAndSelectionArgsForCCGroupMessage(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (this.mDataFilter.filterType == -13) {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/email_v2");
        } else {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
        aSelectionBuilder.append(" AND contact_id NOT IN (");
        aSelectionBuilder.append("SELECT _id FROM view_contacts WHERE is_camcard = 0 )");
    }

    private void setSelectionAndSelectionArgsForRemoveGroupMembers(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        aSelectionBuilder.append("mimetype").append(" = ? AND ").append("data1").append(" = ? AND ").append("raw_contact_id").append(" in (select ").append("_id").append(" from raw_contacts where ").append("deleted").append(" =0) ");
        aSelectionArgs.add("vnd.android.cursor.item/group_membership");
        aSelectionArgs.add(String.valueOf(this.mDataFilter.groupId));
    }

    private void setSelectionAndSelectionArgsForMessaging(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (this.mDataFilter.filterType == -4) {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/email_v2");
        } else if (this.mCust == null || !this.mCust.getEnableEmailContactInMms()) {
            aSelectionBuilder.append("mimetype IN (?)");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
        } else {
            this.mCust.setSelectionQueryArgs(aSelectionBuilder, aSelectionArgs);
        }
    }

    private void setSelectionAndSelectionArgsForMessagePlusOrRcse(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (this.mDataFilter.filterType == -6) {
            aSelectionBuilder.append("mimetype=?");
            aSelectionArgs.add("vnd.android.cursor.item/himessage");
        } else if (this.mDataFilter.filterType == -7) {
            aSelectionBuilder.append("mimetype=?");
            aSelectionArgs.add("vnd.android.cursor.item/rcs");
        } else if (this.mDataFilter.filterType == -8) {
            aSelectionBuilder.append("mimetype IN (?,?,?,?)");
            aSelectionArgs.add("vnd.android.cursor.item/rcs");
            aSelectionArgs.add("vnd.android.cursor.item/himessage");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
            aSelectionArgs.add("vnd.android.cursor.item/email_v2");
        } else if (this.mDataFilter.filterType == -9) {
            aSelectionBuilder.append("mimetype IN (?,?,?)");
            aSelectionArgs.add("vnd.android.cursor.item/rcs");
            aSelectionArgs.add("vnd.android.cursor.item/himessage");
            aSelectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView aView, int aPosition, Cursor aCursor) {
        CharSequence charSequence = null;
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(aPosition);
            if (aPosition != 0) {
                if (placement.count != 0) {
                    charSequence = String.valueOf(placement.count);
                }
                aView.setCountView(charSequence);
            } else if (!this.mIsCountGroupMembers || this.mCursor == null) {
                aView.setCountView(placement.count == 0 ? getContactsCount() : String.valueOf(placement.count));
            } else {
                aView.setCountView(String.valueOf(this.mCursor.getCount()));
            }
            aView.setSectionHeader(placement.sectionHeader);
            return;
        }
        aView.setSectionHeader(null);
        aView.setCountView(null);
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        return (ContactListItemView) super.newView(context, partition, cursor, position, parent);
    }

    public void setSelectedContactUri(Uri lLookupUri) {
        this.mUri = lLookupUri;
    }

    public Uri getSelectedContactUri() {
        return this.mUri;
    }

    public void setSmartGroupParameters(String[] smartGroupParameters) {
        if (smartGroupParameters != null) {
            this.mSmartGroupType = smartGroupParameters[0];
            this.mTitle = smartGroupParameters[1];
            this.mPredefinedSmartGroupType = Integer.parseInt(smartGroupParameters[2]);
        }
    }

    private void setSelectionAndSelectionArgsForSmartGroupMessage(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if ("smart_groups_company".equals(this.mSmartGroupType)) {
            setSelectionAndSelectionArgsForSmartCompanyGroupMessage(aSelectionBuilder, aSelectionArgs);
        } else if ("smart_groups_location".equals(this.mSmartGroupType)) {
            setSelectionAndSelectionArgsForSmartLocationGroupMessage(aSelectionBuilder, aSelectionArgs);
        } else if ("smart_groups_last_contact_time".equals(this.mSmartGroupType)) {
            setSelectionAndSelectionArgsForSmartLastContactTimeGroupMessage(aSelectionBuilder, aSelectionArgs);
        }
    }

    private void setSelectionAndSelectionArgsForSmartCompanyGroupMessage(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (this.mTitle == null) {
            return;
        }
        if (this.mPredefinedSmartGroupType == 1) {
            aSelectionBuilder.append("SELECT _id FROM contacts WHERE _id NOT IN ( SELECT DISTINCT contact_id FROM raw_contacts WHERE company IS NOT NULL AND deleted=0 AND REPLACE(company,\" \",\"\")<>\"\" ))");
            return;
        }
        aSelectionBuilder.append("SELECT DISTINCT contact_id FROM raw_contacts WHERE company=? AND deleted=0 )");
        aSelectionArgs.add(this.mTitle);
    }

    private void setSelectionAndSelectionArgsForSmartLocationGroupMessage(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        if (this.mTitle == null) {
            return;
        }
        if (this.mPredefinedSmartGroupType == 2) {
            aSelectionBuilder.append("SELECT _id FROM contacts WHERE _id NOT IN (SELECT DISTINCT contact_id FROM view_data WHERE data6 IS NOT NULL AND data6<>'N' AND mimetype = 'vnd.android.cursor.item/phone_v2' ))");
            return;
        }
        aSelectionBuilder.append("SELECT DISTINCT contact_id FROM view_data WHERE data6=? AND mimetype = 'vnd.android.cursor.item/phone_v2' )");
        aSelectionArgs.add(this.mTitle);
    }

    private void setSelectionAndSelectionArgsForSmartLastContactTimeGroupMessage(StringBuilder aSelectionBuilder, List<String> aSelectionArgs) {
        String curentTime = String.valueOf(System.currentTimeMillis());
        if (this.mPredefinedSmartGroupType == 3) {
            aSelectionBuilder.append("SELECT _id FROM contacts WHERE ?-last_time_contacted<=604800000)");
            aSelectionArgs.add(curentTime);
        } else if (this.mPredefinedSmartGroupType == 4) {
            aSelectionBuilder.append("SELECT _id FROM contacts WHERE ?-last_time_contacted>604800000 AND ?-last_time_contacted<=2592000000)");
            aSelectionArgs.add(curentTime);
            aSelectionArgs.add(curentTime);
        } else if (this.mPredefinedSmartGroupType == 5) {
            aSelectionBuilder.append("SELECT _id FROM contacts WHERE ?-last_time_contacted>2592000000 AND ?-last_time_contacted<=7776000000)");
            aSelectionArgs.add(curentTime);
            aSelectionArgs.add(curentTime);
        } else if (this.mPredefinedSmartGroupType == 6) {
            aSelectionBuilder.append("SELECT _id FROM contacts WHERE ?-last_time_contacted>7776000000)");
            aSelectionArgs.add(curentTime);
        }
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        configHwSearchUri(loader, directoryId);
        configHwSearchProjection(loader);
        configHwSearchSortOrder(loader);
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId) {
        boolean isEnterpriseDirectory = DirectoryCompat.isEnterpriseDirectoryId(directoryId);
        Uri uri = null;
        switch (this.mDataFilter.filterType) {
            case -50:
                if (this.mRcsCust != null && EmuiFeatureManager.isRcsFeatureEnable()) {
                    uri = this.mRcsCust.configRCSSearchUri("search_type", "search_phone", getQueryString(), this.mDataFilter.filterType);
                    break;
                }
            case -13:
            case -9:
            case -8:
            case -7:
            case -6:
            case -4:
            case -2:
                uri = getHwSearchBaseUri(SearchContract$DataSearch.EMAIL_CONTENT_FILTER_URI, "search_type", "search_email");
                break;
            case -12:
            case -1:
                uri = getHwSearchBaseUri(SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, "search_type", "search_phone");
                break;
            case -11:
                uri = getHwSearchBaseUri(SearchContract$ContactsSearch.CONTACTS_CONTENT_FILTER_URI, "search_type", "search_contacts");
                break;
            case -5:
            case -3:
                boolean search_mms = this.mCust != null && this.mCust.getEnableEmailContactInMms();
                uri = getHwSearchBaseUri(isEnterpriseDirectory ? PhoneCompat.getContentFilterUri() : SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, "search_type", search_mms ? "search_contacts_mms" : "search_phone");
                break;
            default:
                HwLog.e("ContactDataMulti", "invalid filter type =" + this.mDataFilter.filterType);
                ExceptionCapture.captureMatchDataListFilterException("invalid filter type =" + this.mDataFilter.filterType, null);
                return;
        }
        if (uri != null) {
            if (directoryId == 0 || isEnterpriseDirectory) {
                Builder builder = uri.buildUpon();
                if (isEnterpriseDirectory) {
                    builder.appendQueryParameter("directory", String.valueOf(directoryId));
                }
                switch (this.mDataFilter.filterType) {
                    case -50:
                        if (this.mRcsCust != null && EmuiFeatureManager.isRcsFeatureEnable()) {
                            this.mRcsCust.configRCSBuilder("search_email", builder);
                            break;
                        }
                    case -13:
                    case -12:
                        builder.appendQueryParameter("is_camcard", String.valueOf(Boolean.TRUE));
                        break;
                    case -11:
                        builder.appendQueryParameter("include_groups", String.valueOf(this.mDataFilter.groupId));
                        break;
                    case -5:
                    case -3:
                        if (this.mCust != null && this.mCust.getEnableEmailContactInMms()) {
                            builder.appendQueryParameter("search_email", String.valueOf(true));
                            break;
                        }
                    case -2:
                    case -1:
                        if (this.mDataFilter.groupId != -1) {
                            builder.appendQueryParameter("include_groups", String.valueOf(this.mDataFilter.groupId));
                            break;
                        } else {
                            builder.appendQueryParameter("starred", String.valueOf(Boolean.TRUE));
                            break;
                        }
                }
                loader.setUri(builder.build());
                return;
            }
            loader.setUri(uri);
        }
    }

    protected void configHwSearchProjection(CursorLoader loader) {
        if (this.mDataFilter.filterType == -11) {
            loader.setProjection(SearchContract$DataQuery.CONTACT_SEARCH_PROJECTION);
        } else {
            loader.setProjection(SearchContract$DataQuery.DATA_SEARCH_PROJECTION);
        }
    }

    protected void configHwSearchSortOrder(CursorLoader loader) {
        String sortOrder;
        if (getSortOrder() != 1) {
            sortOrder = "sort_key_alt , raw_contact_id";
        } else if (TextUtils.isEmpty(getQueryString()) || !Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            sortOrder = "sort_key , raw_contact_id ,is_primary DESC";
        } else {
            sortOrder = "pinyin_name , raw_contact_id ,is_primary DESC";
        }
        loader.setSortOrder(sortOrder);
        configureGroupby(loader, 0);
    }
}
