package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.ContactSaveService;
import com.android.contacts.ContactsUtils;
import com.android.contacts.group.SmartGroupUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.camcard.CCUtils;
import com.android.contacts.hap.list.ContactMultiselectionFragment.MultiSelectListener;
import com.android.contacts.hap.rcs.list.RcsContactDataMultiSelectFragment;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.Constants;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.android.contacts.widget.PinnedHeaderListView;
import com.android.contacts.widget.PinnedHeaderListView.ListViewOperationListener;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;

public class ContactDataMultiSelectFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    protected ContactMultiSelectionActivity localActivityRef;
    protected ActionBar mActionBar;
    protected OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int idCancel;
            int idDone;
            if (EmuiVersion.isSupportEmui()) {
                idCancel = 16908295;
                idDone = 16908296;
            } else {
                idCancel = R.id.icon1;
                idDone = R.id.icon2;
            }
            int viewId = v.getId();
            if (viewId == idCancel) {
                ContactDataMultiSelectFragment.this.getActivity().finish();
            } else if (viewId != idDone) {
            } else {
                if (ContactDataMultiSelectFragment.this.localActivityRef.mSelectedDataUris.size() <= 0) {
                    Toast.makeText(ContactDataMultiSelectFragment.this.mContext, R.string.no_contacts_selected, 0).show();
                } else if (ContactDataMultiSelectFragment.this.mRcsCust == null || ContactDataMultiSelectFragment.this.mRcsCust.ifGroupMemberSizeValid(ContactDataMultiSelectFragment.this.localActivityRef)) {
                    ContactDataMultiSelectFragment.this.doOperation();
                }
            }
        }
    };
    private MenuItem mActionMenu;
    protected DataListAdapter mAdapter;
    private boolean mBlackListEnabled;
    private EditText mContactsSearchView;
    Activity mContext;
    private HwCustContactDataMultiSelectFragment mCust = null;
    protected ActionBarCustom mCustActionBar;
    private ActionBarCustomTitle mCustomTitle;
    public boolean mDefaultSelectionCompleted = false;
    private int mHeadderItem = 0;
    protected boolean mIsAllSelected = false;
    private ListViewOperationListener mListViewOperationListener = new ListViewOperationListener() {
        public void onMaxLimitReached() {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactDataMultiSelectFragment", "Max Limit Reached");
            }
            ContactDataMultiSelectFragment.this.showToast();
        }

        public boolean isMaxLimitReached(int position) {
            if (!ContactDataMultiSelectFragment.this.isLimitSet() || ContactDataMultiSelectFragment.this.mMaxLimit == -1 || ContactDataMultiSelectFragment.this.localActivityRef.mSelectedDataUris.size() < ContactDataMultiSelectFragment.this.mMaxLimit || ContactDataMultiSelectFragment.this.getListView().isItemChecked(position)) {
                return false;
            }
            return true;
        }
    };
    private ContactMultiselectListLoader mLoader;
    private int mMaxLimit;
    private MultiSelectListener mMultiListener;
    protected Activity mParentActivity;
    private RcsContactDataMultiSelectFragment mRcsCust = null;
    protected boolean mScreenModeChange;
    private int mSelectedCountInSearch;
    private String[] mSmartGroupParameters;
    protected TextView mTextViewCount;
    private int mTotalContactCount;
    private int mTotalContacts = 0;
    private int mUpdateSearchCount;

    public HashMap<Long, Long> getFrequentDataMap() {
        if (this.mLoader != null) {
            return this.mLoader.getFrequentDataMap();
        }
        return null;
    }

    public ContactDataMultiSelectFragment() {
        setPhotoLoaderEnabled(true);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        this.mContext = getActivity();
        this.mActionBar = this.mContext.getActionBar();
        if (!EmuiVersion.isSupportEmui() && this.mCustActionBar == null) {
            this.mCustActionBar = new ActionBarCustom(this.mContext, this.mActionBar);
        }
        configureActionBar();
        View lView = inflater.inflate(R.layout.contact_picker_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(getContext()));
        return lView;
    }

    protected void configureActionBar() {
        this.mCustomTitle = new ActionBarCustomTitle(this.mContext);
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setCustomTitle(this.mActionBar, this.mCustomTitle.getTitleLayout());
        } else {
            this.mCustActionBar.setStartIcon(true, null, this.mActionBarListener);
            this.mCustActionBar.setCustomTitle(this.mCustomTitle.getTitleLayout());
        }
        this.mCustomTitle.setCustomTitle(getContactDataMultiselectionTitle(), this.localActivityRef.mSelectedDataUris.size());
    }

    protected void configureOptionsMenu(Menu menu) {
        this.mActionMenu = menu.findItem(R.id.menu_action_operation);
        buildActionMenu(this.mActionMenu);
        ViewUtil.setMenuItemsStateListIcon(this.mContext, menu);
        this.mActionMenu.setVisible(true);
    }

    private void buildActionMenu(MenuItem menu) {
        if (getContactsRequest().getActionCode() == 221) {
            menu.setTitle(R.string.remove_button_label);
            menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_ic_remove_light, R.drawable.contact_ic_remove));
            return;
        }
        menu.setTitle(R.string.select_label);
        menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.ic_done_normal_light, R.drawable.ic_done_normal));
    }

    public void setTextViewCount(TextView aTextView) {
        this.mTextViewCount = aTextView;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        ((PinnedHeaderListView) getListView()).setListViewOperationListener(this.mListViewOperationListener);
        getListView().setFastScrollEnabled(!getActivity().isInMultiWindowMode());
        this.mMaxLimit = this.localActivityRef.mMaxLimit;
        this.mSearchLayout = (LinearLayout) getView().findViewById(R.id.contactListsearchlayout);
        this.mInnerSearchLayout = getView().findViewById(R.id.inner_contactListsearchlayout);
        if (this.mSmartGroupParameters.length == 0) {
            this.mSearchLayout.setVisibility(0);
        } else {
            this.mSearchLayout.setVisibility(8);
        }
        this.mContactsSearchView = (EditText) getView().findViewById(R.id.search_view);
        ContactsUtils.configureSearchViewInputType(this.mContactsSearchView);
        this.mContactsSearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(this.mContext, getResources().getString(R.string.contact_hint_findContacts), this.mContactsSearchView.getTextSize()));
        this.mContactsSearchView.setFocusable(false);
        this.mContactsSearchView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                ContactDataMultiSelectFragment.this.mInnerSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
                ContactDataMultiSelectFragment.this.mContactsSearchView.setFocusableInTouchMode(true);
                ContactDataMultiSelectFragment.this.mContactsSearchView.setCursorVisible(true);
                return true;
            }
        });
        final ImageView searchClearButton = (ImageView) getView().findViewById(R.id.clearSearchResult);
        searchClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ContactDataMultiSelectFragment.this.setSearchMode(false);
                ContactDataMultiSelectFragment.this.mContactsSearchView.setText(null);
            }
        });
        this.mContactsSearchView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = null;
                if (ContactDataMultiSelectFragment.this.mScreenModeChange) {
                    ContactDataMultiSelectFragment contactDataMultiSelectFragment = ContactDataMultiSelectFragment.this;
                    contactDataMultiSelectFragment.mUpdateSearchCount = contactDataMultiSelectFragment.mUpdateSearchCount + 1;
                    if (ContactDataMultiSelectFragment.this.mUpdateSearchCount > 1) {
                        ContactDataMultiSelectFragment.this.mScreenModeChange = false;
                    }
                }
                if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                    ContactDataMultiSelectFragment.this.setSearchModeInitialized(false);
                    ContactDataMultiSelectFragment.this.setSearchMode(false);
                    ContactDataMultiSelectFragment.this.setIncludeProfile(true);
                    contactDataMultiSelectFragment = ContactDataMultiSelectFragment.this;
                    if (s != null) {
                        str = s.toString();
                    }
                    contactDataMultiSelectFragment.setQueryString(str, true);
                    searchClearButton.setVisibility(8);
                    return;
                }
                ContactDataMultiSelectFragment.this.setSearchModeInitialized(true);
                ContactDataMultiSelectFragment.this.setSearchMode(true);
                ContactDataMultiSelectFragment.this.setIncludeProfile(false);
                ContactDataMultiSelectFragment.this.setQueryString(s.toString(), true);
                searchClearButton.setVisibility(0);
            }
        });
    }

    public void onResume() {
        super.onResume();
        this.mInnerSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
        this.mContactsSearchView.setCursorVisible(false);
    }

    private void showToast() {
        Toast.makeText(this.mParentActivity, getString(R.string.max_contact_selected_Toast, new Object[]{Integer.valueOf(this.mMaxLimit)}), 0).show();
    }

    public void onAttach(Activity activity) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactDataMultiSelectFragment", "onAttach() is called!");
        }
        super.configureAdapter();
        super.onAttach(activity);
        this.mParentActivity = activity;
        this.mSmartGroupParameters = SmartGroupUtil.parseSmartGroupUri((Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri"));
        if (this.mParentActivity instanceof ContactMultiSelectionActivity) {
            this.localActivityRef = (ContactMultiSelectionActivity) this.mParentActivity;
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (HwLog.HWFLOW) {
            HwLog.i("ContactDataMultiSelectFragment", "On save instance state called");
        }
        if (this.localActivityRef != null) {
            outState.putStringArrayList("data_key", new ArrayList(this.localActivityRef.mSelectedData));
            if (this.mRcsCust != null) {
                this.mRcsCust.handleCustomizationsOnSaveInstanceState(outState, this.localActivityRef);
            }
            ArrayList<String> lList = new ArrayList();
            for (Uri uri : this.localActivityRef.mSelectedDataUris) {
                if (uri != null) {
                    lList.add(uri.toString());
                } else {
                    HwLog.e("ContactDataMultiSelectFragment", "localActivityRef.mSelectedDataUris uri == NULL");
                }
            }
            outState.putStringArrayList("ids_key", lList);
            outState.putSerializable("contactsids_key", new ArrayList(this.localActivityRef.mSelectedContactId));
        }
        outState.putSerializable("default_selection", Boolean.valueOf(this.mDefaultSelectionCompleted));
        outState.putBoolean("select_all_item_visible", this.mSelectAllVisible);
        outState.putBoolean("screen_mode_change", true);
        outState.putInt("current_selected_count", this.mSelectedCountInSearch);
        super.onSaveInstanceState(outState);
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        this.mContext = getActivity();
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustContactDataMultiSelectFragment) HwCustUtils.createObj(HwCustContactDataMultiSelectFragment.class, new Object[]{this.mContext});
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsContactDataMultiSelectFragment(this.mContext);
        }
        if (savedState != null) {
            if (HwLog.HWFLOW) {
                HwLog.i("ContactDataMultiSelectFragment", "onCreate arguments bundle is not null");
            }
            this.localActivityRef.mSelectedData.addAll(savedState.getStringArrayList("data_key"));
            for (String uriString : savedState.getStringArrayList("ids_key")) {
                this.localActivityRef.mSelectedDataUris.add(Uri.parse(uriString));
            }
            this.localActivityRef.mSelectedContactId.addAll((ArrayList) savedState.getSerializable("contactsids_key"));
            this.mDefaultSelectionCompleted = ((Boolean) savedState.getSerializable("default_selection")).booleanValue();
            this.mSelectAllVisible = savedState.getBoolean("select_all_item_visible", true);
            if (this.mRcsCust != null) {
                this.mRcsCust.handleCustomizationsOnCreate(savedState, this.localActivityRef);
            }
            this.mScreenModeChange = savedState.getBoolean("screen_mode_change", false);
            this.mSelectedCountInSearch = savedState.getInt("current_selected_count");
        }
    }

    protected ContactEntryListAdapter createListAdapter() {
        this.mAdapter = getListAdapter();
        this.mAdapter.setDisplayPhotos(true);
        return this.mAdapter;
    }

    protected void onItemClick(int position, long id) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactDataMultiSelectFragment", "onItemClick() is called!, position: [" + position + "]");
        }
        int lHeaderCount = 0;
        boolean isSearchMode = isSearchMode();
        if (isSearchMode) {
            lHeaderCount = 1;
        }
        boolean lCheckedStated = getListView().isItemChecked(position);
        Uri lLookupUri;
        if (lCheckedStated) {
            lLookupUri = ((ContactDataMultiselectAdapter) getAdapter()).getContactUri(position);
            if (!(this.mMultiListener == null || lLookupUri == null)) {
                this.mMultiListener.onSelectionChanged(lLookupUri);
                ((ContactDataMultiselectAdapter) getAdapter()).setSelectedContactUri(lLookupUri);
            }
        } else if (this.localActivityRef.mSelectedDataUris.size() > 1 && this.mMultiListener != null) {
            lLookupUri = ((ContactDataMultiselectAdapter) getAdapter()).getContactUri(position);
            this.mMultiListener.onSelectionChanged(lLookupUri);
            Uri currentUri = ((ContactDataMultiselectAdapter) getAdapter()).getSelectedContactUri();
            if (currentUri != null && currentUri.equals(lLookupUri)) {
                ((ContactDataMultiselectAdapter) getAdapter()).setSelectedContactUri(null);
            }
        }
        updateCacheWithCheckedState(position - lHeaderCount, lCheckedStated, isSearchMode);
        showSelectedItemCountInfo();
        getActivity().invalidateOptionsMenu();
    }

    private void updateCacheWithCheckedState(int aPosition, boolean isChecked, boolean isSearchMode) {
        Uri selectedUri = this.mAdapter.getSelectedDataUri(aPosition);
        String selectedData = this.mAdapter.getSelectedData(aPosition);
        long selectedContactId = this.mAdapter.getContactId(aPosition);
        if (isChecked) {
            addToCache(selectedUri, selectedData, selectedContactId, isSearchMode);
            if (this.mRcsCust != null) {
                this.mRcsCust.addSelectedName(selectedData, this.mAdapter.getContactDisplayName(aPosition), this.localActivityRef);
                return;
            }
            return;
        }
        this.localActivityRef.mSelectedDataUris.remove(selectedUri);
        if (selectedData != null) {
            this.localActivityRef.mSelectedData.remove(selectedData);
            if (this.mRcsCust != null) {
                this.mRcsCust.rmSelectedName(selectedData, this.localActivityRef);
            }
        }
        this.localActivityRef.mSelectedContactId.remove(Long.valueOf(selectedContactId));
        if (isSearchMode) {
            this.mSelectedCountInSearch--;
        }
    }

    private void addToCache(Uri selectedUri, String selectedData, long selectedContactId, boolean isSearchMode) {
        if (!isLimitSet() || this.mMaxLimit == -1 || this.localActivityRef.mSelectedDataUris.size() < this.mMaxLimit) {
            if (!this.localActivityRef.mSelectedDataUris.contains(selectedUri)) {
                this.localActivityRef.mSelectedDataUris.add(selectedUri);
                if (isSearchMode) {
                    this.mSelectedCountInSearch++;
                }
            }
            if (!this.localActivityRef.mSelectedData.contains(selectedData)) {
                this.localActivityRef.mSelectedData.add(selectedData);
            }
            if (!this.localActivityRef.mSelectedContactId.contains(Long.valueOf(selectedContactId))) {
                this.localActivityRef.mSelectedContactId.add(Long.valueOf(selectedContactId));
            }
            if (this.mRcsCust != null) {
                this.mRcsCust.addSelectedNameKey(selectedData);
            }
            return;
        }
        showToast();
    }

    protected DataListAdapter getListAdapter() {
        ContactDataMultiselectAdapter adapter = new ContactDataMultiselectAdapter(this.mParentActivity, getListView());
        adapter.setDataFilter(getFilterForRequest());
        if (this.mSmartGroupParameters.length != 0) {
            adapter.setSmartGroupParameters(this.mSmartGroupParameters);
        }
        return adapter;
    }

    public CursorLoader createCursorLoader(int id) {
        boolean z = false;
        if (EmuiFeatureManager.isBlackListFeatureEnabled()) {
            z = this.mParentActivity.getIntent().getBooleanExtra("Launch_BlackList_Multi_Pick", false);
        }
        this.mBlackListEnabled = z;
        ContactMultiselectListLoader loader = new ContactMultiselectListLoader(this.mContext, null, null, null, null, null);
        if (id == 0 || id == 1073741823) {
            this.mLoader = loader;
        }
        return loader;
    }

    protected DataListFilter getFilterForRequest() {
        ContactsRequest lRequest = getContactsRequest();
        int filterType = 0;
        long groupId = -1;
        Uri groupUri;
        switch (lRequest.getActionCode()) {
            case 208:
                groupUri = (Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri");
                if (!CCUtils.CAMCARD_URL.equals(groupUri)) {
                    filterType = -1;
                    if (groupUri != null) {
                        if (!Constants.STRRED_URL.equals(groupUri)) {
                            if (this.mSmartGroupParameters.length == 0) {
                                groupId = Long.parseLong(groupUri.getLastPathSegment());
                                break;
                            }
                            groupId = -2;
                            break;
                        }
                        groupId = -1;
                        break;
                    }
                }
                filterType = -12;
                break;
                break;
            case 209:
                groupUri = (Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri");
                if (!CCUtils.CAMCARD_URL.equals(groupUri)) {
                    filterType = -2;
                    if (groupUri != null) {
                        if (!Constants.STRRED_URL.equals(groupUri)) {
                            if (this.mSmartGroupParameters.length == 0) {
                                groupId = Long.parseLong(groupUri.getLastPathSegment());
                                break;
                            }
                            groupId = -2;
                            break;
                        }
                        groupId = -1;
                        break;
                    }
                }
                filterType = -13;
                break;
                break;
            case 210:
                filterType = -5;
                break;
            case 211:
                filterType = -3;
                break;
            case 212:
                filterType = -4;
                break;
            case 213:
                filterType = -6;
                break;
            case 214:
                filterType = -7;
                break;
            case 215:
                filterType = -8;
                break;
            case 216:
                filterType = -9;
                break;
            case 221:
                filterType = -11;
                Uri groupUrii = (Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri");
                if (groupUrii != null) {
                    groupId = Long.parseLong(groupUrii.getLastPathSegment());
                    break;
                }
                break;
            default:
                if (this.mRcsCust != null) {
                    filterType = this.mRcsCust.getFilterForCustomizationsRequest(lRequest.getActionCode(), 0);
                    break;
                }
                break;
        }
        return new DataListFilter(filterType, null, null, null, null, groupId, null, false, null);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mMultiListener = null;
        this.mUpdateSearchCount = 0;
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        getActivity().invalidateOptionsMenu();
        showSelectedItemCountInfo();
        this.mContactsSearchView.setFocusable(false);
        super.onItemClick(parent, view, position, id);
    }

    private void selectAllItems() {
        int startIndex = getListView().getHeaderViewsCount();
        boolean isSearchMode = isSearchMode();
        if (isSearchMode) {
            startIndex = 1;
        }
        for (int i = startIndex; i < getListView().getCount() - getListView().getFooterViewsCount(); i++) {
            int selectedItemCount = this.localActivityRef.mSelectedDataUris.size();
            if (isLimitSet() && this.mMaxLimit > 0 && selectedItemCount >= this.mMaxLimit) {
                showToast();
                break;
            }
            long currentContactId = this.mAdapter.getContactId(i - startIndex);
            Uri selectedUri = this.mAdapter.getSelectedDataUri(i - startIndex);
            String selectedData = this.mAdapter.getSelectedData(i - startIndex);
            addToCache(selectedUri, selectedData, currentContactId, isSearchMode);
            if (this.mRcsCust != null) {
                this.mRcsCust.addSelectedName(selectedData, this.mAdapter.getContactDisplayName(i - startIndex), this.localActivityRef);
            }
        }
        this.mAdapter.notifyChange();
    }

    private void optionsItemSelectedAll() {
        if (this.localActivityRef.checkIsSelectAllWithDef()) {
            switch (getContactsRequest().getActionCode()) {
                case 208:
                case 210:
                case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
                    selectAllItemsWithDef(2);
                    return;
                case 209:
                case 212:
                    selectAllItemsWithDef(4);
                    return;
                default:
                    selectAllItems();
                    return;
            }
        }
        selectAllItems();
    }

    private void selectAllItemsWithDef(int aPhoneType) {
        int startIndex = getListView().getHeaderViewsCount();
        boolean isSearchMode = isSearchMode();
        if (isSearchMode) {
            startIndex = 1;
        }
        long lDefaultSelectedId = -1;
        String lDefaultSelectedData = null;
        long previousId = -1;
        long lDefaultTimes = -1;
        HashMap<Long, Long> frequentDataMap = this.mLoader.getFrequentDataMap();
        boolean isPrimaryFound = false;
        boolean isMobileNumberFound = false;
        boolean isDefaultSelectedNumberFound = false;
        for (int i = startIndex; i < getListView().getCount() - getListView().getFooterViewsCount(); i++) {
            long ContactId = this.mAdapter.getContactId(i - startIndex);
            if (!(previousId == -1 || ContactId == previousId)) {
                int selectedItemCount = this.localActivityRef.mSelectedDataUris.size();
                if (isLimitSet() && this.mMaxLimit > 0 && selectedItemCount >= this.mMaxLimit) {
                    showToast();
                    break;
                }
                if (!(lDefaultSelectedId == -1 || lDefaultSelectedData == null)) {
                    addToCache(Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(lDefaultSelectedId)), lDefaultSelectedData, lDefaultSelectedId, isSearchMode);
                }
                isPrimaryFound = false;
                isMobileNumberFound = false;
                isDefaultSelectedNumberFound = false;
                lDefaultSelectedId = -1;
                lDefaultSelectedData = null;
                lDefaultTimes = -1;
            }
            previousId = ContactId;
            long selectedId = this.mAdapter.getSelectedDataId(i - startIndex);
            String aSelectedData = this.mAdapter.getSelectedData(i - startIndex);
            int phoneType;
            if (aPhoneType == 2) {
                phoneType = this.mAdapter.getDataTypeByNum(i - startIndex);
            } else {
                phoneType = this.mAdapter.getDataType(i - startIndex);
            }
            int isPrimary = this.mAdapter.getDataPrimary(i - startIndex);
            long currentTimes = frequentDataMap.containsKey(Long.valueOf(selectedId)) ? ((Long) frequentDataMap.get(Long.valueOf(selectedId))).longValue() : 0;
            if (!isPrimaryFound && isPrimary == 1) {
                lDefaultSelectedId = selectedId;
                lDefaultSelectedData = aSelectedData;
                isPrimaryFound = true;
                isMobileNumberFound = false;
                isDefaultSelectedNumberFound = false;
            } else if (isPrimaryFound || phoneType != aPhoneType) {
                if (!(isPrimaryFound || isMobileNumberFound || isDefaultSelectedNumberFound)) {
                    lDefaultSelectedId = selectedId;
                    lDefaultSelectedData = aSelectedData;
                    isDefaultSelectedNumberFound = true;
                }
            } else if (currentTimes > lDefaultTimes) {
                lDefaultSelectedId = selectedId;
                lDefaultSelectedData = aSelectedData;
                lDefaultTimes = currentTimes;
                isMobileNumberFound = true;
                isDefaultSelectedNumberFound = false;
            }
        }
        if (!(lDefaultSelectedId == -1 || lDefaultSelectedData == null)) {
            Uri dataUri = Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(lDefaultSelectedId));
            if (!isLimitSet() || this.mMaxLimit == -1 || this.localActivityRef.mSelectedDataUris.size() < this.mMaxLimit) {
                addToCache(dataUri, lDefaultSelectedData, lDefaultSelectedId, isSearchMode);
            } else {
                HwLog.d("ContactDataMultiSelectFragment", "contacts selected is to MAX");
            }
        }
        this.mAdapter.notifyChange();
    }

    private void deselectAllItems() {
        if (this.mRcsCust != null && EmuiFeatureManager.isRcsFeatureEnable() && isSearchMode()) {
            this.mSelectedCountInSearch = this.mRcsCust.resetSelectedDataInSearch(getListView(), this.mAdapter, this.localActivityRef, this.mSelectedCountInSearch);
            return;
        }
        if (this.mRcsCust == null || !this.mRcsCust.isFromActivityForGroupChat(this.localActivityRef)) {
            int startIndex = 0;
            if (isSearchMode()) {
                startIndex = 1;
            }
            for (int i = startIndex; i < getListView().getCount() - getListView().getFooterViewsCount(); i++) {
                Uri selectedUri = this.mAdapter.getSelectedDataUri(i - startIndex);
                String selectedData = this.mAdapter.getSelectedData(i - startIndex);
                long selectedContactId = this.mAdapter.getContactId(i - startIndex);
                this.localActivityRef.mSelectedDataUris.remove(selectedUri);
                this.localActivityRef.mSelectedData.remove(selectedData);
                this.localActivityRef.mSelectedContactId.remove(Long.valueOf(selectedContactId));
                if (this.mRcsCust != null) {
                    this.mRcsCust.rmSelectedName(selectedData, this.localActivityRef);
                }
            }
        } else {
            this.mRcsCust.resetSelectedDataInNotSearch(getListView(), this.mAdapter, this.localActivityRef);
            if (this.localActivityRef.getRcsCust() != null) {
                this.localActivityRef.getRcsCust().clearSelectedPersonMap();
            }
            this.localActivityRef.mCallLogCount = this.localActivityRef.getRcsCust().getExsitedCallLogCount(this.localActivityRef);
        }
        this.mSelectedCountInSearch = 0;
        this.mAdapter.notifyChange();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.mSelectAllItem = menu.findItem(R.id.menu_action_selectall);
        configureOptionsMenu(menu);
    }

    private void setSelectAllItemTitle(boolean isAllSelected) {
        if (this.mSelectAllItem != null) {
            if (isAllSelected) {
                this.mSelectAllItem.setTitle(R.string.menu_select_none);
                this.mSelectAllItem.setIcon(this.mSelectNoneDrawable);
            } else {
                this.mSelectAllItem.setTitle(R.string.contact_menu_select_all);
                this.mSelectAllItem.setIcon(this.mSelectAllDrawable);
            }
            this.mSelectAllItem.setChecked(isAllSelected);
        }
    }

    private void setSelectAllItemVisible(boolean visible) {
        this.mSelectAllVisible = visible;
        if (this.mSelectAllItem != null) {
            this.mSelectAllItem.setEnabled(visible);
        }
    }

    protected void showSelectedItemCountInfo() {
        if (this.mTextViewCount != null) {
            int lCheckedItemCount = this.localActivityRef.mSelectedDataUris.size();
            if (lCheckedItemCount > 0) {
                this.mTextViewCount.setText(lCheckedItemCount + "");
            } else {
                this.mTextViewCount.setText("");
            }
        }
    }

    private void sendGroupMessageOrEmail(int actionCode) {
        String separator = actionCode == 209 ? "," : ";";
        StringBuilder dataBuilder = new StringBuilder();
        for (String data : this.localActivityRef.mSelectedData) {
            dataBuilder.append(data).append(separator);
        }
        dataBuilder.setLength(dataBuilder.length() - 1);
        if (actionCode == 209) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setData(Uri.parse("mailto:" + dataBuilder));
            intent.addCategory("android.intent.category.DEFAULT");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                startActivity(new Intent("android.intent.action.SENDTO", Uri.fromParts("sms", dataBuilder.toString(), null)));
            } catch (ActivityNotFoundException e2) {
                Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
            }
        }
        this.mParentActivity.finish();
    }

    protected void setResultWithInteger() {
        Intent intent = new Intent();
        ArrayList<Uri> lData = new ArrayList(this.localActivityRef.mSelectedDataUris);
        ArrayList<Integer> lIntId = new ArrayList();
        ArrayList<Integer> lCallIntId = new ArrayList();
        for (int i = 0; i < lData.size(); i++) {
            if (lData.get(i) != null) {
                String uriTemp = ((Uri) lData.get(i)).toString();
                String[] ss = uriTemp.split("/");
                int id = Integer.parseInt(ss[ss.length - 1]);
                if (uriTemp.contains("content://call_log/calls")) {
                    lCallIntId.add(Integer.valueOf(id));
                } else {
                    lIntId.add(Integer.valueOf(id));
                }
            }
        }
        intent.putExtra("SelItemData_KeyValue", lIntId);
        intent.putExtra("SelItemCalls_KeyValue", lCallIntId);
        this.mParentActivity.setResult(-1, intent);
        this.mParentActivity.finish();
    }

    protected void sendToWithInteger() {
        Intent intent = new Intent("android.intent.action.SENDTO", Uri.fromParts("sms", "", null));
        ArrayList<Uri> lData = new ArrayList(this.localActivityRef.mSelectedDataUris);
        ArrayList<Integer> lIntId = new ArrayList();
        for (int i = 0; i < lData.size(); i++) {
            String[] ss = ((Uri) lData.get(i)).toString().split("/");
            lIntId.add(Integer.valueOf(Integer.parseInt(ss[ss.length - 1])));
        }
        intent.putExtra("SelItemData_KeyValue", lIntId);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
        }
        this.mParentActivity.finish();
    }

    protected void setResultForMessaging() {
        Intent intent = new Intent();
        intent.putExtra("SelItemData_KeyValue", new ArrayList(this.localActivityRef.mSelectedDataUris));
        this.mParentActivity.setResult(-1, intent);
        this.mParentActivity.finish();
    }

    protected void configureListView(ListView aListView) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactDataMultiSelectFragment", "configureListView() is called!");
        }
        aListView.setOnFocusChangeListener(null);
        aListView.setOnTouchListener(null);
        aListView.setChoiceMode(2);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_operation:
                if (this.mRcsCust == null || this.mRcsCust.ifGroupMemberSizeValid(this.localActivityRef)) {
                    doOperation();
                    break;
                }
            case R.id.menu_action_selectall:
                hideSoftKeyboard();
                if (this.mIsAllSelected) {
                    deselectAllItems();
                    setSelectAllItemTitle(false);
                    this.mIsAllSelected = false;
                } else {
                    optionsItemSelectedAll();
                    setSelectAllItemTitle(true);
                    this.mIsAllSelected = true;
                }
                showSelectedItemCountInfo();
                getActivity().invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void doOperation() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactDataMultiSelectFragment", "doOperation() is called!");
        }
        ContactsRequest lRequest = getContactsRequest();
        switch (lRequest.getActionCode()) {
            case 208:
                if (this.localActivityRef.mSelectedData.size() > 0) {
                    sendToWithInteger();
                    return;
                }
                return;
            case 209:
                if (this.localActivityRef.mSelectedData.size() > 0) {
                    sendGroupMessageOrEmail(lRequest.getActionCode());
                    return;
                }
                return;
            case 210:
            case 211:
            case 212:
            case 213:
            case 214:
            case 215:
            case 216:
            case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
                if (this.localActivityRef.mSelectedDataUris.size() <= 0) {
                    return;
                }
                if (this.localActivityRef.isExpectIntegerListBack) {
                    setResultWithInteger();
                    return;
                } else {
                    setResultForMessaging();
                    return;
                }
            case 221:
                if (this.localActivityRef.mSelectedContactId.size() <= 0) {
                    return;
                }
                if (this.mCust == null || !this.mCust.handleRemoveGrpMemOperationCust(this, getFragmentManager())) {
                    startRemoveService();
                    return;
                }
                return;
            default:
                if (this.mRcsCust != null) {
                    this.mRcsCust.doOperationForCustomizations(lRequest.getActionCode(), this.localActivityRef, this);
                    return;
                }
                return;
        }
    }

    public void startRemoveService() {
        Uri groupUrii = (Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri");
        long groupId = -1;
        if (groupUrii != null) {
            groupId = Long.parseLong(groupUrii.getLastPathSegment());
        }
        int i = 0;
        long[] removeId = new long[this.localActivityRef.mSelectedContactId.size()];
        for (Long longValue : this.localActivityRef.mSelectedContactId) {
            removeId[i] = longValue.longValue();
            i++;
        }
        this.mContext.startService(ContactSaveService.createGroupUpdateIntent(this.mContext, groupId, null, new long[0], removeId, this.mParentActivity.getClass(), "saveCompleted", false));
        this.mParentActivity.finish();
    }

    public void showSelectedItemCountInfo(int aCount) {
        if (this.mTextViewCount != null) {
            if (aCount > 0) {
                this.mTextViewCount.setText(aCount + "");
            } else {
                this.mTextViewCount.setText("");
            }
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mSelectAllItem != null) {
            if (this.mSelectAllItem.isEnabled() != this.mSelectAllVisible) {
                this.mSelectAllItem.setEnabled(this.mSelectAllVisible);
            }
            if (this.mTotalContacts == 0) {
                this.mSelectAllItem.setEnabled(false);
            }
            int selectedDataUriCount = this.localActivityRef.mSelectedDataUris.size();
            int lSelectedContactsCount = selectedDataUriCount;
            lSelectedContactsCount = selectedDataUriCount - this.localActivityRef.mCallLogCount;
            if (isSearchMode()) {
                updateSelectedCountInSearch();
                lSelectedContactsCount = this.mSelectedCountInSearch;
            }
            if (lSelectedContactsCount <= 0 || lSelectedContactsCount < this.mTotalContacts) {
                setSelectAllItemTitle(false);
                this.mIsAllSelected = false;
            } else {
                setSelectAllItemTitle(true);
                this.mIsAllSelected = true;
            }
            setCustomTitle();
        }
    }

    protected void setCustomTitle() {
        this.mCustomTitle.setCustomTitle(getContactDataMultiselectionTitle(), this.localActivityRef.mSelectedDataUris.size());
        if (this.localActivityRef.mSelectedDataUris.size() > 0) {
            this.mActionMenu.setEnabled(true);
        } else {
            this.mActionMenu.setEnabled(false);
        }
    }

    private String getContactDataMultiselectionTitle() {
        if (this.mContext == null) {
            return null;
        }
        String title = this.mContext.getString(R.string.contacts_not_selected_text);
        if (!(this.localActivityRef == null || this.localActivityRef.mSelectedDataUris == null)) {
            title = CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.localActivityRef.mSelectedDataUris.size());
        }
        return title;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader instanceof ContactMultiselectListLoader) {
            int loaderId = loader.getId();
            if (loaderId == 0 || loaderId == 1073741823) {
                this.mLoader = (ContactMultiselectListLoader) loader;
            }
        }
        super.onLoadFinished((Loader) loader, data);
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        super.onPartitionLoaded(partitionIndex, data);
        if (data != null) {
            if (isLimitSet()) {
                this.mTotalContacts = data.getCount() > this.mMaxLimit ? this.mMaxLimit : data.getCount();
            } else {
                this.mTotalContacts = data.getCount();
            }
        }
        if (!(data == null || data.getCount() == this.mTotalContactCount)) {
            this.mTotalContactCount = data.getCount();
            if (!this.mDefaultSelectionCompleted) {
                String lAction = this.mParentActivity.getIntent().getAction();
                if ("android.intent.action.HAP_SEND_GROUP_MESSAGE".equals(lAction)) {
                    this.localActivityRef.mSelectedDataUris.clear();
                    selectAllItemsWithDef(2);
                    this.mDefaultSelectionCompleted = true;
                } else if ("android.intent.action.HAP_SEND_GROUP_MAIL".equals(lAction)) {
                    this.localActivityRef.mSelectedDataUris.clear();
                    selectAllItemsWithDef(4);
                    this.mDefaultSelectionCompleted = true;
                } else if (this.mBlackListEnabled) {
                    this.mDefaultSelectionCompleted = true;
                }
                data.moveToFirst();
            }
            if (data.getCount() == 0) {
                setEmptyText(R.string.noContacts);
                if (!isSearchMode()) {
                    showSelectedItemCountInfo(0);
                    this.localActivityRef.mSelectedData.clear();
                    this.localActivityRef.mSelectedDataUris.clear();
                    if (this.localActivityRef.getRcsCust() != null) {
                        this.localActivityRef.getRcsCust().clearSelectedPersonMap();
                    }
                    this.mSearchLayout.setVisibility(8);
                }
                setSelectAllItemVisible(false);
            } else {
                if (this.mSmartGroupParameters.length == 0) {
                    this.mSearchLayout.setVisibility(0);
                }
                setSelectAllItemVisible(true);
                getEmptyTextView().setVisibility(4);
                getListView().setVisibility(0);
                showSelectedItemCountInfo();
                updateSelectedCountInSearch();
            }
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (ContactDataMultiSelectFragment.this.mAdapter != null) {
                    ContactDataMultiSelectFragment.this.mAdapter.notifyChange();
                }
                ContactDataMultiSelectFragment.this.invalidateMenu();
            }
        }, 100);
    }

    private void invalidateMenu() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    protected void setEmptyText(int resourceId) {
        TextView emptyView = getEmptyTextView();
        emptyView.setText(getString(getEmptyListScreen()));
        if (this.mAdapter.areAllPartitionsEmpty()) {
            emptyView.setVisibility(0);
            getListView().setVisibility(4);
        }
    }

    private int getEmptyListScreen() {
        int i;
        switch (getContactsRequest().getActionCode()) {
            case 208:
            case 210:
            case 211:
                if (this.mCust != null && this.mCust.getEnableEmailContactInMms()) {
                    return this.mCust.getPickEmailNumber(isSearchMode());
                }
                if (isSearchMode()) {
                    i = R.string.no_matching_results_with_number;
                } else {
                    i = R.string.contact_noNumbers;
                }
                return i;
            case 209:
            case 212:
                if (isSearchMode()) {
                    i = R.string.no_matching_contacts_with_email;
                } else {
                    i = R.string.contact_noEmails;
                }
                return i;
            case 221:
                return R.string.contact_list_FoundAllContactsZero;
            default:
                return R.string.noContacts;
        }
    }

    protected void showCount(int partitionIndex, Cursor data) {
        if (this.mMultiListener == null) {
            return;
        }
        if (data == null || data.getCount() <= 0) {
            this.mMultiListener.onSelectionChanged(null);
            ((ContactDataMultiselectAdapter) this.mAdapter).setSelectedContactUri(null);
            return;
        }
        data.moveToFirst();
        Uri lookUpUri = Contacts.getLookupUri(data.getLong(data.getColumnIndex("contact_id")), data.getString(data.getColumnIndex("lookup")));
        this.mMultiListener.onSelectionChanged(lookUpUri);
        ((ContactDataMultiselectAdapter) this.mAdapter).setSelectedContactUri(lookUpUri);
    }

    public void setListener(MultiSelectListener aListener) {
        this.mMultiListener = aListener;
    }

    public void handleEmptyList(int aCount) {
        setEmptyText(R.string.noContacts);
        super.handleEmptyList(aCount);
    }

    private void hideSoftKeyboard() {
        ((InputMethodManager) this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(getListView().getWindowToken(), 0);
    }

    protected void updateSelectedCountInSearch() {
        if (isSearchMode()) {
            if (!this.mScreenModeChange) {
                this.mSelectedCountInSearch = 0;
                int listItemCount = getListView().getCount() - getListView().getFooterViewsCount();
                for (int i = this.mHeadderItem; i < listItemCount; i++) {
                    if (this.localActivityRef.mSelectedDataUris.contains(this.mAdapter.getSelectedDataUri(i))) {
                        this.mSelectedCountInSearch++;
                    }
                }
            }
            if (this.mSelectedCountInSearch == this.mTotalContacts) {
                setSelectAllItemTitle(true);
                this.mIsAllSelected = true;
                return;
            }
            setSelectAllItemTitle(false);
            this.mIsAllSelected = false;
        }
    }

    public void setCustActionBar(ActionBarCustom custActionBar) {
        this.mCustActionBar = custActionBar;
        configureActionBar();
    }

    private boolean isLimitSet() {
        if (getContactsRequest().getActionCode() != 221) {
            return true;
        }
        return false;
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        ListView listView = getListView();
        if (listView != null) {
            listView.setFastScrollEnabled(!isInMultiWindowMode);
        }
    }
}
