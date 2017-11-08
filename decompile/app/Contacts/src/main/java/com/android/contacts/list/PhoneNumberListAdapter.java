package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.CommonDataKinds.Callable;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.common.widget.CompositeCursorAdapter.Partition;
import com.android.contacts.ContactPhotoManager;
import com.android.contacts.ContactPhotoManager.DefaultImageRequest;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.ContactListItemView.PhotoPosition;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.IndexerListAdapter.Placement;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.SearchContract$DataSearch;
import com.huawei.cspcommon.util.SearchContract$PhoneQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhoneNumberListAdapter extends ContactEntryListAdapter {
    private static final String TAG = PhoneNumberListAdapter.class.getSimpleName();
    private ArrayList<String> mDataAdded = new ArrayList();
    private boolean mExcludeSim;
    private PhotoPosition mPhotoPosition;
    private DefaultImageRequest mRequest = new DefaultImageRequest();
    private final CharSequence mUnknownNameText;
    private boolean mUseCallableUri;
    private boolean mUseSelectionInSearchMode;

    public static class PhoneQuery {
        static final String[] PROJECTION_ALTERNATIVE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "lookup", "photo_id", "display_name_alt", "company"};
        private static final String[] PROJECTION_ALTERNATIVE_PRIVATE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "lookup", "photo_id", "display_name_alt", "is_private", "company"};
        static final String[] PROJECTION_PRIMARY = new String[]{"contact_id", "_id", "data2", "data3", "data1", "lookup", "photo_id", "display_name", "company"};
        private static final String[] PROJECTION_PRIMARY_PRIVATE = new String[]{"contact_id", "_id", "data2", "data3", "data1", "lookup", "photo_id", "display_name", "is_private", "company"};
    }

    public PhoneNumberListAdapter(Context context) {
        super(context);
        setDefaultFilterHeaderText(R.string.list_filter_all_numbers);
        this.mUnknownNameText = context.getText(17039374);
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        Builder builder;
        if (directoryId != 0) {
            HwLog.w(TAG, "PhoneNumberListAdapter is not ready for non-default directory ID (directoryId: " + directoryId + ")");
        }
        if (isSearchMode()) {
            builder = (this.mUseCallableUri ? Callable.CONTENT_FILTER_URI : Phone.CONTENT_FILTER_URI).buildUpon();
            String query = getQueryString();
            if (TextUtils.isEmpty(query)) {
                builder.appendPath("");
            } else {
                builder.appendPath(query);
            }
            builder.appendQueryParameter("directory", String.valueOf(directoryId));
            if (this.mUseSelectionInSearchMode) {
                applyFilter(loader, builder, directoryId, getFilter());
            }
        } else {
            Uri baseUri = this.mUseCallableUri ? Callable.CONTENT_URI : Phone.CONTENT_URI;
            StringBuffer sb = new StringBuffer();
            sb.append("contact_id").append(", replace(replace(").append("data1").append(", '-', ''), ' ', '')");
            builder = baseUri.buildUpon().appendQueryParameter("group_by", sb.toString()).build().buildUpon();
            builder.appendQueryParameter("android.provider.extra.ADDRESS_BOOK_INDEX", "true");
            applyFilter(loader, builder, directoryId, getFilter());
        }
        loader.setUri(builder.build());
        if (EmuiFeatureManager.isPrivacyFeatureEnabled() && getIfExcludePrivateContacts()) {
            loader.setSelection("is_private=0");
        }
        if (getContactNameDisplayOrder() == 1) {
            loader.setProjection(EmuiFeatureManager.isPrivacyFeatureEnabled() ? PhoneQuery.PROJECTION_PRIMARY_PRIVATE : PhoneQuery.PROJECTION_PRIMARY);
        } else {
            loader.setProjection(EmuiFeatureManager.isPrivacyFeatureEnabled() ? PhoneQuery.PROJECTION_ALTERNATIVE_PRIVATE : PhoneQuery.PROJECTION_ALTERNATIVE);
        }
        if (getSortOrder() == 1) {
            loader.setSortOrder("sort_key");
        } else {
            loader.setSortOrder("sort_key_alt");
        }
    }

    private void applyFilter(CursorLoader loader, Builder uriBuilder, long directoryId, ContactListFilter filter) {
        if (filter != null && directoryId == 0) {
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            switch (filter.filterType) {
                case -5:
                case -2:
                case -1:
                    break;
                case -3:
                    selection.append("in_visible_group=1");
                    selection.append(" AND has_phone_number=1");
                    break;
                case 0:
                    selection.append("(");
                    if (this.mDataAdded != null) {
                        for (String lContactId : this.mDataAdded) {
                            if (!(lContactId == null || "".equals(lContactId))) {
                                selection.append("_id != ").append(lContactId).append(" AND ");
                            }
                        }
                    }
                    if (!(filter.accountType == null || filter.accountName == null)) {
                        if (this.mExcludeSim) {
                            selection.append("account_type!=? AND account_name!=?");
                        } else {
                            selection.append("account_type=? AND account_name=?");
                        }
                        selectionArgs.add(filter.accountType);
                        selectionArgs.add(filter.accountName);
                        selection.append(" AND ");
                    }
                    if (filter.dataSet != null) {
                        selection.append("data_set=?");
                        selectionArgs.add(filter.dataSet);
                    } else {
                        selection.append("data_set IS NULL");
                    }
                    selection.append(")");
                    break;
                default:
                    HwLog.w(TAG, "Unsupported filter type came (type: " + filter.filterType + ", toString: " + filter + ")" + " showing all contacts.");
                    break;
            }
            loader.setSelection(selection.toString());
            loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[0]));
        }
    }

    public Uri getDataUri(int position) {
        int partitionIndex = getPartitionForPosition(position);
        Cursor item = (Cursor) getItem(position);
        if (item != null) {
            return getDataUri(partitionIndex, item);
        }
        return null;
    }

    public Uri getDataUri(int partitionIndex, Cursor cursor) {
        long directoryId = -1;
        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            directoryId = ((DirectoryPartition) partition).getDirectoryId();
        }
        if (DirectoryCompat.isRemoteDirectory(directoryId) || DirectoryCompat.isEnterpriseDirectoryId(directoryId)) {
            return null;
        }
        return ContentUris.withAppendedId(Data.CONTENT_URI, cursor.getLong(1));
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        ContactListItemView view = new ContactListItemView(context, null);
        view.setUnknownNameText(this.mUnknownNameText);
        return view;
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        if (isSearchMode()) {
            view.setSearchMatchType(cursor);
        }
        view.setHighlightedPrefix(isSearchMode() ? getLowerCaseQueryString() : null);
        cursor.moveToPosition(position);
        boolean isFirstEntry = true;
        long currentContactId = cursor.getLong(0);
        if (cursor.moveToPrevious() && !cursor.isBeforeFirst() && currentContactId == cursor.getLong(0)) {
            isFirstEntry = false;
        }
        cursor.moveToPosition(position);
        if (cursor.moveToNext() && !cursor.isAfterLast() && currentContactId == cursor.getLong(0)) {
        }
        cursor.moveToPosition(position);
        bindSectionHeaderAndDivider(view, position);
        if (isFirstEntry) {
            bindName(view, cursor);
            bindPhoto(view, cursor);
        } else {
            unbindName(view);
            if (ContactDisplayUtils.isSimpleDisplayMode()) {
                view.removePhotoView();
            } else {
                view.removePhotoView(true, false);
            }
        }
        bindPhoneNumber(view, cursor);
        if (isSearchMode()) {
            view.showCompany(cursor, cursor.getColumnIndex("company"), -1);
        } else {
            view.setCompany(null);
        }
    }

    protected void bindPhoneNumber(ContactListItemView view, Cursor cursor) {
        CharSequence label = null;
        if (!cursor.isNull(2)) {
            label = Phone.getTypeLabel(getContext().getResources(), cursor.getInt(2), cursor.getString(3));
        }
        view.showPhoneNumber(cursor.getString(4), label);
    }

    protected void bindSectionHeaderAndDivider(ContactListItemView view, int position) {
        String str = null;
        if (isSectionHeaderDisplayEnabled()) {
            Placement placement = getItemPlacementInSection(position);
            if (placement.firstInSection) {
                str = placement.sectionHeader;
            }
            view.setSectionHeader(str);
            return;
        }
        view.setSectionHeader(null);
    }

    protected void bindName(ContactListItemView view, Cursor cursor) {
        view.showDisplayName(cursor, 7, getContactNameDisplayOrder());
    }

    protected void unbindName(ContactListItemView view) {
        view.hideDisplayName();
    }

    protected void bindPhoto(ContactListItemView view, Cursor cursor) {
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            view.removePhotoView();
            return;
        }
        int i;
        long photoId = 0;
        if (!cursor.isNull(6)) {
            photoId = cursor.getLong(6);
        }
        boolean lIsPrivateContact = false;
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            lIsPrivateContact = CommonUtilMethods.isPrivateContact(cursor);
        }
        DefaultImageRequest request = null;
        if (photoId <= 0) {
            this.mRequest.displayName = cursor.getString(7);
            this.mRequest.identifier = cursor.getString(0);
            this.mRequest.isCircular = true;
            request = this.mRequest;
        }
        ContactPhotoManager photoLoader = getPhotoLoader();
        ImageView photoView = view.getPhotoView(photoId);
        if (lIsPrivateContact) {
            i = 4;
        } else {
            i = 0;
        }
        photoLoader.loadThumbnail(photoView, photoId, false, request, i);
    }

    public void setPhotoPosition(PhotoPosition photoPosition) {
        this.mPhotoPosition = photoPosition;
    }

    public void setExcludeSim(boolean aExcludeSim) {
        this.mExcludeSim = aExcludeSim;
    }

    public void setUseSelectionInSearchMode(boolean aUseSelection) {
        this.mUseSelectionInSearchMode = aUseSelection;
    }

    public void setDataAdded(ArrayList<String> aContactsAdded) {
        this.mDataAdded = aContactsAdded;
    }

    public void setUseCallableUri(boolean useCallableUri) {
        this.mUseCallableUri = useCallableUri;
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        configHwSearchUri(loader, directoryId, getFilter());
        configHwSearchProjection(loader);
        configHwSearchSortOrder(loader);
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = getHwSearchBaseUri(SearchContract$DataSearch.PHONE_CONTENT_FILTER_URI, "search_type", "search_phone");
        if (filter == null || directoryId != 0) {
            loader.setUri(uri);
            return;
        }
        Builder builder = uri.buildUpon();
        switch (filter.filterType) {
            case -5:
            case -2:
            case -1:
                break;
            case -3:
                builder.appendQueryParameter("has_phone_number", String.valueOf(Boolean.TRUE));
                break;
            case 0:
                if (this.mDataAdded != null && this.mDataAdded.size() > 0) {
                    builder.appendQueryParameter("exclude_data", buildIdString(this.mDataAdded));
                }
                if (!(filter.accountType == null || filter.accountName == null)) {
                    if (!this.mExcludeSim) {
                        filter.addAccountQueryParameterToUrl(builder);
                        break;
                    } else {
                        buildAccountTypeNameString(builder, filter);
                        break;
                    }
                }
            default:
                HwLog.w(TAG, "Unsupported filter type came (type: " + filter.filterType + ", toString: " + filter + ")" + " showing all contacts.");
                break;
        }
        if (EmuiFeatureManager.isPrivacyFeatureEnabled() && getIfExcludePrivateContacts()) {
            builder.appendQueryParameter("is_private", String.valueOf(Boolean.FALSE));
        }
        loader.setUri(builder.build());
    }

    protected void configHwSearchProjection(CursorLoader loader) {
        String[] projection;
        int sortOrder = getContactNameDisplayOrder();
        if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
            if (sortOrder == 1) {
                projection = SearchContract$PhoneQuery.PROJECTION_PRIMARY_PRIVATE;
            } else {
                projection = SearchContract$PhoneQuery.PROJECTION_ALTERNATIVE_PRIVATE;
            }
        } else if (sortOrder == 1) {
            projection = SearchContract$PhoneQuery.PROJECTION_PRIMARY;
        } else {
            projection = SearchContract$PhoneQuery.PROJECTION_ALTERNATIVE;
        }
        loader.setProjection(projection);
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

    private String buildIdString(ArrayList<String> idList) {
        if (idList == null || idList.size() == 0) {
            return null;
        }
        StringBuilder idBuilder = new StringBuilder(idList.size() * 4);
        for (String id : idList) {
            idBuilder.append(id).append(",");
        }
        idBuilder.setLength(idBuilder.length() - 1);
        return idBuilder.toString();
    }

    protected void bindWorkProfileIcon(ContactListItemView view, int partitionIndex) {
        long directoryId = -1;
        Partition partition = getPartition(partitionIndex);
        if (partition instanceof DirectoryPartition) {
            directoryId = ((DirectoryPartition) partition).getDirectoryId();
        }
        view.setWorkProfileIconEnabled(ContactsUtils.determineUserType(Long.valueOf(directoryId), null) == 1);
    }

    private void buildAccountTypeNameString(Builder builder, ContactListFilter filter) {
        String str;
        StringBuilder accountBuilder = new StringBuilder();
        if (TextUtils.isEmpty(filter.accountName)) {
            str = "account_name\u0002\u0001";
        } else {
            str = "account_name\u0002" + filter.accountName + "\u0001";
        }
        accountBuilder.append(str);
        if (TextUtils.isEmpty(filter.accountType)) {
            str = "account_type\u0002\u0001";
        } else {
            str = "account_type\u0002" + filter.accountType + "\u0001";
        }
        accountBuilder.append(str);
        if (TextUtils.isEmpty(filter.dataSet)) {
            accountBuilder.append("data_set").append("\u0002\u0001");
        } else {
            accountBuilder.append("data_set").append("\u0002").append(filter.dataSet).append("\u0001");
        }
        if (accountBuilder.length() > 0) {
            accountBuilder.setLength(accountBuilder.length() - 1);
            builder.appendQueryParameter("exclude_accounts", accountBuilder.toString());
        }
    }
}
