package com.android.contacts.list;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.IndexerListAdapter;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.HideRowsCursor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public abstract class ContactEntryListAdapter extends IndexerListAdapter {
    protected LazyItemCheckedListener itemCheckedListener;
    private AccountsDataManager mAccountDataLoader;
    private String mContactsCount = "";
    private CharSequence mDefaultFilterHeaderText;
    private int mDirectoryResultLimit = Integer.MAX_VALUE;
    private int mDirectorySearchMode;
    private int mDisplayOrder;
    private boolean mDisplayPhotos;
    private boolean mEmptyListEnabled = true;
    private boolean mExcludePrivateContacts;
    private ContactListFilter mFilter;
    private boolean mHighLightVisible;
    private boolean mIncludeProfile;
    private boolean mLoading = true;
    private char[] mLowerCaseQueryString;
    private boolean mOldSimpleDisplayMode;
    private ContactPhotoManager mPhotoLoader;
    private boolean mProfileExists;
    private ArrayList<String> mQueryMultiString;
    private String mQueryString;
    private boolean mQuickContactEnabled;
    private boolean mSearchMode;
    private boolean mSelectionVisible;
    private int mSortOrder;
    private boolean mVoiceSearchMode = false;

    public interface LazyItemCheckedListener {
        void setItemChecked(int i, Uri uri, boolean z);
    }

    public abstract void configureLoader(CursorLoader cursorLoader, long j);

    public ContactEntryListAdapter(Context context) {
        super(context);
        addPartitions();
        setDefaultFilterHeaderText(R.string.local_search_label);
        this.mOldSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
    }

    public void setLazyItemCheckedListener(LazyItemCheckedListener listener) {
        this.itemCheckedListener = listener;
    }

    protected void setDefaultFilterHeaderText(int resourceId) {
        this.mDefaultFilterHeaderText = getContext().getResources().getText(resourceId);
    }

    protected View createPinnedSectionHeaderView(Context context, ViewGroup parent) {
        return new ContactListPinnedHeaderView(context, null);
    }

    protected void setPinnedSectionTitle(View pinnedHeaderView, String title) {
        ((ContactListPinnedHeaderView) pinnedHeaderView).setSectionHeader(title);
    }

    protected void setPinnedHeaderContactsCount(View header) {
        ((ContactListPinnedHeaderView) header).setCountView(this.mContactsCount);
    }

    protected void clearPinnedHeaderContactsCount(View header) {
        ((ContactListPinnedHeaderView) header).setCountView(null);
    }

    protected void addPartitions() {
        addPartition(createDefaultDirectoryPartition());
    }

    protected DirectoryPartition createDefaultDirectoryPartition() {
        DirectoryPartition partition = new DirectoryPartition(true, true);
        partition.setDirectoryId(0);
        partition.setDirectoryType(getContext().getString(R.string.contactsList));
        partition.setPriorityDirectory(true);
        partition.setPhotoSupported(true);
        return partition;
    }

    void removeRemoteDirectories() {
        for (int i = getPartitionCount() - 1; i >= 0; i--) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                long directoryId = ((DirectoryPartition) partition).getDirectoryId();
                if (directoryId != 0) {
                    if (DirectoryCompat.isEnterpriseDirectoryId(directoryId)) {
                    }
                }
            }
            removePartition(i);
        }
    }

    void removeDirectoriesAfterDefault() {
        int i = getPartitionCount() - 1;
        while (i >= 0) {
            Partition partition = getPartition(i);
            if (!(partition instanceof DirectoryPartition) || ((DirectoryPartition) partition).getDirectoryId() != 0) {
                removePartition(i);
                i--;
            } else {
                return;
            }
        }
    }

    private int getPartitionByDirectoryId(long id) {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if ((partition instanceof DirectoryPartition) && ((DirectoryPartition) partition).getDirectoryId() == id) {
                return i;
            }
        }
        return -1;
    }

    void setScrollingState(boolean b) {
    }

    boolean isScrolling() {
        return false;
    }

    public void onDataReloadDefault() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition) partition;
                long directoryId = directoryPartition.getDirectoryId();
                if (directoryId == 0 || DirectoryCompat.isEnterpriseDirectoryId(directoryId)) {
                    directoryPartition.setStatus(0);
                }
            }
        }
    }

    public void onRemoteDataReload() {
        boolean notify = false;
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                DirectoryPartition directoryPartition = (DirectoryPartition) partition;
                if (!(directoryPartition.getDirectoryId() == 0 || DirectoryCompat.isEnterpriseDirectoryId(directoryPartition.getDirectoryId()))) {
                    if (!directoryPartition.isLoading()) {
                        notify = true;
                    }
                    directoryPartition.setStatus(0);
                }
            }
        }
        if (notify) {
            notifyDataSetChanged();
        }
    }

    public void clearPartitions() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if (partition instanceof DirectoryPartition) {
                ((DirectoryPartition) partition).setStatus(0);
            }
        }
        super.clearPartitions();
    }

    public boolean isSearchMode() {
        return this.mSearchMode;
    }

    public void setSearchMode(boolean flag) {
        this.mSearchMode = flag;
    }

    public boolean isVoiceSearchMode() {
        return this.mVoiceSearchMode;
    }

    public void setVoiceSearchMode(boolean flag) {
        this.mVoiceSearchMode = flag;
    }

    public String getQueryString() {
        return this.mQueryString;
    }

    public void setQueryString(String queryString) {
        this.mQueryString = queryString;
        if (TextUtils.isEmpty(queryString)) {
            this.mLowerCaseQueryString = null;
        } else {
            this.mLowerCaseQueryString = queryString.toLowerCase().toCharArray();
        }
    }

    public ArrayList<String> getQueryMultiString() {
        return this.mQueryMultiString;
    }

    public void setQueryMultiString(ArrayList<String> queryMultiString) {
        this.mQueryMultiString = queryMultiString;
    }

    public char[] getLowerCaseQueryString() {
        if (this.mLowerCaseQueryString == null) {
            return null;
        }
        char[] lowerperCaseQuerystring = new char[this.mLowerCaseQueryString.length];
        System.arraycopy(this.mLowerCaseQueryString, 0, lowerperCaseQuerystring, 0, this.mLowerCaseQueryString.length);
        return lowerperCaseQuerystring;
    }

    public void setDirectorySearchMode(int mode) {
        this.mDirectorySearchMode = mode;
    }

    public int getDirectoryResultLimit() {
        return this.mDirectoryResultLimit;
    }

    public void setDirectoryResultLimit(int limit) {
        this.mDirectoryResultLimit = limit;
    }

    public int getContactNameDisplayOrder() {
        return this.mDisplayOrder;
    }

    public void setContactNameDisplayOrder(int displayOrder) {
        this.mDisplayOrder = displayOrder;
    }

    public int getSortOrder() {
        return this.mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.mSortOrder = sortOrder;
    }

    public void setPhotoLoader(ContactPhotoManager photoLoader) {
        this.mPhotoLoader = photoLoader;
    }

    protected ContactPhotoManager getPhotoLoader() {
        return this.mPhotoLoader;
    }

    public void setAccountDataLoader(AccountsDataManager accountDataLoader) {
        this.mAccountDataLoader = accountDataLoader;
    }

    protected AccountsDataManager getAccountDataManager() {
        return this.mAccountDataLoader;
    }

    public boolean getDisplayPhotos() {
        return this.mDisplayPhotos;
    }

    public void setDisplayPhotos(boolean displayPhotos) {
        this.mDisplayPhotos = displayPhotos;
    }

    public void setEmptyListEnabled(boolean flag) {
        this.mEmptyListEnabled = flag;
    }

    public boolean isSelectionVisible() {
        return this.mSelectionVisible;
    }

    public void setSelectionVisible(boolean flag) {
        this.mSelectionVisible = flag;
    }

    public boolean isHighLightVisible() {
        return this.mHighLightVisible;
    }

    public void setHighLightVisible(boolean flag) {
        this.mHighLightVisible = flag;
    }

    public void setQuickContactEnabled(boolean quickContactEnabled) {
        this.mQuickContactEnabled = quickContactEnabled;
    }

    public boolean shouldIncludeProfile() {
        return this.mIncludeProfile;
    }

    public void setIncludeProfile(boolean includeProfile) {
        this.mIncludeProfile = includeProfile;
    }

    public void setProfileExists(boolean exists) {
        this.mProfileExists = exists;
    }

    public boolean hasProfile() {
        return this.mProfileExists;
    }

    public void configureDirectoryLoader(DirectoryListLoader loader) {
        loader.setDirectorySearchMode(this.mDirectorySearchMode);
        loader.setLocalInvisibleDirectoryEnabled(false);
    }

    public void changeDirectories(Cursor cursor) {
        if (cursor.getCount() == 0) {
            HwLog.e("ContactEntryListAdapter", "Directory search loader returned an empty cursor, which implies we have no directory entries.", new RuntimeException());
            return;
        }
        HashSet<Long> directoryIds = new HashSet();
        int idColumnIndex = cursor.getColumnIndex("_id");
        int directoryTypeColumnIndex = cursor.getColumnIndex("directoryType");
        int displayNameColumnIndex = cursor.getColumnIndex("displayName");
        int photoSupportColumnIndex = cursor.getColumnIndex("photoSupport");
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()) {
            long id = cursor.getLong(idColumnIndex);
            directoryIds.add(Long.valueOf(id));
            if (getPartitionByDirectoryId(id) == -1) {
                DirectoryPartition partition = new DirectoryPartition(false, true);
                partition.setDirectoryId(id);
                if (DirectoryCompat.isRemoteDirectory(id)) {
                    if (DirectoryCompat.isEnterpriseDirectoryId(id)) {
                        partition.setLabel(this.mContext.getString(R.string.directory_search_label_work));
                    }
                } else if (DirectoryCompat.isEnterpriseDirectoryId(id)) {
                    partition.setLabel(this.mContext.getString(R.string.list_filter_phones_work));
                }
                partition.setDirectoryType(cursor.getString(directoryTypeColumnIndex));
                partition.setDisplayName(cursor.getString(displayNameColumnIndex));
                int photoSupport = cursor.getInt(photoSupportColumnIndex);
                boolean z = photoSupport != 1 ? photoSupport == 3 : true;
                partition.setPhotoSupported(z);
                if (DirectoryCompat.isEnterpriseDirectoryId(id)) {
                    partition.setPriorityDirectory(true);
                }
                addPartition(partition);
            }
        }
        int i = getPartitionCount();
        while (true) {
            i--;
            if (i >= 0) {
                Partition partition2 = getPartition(i);
                if ((partition2 instanceof DirectoryPartition) && !directoryIds.contains(Long.valueOf(((DirectoryPartition) partition2).getDirectoryId()))) {
                    removePartition(i);
                }
            } else {
                invalidate();
                notifyDataSetChanged();
                return;
            }
        }
    }

    public void notifyChange() {
        invalidate();
        notifyDataSetChanged();
    }

    public void changeCursor(int partitionIndex, Cursor cursor) {
        if (partitionIndex < getPartitionCount()) {
            Partition partition = getPartition(partitionIndex);
            if (partition instanceof DirectoryPartition) {
                ((DirectoryPartition) partition).setStatus(2);
            }
            if (this.mDisplayPhotos && this.mPhotoLoader != null && isPhotoSupported(partitionIndex)) {
                this.mPhotoLoader.refreshCache();
            }
            super.changeCursor(partitionIndex, cursor);
            if (partitionIndex == getIndexedPartition()) {
                updateIndexer(cursor);
            }
        }
    }

    private void updateIndexer(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            setIndexer(null);
            return;
        }
        Bundle bundle = cursor.getExtras();
        if (bundle == null || !bundle.containsKey("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES")) {
            setIndexer(null);
        } else {
            setIndexer(new ContactsSectionIndexer(this.mContext, bundle.getStringArray("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES"), bundle.getIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS")));
        }
    }

    public int getViewTypeCount() {
        return (getItemViewTypeCount() * 2) + 1;
    }

    public int getItemViewType(int partitionIndex, int position) {
        int type = super.getItemViewType(partitionIndex, position);
        if (isUserProfile(position) || !isSectionHeaderDisplayEnabled() || partitionIndex != getIndexedPartition()) {
            return type;
        }
        if (!getItemPlacementInSection(position).firstInSection) {
            type += getItemViewTypeCount();
        }
        return type;
    }

    public boolean isEmpty() {
        if (!this.mEmptyListEnabled) {
            return false;
        }
        if (isSearchMode()) {
            return TextUtils.isEmpty(getQueryString());
        }
        if (this.mLoading) {
            return false;
        }
        return super.isEmpty();
    }

    public boolean isLoading() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if ((partition instanceof DirectoryPartition) && ((DirectoryPartition) partition).isLoading()) {
                return true;
            }
        }
        return false;
    }

    public boolean areAllPartitionsEmpty() {
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            if (!isPartitionEmpty(i)) {
                return false;
            }
        }
        return true;
    }

    public void configureDefaultPartition(boolean showIfEmpty, boolean hasHeader) {
        int defaultPartitionIndex = -1;
        int count = getPartitionCount();
        for (int i = 0; i < count; i++) {
            Partition partition = getPartition(i);
            if ((partition instanceof DirectoryPartition) && ((DirectoryPartition) partition).getDirectoryId() == 0) {
                defaultPartitionIndex = i;
                break;
            }
        }
        if (defaultPartitionIndex != -1) {
            setShowIfEmpty(defaultPartitionIndex, showIfEmpty);
            setHasHeader(defaultPartitionIndex, hasHeader);
        }
    }

    protected View newHeaderView(Context context, int partition, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.directory_header, parent, false);
    }

    protected void bindHeaderView(View view, int partitionIndex, Cursor cursor) {
        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            DirectoryPartition directoryPartition = (DirectoryPartition) partition;
            long directoryId = directoryPartition.getDirectoryId();
            TextView labelTextView = (TextView) view.findViewById(R.id.label);
            TextView displayNameTextView = (TextView) view.findViewById(R.id.display_name);
            int count = (cursor == null || cursor.isClosed()) ? 0 : cursor.getCount();
            if (directoryId == 0 || directoryId == 1) {
                CharSequence latelText;
                if (isSearchMode()) {
                    latelText = this.mContext.getResources().getQuantityString(R.plurals.local_search_result_label, count, new Object[]{Integer.valueOf(count)});
                } else {
                    latelText = this.mDefaultFilterHeaderText;
                }
                labelTextView.setText(latelText == null ? null : latelText.toString().toUpperCase());
                displayNameTextView.setText(null);
            } else if (DirectoryCompat.isEnterpriseDirectoryId(directoryId)) {
                labelTextView.setText(directoryPartition.getLabel().toUpperCase());
            } else {
                String displayName;
                String directoryName = directoryPartition.getDisplayName();
                if (TextUtils.isEmpty(directoryName)) {
                    displayName = directoryPartition.getDirectoryType();
                } else {
                    displayName = directoryName;
                }
                labelTextView.setText(displayName == null ? null : displayName.toUpperCase());
                displayNameTextView.setText(null);
            }
            TextView countText = (TextView) view.findViewById(R.id.count);
            boolean isRemoteContact = DirectoryCompat.isRemoteDirectory(directoryId);
            if (directoryPartition.isLoading() && isRemoteContact) {
                countText.setText(upPercase(R.string.search_results_searching));
            } else if (isRemoteContact && count >= getDirectoryResultLimit()) {
                countText.setText(CommonUtilMethods.upPercase(this.mContext.getString(R.string.foundTooManyContacts, new Object[]{Integer.valueOf(getDirectoryResultLimit())})));
            } else if (isSearchMode() && (directoryId == 0 || directoryId == 1)) {
                countText.setText(null);
            } else {
                countText.setText(CommonUtilMethods.upPercase(String.valueOf(count)));
            }
        }
    }

    protected boolean isUserProfile(int position) {
        boolean isUserProfile = false;
        if (position == 0) {
            int partition = getPartitionForPosition(position);
            if (partition >= 0) {
                Cursor cursor = (Cursor) getItem(position);
                if (!(cursor == null || cursor.isClosed())) {
                    int profileColumnIndex = cursor.getColumnIndex("is_user_profile");
                    if (profileColumnIndex != -1) {
                        isUserProfile = cursor.getInt(profileColumnIndex) == 1;
                    }
                    int offset = getCursor(partition).getPosition();
                    if (offset < cursor.getCount()) {
                        cursor.moveToPosition(offset);
                    }
                }
            }
        }
        return isUserProfile;
    }

    public boolean isPhotoSupported(int partitionIndex) {
        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            return ((DirectoryPartition) partition).isPhotoSupported();
        }
        return true;
    }

    public ContactListFilter getFilter() {
        return this.mFilter;
    }

    public void setFilter(ContactListFilter filter) {
        this.mFilter = filter;
    }

    public void setContactsCount(String count) {
        this.mContactsCount = count;
    }

    public String getContactsCount() {
        return this.mContactsCount;
    }

    public void setExcludePrivateContacts(boolean aExcludePrivateContacts) {
        this.mExcludePrivateContacts = aExcludePrivateContacts;
    }

    public boolean getIfExcludePrivateContacts() {
        return this.mExcludePrivateContacts;
    }

    public Uri getSelectedContactUri(int position) {
        return null;
    }

    public void hideOneContact(long contactId) {
        int position = -1;
        Cursor cursor = (Cursor) getItem(0);
        if (cursor != null && cursor.getCount() <= 5000) {
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                if (cursor.getLong(0) == contactId) {
                    position = cursor.getPosition();
                    break;
                }
            }
            if (-1 != position && (cursor instanceof HideRowsCursor)) {
                HideRowsCursor sortCursor = (HideRowsCursor) cursor;
                Bundle bundle = cursor.getExtras();
                if (bundle != null) {
                    int[] counts = bundle.getIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS");
                    String[] sections = bundle.getStringArray("android.provider.extra.ADDRESS_BOOK_INDEX_TITLES");
                    if (counts != null && sections != null && counts.length == sections.length && counts.length > 0) {
                        int countsIndex = 0;
                        int sectionPos = position;
                        while (countsIndex < counts.length && sectionPos >= counts[countsIndex]) {
                            sectionPos -= counts[countsIndex];
                            countsIndex++;
                        }
                        if (countsIndex < counts.length && counts[countsIndex] > 1) {
                            counts[countsIndex] = counts[countsIndex] - 1;
                            if (sortCursor.addHideRow(position)) {
                                bundle.putIntArray("android.provider.extra.ADDRESS_BOOK_INDEX_COUNTS", counts);
                                setContactsCount(String.valueOf(cursor.getCount()));
                                updateIndexer(cursor);
                                notifyChange();
                            }
                        }
                    }
                }
            }
        }
    }

    public final void upateSimpleDisplayMode() {
        boolean newSimpleDisplayMode = ContactDisplayUtils.isSimpleDisplayMode();
        if (this.mOldSimpleDisplayMode != newSimpleDisplayMode) {
            this.mOldSimpleDisplayMode = newSimpleDisplayMode;
            notifyChange();
        }
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        bindWorkProfileIcon((ContactListItemView) itemView, partition);
    }

    protected void bindWorkProfileIcon(ContactListItemView view, int partitionId) {
        Partition partition = getPartition(partitionId);
        if (partition instanceof DirectoryPartition) {
            boolean z;
            if (ContactsUtils.determineUserType(Long.valueOf(((DirectoryPartition) partition).getDirectoryId()), null) == 1) {
                z = true;
            } else {
                z = false;
            }
            view.setWorkProfileIconEnabled(z);
        }
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
    }

    protected Uri getHwSearchBaseUri(Uri baseUri, String searchType, String searchValue) {
        Uri uri = Uri.withAppendedPath(baseUri, Uri.encode(getQueryString())).buildUpon().appendQueryParameter(searchType, searchValue).build();
        if (EmuiFeatureManager.isSearchContactsMulti() && "search_contacts".equals(searchValue)) {
            return uri.buildUpon().appendQueryParameter("search_contacts_multi", "true").build();
        }
        return uri;
    }

    protected void configHwSearchSortOrder(CursorLoader loader) {
        String sortOrder;
        if (getSortOrder() != 1) {
            sortOrder = "sort_key_alt";
        } else if (TextUtils.isEmpty(getQueryString()) || !Locale.CHINESE.getLanguage().equals(Locale.getDefault().getLanguage())) {
            sortOrder = "sort_key";
        } else {
            sortOrder = "pinyin_name";
        }
        loader.setSortOrder(sortOrder);
    }

    public String upPercase(int resId) {
        return CommonUtilMethods.upPercase(this.mContext.getResources().getString(resId));
    }
}
