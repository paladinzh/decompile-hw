package com.android.contacts.list;

import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.compatibility.ContactsCompat;
import com.android.contacts.compatibility.DirectoryCompat;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.hwsearch.HwSearchCursor;
import com.android.contacts.hap.rcs.list.RcsDefaultContactListAdapter;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.MultiUsersUtils;
import com.android.contacts.list.ContactEntryListFragment.ContactsSearchLoader;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.util.ContactDisplayUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.SearchContract$ContactsSearch;
import com.huawei.cspcommon.util.SortUtils;
import java.util.ArrayList;
import java.util.List;

public class DefaultContactListAdapter extends ContactListAdapter {
    private static final boolean DEBUG = HwLog.HWDBG;
    public static final String TAG = DefaultContactListAdapter.class.getSimpleName();
    private boolean mIgnoreShowSimContactsPref;
    private RcsDefaultContactListAdapter mRcsCust;
    private boolean mShowCompany;
    private SparseArray<String> mSparseArray;

    public DefaultContactListAdapter(Context context) {
        super(context);
        this.mIgnoreShowSimContactsPref = false;
        this.mRcsCust = null;
        this.mSparseArray = new SparseArray();
        this.mIgnoreShowSimContactsPref = false;
        if (EmuiFeatureManager.isRcsFeatureEnable() && MultiUsersUtils.isSmsEnabledForCurrentUser(context)) {
            this.mRcsCust = new RcsDefaultContactListAdapter(context);
        }
    }

    public void setIgnoreShowSimContactsPref(boolean ignore) {
        this.mIgnoreShowSimContactsPref = ignore;
    }

    public void configureLoader(CursorLoader loader, long directoryId) {
        String sortOrder;
        boolean z = false;
        boolean isRemoteSearch = isSearchMode() && directoryId != 0;
        if (loader instanceof ProfileAndContactsLoader) {
            ProfileAndContactsLoader profileAndContactsLoader = (ProfileAndContactsLoader) loader;
            if (!(null == null || isRemoteSearch)) {
                z = true;
            }
            profileAndContactsLoader.setLoadProfile(z);
        } else if (loader instanceof VoiceSearchContactsLoader) {
            ((VoiceSearchContactsLoader) loader).setLoadProfile(false);
        }
        ContactListFilter filter = getFilter();
        if ((isRemoteSearch || ((loader instanceof ContactsSearchLoader) && ((ContactsSearchLoader) loader).getQueryString() != null)) && !isVoiceSearchMode()) {
            configureLoaderForSearch(loader, directoryId, filter);
        } else {
            configureUri(loader, directoryId, filter);
            if (isVoiceSearchMode()) {
                loader.setProjection(getVoiceSearchProjection());
            } else {
                loader.setProjection(getProjection());
            }
            configureSelection(loader, directoryId, filter);
        }
        if (getSortOrder() == 1) {
            if (QueryUtil.isSpecialLanguageForSearch() && isSearchMode() && directoryId == 0) {
                sortOrder = "times_contacted DESC,last_time_contacted DESC, sort_key";
            } else {
                sortOrder = "sort_key";
            }
        } else if (QueryUtil.isSpecialLanguageForSearch() && isSearchMode() && directoryId == 0) {
            sortOrder = "times_contacted DESC,last_time_contacted DESC, sort_key_alt";
        } else {
            sortOrder = "sort_key_alt";
        }
        loader.setSortOrder(sortOrder);
    }

    private void configureLoaderForSearch(CursorLoader loader, long directoryId, ContactListFilter filter) {
        String query = getQueryString();
        if (query == null) {
            query = "";
        }
        query = query.trim();
        if (TextUtils.isEmpty(query)) {
            loader.setUri(Contacts.CONTENT_URI);
            loader.setProjection(getFilterProjection(false));
            loader.setSelection("0");
            return;
        }
        Builder builder = Contacts.CONTENT_FILTER_URI.buildUpon();
        builder.appendPath(query);
        builder.appendQueryParameter("directory", String.valueOf(directoryId));
        if (!(directoryId == 0 || directoryId == 1)) {
            builder.appendQueryParameter("limit", String.valueOf(getDirectoryResultLimit()));
        }
        if (filter != null && filter.filterType == 0) {
            filter.addAccountQueryParameterToUrl(builder);
        }
        builder.appendQueryParameter("deferred_snippeting", CallInterceptDetails.BRANDED_STATE);
        loader.setUri(builder.build());
        loader.setProjection(getFilterProjection(true));
        configureSelection(loader, directoryId, filter);
    }

    private void configureUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        Uri uri = Contacts.CONTENT_URI;
        if (filter != null && filter.filterType == -6) {
            String lookupKey = getSelectedContactLookupKey();
            uri = lookupKey != null ? Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey) : ContentUris.withAppendedId(Contacts.CONTENT_URI, getSelectedContactId());
        }
        if (directoryId == 0) {
            uri = ContactListAdapter.buildSectionIndexerUri(uri);
        }
        loader.setUri(CommonUtilMethods.configureFilterUri(this.mContext, filter, uri));
    }

    private void configureSelection(CursorLoader loader, long directoryId, ContactListFilter filter) {
        StringBuilder voiceSearchSelection = new StringBuilder();
        if (isVoiceSearchMode()) {
            ArrayList<String> queryMultiStrings = getQueryMultiString();
            if (queryMultiStrings != null && queryMultiStrings.size() > 0) {
                voiceSearchSelection.append("display_name").append(" LIKE '%").append((String) queryMultiStrings.get(0)).append("%'");
                for (int i = 1; i < queryMultiStrings.size(); i++) {
                    voiceSearchSelection.append(" OR ").append("display_name").append(" LIKE '%").append((String) queryMultiStrings.get(i)).append("%'");
                }
                loader.setSelection(voiceSearchSelection.toString());
            }
        }
        if (filter != null && directoryId == 0) {
            StringBuilder selection = new StringBuilder();
            List<String> selectionArgs = new ArrayList();
            CommonUtilMethods.configureFilterSelection(this.mContext, filter, selection, selectionArgs, this.mIgnoreShowSimContactsPref);
            if (EmuiFeatureManager.isPrivacyFeatureEnabled() && getIfExcludePrivateContacts()) {
                if (!TextUtils.isEmpty(selection)) {
                    selection.insert(0, "(").insert(selection.length(), ")").append(" AND ");
                }
                selection.append("is_private=0");
            }
            if (!isVoiceSearchMode()) {
                loader.setSelection(selection.toString());
            } else if (!TextUtils.isEmpty(selection)) {
                loader.setSelection("(" + voiceSearchSelection.toString() + ") AND " + "(" + selection.toString() + ")");
            }
            loader.setSelectionArgs((String[]) selectionArgs.toArray(new String[0]));
        }
    }

    protected void bindView(View itemView, int partition, Cursor cursor, int position) {
        boolean z;
        if (position == 0) {
            PLog.d(0, "DefaultContactListAdapter bindview begin");
        }
        super.bindView(itemView, partition, cursor, position);
        ContactListItemView view = (ContactListItemView) itemView;
        int dividerPaddingEnd = isSearchMode() ? 0 : this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_end);
        int dividerPaddingStart = isSearchMode() ? 0 : this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_start);
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            dividerPaddingStart = isSearchMode() ? 0 : this.mContext.getResources().getDimensionPixelSize(R.dimen.contact_list_divider_margin_start_Simple);
        }
        view.setHorizontalDividerPadding(dividerPaddingStart, dividerPaddingEnd);
        if (cursor instanceof MultiCursor) {
            z = ((MultiCursor) cursor).getCurrentCursor() instanceof HwSearchCursor;
        } else {
            z = false;
        }
        char[] lprefixarray = null;
        if (isSearchMode()) {
            if (z) {
                view.setSearchMatchType(cursor);
            } else {
                view.setDefaultType();
                view.setSearchMatchType(cursor);
            }
            lprefixarray = getLowerCaseQueryString();
        }
        view.setHighlightedPrefix(lprefixarray);
        if (isSelectionVisible()) {
            boolean isSelected = isSelectedContact(partition, cursor);
            if (!isHighLightVisible()) {
                view.setActivated(isSelected);
            } else if (isSelected) {
                view.setBackgroundColor(this.mContext.getResources().getColor(R.color.split_itme_selected));
            } else {
                view.setBackgroundColor(0);
            }
        }
        if (DEBUG) {
            HwLog.d(TAG, "getDisplayPhotos():" + getDisplayPhotos());
        }
        if (getDisplayPhotos()) {
            bindPhoto(view, partition, cursor);
        }
        if (ContactDisplayUtils.isSimpleDisplayMode()) {
            view.setIsDisplayAlph(true);
        }
        bindAlpha(view, position);
        if (isSearchMode()) {
            view.setIsDisplayAlph(false);
            view.removeAlphaView();
        }
        bindName(view, cursor);
        bindPresenceAndStatusMessage(view, cursor);
        bindSimIcon(view, cursor);
        if ((!isSearchMode() || partition <= 0) && !view.isSimAccount()) {
            bindAccountInfo(view, cursor);
        } else {
            view.setAccountIcons(null);
        }
        if (this.mRcsCust != null) {
            this.mRcsCust.bindCustomizationsInfo(view, cursor);
        }
        if (!isSearchMode() || isVoiceSearchMode()) {
            view.setSnippet(null);
        } else if (z) {
            bindSearchSnippetInfo(view, cursor);
        } else {
            bindSearchSnippet(view, cursor);
        }
        if (isSearchMode() || (this.mShowCompany && !ContactDisplayUtils.isSimpleDisplayMode())) {
            view.showCompany(cursor, cursor.getColumnIndex("company"), cursor.getColumnIndex("title"));
        } else {
            view.setCompany(null);
        }
        if (position == 0) {
            PLog.d(7, "DefaultContactListAdapter bindview end");
        }
    }

    private void bindAlpha(ContactListItemView view, int position) {
        view.setIsDisplayAlph(true);
        String text = "";
        if (!(position == -1 || this.mSparseArray == null || this.mSparseArray.size() <= 0)) {
            text = (String) this.mSparseArray.get(position);
        }
        view.setAlphaView(text);
    }

    private boolean isCustomFilterForPhoneNumbersOnly() {
        return SharePreferenceUtil.getDefaultSp_de(getContext()).getBoolean("only_phones", false);
    }

    protected void configHwSearchLoader(CursorLoader loader, long directoryId) {
        configHwSearchUri(loader, directoryId, getFilter());
        configHwSearchProjection(loader);
        configHwSearchSortOrder(loader);
    }

    protected void configHwSearchUri(CursorLoader loader, long directoryId, ContactListFilter filter) {
        boolean isEnterpriseDirectory = DirectoryCompat.isEnterpriseDirectoryId(directoryId);
        Uri uri = getHwSearchBaseUri(isEnterpriseDirectory ? ContactsCompat.getContentUri() : SearchContract$ContactsSearch.CONTACTS_CONTENT_FILTER_URI, "search_type", "search_contacts");
        if (filter == null || !(directoryId == 0 || isEnterpriseDirectory)) {
            loader.setUri(uri);
            return;
        }
        Builder builder = uri.buildUpon();
        if (!(filter.filterType == -3 || filter.filterType == -6 || isEnterpriseDirectory || !PhoneCapabilityTester.isOnlySyncMyContactsEnabled(this.mContext))) {
            builder.appendQueryParameter("directory", String.valueOf(0));
        }
        if (isEnterpriseDirectory) {
            builder.appendQueryParameter("directory", String.valueOf(directoryId));
        }
        SharedPreferences pref = SharePreferenceUtil.getDefaultSp_de(getContext());
        boolean showSimContactspreference = pref.getBoolean("preference_show_sim_contacts", true);
        if (this.mIgnoreShowSimContactsPref) {
            showSimContactspreference = true;
        }
        boolean isDisplayOnlyContactsWithPhoneNumber = pref.getBoolean("preference_contacts_only_phonenumber", false);
        switch (filter.filterType) {
            case -16:
                SortUtils.buildAccountTypeString(builder, AccountTypeManager.getInstance(this.mContext).getAccounts(true));
                builder.appendQueryParameter("exclude_types", "com.android.huawei.secondsim");
                builder.appendQueryParameter("raw_contact_is_read_only", String.valueOf(Boolean.FALSE));
                break;
            case -15:
                SortUtils.buildAccountTypeString(builder, AccountTypeManager.getInstance(this.mContext).getAccounts(true));
                builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
                builder.appendQueryParameter("raw_contact_is_read_only", String.valueOf(Boolean.FALSE));
                break;
            case -13:
                SortUtils.buildAccountTypeString(builder, AccountTypeManager.getInstance(this.mContext).getAccounts(true));
                builder.appendQueryParameter("raw_contact_is_read_only", String.valueOf(Boolean.FALSE));
                break;
            case -12:
                SortUtils.buildAccountTypeString(builder, AccountTypeManager.getInstance(this.mContext).getAccounts(true));
                if (SimFactoryManager.isDualSim()) {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim,com.android.huawei.secondsim");
                } else {
                    builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
                }
                builder.appendQueryParameter("raw_contact_is_read_only", String.valueOf(Boolean.FALSE));
                break;
            case -5:
                builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                break;
            case -4:
                builder.appendQueryParameter("starred", String.valueOf(Boolean.TRUE));
                break;
            case -3:
                builder.appendQueryParameter("visible_contacts", String.valueOf(Boolean.TRUE));
                builder.appendQueryParameter("has_phone_number", String.valueOf(isCustomFilterForPhoneNumbersOnly() | isDisplayOnlyContactsWithPhoneNumber));
                break;
            case -2:
                if (!showSimContactspreference) {
                    if (SimFactoryManager.isDualSim()) {
                        builder.appendQueryParameter("exclude_types", "com.android.huawei.sim,com.android.huawei.secondsim");
                    } else {
                        builder.appendQueryParameter("exclude_types", "com.android.huawei.sim");
                    }
                }
                builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                break;
            case 0:
                filter.addAccountQueryParameterToUrl(builder);
                builder.appendQueryParameter("has_phone_number", String.valueOf(isDisplayOnlyContactsWithPhoneNumber));
                break;
        }
        if (EmuiFeatureManager.isPrivacyFeatureEnabled() && getIfExcludePrivateContacts()) {
            builder.appendQueryParameter("is_private", String.valueOf(Boolean.FALSE));
        }
        loader.setUri(builder.build());
    }

    public void setShowCompany(boolean isShowCompany) {
        this.mShowCompany = isShowCompany;
    }

    protected View newHeaderView(Context context, int partition, Cursor cursor, ViewGroup parent) {
        View header = null;
        if (this.mContext instanceof PeopleActivity) {
            header = ((PeopleActivity) this.mContext).getContactListHelper().getDirectoryHeader();
        }
        if (header == null) {
            return super.newHeaderView(context, partition, cursor, parent);
        }
        return header;
    }

    protected View newView(Context context, int partition, Cursor cursor, int position, ViewGroup parent) {
        if (!(this.mContext instanceof PeopleActivity)) {
            return super.newView(context, partition, cursor, position, parent);
        }
        ContactListItemView view = ((PeopleActivity) this.mContext).getContactListHelper().getContactListItemView();
        view.setUnknownNameText(this.mUnknownNameText);
        view.setActivatedStateSupported(isSelectionVisible());
        return view;
    }

    public void setCursorMap(SparseArray<String> sparseArray) {
        Object obj = null;
        String str = TAG;
        StringBuilder append = new StringBuilder().append("setCursorMap,sparseArray size:");
        if (sparseArray != null) {
            obj = Integer.valueOf(sparseArray.size());
        }
        HwLog.i(str, append.append(obj).toString());
        this.mSparseArray = sparseArray;
    }
}
