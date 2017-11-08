package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.compatibility.ContactsCompat;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.utils.EasContactsCache;
import com.android.contacts.hap.utils.SimContactsCache;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ContactQuery;
import com.huawei.cspcommon.util.SearchContract$ContactQuery;
import com.huawei.cust.HwCustUtils;

public abstract class ContactListAdapter extends ContactEntryListAdapter {
    HwCustContactListCustomizations mHwCustContactListCustObj;
    boolean mScrolling = false;
    private long mSelectedContactDirectoryId;
    private long mSelectedContactId;
    private String mSelectedContactLookupKey;
    protected CharSequence mUnknownNameText;
    Uri mUri;

    public ContactListAdapter(Context context) {
        super(context);
        this.mUnknownNameText = context.getText(R.string.missing_name);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mHwCustContactListCustObj = (HwCustContactListCustomizations) HwCustUtils.createObj(HwCustContactListCustomizations.class, new Object[0]);
        }
    }

    public long getSelectedContactDirectoryId() {
        return this.mSelectedContactDirectoryId;
    }

    public String getSelectedContactLookupKey() {
        return this.mSelectedContactLookupKey;
    }

    public long getSelectedContactId() {
        return this.mSelectedContactId;
    }

    public void setSelectedContact(long selectedDirectoryId, String lookupKey, long contactId) {
        this.mSelectedContactDirectoryId = selectedDirectoryId;
        this.mSelectedContactLookupKey = lookupKey;
        this.mSelectedContactId = contactId;
    }

    protected static Uri buildSectionIndexerUri(Uri uri) {
        return uri.buildUpon().appendQueryParameter("android.provider.extra.ADDRESS_BOOK_INDEX", "true").build();
    }

    public String getContactDisplayName(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return null;
        }
        return cursor.getString(1);
    }

    public boolean getIsStaredContact(int position) {
        boolean z = true;
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return false;
        }
        int columnIndex = cursor.getColumnIndex("starred");
        if (-1 == columnIndex) {
            return false;
        }
        if (cursor.getInt(columnIndex) != 1) {
            z = false;
        }
        return z;
    }

    public boolean getIsProfileContact(int position) {
        boolean z = true;
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return false;
        }
        int columnIndex = cursor.getColumnIndex("is_user_profile");
        if (-1 == columnIndex) {
            return false;
        }
        if (cursor.getInt(columnIndex) != 1) {
            z = false;
        }
        return z;
    }

    public Uri getContactUri(int position) {
        int partitionIndex = getPartitionForPosition(position);
        Cursor item = (Cursor) getItem(position);
        if (item != null) {
            return getContactUri(partitionIndex, item);
        }
        return null;
    }

    public Uri getContactUri(int partitionIndex, Cursor cursor) {
        Uri uri = Contacts.getLookupUri(cursor.getLong(0), cursor.getString(6));
        long directoryId = ((DirectoryPartition) getPartition(partitionIndex)).getDirectoryId();
        if (directoryId != 0) {
            return uri.buildUpon().appendQueryParameter("directory", String.valueOf(directoryId)).build();
        }
        return uri;
    }

    public boolean isSelectedContact(int partitionIndex, Cursor cursor) {
        long directoryId = ((DirectoryPartition) getPartition(partitionIndex)).getDirectoryId();
        if (getSelectedContactDirectoryId() != directoryId) {
            return false;
        }
        String lookupKey = getSelectedContactLookupKey();
        if (lookupKey != null && TextUtils.equals(lookupKey, cursor.getString(6))) {
            return true;
        }
        boolean z = (directoryId == 0 || directoryId == 1) ? false : getSelectedContactId() == cursor.getLong(0);
        return z;
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(this.mUnknownNameText);
        view.setActivatedStateSupported(isSelectionVisible());
        return view;
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position, Cursor cursor, boolean aIsLastItem) {
        CharSequence charSequence = null;
        if (view.getVisibility() == 8) {
            return;
        }
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);
            if (position == 0) {
                view.setCountView(placement.count == 0 ? getContactsCount() : String.valueOf(placement.count));
            } else {
                if (placement.count != 0) {
                    charSequence = String.valueOf(placement.count);
                }
                view.setCountView(charSequence);
            }
            view.setSectionHeader(placement.sectionHeader);
            view.setAccountFilterText(getFilter());
            return;
        }
        view.setSectionHeader(null);
        view.setCountView(null);
        view.setAccountFilterText(null);
    }

    protected long getPhotoId(Cursor cursor) {
        int photoIdIndex = cursor.getColumnIndex("photo_id");
        if (photoIdIndex != -1) {
            return cursor.getLong(photoIdIndex);
        }
        return 0;
    }

    protected void bindPhoto(ContactListItemView view, int partitionIndex, Cursor cursor) {
        if (!isPhotoSupported(partitionIndex) || ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        long photoId = getPhotoId(cursor);
        boolean lIsPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            lIsPrivateContact = CommonUtilMethods.isPrivateContact(cursor);
        }
        if (photoId > 0) {
            int i;
            ContactPhotoManager photoLoader = getPhotoLoader();
            ImageView photoView = view.getPhotoView(photoId);
            if (lIsPrivateContact) {
                i = 6;
            } else {
                i = 2;
            }
            photoLoader.loadThumbnail(photoView, photoId, false, null, i);
        } else {
            int i2;
            DefaultImageRequest request = new DefaultImageRequest();
            request.displayName = cursor.getString(1);
            String contactId = getContactId(cursor);
            if (!TextUtils.isEmpty(contactId)) {
                request.identifier = convertContactIdToLocal(contactId);
            }
            request.isCircular = true;
            Uri photoUri = null;
            if (photoId == 0) {
                int photoUriIndex = cursor.getColumnIndex("photo_thumb_uri");
                if (photoUriIndex != -1) {
                    String photoUriString = cursor.getString(photoUriIndex);
                    photoUri = photoUriString == null ? null : Uri.parse(photoUriString);
                }
            }
            ContactPhotoManager photoLoader2 = getPhotoLoader();
            ImageView photoView2 = view.getPhotoView(photoId);
            if (lIsPrivateContact) {
                i2 = 4;
            } else {
                i2 = 0;
            }
            photoLoader2.loadDirectoryPhoto(photoView2, photoUri, false, i2, request);
        }
    }

    protected String getContactId(Cursor cursor) {
        return cursor.getString(0);
    }

    private String convertContactIdToLocal(String ContactId) {
        long id = Long.parseLong(ContactId);
        if (id > 1000000000) {
            id -= 1000000000;
        }
        return String.valueOf(id);
    }

    protected void bindCheckBox(ContactListItemView view) {
        view.showCheckBox();
    }

    protected void hideCheckBox(ContactListItemView view) {
        view.hideCheckbox();
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 1, getContactNameDisplayOrder());
    }

    protected void bindSimIcon(ContactListItemView view, Cursor cursor) {
        if (EmuiFeatureManager.isSimAccountIndicatorEnabled()) {
            Integer simType;
            if (QueryUtil.isHAPProviderInstalled()) {
                long photoId = 0;
                if (!cursor.isNull(4)) {
                    photoId = cursor.getLong(4);
                }
                if (photoId == -2) {
                    simType = Integer.valueOf(0);
                } else if (photoId == -3) {
                    simType = Integer.valueOf(1);
                } else {
                    simType = Integer.valueOf(-100);
                }
            } else {
                simType = Integer.valueOf(SimContactsCache.getInstance(this.mContext.getApplicationContext()).getMatchedContact(cursor.getLong(0)));
            }
            if (simType.intValue() != -100) {
                view.setAccountTypeToSimAccount(true);
                Bitmap bm = SimContactsCache.getSimSmallBitmap(this.mContext, simType.intValue());
                String simAccountIndicatorContentDescription = SimFactoryManager.getSimAccountDisplayIconContentDescription(simType.intValue());
                if (bm != null) {
                    view.setSimAccountIndicatorInfo(bm);
                    view.setSimAccountIndicatorContentDescription(simAccountIndicatorContentDescription);
                    return;
                }
                view.setSimAccountIndicatorInfo(null);
                return;
            }
            view.setAccountTypeToSimAccount(false);
            view.setSimAccountIndicatorInfo(null);
        }
    }

    public void setScrollingState(boolean b) {
        this.mScrolling = b;
    }

    public boolean isScrolling() {
        return this.mScrolling;
    }

    protected void bindPresenceAndStatusMessage(ContactListItemView view, Cursor cursor) {
        view.showPresenceAndStatusMessage(cursor, 2, 3, !this.mScrolling);
    }

    protected void bindSearchSnippetInfo(ContactListItemView view, Cursor cursor) {
        view.showSnippetInfo(cursor);
    }

    public void bindSearchSnippet(ContactListItemView view, Cursor cursor) {
        view.showSnippet(cursor);
    }

    protected void bindAccountInfo(ContactListItemView aView, Cursor aCursor) {
        long lContactId = aCursor.getLong(0);
        if (ContactsContract.isProfileId(lContactId)) {
            aView.setAccountIcons(null, null);
            return;
        }
        String[] accountTypeDescriptions = new String[3];
        aView.setAccountIcons(getAccountDataManager().getAccountData(lContactId, accountTypeDescriptions), accountTypeDescriptions);
        if (this.mHwCustContactListCustObj != null && HwCustCommonConstants.EAS_ACCOUNT_ICON_DISP_EMABLED && !aCursor.isNull(0) && EasContactsCache.getInstance(this.mContext.getApplicationContext()).isEasContact(lContactId)) {
            if (EasContactsCache.getEasSmallIcon(this.mContext) != null) {
                aView.setAccountIcons(new Bitmap[]{EasContactsCache.getEasSmallIcon(this.mContext)}, new String[]{"Exchange"});
                return;
            }
            aView.setAccountIcons(null, null);
        }
    }

    public int getSelectedContactPosition() {
        if (this.mSelectedContactLookupKey == null && this.mSelectedContactId == 0) {
            return -1;
        }
        int partitionIndex = -1;
        int partitionCount = getPartitionCount();
        for (int i = 0; i < partitionCount; i++) {
            if (((DirectoryPartition) getPartition(i)).getDirectoryId() == this.mSelectedContactDirectoryId) {
                partitionIndex = i;
                break;
            }
        }
        if (partitionIndex == -1) {
            return -1;
        }
        Cursor cursor = getCursor(partitionIndex);
        if (cursor == null) {
            return -1;
        }
        cursor.moveToPosition(-1);
        int offset = -1;
        while (cursor.moveToNext()) {
            if (this.mSelectedContactLookupKey != null) {
                if (this.mSelectedContactLookupKey.equals(cursor.getString(6))) {
                    offset = cursor.getPosition();
                    break;
                }
            }
            if (this.mSelectedContactId != 0 && ((this.mSelectedContactDirectoryId == 0 || this.mSelectedContactDirectoryId == 1) && cursor.getLong(0) == this.mSelectedContactId)) {
                offset = cursor.getPosition();
                break;
            }
        }
        if (offset == -1) {
            return -1;
        }
        int position = getPositionForPartition(partitionIndex) + offset;
        if (hasHeader(partitionIndex)) {
            position++;
        }
        return position;
    }

    public int getSelectedContactPosition(long directoryId, String LookupKey, long selectedContactId) {
        if (LookupKey == null && selectedContactId == 0) {
            return -1;
        }
        int partitionIndex = -1;
        int partitionCount = getPartitionCount();
        for (int i = 0; i < partitionCount; i++) {
            Partition tmpPartition = getPartition(i);
            if ((tmpPartition instanceof DirectoryPartition) && ((DirectoryPartition) tmpPartition).getDirectoryId() == directoryId) {
                partitionIndex = i;
                break;
            }
        }
        if (partitionIndex == -1) {
            return -1;
        }
        Cursor cursor = getCursor(partitionIndex);
        if (cursor == null || cursor.isClosed()) {
            return -1;
        }
        cursor.moveToPosition(-1);
        int offset = -1;
        while (cursor.moveToNext()) {
            if (LookupKey != null) {
                if (LookupKey.equals(cursor.getString(6))) {
                    offset = cursor.getPosition();
                    break;
                }
            }
            if (selectedContactId != 0 && ((directoryId == 0 || directoryId == 1) && cursor.getLong(0) == selectedContactId)) {
                offset = cursor.getPosition();
                break;
            }
        }
        if (offset == -1) {
            return -1;
        }
        int position = getPositionForPartition(partitionIndex) + offset;
        if (hasHeader(partitionIndex)) {
            position++;
        }
        return position;
    }

    public Uri getFirstContactUri() {
        int partitionCount = getPartitionCount();
        for (int i = 0; i < partitionCount; i++) {
            if (!((DirectoryPartition) getPartition(i)).isLoading()) {
                Cursor cursor = getCursor(i);
                if (cursor != null && cursor.moveToFirst()) {
                    return getContactUri(i, cursor);
                }
            }
        }
        return null;
    }

    public void changeCursor(int partitionIndex, Cursor cursor) {
        boolean z = false;
        super.changeCursor(partitionIndex, cursor);
        if (cursor == null) {
            setProfileExists(false);
            return;
        }
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            setProfileExists(doesProfileExists(cursor));
        } else {
            if (cursor.getExtras() != null) {
                z = cursor.getExtras().getBoolean("has_profile");
            }
            setProfileExists(z);
        }
    }

    private boolean doesProfileExists(Cursor cursor) {
        if (cursor.getInt(getProfileColumnIndex()) == 1) {
            return true;
        }
        if (cursor.getExtras() != null) {
            return cursor.getExtras().getBoolean("has_profile");
        }
        return false;
    }

    protected int getProfileColumnIndex() {
        return 7;
    }

    protected final String[] getProjection() {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            if (getContactNameDisplayOrder() == 1) {
                return ContactQuery.CONTACT_PROJECTION_PRIMARY_PRIVATE;
            }
            return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE_PRIVATE;
        } else if (getContactNameDisplayOrder() == 1) {
            return ContactQuery.CONTACT_PROJECTION_PRIMARY;
        } else {
            return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE;
        }
    }

    protected final String[] getFilterProjection(boolean forSearch) {
        int sortOrder = getContactNameDisplayOrder();
        if (forSearch) {
            if (sortOrder == 1) {
                return ContactQuery.FILTER_PROJECTION_PRIMARY;
            }
            return ContactQuery.getProjectionFilterProjectionAlternative();
        } else if (sortOrder == 1) {
            return ContactQuery.CONTACT_PROJECTION_PRIMARY;
        } else {
            return ContactQuery.CONTACT_PROJECTION_ALTERNATIVE;
        }
    }

    protected final String[] getVoiceSearchProjection() {
        return ContactQuery.VOICE_SEARCH_PROJECTION;
    }

    protected final String[] getRawContactsProjection() {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            return ContactQuery.PROJECTION_RAW_CONTACT_PRIVATE;
        }
        return ContactQuery.PROJECTION_RAW_CONTACT;
    }

    public void setSelectedContactUri(Uri lLookupUri) {
        this.mUri = lLookupUri;
    }

    public long getContactId(int aPosition) {
        Cursor cursor = (Cursor) getItem(aPosition);
        if (cursor == null) {
            return -1;
        }
        return cursor.getLong(0);
    }

    public long getContactDirectoryId(int position) {
        Partition partition = getPartition(getPartitionForPosition(position));
        if (partition instanceof DirectoryPartition) {
            return ((DirectoryPartition) partition).getDirectoryId();
        }
        return 0;
    }

    protected final String[] getSearchProjection(boolean forSearch) {
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            if (getContactNameDisplayOrder() == 1) {
                return SearchContract$ContactQuery.CONTACTS_SEARCH_PROJECTION_PRIMARY_PRIVATE;
            }
            return SearchContract$ContactQuery.CONTACTS_SEARCH_PROJECTION_ALTERNATIVE_PRIVATE;
        } else if (getContactNameDisplayOrder() == 1) {
            return SearchContract$ContactQuery.CONTACTS_SEARCH_PROJECTION_PRIMARY;
        } else {
            return SearchContract$ContactQuery.CONTACTS_SEARCH_PROJECTION_ALTERNATIVE;
        }
    }

    public boolean isEnterpriseContact(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor != null) {
            return ContactsCompat.isEnterpriseContactId(cursor.getLong(0));
        }
        return false;
    }

    protected void configHwSearchProjection(CursorLoader loader) {
        loader.setProjection(getSearchProjection(false));
    }
}
