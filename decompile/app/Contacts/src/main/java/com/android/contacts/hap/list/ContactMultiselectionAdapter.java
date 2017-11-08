package com.android.contacts.hap.list;

import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListItemView;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.SearchContract$ContactsSearch;
import com.huawei.cspcommon.util.SortUtils;
import java.util.ArrayList;
import java.util.List;

public class ContactMultiselectionAdapter extends ContactListAdapter {
    Cursor mCursor;
    private int mMissingItemsIndex;
    private boolean mShowCheckBox;

    public ContactMultiselectionAdapter(Context context) {
        super(context);
        this.mShowCheckBox = true;
        this.mMissingItemsIndex = -1;
        this.mMissingItemsIndex = -1;
    }

    public ContactMultiselectionAdapter(Context context, int missingItemIndex) {
        super(context);
        this.mShowCheckBox = true;
        this.mMissingItemsIndex = -1;
        this.mMissingItemsIndex = missingItemIndex;
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        String sortOrder;
        ContactListFilter filter = getFilter();
        if (isSearchMode()) {
            String query = getQueryString();
            if (query == null) {
                query = "";
            }
            query = query.trim();
            if (TextUtils.isEmpty(query)) {
                loader.setUri(Contacts.CONTENT_URI);
                loader.setProjection(getFilterProjection(false));
                loader.setSelection("0");
            } else {
                Builder builder = Contacts.CONTENT_FILTER_URI.buildUpon();
                builder.appendPath(query);
                builder.appendQueryParameter("directory", String.valueOf(directoryId));
                if (!(directoryId == 0 || directoryId == 1)) {
                    builder.appendQueryParameter("limit", String.valueOf(getDirectoryResultLimit()));
                }
                builder.appendQueryParameter("deferred_snippeting", CallInterceptDetails.BRANDED_STATE);
                loader.setUri(builder.build());
                loader.setProjection(getFilterProjection(true));
                if (this.mMissingItemsIndex >= 0) {
                    configMissingItemsSelection(loader, directoryId);
                } else {
                    configureSelection(loader, directoryId, filter);
                }
            }
        } else {
            configureUri(loader, directoryId, filter);
            configureProjection(loader, directoryId, filter);
            if (this.mMissingItemsIndex >= 0) {
                configMissingItemsSelection(loader, directoryId);
            } else {
                configureSelection(loader, directoryId, filter);
            }
        }
        if (getSortOrder() == 1) {
            sortOrder = "sort_key";
        } else {
            sortOrder = "sort_key_alt";
        }
        loader.setSortOrder(sortOrder);
    }

    public void changeCursor(int partitionIndex, Cursor cursor) {
        this.mCursor = cursor;
        super.changeCursor(partitionIndex, cursor);
    }

    private void configMissingItemsSelection(CursorLoader loader, long directoryId) {
        if (directoryId == 0) {
            StringBuilder selection = new StringBuilder();
            switch (this.mMissingItemsIndex) {
                case 0:
                    selection.append("has_name").append("=0");
                    break;
                case 1:
                    selection.append("has_phone_number").append("=0");
                    break;
                case 2:
                    selection.append("has_phone_number").append("=0");
                    selection.append(" AND ").append("has_email").append("=0");
                    break;
            }
            loader.setSelection(selection.toString());
        }
    }

    protected void configureSelection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        if (filter != null && directoryId == 0) {
            boolean isAccountinFilterEmpty;
            String accountTypeStr = CommonUtilMethods.getWritableAccountStrExcludeSim(true, this.mContext);
            if (TextUtils.isEmpty(filter.accountType) || TextUtils.isEmpty(filter.accountName)) {
                isAccountinFilterEmpty = true;
            } else {
                isAccountinFilterEmpty = isReadOnlySimFilterType(filter.accountType);
            }
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            SharedPreferences pref = SharePreferenceUtil.getDefaultSp_de(this.mContext);
            boolean showSimContactspreference = pref.getBoolean("preference_show_sim_contacts", true);
            boolean isDisplayOnlyContactsWithPhoneNumber = pref.getBoolean("preference_contacts_only_phonenumber", false);
            switch (filter.filterType) {
                case -21:
                    if (!filter.mIsNoCompanyGroup) {
                        selectionArgs.add(filter.mCurrentCompanyName);
                        List<AccountWithDataSet> accountList = AccountTypeManager.getInstance(this.mContext).getAccountsExcludeBothSim(true);
                        if (accountList != null && accountList.size() > 0) {
                            selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE ");
                            selection.append("(company !=? OR company IS NULL) AND (");
                            int i = 0;
                            for (AccountWithDataSet account : accountList) {
                                if (i > 0) {
                                    selection.append(" OR ");
                                }
                                selection.append("account_name=? AND account_type=?");
                                selectionArgs.add(account.name);
                                selectionArgs.add(account.type);
                                if (account.dataSet != null) {
                                    selection.append(" AND data_set=?");
                                    selectionArgs.add(account.dataSet);
                                } else {
                                    selection.append(" AND data_set IS NULL");
                                }
                                i++;
                            }
                            selection.append("))");
                            break;
                        }
                    }
                    selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE ");
                    selection.append("company IS NOT NULL)");
                    break;
                case -20:
                    setSelectionAndSelectionArgsForCamcardGroup(selection, selectionArgs, filter);
                    break;
                case -19:
                    if (!isAccountinFilterEmpty && accountTypeStr != null && accountTypeStr.length() > 0) {
                        selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type");
                        selection.append(" IN ( ");
                        selection.append(accountTypeStr);
                        selection.append(" )) AND ");
                        selection.append("is_private = 0");
                        break;
                    }
                    selection.append("1 == 0");
                    break;
                case -18:
                    if (!isAccountinFilterEmpty) {
                        selection.append("is_private").append("!=0");
                        break;
                    } else {
                        selection.append("1 == 0");
                        break;
                    }
                case -17:
                    selection.append("_id NOT IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type IN ( 'com.android.huawei.sim', 'com.android.huawei.secondsim'))");
                    break;
                case -14:
                    setSelectionForExportContacts(selection, filter, selectionArgs);
                    break;
                case -13:
                    selection.append("_id NOT IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE raw_contact_is_read_only = 1)");
                    break;
                case -11:
                    if (!filter.mIsResultBackRequired) {
                        setSelectionAndSelectionArgsForAddMembersToGroup(selection, selectionArgs, filter);
                        break;
                    } else {
                        setSelectionAndSelectionArgsForAddMembersToGroupForResultBack(selection, selectionArgs, filter);
                        break;
                    }
                case -10:
                    selection.append("starred!=0");
                    break;
                case -9:
                    setSelectionAndSelectionArgsForFavoritesEdit(selection, selectionArgs, filter);
                    break;
                case -4:
                    selection.append("starred!=0");
                    break;
                case -3:
                    selection.append("in_visible_group=1");
                    if (isDisplayOnlyContactsWithPhoneNumber) {
                        selection.append(" AND ").append("has_phone_number").append("=1");
                    }
                    setSimReadOnlySelection(selection, filter.mIsShareOrDelete);
                    break;
                case -2:
                    if (showSimContactspreference) {
                        setSimReadOnlySelection(selection, filter.mIsShareOrDelete);
                    } else {
                        selection.append("_id").append(" IN (SELECT DISTINCT ").append("contact_id").append(" FROM view_raw_contacts WHERE ").append("account_type");
                        if (SimFactoryManager.isDualSim()) {
                            selection.append(" NOT IN ('").append("com.android.huawei.sim").append("','").append("com.android.huawei.secondsim").append("')");
                        } else {
                            selection.append("!=?");
                            selectionArgs.add("com.android.huawei.sim");
                        }
                        selection.append(")");
                    }
                    if (isDisplayOnlyContactsWithPhoneNumber) {
                        if (!TextUtils.isEmpty(selection)) {
                            selection.append(" AND ");
                        }
                        selection.append("has_phone_number").append("=1");
                        break;
                    }
                    break;
                case 0:
                    if (!isAccountinFilterEmpty) {
                        selection.append("_id IN (SELECT DISTINCT contact_id FROM view_raw_contacts WHERE account_type=? AND account_name=?");
                        selectionArgs.add(filter.accountType);
                        selectionArgs.add(filter.accountName);
                        if (filter.dataSet != null) {
                            selection.append(" AND data_set=?");
                            selectionArgs.add(filter.dataSet);
                        } else {
                            selection.append(" AND data_set IS NULL");
                        }
                        selection.append(")");
                        if (isDisplayOnlyContactsWithPhoneNumber) {
                            selection.append(" AND ");
                            selection.append("has_phone_number").append("=1");
                            break;
                        }
                    }
                    selection.append("1 == 0");
                    break;
                    break;
                case 1:
                    selection.append("mimetype=? AND data1=?");
                    selectionArgs.add("vnd.android.cursor.item/group_membership");
                    selectionArgs.add(String.valueOf(filter.groupId));
                    break;
                default:
                    HwLog.v("ContactMultiselectionAdapter", "don't handle this filter type: " + filter.filterType);
                    break;
            }
            if (HwLog.HWDBG) {
                HwLog.d("ContactMultiselectionAdapter", " Selection : " + selection.toString());
            }
            loader.setSelection(selection.toString());
            loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[0]));
        }
    }

    private void configureProjection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        loader.setProjection(getProjection());
    }

    private void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = Contacts.CONTENT_URI;
        if (directoryId == 0) {
            uri = ContactListAdapter.buildSectionIndexerUri(uri);
        }
        if (!(!PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext) || filter == null || filter.filterType == -3)) {
            uri = uri.buildUpon().appendQueryParameter("directory", String.valueOf(0)).build();
        }
        loader.setUri(uri);
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        boolean z;
        if (position == 0) {
            PLog.d(0, "ContactMultiselectionAdapter bindView begin");
        }
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        if (isSearchMode()) {
            view.setSearchMatchType(cursor);
        }
        view.setHighlightedPrefix(isSearchMode() ? getLowerCaseQueryString() : null);
        if (position == cursor.getCount() - 1) {
            z = true;
        } else {
            z = false;
        }
        bindSectionHeaderAndDivider(view, position, cursor, z);
        if (this.mMissingItemsIndex >= 0) {
            view.setAccountFilterText(null);
        }
        bindPhoto(view, partition, cursor);
        bindName(view, cursor);
        bindSimIcon(view, cursor);
        if (this.mShowCheckBox) {
            bindCheckBox(view);
            view.setAccountIcons(null);
            this.itemCheckedListener.setItemChecked(position, getContactLookupUri(cursor, position), isSearchMode());
        } else {
            hideCheckBox(view);
            if (!isSearchMode() || partition <= 0) {
                bindAccountInfo(view, cursor);
            } else {
                view.setAccountIcons(null);
            }
        }
        if (isBindPresenceAndStatusMessage()) {
            bindPresenceAndStatusMessage(view, cursor);
        }
        if (this.mContext.getResources().getBoolean(R.bool.show_account_icons)) {
            bindAccountInfo(view, cursor);
        }
        if (isSearchMode()) {
            bindSearchSnippetInfo(view, cursor);
        } else {
            view.setSnippet(null);
        }
        if (isSearchMode()) {
            view.showCompany(cursor, cursor.getColumnIndex("company"), -1);
        } else {
            view.setCompany(null);
        }
        if (position == 0) {
            PLog.d(24, "ContactMultiselectionAdapter bindView end");
        }
    }

    protected Uri getContactLookupUri(Cursor cursor, int pos) {
        return Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(Long.valueOf(cursor.getLong(0)).longValue()));
    }

    protected boolean isBindPresenceAndStatusMessage() {
        return true;
    }

    public Cursor getDataCursor() {
        return this.mCursor;
    }

    private void setSelectionAndSelectionArgsForAddMembersToGroupForResultBack(StringBuilder aSelectionBuilder, List<String> aSelectionArgs, ContactListFilter aFilter) {
        int i = 0;
        if (TextUtils.isEmpty(aFilter.accountName) || TextUtils.isEmpty(aFilter.accountType)) {
            aSelectionBuilder.append("1 == 0");
            return;
        }
        aSelectionBuilder.append("_id IN (");
        aSelectionBuilder.append("SELECT contact_id FROM view_raw_contacts WHERE ").append("deleted=0 ");
        aSelectionBuilder.append(" AND account_name=? AND ").append("account_type=? AND ");
        aSelectionArgs.add(aFilter.accountName);
        aSelectionArgs.add(aFilter.accountType);
        if (aFilter.dataSet == null) {
            aSelectionBuilder.append("data_set IS NULL");
        } else {
            aSelectionBuilder.append("data_set=?");
            aSelectionArgs.add(aFilter.dataSet);
        }
        aSelectionBuilder.append(")");
        if (aFilter.mSelectedContacts != null && aFilter.mSelectedContacts.length > 0) {
            aSelectionBuilder.append(" AND name_raw_contact_id NOT IN (");
            for (long id : aFilter.mSelectedContacts) {
                aSelectionBuilder.append(id).append(",");
            }
            aSelectionBuilder.setLength(aSelectionBuilder.length() - 1);
            aSelectionBuilder.append(")");
        }
        if (aFilter.groupId != 0) {
            aSelectionBuilder.append(" AND _id NOT IN (");
            aSelectionBuilder.append("SELECT contact_id FROM view_data WHERE mimetype=? AND data1=?");
            aSelectionArgs.add("vnd.android.cursor.item/group_membership");
            aSelectionArgs.add(String.valueOf(aFilter.groupId));
            if (aFilter.mRemovedContactIds != null && aFilter.mRemovedContactIds.length > 0) {
                aSelectionBuilder.append(" AND raw_contact_id  NOT IN (");
                long[] jArr = aFilter.mRemovedContactIds;
                int length = jArr.length;
                while (i < length) {
                    aSelectionBuilder.append(jArr[i]).append(",");
                    i++;
                }
                aSelectionBuilder.setLength(aSelectionBuilder.length() - 1);
                aSelectionBuilder.append(")");
            }
            aSelectionBuilder.append(")");
        }
    }

    private void setSelectionAndSelectionArgsForFavoritesEdit(StringBuilder aSelectionBuilder, List<String> list, ContactListFilter aFilter) {
        long[] alreadySelectedContacts = aFilter.mSelectedContacts;
        int selectedContactsLen = 0;
        if (alreadySelectedContacts != null) {
            selectedContactsLen = alreadySelectedContacts.length;
        }
        if (aFilter.mIsFavoritesForWidget) {
            aSelectionBuilder.append("starred=0");
            return;
        }
        aSelectionBuilder.append("_id").append(" IN (");
        aSelectionBuilder.append("SELECT ").append("contact_id").append(" FROM view_raw_contacts WHERE ").append("deleted=0");
        if (selectedContactsLen > 0) {
            StringBuilder selectedContactsSelectionBuilder = new StringBuilder("(");
            for (int idx = 0; idx < selectedContactsLen; idx++) {
                if (idx > 0) {
                    selectedContactsSelectionBuilder.append(",");
                }
                selectedContactsSelectionBuilder.append(alreadySelectedContacts[idx]);
            }
            selectedContactsSelectionBuilder.append(")");
            aSelectionBuilder.append(" AND ").append("contact_id").append(" NOT IN ");
            aSelectionBuilder.append(selectedContactsSelectionBuilder.toString());
        }
        aSelectionBuilder.append(")");
    }

    private void setSelectionAndSelectionArgsForAddMembersToGroup(StringBuilder aSelectionBuilder, List<String> aSelectionArgs, ContactListFilter aFilter) {
        if (TextUtils.isEmpty(aFilter.accountName) || TextUtils.isEmpty(aFilter.accountType)) {
            aSelectionBuilder.append("1 == 0");
            return;
        }
        aSelectionBuilder.append("_id IN (");
        aSelectionBuilder.append("SELECT contact_id FROM view_raw_contacts WHERE ").append("deleted=0 ");
        aSelectionBuilder.append(" AND account_name=? AND ").append("account_type=? AND ");
        aSelectionArgs.add(aFilter.accountName);
        aSelectionArgs.add(aFilter.accountType);
        if (aFilter.dataSet == null) {
            aSelectionBuilder.append("data_set IS NULL");
        } else {
            aSelectionBuilder.append("data_set=?");
            aSelectionArgs.add(aFilter.dataSet);
        }
        aSelectionBuilder.append(")");
        if (aFilter.groupId != 0) {
            aSelectionBuilder.append(" AND _id NOT IN (");
            aSelectionBuilder.append("SELECT contact_id FROM view_data WHERE mimetype=? AND data1=?)");
            aSelectionArgs.add("vnd.android.cursor.item/group_membership");
            aSelectionArgs.add(String.valueOf(aFilter.groupId));
        }
    }

    private void setSelectionAndSelectionArgsForCamcardGroup(StringBuilder aSelectionBuilder, List<String> aSelectionArgs, ContactListFilter aFilter) {
        aSelectionBuilder.append("is_camcard").append(" != ").append(2).append(" AND ").append("_id").append(" IN (");
        aSelectionBuilder.append("SELECT ").append("contact_id").append(" FROM view_data,groups WHERE groups.title = ? AND groups._id = data1 AND ").append("mimetype").append("=?)");
        aSelectionArgs.add("PREDEFINED_HUAWEI_GROUP_CCARD");
        aSelectionArgs.add("vnd.android.cursor.item/group_membership");
    }

    private void setSelectionForExportContacts(StringBuilder aSelectionBuilder, ContactListFilter aFilter, List<String> aSelectionArgs) {
        if (aFilter.mAccounts != null && !aFilter.mAccounts.isEmpty()) {
            aSelectionBuilder.append("_id IN (");
            aSelectionBuilder.append("SELECT contact_id FROM view_raw_contacts WHERE (");
            boolean firstOne = true;
            for (AccountWithDataSet account : aFilter.mAccounts) {
                if (!TextUtils.isEmpty(account.type)) {
                    if (firstOne) {
                        firstOne = false;
                    } else {
                        aSelectionBuilder.append(" OR ");
                    }
                    aSelectionBuilder.append('(');
                    aSelectionBuilder.append("account_type");
                    aSelectionBuilder.append(" = ? AND ");
                    aSelectionBuilder.append("account_name");
                    aSelectionBuilder.append(" = ? )");
                    aSelectionArgs.add(account.type);
                    aSelectionArgs.add(account.name);
                }
            }
            aSelectionBuilder.append("))");
        }
    }

    public Uri getSelectedContactUri(int aPosition) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(aPosition)) {
            return null;
        }
        return Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(this.mCursor.getLong(0)));
    }

    public int getSelectedContactId(int position) {
        if (this.mCursor == null || !this.mCursor.moveToPosition(position)) {
            return -1;
        }
        return this.mCursor.getInt(0);
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        return (ContactListItemView) super.newView(context, partition, cursor, position, parent);
    }

    public void switchMode(boolean isDeleteMode) {
        this.mShowCheckBox = isDeleteMode;
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        configHwSearchUri(loader, directoryId, getFilter());
        configHwSearchProjection(loader);
        configHwSearchSortOrder(loader);
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = getHwSearchBaseUri(SearchContract$ContactsSearch.CONTACTS_CONTENT_FILTER_URI, "search_type", "search_contacts");
        if (filter == null || directoryId != 0) {
            loader.setUri(uri);
            return;
        }
        Builder builder = uri.buildUpon();
        if (PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext) && filter.filterType != -3) {
            builder.appendQueryParameter("directory", String.valueOf(0));
        }
        boolean isEmpty = !TextUtils.isEmpty(filter.accountType) ? TextUtils.isEmpty(filter.accountName) : true;
        SharedPreferences pref = SharePreferenceUtil.getDefaultSp_de(this.mContext);
        boolean showSimContactspreference = pref.getBoolean("preference_show_sim_contacts", true);
        boolean isDisplayOnlyContactsWithPhoneNumber = pref.getBoolean("preference_contacts_only_phonenumber", false);
        List<AccountWithDataSet> accountList;
        switch (filter.filterType) {
            case -21:
                if (!filter.mIsNoCompanyGroup) {
                    builder.appendQueryParameter("company_name", Uri.encode(filter.mCurrentCompanyName));
                    SortUtils.buildAccountTypeString(builder, AccountTypeManager.getInstance(this.mContext).getAccountsExcludeBothSim(true));
                    break;
                }
                builder.appendQueryParameter("company_name", Uri.encode(""));
                break;
            case -19:
                accountList = CommonUtilMethods.getWritableAccountListStrExcludeSim(true, this.mContext);
                if (!isEmpty && accountList.size() > 0) {
                    SortUtils.buildAccountTypeString(builder, accountList);
                    builder.appendQueryParameter("is_private", String.valueOf(Boolean.FALSE));
                    break;
                }
            case -18:
                accountList = CommonUtilMethods.getWritableAccountListStrExcludeSim(true, this.mContext);
                if (!isEmpty && accountList.size() > 0) {
                    SortUtils.buildAccountTypeString(builder, accountList);
                    builder.appendQueryParameter("is_private", String.valueOf(Boolean.TRUE));
                    break;
                }
            case -17:
                if (!SimFactoryManager.isDualSim()) {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
                    break;
                } else {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim,com.android.huawei.secondsim");
                    break;
                }
            case -14:
                buildAccountTypeNameString(builder, filter.mAccounts);
                break;
            case -11:
                builder.appendQueryParameter("account_name", filter.accountName);
                builder.appendQueryParameter("account_type", filter.accountType);
                if (!TextUtils.isEmpty(filter.dataSet)) {
                    builder.appendQueryParameter("data_set", filter.dataSet);
                }
                if (filter.groupId != 0) {
                    builder.appendQueryParameter("exclude_groups", String.valueOf(filter.groupId));
                }
                if (filter.mIsResultBackRequired) {
                    if (filter.mSelectedContacts != null && filter.mSelectedContacts.length > 0) {
                        builder.appendQueryParameter("exclude_contacts", buildIdString(filter.mSelectedContacts));
                    }
                    if (!(filter.groupId == 0 || filter.mRemovedContactIds == null || filter.mRemovedContactIds.length <= 0)) {
                        builder.appendQueryParameter("exclude_raw_contacts", buildIdString(filter.mRemovedContactIds));
                        break;
                    }
                }
                break;
            case -10:
            case -4:
                builder.appendQueryParameter("starred", String.valueOf(Boolean.TRUE));
                break;
            case -9:
                if (!filter.mIsFavoritesForWidget) {
                    long[] alreadySelectedContacts = filter.mSelectedContacts;
                    int selectedContactsLen = alreadySelectedContacts != null ? alreadySelectedContacts.length : 0;
                    if (selectedContactsLen > 0) {
                        StringBuilder selectedContactsSelectionBuilder = new StringBuilder();
                        for (int idx = 0; idx < selectedContactsLen; idx++) {
                            selectedContactsSelectionBuilder.append(alreadySelectedContacts[idx]).append(",");
                        }
                        if (selectedContactsSelectionBuilder.length() > 0) {
                            selectedContactsSelectionBuilder.setLength(selectedContactsSelectionBuilder.length() - 1);
                            builder.appendQueryParameter("exclude_contacts", selectedContactsSelectionBuilder.toString());
                            break;
                        }
                    }
                }
                builder.appendQueryParameter("starred", String.valueOf(Boolean.FALSE));
                break;
                break;
            case -5:
                builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                break;
            case -3:
                if (showSimContactspreference) {
                    if (filter.mIsShareOrDelete) {
                        hideSimAccount(builder);
                    }
                } else if (SimFactoryManager.isDualSim()) {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim,com.android.huawei.secondsim");
                } else {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
                }
                builder.appendQueryParameter("visible_contacts", String.valueOf(Boolean.TRUE));
                builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                break;
            case -2:
                if (showSimContactspreference) {
                    if (filter.mIsShareOrDelete) {
                        hideSimAccount(builder);
                    }
                } else if (SimFactoryManager.isDualSim()) {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim,com.android.huawei.secondsim");
                } else {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
                }
                builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                break;
            case 0:
                if (!isEmpty) {
                    filter.addAccountQueryParameterToUrl(builder);
                    builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                }
                if (filter.mIsShareOrDelete && CommonUtilMethods.isSimAccount(filter.accountType)) {
                    hideSimAccount(builder);
                    break;
                }
            case 1:
                builder.appendQueryParameter("include_groups", String.valueOf(filter.groupId));
                break;
        }
        loader.setUri(builder.build());
    }

    private String buildIdString(long[] ids) {
        if (ids == null || ids.length == 0) {
            return null;
        }
        StringBuilder idBuilder = new StringBuilder(ids.length * 4);
        for (long id : ids) {
            idBuilder.append(id).append(",");
        }
        idBuilder.setLength(idBuilder.length() - 1);
        return idBuilder.toString();
    }

    private void buildAccountTypeNameString(Builder builder, List<AccountWithDataSet> accountList) {
        if (accountList != null && accountList.size() > 0) {
            StringBuilder accountBuilder = new StringBuilder();
            for (AccountWithDataSet account : accountList) {
                String str;
                if (TextUtils.isEmpty(account.name)) {
                    str = "account_name\u0002\u0001";
                } else {
                    str = "account_name\u0002" + account.name + "\u0001";
                }
                accountBuilder.append(str);
                if (TextUtils.isEmpty(account.type)) {
                    str = "account_type\u0002\u0001";
                } else {
                    str = "account_type\u0002" + account.type + "\u0001";
                }
                accountBuilder.append(str);
                accountBuilder.append("data_set").append("\u0002\u0001");
            }
            if (accountBuilder.length() > 0) {
                accountBuilder.setLength(accountBuilder.length() - 1);
                builder.appendQueryParameter("include_accounts", accountBuilder.toString());
            }
        }
    }

    private boolean isReadOnlySimFilterType(String accountType) {
        boolean z = false;
        if ("com.android.huawei.sim".equalsIgnoreCase(accountType)) {
            if (!SimFactoryManager.isSimLoadingFinished(0)) {
                z = true;
            }
            return z;
        } else if (!"com.android.huawei.secondsim".equalsIgnoreCase(accountType)) {
            return false;
        } else {
            if (!SimFactoryManager.isSimLoadingFinished(1)) {
                z = true;
            }
            return z;
        }
    }

    private void setSimReadOnlySelection(StringBuilder selection, boolean isShareOrDelete) {
        if (isShareOrDelete) {
            boolean isFirstSimReadOnly = !SimFactoryManager.isSimLoadingFinished(0);
            boolean isSecondSimReadOnly = !SimFactoryManager.isSimLoadingFinished(1);
            if (isFirstSimReadOnly || isSecondSimReadOnly) {
                StringBuilder simSelection = new StringBuilder();
                simSelection.append("_id").append(" IN (SELECT DISTINCT ").append("contact_id").append(" FROM view_raw_contacts WHERE ").append("account_type");
                StringBuilder acccoutSb = new StringBuilder();
                if (isFirstSimReadOnly) {
                    acccoutSb.append("'").append("com.android.huawei.sim").append("'");
                }
                if (isSecondSimReadOnly) {
                    if (!TextUtils.isEmpty(acccoutSb)) {
                        acccoutSb.append(",");
                    }
                    acccoutSb.append("'").append("com.android.huawei.secondsim").append("'");
                }
                if (!TextUtils.isEmpty(selection)) {
                    selection.append(" AND ");
                }
                selection.append(simSelection).append(" NOT IN (").append(acccoutSb.toString()).append("))");
            }
        }
    }

    private boolean isSim1AccountReadOnly() {
        boolean z = false;
        if (SimFactoryManager.isDualSim()) {
            if (!SimFactoryManager.isSimLoadingFinished(0)) {
                z = true;
            }
            return z;
        }
        if (!SimFactoryManager.isSimLoadingFinished(-1)) {
            z = true;
        }
        return z;
    }

    private boolean isSim2AccountReadOnly() {
        boolean z = false;
        if (!SimFactoryManager.isDualSim()) {
            return false;
        }
        if (!SimFactoryManager.isSimLoadingFinished(1)) {
            z = true;
        }
        return z;
    }

    private void hideSimAccount(Builder builder) {
        if (SimFactoryManager.isDualSim()) {
            boolean hideSim1Account = isSim1AccountReadOnly();
            boolean hideSim2Account = isSim2AccountReadOnly();
            if (hideSim1Account && hideSim2Account) {
                builder.appendQueryParameter("exclude_types", "com.android.huawei.sim,com.android.huawei.secondsim");
            } else if (hideSim1Account) {
                builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
            } else if (hideSim2Account) {
                builder.appendQueryParameter("exclude_types", "com.android.huawei.secondsim");
            }
        } else if (isSim1AccountReadOnly()) {
            builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
        }
    }
}
