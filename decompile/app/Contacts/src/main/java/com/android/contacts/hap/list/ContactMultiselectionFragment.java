package com.android.contacts.hap.list;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.group.GroupEditorFragment.Member;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.GroupAndContactMetaData;
import com.android.contacts.hap.GroupAndContactMetaData.GroupsData;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity.TranslucentActivity;
import com.android.contacts.hap.activities.DeleteProgressBarActivity;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.hap.hwsearch.HwSearchCursor;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.util.AlertDialogFragmet;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactEntryListAdapter.LazyItemCheckedListener;
import com.android.contacts.list.ContactEntryListFragment;
import com.android.contacts.list.ContactListAdapter;
import com.android.contacts.list.ContactListFilter;
import com.android.contacts.list.ContactListFilterController;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.list.MultiCursor;
import com.android.contacts.list.ProfileAndContactsLoader;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HiCloudUtil;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.android.contacts.widget.ActionBarEx;
import com.android.contacts.widget.PinnedHeaderListView;
import com.android.contacts.widget.PinnedHeaderListView.ListViewOperationListener;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class ContactMultiselectionFragment extends ContactEntryListFragment<ContactEntryListAdapter> {
    ContactListAdapter adapter;
    protected ActionBar mActionBar;
    private OnClickListener mActionBarListener;
    private int mActionCode;
    protected MenuItem mActionMenu;
    private AlertDialog mAddCompanyDialog;
    private ArrayList<Uri> mAllMembers;
    private ArrayList<String> mAllMembersID;
    private ArrayList<Member> mAllMembersList;
    protected int mContactsNumberInSearch;
    private EditText mContactsSearchView;
    protected Activity mContext;
    private Uri mCurSelectionUri;
    private HwCustContactMultiselectionFragment mCust;
    private Uri mDefaultSelectionLookUpUri;
    private boolean mFirstStarted;
    private Handler mHandler;
    protected boolean mHasGrayForFav;
    private int mHeadderItem;
    private int mHeaderItemInSearch;
    private boolean mIsAllSelected;
    private float mLandscapeListWeight;
    private ListViewOperationListener mListViewOperationListener;
    private MultiSelectListener mMultiListener;
    protected boolean mNeedResultBack;
    protected LinkedHashSet<Uri> mSelectedContacts;
    protected LinkedHashSet<String> mSelectedContactsID;
    protected int mSelectedCountInSearch;
    private ArrayList<String> mSelectedInShareContacts;
    private ArrayList<Member> mSelectedMembersList;
    protected ActionBarCustomTitle mTitle;
    protected int mTotalContacts;
    private float mVertivalListWeight;

    public interface MultiSelectListener {
        void onSelectionChanged(Uri uri);
    }

    private static class CompanyDialogClickListener implements DialogInterface.OnClickListener {
        private CompanyDialogClickListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            StatisticalHelper.report(1171);
            if (dialog != null) {
                dialog.dismiss();
            }
        }
    }

    public ContactMultiselectionFragment() {
        this.mIsAllSelected = false;
        this.mHeadderItem = 0;
        this.mHeaderItemInSearch = 1;
        this.mSelectedInShareContacts = new ArrayList();
        this.mSelectedContacts = new LinkedHashSet();
        this.mSelectedContactsID = new LinkedHashSet();
        this.mSelectedMembersList = new ArrayList();
        this.mTotalContacts = 0;
        this.mAllMembers = new ArrayList();
        this.mAllMembersID = new ArrayList();
        this.mAllMembersList = new ArrayList();
        this.mCust = null;
        this.mLandscapeListWeight = 0.0f;
        this.mVertivalListWeight = 0.0f;
        this.mHasGrayForFav = false;
        this.mFirstStarted = true;
        this.mHandler = new Handler();
        this.mActionBarListener = new OnClickListener() {
            public void onClick(View v) {
                int viewId = v.getId();
                if (viewId == 16908295) {
                    ContactMultiselectionFragment.this.doActionCancel();
                } else if (viewId != 16908296) {
                } else {
                    if (ContactMultiselectionFragment.this.mSelectedContacts.size() > 0) {
                        ContactMultiselectionFragment.this.doOperation();
                    } else {
                        Toast.makeText(ContactMultiselectionFragment.this.mContext, R.string.no_contacts_selected, 0).show();
                    }
                }
            }
        };
        this.mListViewOperationListener = new ListViewOperationListener() {
            public void onMaxLimitReached() {
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactMultiselectionFragment", "Max Limit Reached");
                }
                ContactMultiselectionFragment.this.showToast();
            }

            public boolean isMaxLimitReached(int position) {
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactMultiselectionFragment", "isMaxLimitReached");
                }
                if (ContactMultiselectionFragment.this.mSelectedContacts.size() < ContactMultiselectionFragment.this.mTotalContacts || ContactMultiselectionFragment.this.getListView().isItemChecked(position)) {
                    return false;
                }
                return true;
            }
        };
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "ContactMultiselectionFragment constructer is called!");
        }
        this.mMissingItemIndex = -1;
        initConfigProperties();
    }

    public ContactMultiselectionFragment(int missingItemIndex) {
        this.mIsAllSelected = false;
        this.mHeadderItem = 0;
        this.mHeaderItemInSearch = 1;
        this.mSelectedInShareContacts = new ArrayList();
        this.mSelectedContacts = new LinkedHashSet();
        this.mSelectedContactsID = new LinkedHashSet();
        this.mSelectedMembersList = new ArrayList();
        this.mTotalContacts = 0;
        this.mAllMembers = new ArrayList();
        this.mAllMembersID = new ArrayList();
        this.mAllMembersList = new ArrayList();
        this.mCust = null;
        this.mLandscapeListWeight = 0.0f;
        this.mVertivalListWeight = 0.0f;
        this.mHasGrayForFav = false;
        this.mFirstStarted = true;
        this.mHandler = new Handler();
        this.mActionBarListener = /* anonymous class already generated */;
        this.mListViewOperationListener = /* anonymous class already generated */;
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "ContactMultiselectionFragment constructer is called!");
        }
        this.mMissingItemIndex = missingItemIndex;
        initConfigProperties();
    }

    public ContactMultiselectionFragment(float landListWeight, float verticalListWeight) {
        this();
        this.mLandscapeListWeight = landListWeight;
        this.mVertivalListWeight = verticalListWeight;
    }

    private void initConfigProperties() {
        setPhotoLoaderEnabled(true);
        setQuickContactEnabled(false);
        setSectionHeaderDisplayEnabled(true);
        setVisibleScrollbarEnabled(true);
        setSelectionVisible(true);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        this.mInnerSearchLayout.setBackgroundResource(R.drawable.contact_textfield_default_holo_light);
        this.mContactsSearchView.setCursorVisible(false);
    }

    protected View inflateView(LayoutInflater inflater, ViewGroup container) {
        PLog.d(0, "ContactMultiselectionFragment inflateView begin");
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "inflateView() is called!");
        }
        this.mContext = getActivity();
        this.mNeedResultBack = this.mContext.getIntent().getBooleanExtra("result_required_back", false);
        this.mActionCode = getContactsRequest().getActionCode();
        this.mActionBar = this.mContext.getActionBar();
        this.mTitle = new ActionBarCustomTitle(this.mContext, inflater);
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(this.mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(this.mActionBar, false, null, null);
            ActionBarEx.setCustomTitle(this.mActionBar, this.mTitle.getTitleLayout());
            this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
        } else {
            getActivity().setTitle(getMultiselectionTitle() + HwCustPreloadContacts.EMPTY_STRING + this.mSelectedContacts.size());
        }
        View lView = inflater.inflate(R.layout.contact_picker_content, container, false);
        ((ViewStub) lView.findViewById(R.id.pinnedHeaderList_stub)).setLayoutResource(initViewStub());
        float weight = 0.0f;
        if (1 == getResources().getConfiguration().orientation && this.mVertivalListWeight > 0.0f) {
            weight = this.mVertivalListWeight;
            this.mHasGrayForFav = true;
        } else if (2 == getResources().getConfiguration().orientation && this.mLandscapeListWeight > 0.0f) {
            weight = this.mLandscapeListWeight;
            this.mHasGrayForFav = true;
        }
        if (this.mHasGrayForFav) {
            updateWindowToGray(lView, weight);
        } else if (getActivity() instanceof TranslucentActivity) {
            getActivity().getWindow().setBackgroundDrawableResource(R.drawable.multiselection_background);
        }
        PLog.d(0, "ContactMultiselectionFragment inflateView end");
        return lView;
    }

    private void updateWindowToGray(View lView, float weight) {
        FrameLayout listAndMenu = (FrameLayout) lView.findViewById(R.id.list_and_menu_layout);
        listAndMenu.setLayoutParams(new LayoutParams(0, -1, weight));
        getActivity().overridePendingTransition(0, 0);
        getActivity().getWindow().setBackgroundDrawableResource(17170445);
        lView.findViewById(R.id.content_layout).setBackgroundResource(R.color.split_transparent);
        listAndMenu.setBackgroundResource(R.drawable.multiselection_background);
        View grayView = lView.findViewById(R.id.gray_view);
        grayView.setLayoutParams(new LayoutParams(0, -1, 1.0f - weight));
        grayView.setVisibility(0);
        grayView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                ContactMultiselectionFragment.this.getActivity().finish();
            }
        });
        if (this.mFirstStarted) {
            Animation animation;
            if (CommonUtilMethods.isLayoutRTL()) {
                animation = AnimationUtils.loadAnimation(this.mContext, R.anim.slide_in_right);
            } else {
                animation = AnimationUtils.loadAnimation(this.mContext, R.anim.slide_in_left);
            }
            listAndMenu.startAnimation(animation);
        }
        createSplitActionBar(this.mContext, lView);
    }

    protected int initViewStub() {
        return CommonUtilMethods.getPinnedHeaderListViewResId(getContext());
    }

    protected String getMultiselectionTitle() {
        if (this.mContext == null) {
            return null;
        }
        String title = this.mContext.getString(R.string.contacts_not_selected_text);
        if (this.mSelectedContacts != null) {
            title = CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.mSelectedContacts.size());
        }
        return title;
    }

    protected void onCreateView(LayoutInflater inflater, ViewGroup container) {
        super.onCreateView(inflater, container);
        if (needToSetMaxLimit()) {
            ((PinnedHeaderListView) getListView()).setListViewOperationListener(this.mListViewOperationListener);
        }
        this.mSearchLayout = (LinearLayout) getView().findViewById(R.id.contactListsearchlayout);
        this.mInnerSearchLayout = getView().findViewById(R.id.inner_contactListsearchlayout);
        if (this.mMissingItemIndex >= 0 || !isShowSearchLayout()) {
            this.mSearchLayout.setVisibility(8);
        } else {
            this.mSearchLayout.setVisibility(0);
        }
        this.mContactsSearchView = (EditText) getView().findViewById(R.id.search_view);
        ContactsUtils.configureSearchViewInputType(this.mContactsSearchView);
        this.mContactsSearchView.setHint(CommonUtilMethods.getSearchViewSpannableHint(this.mContext, getHintString(), this.mContactsSearchView.getTextSize()));
        this.mContactsSearchView.setCursorVisible(false);
        this.mContactsSearchView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                ContactMultiselectionFragment.this.mInnerSearchLayout.setBackgroundResource(R.drawable.textfield_activated_holo_light);
                ContactMultiselectionFragment.this.mContactsSearchView.setCursorVisible(true);
                return true;
            }
        });
        final ImageView searchClearButton = (ImageView) getView().findViewById(R.id.clearSearchResult);
        searchClearButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ContactMultiselectionFragment.this.setSearchMode(false);
                ContactMultiselectionFragment.this.mContactsSearchView.setText(null);
            }
        });
        this.mContactsSearchView.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = null;
                if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                    ContactMultiselectionFragment.this.setSearchModeInitialized(false);
                    ContactMultiselectionFragment.this.setSearchMode(false);
                    ContactMultiselectionFragment.this.setIncludeProfile(true);
                    ContactMultiselectionFragment contactMultiselectionFragment = ContactMultiselectionFragment.this;
                    if (s != null) {
                        str = s.toString();
                    }
                    contactMultiselectionFragment.setQueryString(str, true);
                    searchClearButton.setVisibility(8);
                    return;
                }
                ContactMultiselectionFragment.this.setSearchModeInitialized(true);
                ContactMultiselectionFragment.this.setSearchMode(true);
                ContactMultiselectionFragment.this.setIncludeProfile(false);
                ContactMultiselectionFragment.this.setQueryString(s.toString(), true);
                searchClearButton.setVisibility(0);
            }
        });
        getListView().setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                ContactMultiselectionFragment.this.hideSoftKeyboard();
                return false;
            }
        });
        CommonUtilMethods.addFootEmptyViewPortrait(getListView(), this.mContext);
    }

    protected String getHintString() {
        return getString(R.string.contact_hint_findContacts);
    }

    private void hideSoftKeyboard() {
        ((InputMethodManager) this.mContext.getSystemService("input_method")).hideSoftInputFromWindow(getListView().getWindowToken(), 0);
    }

    protected ContactListAdapter createListAdapter() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "createListAdapter() is called!");
        }
        this.adapter = getListAdapter();
        this.adapter.setFilter(getFilterForRequest());
        this.adapter.setSectionHeaderDisplayEnabled(true);
        this.adapter.setDisplayPhotos(true);
        this.adapter.setLazyItemCheckedListener(getLazyCheckListener());
        this.adapter.setSelectedContactUri(this.mCurSelectionUri);
        return this.adapter;
    }

    protected LazyItemCheckedListener getLazyCheckListener() {
        return new LazyItemCheckedListener() {
            public void setItemChecked(int position, Uri bindUri, boolean isSearchMode) {
                ContactMultiselectionFragment.this.getListView().setItemChecked((isSearchMode ? ContactMultiselectionFragment.this.mHeadderItem + ContactMultiselectionFragment.this.mHeaderItemInSearch : ContactMultiselectionFragment.this.mHeadderItem) + position, ContactMultiselectionFragment.this.mSelectedContacts.contains(bindUri));
            }
        };
    }

    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        if (savedState != null) {
            ArrayList<String> arrList = savedState.getStringArrayList("selected_contacts");
            if (arrList != null) {
                Iterator<String> it = arrList.iterator();
                while (it.hasNext()) {
                    this.mSelectedContacts.add(Uri.withAppendedPath(Contacts.CONTENT_URI, (String) it.next()));
                }
                this.mSelectedContactsID.addAll(arrList);
            }
            ArrayList<String> contacts2Share = savedState.getStringArrayList("selected_contacts_to_share");
            if (contacts2Share != null) {
                this.mSelectedInShareContacts.addAll(contacts2Share);
            }
            this.mNeedResultBack = savedState.getBoolean("need_result_back_flag");
            this.mTotalContacts = savedState.getInt("total_contacts");
            this.mSelectAllVisible = savedState.getBoolean("select_all_item_visible", true);
            this.mLandscapeListWeight = savedState.getFloat("landscape_list_width");
            this.mVertivalListWeight = savedState.getFloat("portrait_list_width");
            this.mFirstStarted = false;
        }
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustContactMultiselectionFragment) HwCustUtils.createObj(HwCustContactMultiselectionFragment.class, new Object[]{getContext()});
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<String> arrList = new ArrayList();
        Iterator<String> it = this.mSelectedContactsID.iterator();
        while (it.hasNext()) {
            arrList.add((String) it.next());
        }
        outState.putStringArrayList("selected_contacts", arrList);
        if (this.mActionCode == 204) {
            outState.putStringArrayList("selected_contacts_to_share", this.mSelectedInShareContacts);
        }
        outState.putBoolean("need_result_back_flag", this.mNeedResultBack);
        outState.putInt("total_contacts", this.mTotalContacts);
        outState.putBoolean("select_all_item_visible", this.mSelectAllVisible);
        outState.putFloat("landscape_list_width", this.mLandscapeListWeight);
        outState.putFloat("portrait_list_width", this.mVertivalListWeight);
    }

    protected void onItemClick(int position, long id) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "onItemClick() is called!, position: [" + position + "]");
        }
        int lHeaderCount = 0;
        if (isSearchMode()) {
            lHeaderCount = this.mHeaderItemInSearch;
        }
        int lPosition = (getListView().getHeaderViewsCount() + position) - lHeaderCount;
        if (getAdapter().getSelectedContactUri(lPosition) != null) {
            Long contactId = Long.valueOf(ContentUris.parseId(getAdapter().getSelectedContactUri(lPosition)));
            Cursor lCursor = (Cursor) getAdapter().getItem(lPosition + lHeaderCount);
            if (lCursor != null) {
                int lPositionInShared = lCursor.getColumnIndex("lookup");
                if (getListView().isItemChecked(lPosition + lHeaderCount)) {
                    addContactToShare(lCursor.getString(lPositionInShared));
                    Uri lLookupUri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId.longValue()));
                    this.mSelectedContacts.add(lLookupUri);
                    this.mSelectedContactsID.add(contactId.toString());
                    if (!(this.mMultiListener == null || lLookupUri == null)) {
                        this.mMultiListener.onSelectionChanged(lLookupUri);
                        this.adapter.setSelectedContactUri(lLookupUri);
                        this.mCurSelectionUri = lLookupUri;
                    }
                    StatisticalHelper.report(2012);
                } else {
                    this.mSelectedContacts.remove(Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId.longValue())));
                    this.mSelectedContactsID.remove(contactId.toString());
                    this.mSelectedInShareContacts.remove(lCursor.getString(lPositionInShared));
                }
                if (isSearchMode()) {
                    if (getListView().isItemChecked(lPosition + lHeaderCount)) {
                        this.mSelectedCountInSearch++;
                    } else {
                        this.mSelectedCountInSearch--;
                    }
                    if (this.mSelectedCountInSearch == this.mContactsNumberInSearch) {
                        setSelectAllItemTitle(true);
                        this.mIsAllSelected = true;
                    } else {
                        setSelectAllItemTitle(false);
                        this.mIsAllSelected = false;
                    }
                }
                if (this.mActionMenu != null) {
                    if (this.mSelectedContacts.size() > 0) {
                        this.mActionMenu.setEnabled(true);
                    } else {
                        this.mActionMenu.setEnabled(false);
                    }
                } else if (this.mSplitActionBarView != null) {
                    if (this.mSelectedContacts.size() > 0) {
                        this.mSplitActionBarView.setEnable(1, true);
                    } else {
                        this.mSplitActionBarView.setEnable(1, false);
                    }
                }
                getActivity().invalidateOptionsMenu();
            }
        }
    }

    private StringBuilder getSelectedItemLookupIds() {
        StringBuilder lSelectedLookupIDs = new StringBuilder();
        for (int i = 0; i < this.mSelectedInShareContacts.size(); i++) {
            if (i != 0) {
                lSelectedLookupIDs.append(':');
            }
            lSelectedLookupIDs.append((String) this.mSelectedInShareContacts.get(i));
        }
        return lSelectedLookupIDs;
    }

    private long[] getSelectedItemIds() {
        long[] lSelectedContactIDs = new long[this.mSelectedContacts.size()];
        int i = 0;
        for (Uri contactId : this.mSelectedContacts) {
            int i2 = i + 1;
            lSelectedContactIDs[i] = ContentUris.parseId(contactId);
            i = i2;
        }
        return lSelectedContactIDs;
    }

    protected void configureListView(ListView aListView) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "configureListView() is called!");
        }
        aListView.setOnFocusChangeListener(null);
        aListView.setOnTouchListener(null);
        aListView.setChoiceMode(2);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (this.mNeedResultBack) {
            updateSelectedMembers((Cursor) getAdapter().getItem(position), position);
        }
        super.onItemClick(parent, view, position, id);
    }

    private void updateSelectedMembers(Cursor aDataCursor, int position) {
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "updateSelectedMembers() is called! " + position);
        }
        if (aDataCursor != null) {
            Member lMember = new Member(aDataCursor.getLong(aDataCursor.getColumnIndex("name_raw_contact_id")), aDataCursor.getString(6), aDataCursor.getLong(0), aDataCursor.getString(1), aDataCursor.getString(5));
            lMember.setPhotoId(aDataCursor.getInt(4));
            if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                lMember.setIsPrivate(CommonUtilMethods.isPrivateContact(aDataCursor));
            }
            if (getListView().isItemChecked(position + getListView().getHeaderViewsCount())) {
                if (!this.mSelectedMembersList.contains(lMember)) {
                    this.mSelectedMembersList.add(lMember);
                }
            } else if (this.mSelectedMembersList.contains(lMember)) {
                this.mSelectedMembersList.remove(lMember);
            }
        }
    }

    private void updateAllMembers(Cursor aDataCursor) {
        if (aDataCursor != null) {
            this.mAllMembersList.add(new Member(aDataCursor.getLong(aDataCursor.getColumnIndex("name_raw_contact_id")), aDataCursor.getString(6), aDataCursor.getLong(0), aDataCursor.getString(1), aDataCursor.getString(5)));
        }
    }

    private void deselectAllItems() {
        ListView lListView = getListView();
        int lListItemCount = lListView.getCount() - lListView.getFooterViewsCount();
        try {
            if (isSearchMode()) {
                Cursor lCursor = ((ContactMultiselectionAdapter) getAdapter()).getDataCursor();
                int lPosition = lCursor.getColumnIndex("lookup");
                for (int i = this.mHeadderItem + this.mHeaderItemInSearch; i < lListItemCount; i++) {
                    lCursor.moveToPosition(i);
                    Long contactId = Long.valueOf(ContentUris.parseId(((ContactMultiselectionAdapter) getAdapter()).getContactUri(i)));
                    Uri lLookupUri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId.longValue()));
                    if (this.mSelectedContacts.contains(lLookupUri)) {
                        this.mSelectedContacts.remove(lLookupUri);
                        this.mSelectedContactsID.remove(contactId.toString());
                        this.mSelectedInShareContacts.remove(lCursor.getString(lPosition));
                    }
                }
                this.mSelectedCountInSearch = 0;
            } else {
                this.mSelectedContacts.clear();
                this.mSelectedContactsID.clear();
                this.mSelectedInShareContacts.clear();
            }
        } catch (IllegalStateException e) {
            HwLog.e("ContactMultiselectionFragment", "deselectAllItems failed!");
        }
        ((ContactMultiselectionAdapter) getAdapter()).notifyChange();
    }

    protected void resetSelect() {
        this.mSelectedCountInSearch = 0;
        this.mSelectedContacts.clear();
        this.mSelectedContactsID.clear();
        this.mSelectedInShareContacts.clear();
    }

    private void selectAllItems() {
        ListView lListView = getListView();
        int lListItemCount = lListView.getCount() - lListView.getFooterViewsCount();
        boolean lIsMaxContactsSelected = false;
        try {
            synchronized (this.mAllMembers) {
                Cursor lCursor;
                int lPosition;
                int i;
                if (isSearchMode()) {
                    lCursor = ((ContactMultiselectionAdapter) getAdapter()).getDataCursor();
                    lPosition = lCursor.getColumnIndex("lookup");
                    for (i = this.mHeadderItem + this.mHeaderItemInSearch; i < lListItemCount; i++) {
                        lCursor.moveToPosition(i);
                        Long contactId = Long.valueOf(ContentUris.parseId(((ContactMultiselectionAdapter) getAdapter()).getContactUri(i)));
                        Uri lLookupUri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId.longValue()));
                        if (!this.mSelectedContacts.contains(lLookupUri)) {
                            if (addContactToShare(lCursor.getString(lPosition))) {
                                this.mSelectedContacts.add(lLookupUri);
                                this.mSelectedContactsID.add(contactId.toString());
                            } else {
                                lIsMaxContactsSelected = true;
                            }
                        }
                    }
                    this.mSelectedCountInSearch = this.mContactsNumberInSearch;
                } else {
                    this.mSelectedContacts.clear();
                    this.mSelectedContactsID.clear();
                    this.mSelectedInShareContacts.clear();
                    if (!needToSetMaxLimit()) {
                        this.mSelectedContacts.addAll(this.mAllMembers);
                        this.mSelectedContactsID.addAll(this.mAllMembersID);
                    }
                    lCursor = ((ContactMultiselectionAdapter) getAdapter()).getDataCursor();
                    lPosition = lCursor.getColumnIndex("lookup");
                    for (i = this.mHeadderItem; i < lListItemCount; i++) {
                        lCursor.moveToPosition(i);
                        if (!addContactToShare(lCursor.getString(lPosition))) {
                            lIsMaxContactsSelected = true;
                        } else if (needToSetMaxLimit()) {
                            String contactId2 = lCursor.getString(0);
                            this.mSelectedContacts.add(Uri.withAppendedPath(Contacts.CONTENT_URI, contactId2));
                            this.mSelectedContactsID.add(contactId2);
                        }
                    }
                }
                if (lIsMaxContactsSelected) {
                    showToast();
                }
            }
        } catch (IllegalStateException e) {
            HwLog.e("ContactMultiselectionFragment", "selectAllItems failed!");
        }
        ((ContactMultiselectionAdapter) getAdapter()).notifyChange();
    }

    private void deselectAllItemsForResultBack() {
        ListView lListView = getListView();
        int lListItemCount = lListView.getCount() - lListView.getFooterViewsCount();
        if (isSearchMode()) {
            Cursor lCursor = (Cursor) getAdapter().getItem(this.mHeaderItemInSearch);
            for (int i = this.mHeadderItem + this.mHeaderItemInSearch; i < lListItemCount; i++) {
                if (lCursor != null) {
                    Member lMember = new Member(lCursor.getLong(lCursor.getColumnIndex("name_raw_contact_id")), lCursor.getString(6), lCursor.getLong(0), lCursor.getString(1), lCursor.getString(5));
                    if (this.mSelectedMembersList.contains(lMember)) {
                        this.mSelectedMembersList.remove(lMember);
                    }
                    Long contactId = Long.valueOf(ContentUris.parseId(((ContactMultiselectionAdapter) getAdapter()).getContactUri(i)));
                    Uri lLookupUri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId.longValue()));
                    if (this.mSelectedContacts.contains(lLookupUri)) {
                        this.mSelectedContacts.remove(lLookupUri);
                        this.mSelectedContactsID.remove(contactId.toString());
                    }
                    lCursor.moveToNext();
                }
            }
            this.mSelectedCountInSearch = 0;
        } else {
            this.mSelectedContacts.clear();
            this.mSelectedContactsID.clear();
            this.mSelectedMembersList.clear();
        }
        ((ContactMultiselectionAdapter) getAdapter()).notifyChange();
    }

    private void selectAllItemsForResultBack() {
        ListView lListView = getListView();
        int lListItemCount = lListView.getCount() - lListView.getFooterViewsCount();
        if (isSearchMode()) {
            Cursor lCursor = (Cursor) getAdapter().getItem(this.mHeaderItemInSearch);
            for (int i = this.mHeadderItem + this.mHeaderItemInSearch; i < lListItemCount; i++) {
                if (lCursor != null) {
                    Member lMember = new Member(lCursor.getLong(lCursor.getColumnIndex("name_raw_contact_id")), lCursor.getString(6), lCursor.getLong(0), lCursor.getString(1), lCursor.getString(5));
                    if (EmuiFeatureManager.isPrivacyFeatureEnabled()) {
                        lMember.setIsPrivate(CommonUtilMethods.isPrivateContact(lCursor));
                    }
                    if (!this.mSelectedMembersList.contains(lMember)) {
                        this.mSelectedMembersList.add(lMember);
                    }
                    Long contactId = Long.valueOf(ContentUris.parseId(((ContactMultiselectionAdapter) getAdapter()).getContactUri(i)));
                    Uri lLookupUri = Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(contactId.longValue()));
                    if (!this.mSelectedContacts.contains(lLookupUri)) {
                        this.mSelectedContacts.add(lLookupUri);
                        this.mSelectedContactsID.add(contactId.toString());
                    }
                    lCursor.moveToNext();
                }
            }
            this.mSelectedCountInSearch = this.mContactsNumberInSearch;
        } else {
            synchronized (this.mAllMembers) {
                this.mSelectedContacts.clear();
                this.mSelectedMembersList.clear();
                this.mSelectedContacts.addAll(this.mAllMembers);
                this.mSelectedMembersList.addAll(this.mAllMembersList);
            }
        }
        ((ContactMultiselectionAdapter) getAdapter()).notifyChange();
    }

    private ContactListFilter getFilterForRequest() {
        ContactsRequest lRequest = getContactsRequest();
        if (lRequest == null) {
            HwLog.w("ContactMultiselectionFragment", "ContactsRequest is null");
            return ContactListFilter.createFilterWithType(-1);
        }
        String filterAccountType = lRequest.getSimAccountTypeFilter();
        if (filterAccountType != null) {
            return ContactListFilter.createAccountFilter(filterAccountType, SimFactoryManager.getAccountName(filterAccountType), null, null);
        }
        Intent lIntent;
        ContactListFilter filter;
        switch (lRequest.getActionCode()) {
            case 144:
                lIntent = this.mContext.getIntent();
                filter = new ContactListFilter(-21, null, null, null, null);
                filter.mCurrentCompanyName = lIntent.getStringExtra("company_name");
                filter.mIsNoCompanyGroup = lIntent.getBooleanExtra("is_no_company_group", false);
                return filter;
            case 201:
                lIntent = this.mContext.getIntent();
                ContactListFilter lFilter = ContactListFilter.createFromIntent(-9, lIntent);
                lFilter.mIsFavoritesForWidget = lIntent.getBooleanExtra("favorite_from_widget", false);
                return lFilter;
            case 202:
                return new ContactListFilter(-10, null, null, null, null);
            case 203:
                if (!SimFactoryManager.isBothSimLoadingFinished() && HwLog.HWFLOW) {
                    HwLog.i("ContactMultiselectionFragment", "delete contacts whith SIM not readyl");
                }
                if (!QueryUtil.isHAPProviderInstalled()) {
                    filter = ContactListFilter.createFilterWithType(-17);
                    filter.mIsShareOrDelete = true;
                    return filter;
                } else if (this.mCust == null || !this.mCust.supportReadOnly()) {
                    filter = ContactListFilterController.getInstance(this.mContext).getFilter();
                    filter.mIsShareOrDelete = EmuiFeatureManager.isPreLoadingSimContactsEnabled();
                    return filter;
                } else {
                    filter = ContactListFilter.createFilterWithType(-13);
                    filter.mIsShareOrDelete = EmuiFeatureManager.isPreLoadingSimContactsEnabled();
                    return filter;
                }
            case 204:
                if (!SimFactoryManager.isBothSimLoadingFinished() && HwLog.HWFLOW) {
                    HwLog.i("ContactMultiselectionFragment", "share contacts whith SIM not ready");
                }
                filter = ContactListFilterController.getInstance(this.mContext).getFilter();
                filter.mIsShareOrDelete = EmuiFeatureManager.isPreLoadingSimContactsEnabled();
                return filter;
            case 205:
                return ContactListFilter.createFromIntent(-11, this.mContext.getIntent());
            case 217:
                lIntent = this.mContext.getIntent();
                filter = ContactListFilter.createFromIntent(-14, lIntent);
                filter.mAccounts = lIntent.getParcelableArrayListExtra("accounts");
                return filter;
            case 220:
                return new ContactListFilter(-19, "com.android.huawei.phone", "Phone", null, null);
            default:
                return ContactListFilter.restoreDefaultPreferences(SharePreferenceUtil.getDefaultSp_de(this.mContext));
        }
    }

    protected ContactListAdapter getListAdapter() {
        return new ContactMultiselectionAdapter(getActivity(), this.mMissingItemIndex);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_operation:
                if (this.mSelectedContacts.size() > 0) {
                    doOperation();
                    break;
                }
                break;
            case R.id.menu_action_selectall:
                hideSoftKeyboard();
                if (this.mSelectedContacts.size() >= this.mTotalContacts && !this.mIsAllSelected) {
                    showToast();
                } else if (this.mIsAllSelected) {
                    if (isSearchMode()) {
                        setSelectAllItemTitle(false);
                        this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
                    }
                    if (this.mNeedResultBack) {
                        deselectAllItemsForResultBack();
                    } else {
                        deselectAllItems();
                    }
                    this.mIsAllSelected = false;
                } else {
                    if (isSearchMode()) {
                        setSelectAllItemTitle(true);
                        this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
                    }
                    if (this.mNeedResultBack) {
                        selectAllItemsForResultBack();
                    } else {
                        selectAllItems();
                    }
                    this.mIsAllSelected = true;
                }
                getActivity().invalidateOptionsMenu();
                break;
            case R.id.menu_action_cancel:
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void doActionCancel() {
        getActivity().finish();
    }

    protected void doOperation() {
        if (HwLog.HWDBG) {
            HwLog.d("ContactMultiselectionFragment", "doOperation() is called!");
        }
        switch (this.mActionCode) {
            case 144:
                addCompanyContacts();
                return;
            case 201:
                Intent lFavIntent = this.mContext.getIntent();
                if (this.mNeedResultBack) {
                    ((ContactsApplication) this.mContext.getApplicationContext()).setSelectedMemberList(this.mSelectedMembersList);
                    this.mContext.setResult(-1, lFavIntent);
                } else {
                    addToFavorites();
                }
                StatisticalHelper.report(2013);
                break;
            case 202:
                removeFromFavorites();
                break;
            case 203:
                doMultipleDelete();
                return;
            case 204:
                shareContacts();
                StatisticalHelper.report(4034);
                break;
            case 205:
                Intent lIntent = this.mContext.getIntent();
                if (!this.mNeedResultBack) {
                    addMembersToGroup();
                    break;
                }
                ((ContactsApplication) this.mContext.getApplicationContext()).setSelectedMemberList(this.mSelectedMembersList);
                this.mContext.setResult(-1, lIntent);
                break;
            case 217:
                exportContacts();
                break;
            case 220:
                addPrivateContacts();
                break;
        }
        this.mContext.finish();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mHasGrayForFav) {
            menu.findItem(R.id.menu_action_selectall).setVisible(false);
            return;
        }
        super.onCreateOptionsMenu(menu, inflater);
        this.mSelectAllItem = menu.findItem(R.id.menu_action_selectall);
        this.mActionMenu = menu.findItem(R.id.menu_action_operation);
        buildActionMenu(this.mActionMenu);
        ViewUtil.setMenuItemsStateListIcon(this.mContext, menu);
        if (this.mActionCode == 220) {
            this.mActionMenu.setVisible(false);
        } else {
            this.mActionMenu.setVisible(true);
        }
        if (!EmuiVersion.isSupportEmui()) {
            MenuItem cancelItem = menu.findItem(R.id.menu_action_cancel);
            if (cancelItem != null) {
                cancelItem.setVisible(true);
            }
        }
    }

    private void buildActionMenu(MenuItem menu) {
        switch (this.mActionCode) {
            case 144:
            case 201:
            case 205:
                menu.setTitle(R.string.add_label);
                menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.ic_done_normal_light, R.drawable.ic_done_normal));
                return;
            case 202:
                menu.setTitle(R.string.remove_label);
                menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_ic_remove_light, R.drawable.contact_ic_remove));
                return;
            case 203:
                menu.setTitle(R.string.menu_deleteContact);
                menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.ic_trash_normal_light, R.drawable.ic_trash_normal));
                return;
            case 204:
                menu.setTitle(R.string.contact_menu_share_contacts);
                menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_ic_menu_share_light, R.drawable.contact_ic_menu_share));
                return;
            case 217:
                menu.setTitle(R.string.export_label);
                menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.contact_icon_export_out_light, R.drawable.contact_icon_export_out));
                return;
            case 220:
                menu.setTitle(R.string.contact_set);
                menu.setIcon(ImmersionUtils.getImmersionImageID(getContext(), R.drawable.ic_new_contact_light, R.drawable.ic_new_contact));
                ActionBarEx.setEndIcon(this.mActionBar, true, null, this.mActionBarListener);
                return;
            default:
                return;
        }
    }

    private void updateMultiSelection(Cursor data) {
        if (data.getCount() == 0) {
            setSelectAllItemVisible(false);
            if (!isSearchMode()) {
                setEmptyText(R.string.noContacts);
                this.mSearchLayout.setVisibility(8);
                showSelectedItemCountInfo(0);
                this.mSelectedContacts.clear();
                this.mSelectedContactsID.clear();
                this.mSelectedInShareContacts.clear();
            }
            this.mContactsNumberInSearch = 0;
            return;
        }
        getEmptyTextView().setVisibility(4);
        getListView().setVisibility(0);
        if (this.mMissingItemIndex == -1 && isShowSearchLayout()) {
            this.mSearchLayout.setVisibility(0);
        }
        setSelectAllItemVisible(true);
        if (isSearchMode()) {
            ListView lListView = getListView();
            this.mContactsNumberInSearch = (lListView.getCount() - 1) - lListView.getFooterViewsCount();
            this.mSelectedCountInSearch = 0;
            for (int i = this.mHeadderItem + 1; i < lListView.getCount() - lListView.getFooterViewsCount(); i++) {
                if (this.mSelectedContacts.contains(Uri.withAppendedPath(Contacts.CONTENT_URI, String.valueOf(Long.valueOf(ContentUris.parseId(((ContactMultiselectionAdapter) getAdapter()).getContactUri(i))).longValue())))) {
                    this.mSelectedCountInSearch++;
                }
            }
            if (this.mSelectedCountInSearch == this.mContactsNumberInSearch) {
                setSelectAllItemTitle(true);
                this.mIsAllSelected = true;
            } else {
                setSelectAllItemTitle(false);
                this.mIsAllSelected = false;
            }
            if (this.mTitle != null) {
                this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
            }
        }
    }

    protected boolean isShowSearchLayout() {
        return true;
    }

    protected void updateActionBarTitle() {
        this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
    }

    protected void setSelectAllItemTitle(boolean isAllSelected) {
        if (this.mSplitActionBarView != null) {
            if (isAllSelected) {
                this.mSplitActionBarView.refreshDetail(4, new SetButtonDetails(R.drawable.csp_selected_all_normal, R.string.menu_select_none));
                this.mSplitActionBarView.refreshIcon(4, this.mSelectNoneDrawable);
                return;
            }
            this.mSplitActionBarView.refreshDetail(4, new SetButtonDetails(R.drawable.csp_selected_all_normal, R.string.contact_menu_select_all));
            this.mSplitActionBarView.refreshIcon(4, this.mSelectAllDrawable);
        } else if (this.mSelectAllItem != null) {
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
        if (this.mSplitActionBarView != null) {
            this.mSplitActionBarView.setVisibility(4, visible);
        } else if (this.mSelectAllItem != null) {
            this.mSelectAllItem.setVisible(visible);
        }
    }

    @SuppressLint({"HwHardCodeDateFormat"})
    private void shareContacts() {
        IOException e;
        Writer writer;
        Throwable th;
        Uri lUri = Uri.withAppendedPath(Contacts.CONTENT_MULTI_VCARD_URI, Uri.encode(getSelectedItemLookupIds().toString()));
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setType("text/x-vcard");
        if (this.mSelectedInShareContacts.size() > 1000) {
            HwLog.d("ContactMultiselectionFragment", "Share large contacts start!");
            String fileName = "share_contacts" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            File file = new File(getContext().getCacheDir(), fileName);
            Uri largeUri = Uri.withAppendedPath(Uri.withAppendedPath(Contacts.CONTENT_URI, "as_multi_vcard_large"), fileName);
            OutputStreamWriter outputStreamWriter = null;
            BufferedWriter bufferedWriter = null;
            try {
                BufferedWriter bw;
                Writer writer2 = new OutputStreamWriter(new FileOutputStream(file.getAbsolutePath()), "UTF-8");
                try {
                    bw = new BufferedWriter(writer2);
                } catch (IOException e2) {
                    e = e2;
                    writer = writer2;
                    try {
                        HwLog.e("ContactMultiselectionFragment", "Write URI to file share_contacts failed!");
                        e.printStackTrace();
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e3) {
                                HwLog.w("ContactMultiselectionFragment", "share_contacts write stream close failed!");
                                e3.printStackTrace();
                            }
                        }
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        intent.putExtra("android.intent.extra.STREAM", largeUri);
                        getActivity().startActivity(Intent.createChooser(intent, getActivity().getText(R.string.contact_menu_share_contacts)));
                    } catch (Throwable th2) {
                        th = th2;
                        if (bufferedWriter != null) {
                            try {
                                bufferedWriter.close();
                            } catch (IOException e32) {
                                HwLog.w("ContactMultiselectionFragment", "share_contacts write stream close failed!");
                                e32.printStackTrace();
                                throw th;
                            }
                        }
                        if (outputStreamWriter != null) {
                            outputStreamWriter.close();
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    writer = writer2;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    throw th;
                }
                try {
                    bw.write(lUri.toString());
                    bw.flush();
                    if (bw != null) {
                        try {
                            bw.close();
                        } catch (IOException e4) {
                            e32 = e4;
                            bufferedWriter = bw;
                            HwLog.w("ContactMultiselectionFragment", "share_contacts write stream close failed!");
                            e32.printStackTrace();
                            intent.putExtra("android.intent.extra.STREAM", largeUri);
                            getActivity().startActivity(Intent.createChooser(intent, getActivity().getText(R.string.contact_menu_share_contacts)));
                        }
                    }
                    bufferedWriter = bw;
                    if (writer2 != null) {
                        try {
                            writer2.close();
                        } catch (IOException e5) {
                            e32 = e5;
                            HwLog.w("ContactMultiselectionFragment", "share_contacts write stream close failed!");
                            e32.printStackTrace();
                            intent.putExtra("android.intent.extra.STREAM", largeUri);
                            getActivity().startActivity(Intent.createChooser(intent, getActivity().getText(R.string.contact_menu_share_contacts)));
                        }
                        intent.putExtra("android.intent.extra.STREAM", largeUri);
                    }
                } catch (IOException e6) {
                    e32 = e6;
                    bufferedWriter = bw;
                    writer = writer2;
                    HwLog.e("ContactMultiselectionFragment", "Write URI to file share_contacts failed!");
                    e32.printStackTrace();
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    intent.putExtra("android.intent.extra.STREAM", largeUri);
                    getActivity().startActivity(Intent.createChooser(intent, getActivity().getText(R.string.contact_menu_share_contacts)));
                } catch (Throwable th4) {
                    th = th4;
                    bufferedWriter = bw;
                    writer = writer2;
                    if (bufferedWriter != null) {
                        bufferedWriter.close();
                    }
                    if (outputStreamWriter != null) {
                        outputStreamWriter.close();
                    }
                    throw th;
                }
            } catch (IOException e7) {
                e32 = e7;
                HwLog.e("ContactMultiselectionFragment", "Write URI to file share_contacts failed!");
                e32.printStackTrace();
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }
                intent.putExtra("android.intent.extra.STREAM", largeUri);
                getActivity().startActivity(Intent.createChooser(intent, getActivity().getText(R.string.contact_menu_share_contacts)));
            }
            intent.putExtra("android.intent.extra.STREAM", largeUri);
        } else {
            intent.putExtra("android.intent.extra.STREAM", lUri);
        }
        try {
            getActivity().startActivity(Intent.createChooser(intent, getActivity().getText(R.string.contact_menu_share_contacts)));
        } catch (ActivityNotFoundException e8) {
            HwLog.e("ContactMultiselectionFragment", "No activity found for intent: " + intent);
            Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
        }
    }

    private void removeFromFavorites() {
        getContext().startService(ExtendedContactSaveService.createMarkUnmarkFavoriteSelectedContactsIntent(getContext(), getSelectedItemIds(), false));
    }

    private void addToFavorites() {
        if (this.mHasGrayForFav) {
            final Context context = getContext();
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    ContactMultiselectionFragment.this.doAddFavAction(context);
                }
            }, 300);
            return;
        }
        doAddFavAction(getContext());
    }

    private void doAddFavAction(Context context) {
        context.startService(ExtendedContactSaveService.createMarkUnmarkFavoriteSelectedContactsIntent(context, getSelectedItemIds(), true));
    }

    private void doMultipleDelete() {
        if (CommonUtilMethods.isServiceRunning(this.mContext, "com.android.contacts.hap.delete.ExtendedContactSaveService")) {
            Toast.makeText(getActivity(), getString(R.string.repeat_message), 1).show();
            this.mContext.finish();
            return;
        }
        int titleResId;
        int deleteMessageResId;
        ContactListFilter lContactListFilter = getFilterForRequest();
        Bundle lBundle;
        String accountType;
        if (SimFactoryManager.isDualSim()) {
            SharedPreferences prefsOne = SimFactoryManager.getSharedPreferences("SimInfoFile", 0);
            SharedPreferences prefsTwo = SimFactoryManager.getSharedPreferences("SimInfoFile", 1);
            boolean isFirstSimCopyInProgress = prefsOne.getBoolean("sim_copy_contacts_progress", false);
            boolean isSecondSimCopyInProgress = prefsTwo.getBoolean("sim_copy_contacts_progress", false);
            if (lContactListFilter.accountType != null) {
                if ((lContactListFilter.accountType.equals("com.android.huawei.sim") && isFirstSimCopyInProgress) || (lContactListFilter.accountType.equals("com.android.huawei.secondsim") && isSecondSimCopyInProgress)) {
                    lBundle = new Bundle();
                    Bundle bundle = lBundle;
                    bundle.putString("accountType", lContactListFilter.accountType);
                    this.mContext.showDialog(R.id.simCopyProgress, lBundle);
                    return;
                }
            } else if (lContactListFilter.accountType == null) {
                lBundle = new Bundle();
                accountType = null;
                if (isFirstSimCopyInProgress) {
                    accountType = "com.android.huawei.sim";
                } else if (isSecondSimCopyInProgress) {
                    accountType = "com.android.huawei.secondsim";
                }
                if (accountType != null) {
                    lBundle.putString("accountType", accountType);
                    this.mContext.showDialog(R.id.simCopyProgress, lBundle);
                    return;
                }
            }
        }
        boolean isSimCopyInProgress = SimFactoryManager.getSharedPreferences("SimInfoFile", -1).getBoolean("sim_copy_contacts_progress", false);
        if (lContactListFilter.accountType != null) {
            if (lContactListFilter.accountType.equals("com.android.huawei.sim") && isSimCopyInProgress) {
                lBundle = new Bundle();
                bundle = lBundle;
                bundle.putString("accountType", lContactListFilter.accountType);
                this.mContext.showDialog(R.id.simCopyProgress, lBundle);
                return;
            }
        } else if (lContactListFilter.accountType == null) {
            lBundle = new Bundle();
            accountType = null;
            if (isSimCopyInProgress) {
                accountType = "com.android.huawei.sim";
            }
            if (accountType != null) {
                lBundle.putString("accountType", accountType);
                this.mContext.showDialog(R.id.simCopyProgress, lBundle);
                return;
            }
        }
        boolean isHiCloudSyncEnabled = HiCloudUtil.isHicloudSyncStateEnabled(this.mContext);
        boolean isHuaweiAccountIncluded = false;
        if (!(lContactListFilter.filterType == -2 || lContactListFilter.filterType == -3)) {
            if (lContactListFilter.filterType == 0 && "com.android.huawei.phone".equalsIgnoreCase(lContactListFilter.accountType)) {
            }
            if (this.mIsAllSelected) {
                titleResId = R.plurals.delete_multi_contacts__title;
            } else {
                titleResId = R.string.delete_all_contacts_title;
            }
            if (isHiCloudSyncEnabled || !isHuaweiAccountIncluded) {
                deleteMessageResId = R.string.str_displayallcontactwithcheckbox_message;
            } else {
                deleteMessageResId = R.string.str_mutli_delete_messagetext_hicloud;
            }
            AlertDialogFragmet.show(getFragmentManager(), titleResId, this.mIsAllSelected, this.mSelectedContacts.size(), true, this.mContext.getString(deleteMessageResId), deleteMessageResId, true, (Fragment) this, 16843605, (int) R.string.menu_deleteContact, 0);
        }
        isHuaweiAccountIncluded = true;
        if (this.mIsAllSelected) {
            titleResId = R.plurals.delete_multi_contacts__title;
        } else {
            titleResId = R.string.delete_all_contacts_title;
        }
        if (isHiCloudSyncEnabled) {
        }
        deleteMessageResId = R.string.str_displayallcontactwithcheckbox_message;
        AlertDialogFragmet.show(getFragmentManager(), titleResId, this.mIsAllSelected, this.mSelectedContacts.size(), true, this.mContext.getString(deleteMessageResId), deleteMessageResId, true, (Fragment) this, 16843605, (int) R.string.menu_deleteContact, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == -1) {
            Intent lIntent = new Intent(this.mContext, DeleteProgressBarActivity.class);
            lIntent.putExtra("contacts_ids", getSelectedItemIds());
            this.mContext.startActivity(lIntent);
            StatisticalHelper.report(1123);
            onDeleteConfirmed();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onDeleteConfirmed() {
        this.mContext.finish();
        this.mContext.overridePendingTransition(0, 0);
    }

    private void addPrivateContacts() {
        getContext().startService(ExtendedContactSaveService.createMarkUnmarkPrivateContactsIntent(getContext(), getSelectedItemIds(), true));
    }

    private void addCompanyContacts() {
        StatisticalHelper.report(1169);
        Builder builder = new Builder(this.mContext);
        if (this.mIsAllSelected) {
            builder.setTitle(this.mContext.getResources().getString(R.string.add_company_contact_dlg_title_all));
        } else {
            builder.setTitle(this.mContext.getResources().getQuantityString(R.plurals.add_company_contact_dlg_title_part, this.mSelectedContacts.size(), new Object[]{Integer.valueOf(this.mSelectedContacts.size())}));
        }
        if (!isAdded() || getActivity() == null) {
            builder.setMessage(this.mContext.getResources().getString(R.string.contact_add_company_contacts_message, new Object[]{getFilterForRequest().mCurrentCompanyName}));
        } else {
            View view = getActivity().getLayoutInflater().inflate(R.layout.alert_dialog_content, null);
            ((TextView) view.findViewById(R.id.alert_dialog_content)).setText(this.mContext.getResources().getString(R.string.contact_add_company_contacts_message, new Object[]{getFilterForRequest().mCurrentCompanyName}));
            builder.setView(view);
        }
        builder.setPositiveButton(this.mContext.getResources().getString(R.string.contact_menu_add_company_members), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ContactMultiselectionFragment.this.getContext().startService(ExtendedContactSaveService.createAddCompanyContactsIntent(ContactMultiselectionFragment.this.getContext(), ContactMultiselectionFragment.this.getSelectedItemIds(), ContactMultiselectionFragment.this.getFilterForRequest().mCurrentCompanyName, ContactMultiselectionFragment.this.getFilterForRequest().mIsNoCompanyGroup));
                StatisticalHelper.report(1170);
                ContactMultiselectionFragment.this.mContext.finish();
            }
        });
        builder.setNegativeButton(17039360, new CompanyDialogClickListener());
        this.mAddCompanyDialog = builder.create();
        this.mAddCompanyDialog.show();
    }

    public void onPause() {
        super.onPause();
        if (this.mAddCompanyDialog != null) {
            this.mAddCompanyDialog.dismiss();
        }
    }

    public void exportContacts() {
        Intent intent = new Intent();
        intent.putExtra("SelItemData_KeyValue", getSelectedItemIds());
        this.mContext.setResult(-1, intent);
        this.mContext.finish();
    }

    private void addMembersToGroup() {
        ContactListFilter filter = getAdapter().getFilter();
        this.mContext.startService(ExtendedContactSaveService.createAddAndRemoveMembersToGroupIntent(this.mContext, new GroupAndContactMetaData(getSelectedItemIds(), new GroupsData(filter.groupId, filter.accountName, filter.accountType, filter.dataSet), null), false));
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mHasGrayForFav) {
            refreshSplitActionBar();
            return;
        }
        if (this.mSelectAllItem.isVisible() != this.mSelectAllVisible) {
            this.mSelectAllItem.setVisible(this.mSelectAllVisible);
        }
        if (!this.mSelectAllVisible) {
            this.mActionMenu.setVisible(false);
        }
        if (this.mSelectedContacts.size() > 0) {
            this.mActionMenu.setEnabled(true);
        } else {
            this.mActionMenu.setEnabled(false);
        }
        int lSelectedContactsCount = this.mSelectedContacts.size();
        if (isSearchMode()) {
            lSelectedContactsCount = this.mSelectedCountInSearch;
        }
        MenuItem menuItem;
        boolean z;
        if (isSearchMode()) {
            menuItem = this.mSelectAllItem;
            if (this.mContactsNumberInSearch > 0) {
                z = true;
            } else {
                z = false;
            }
            menuItem.setEnabled(z);
            if (this.mContactsNumberInSearch == 0 || this.mSelectedCountInSearch != this.mContactsNumberInSearch) {
                setSelectAllItemTitle(false);
                this.mIsAllSelected = false;
            } else {
                setSelectAllItemTitle(true);
                this.mIsAllSelected = true;
            }
        } else {
            menuItem = this.mSelectAllItem;
            if (this.mTotalContacts > 0) {
                z = true;
            } else {
                z = false;
            }
            menuItem.setEnabled(z);
            if (this.mTotalContacts == 0 || lSelectedContactsCount < this.mTotalContacts) {
                setSelectAllItemTitle(false);
                this.mIsAllSelected = false;
            } else {
                setSelectAllItemTitle(true);
                this.mIsAllSelected = true;
            }
        }
        this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
    }

    protected void onPartitionLoaded(int partitionIndex, Cursor data) {
        super.onPartitionLoaded(partitionIndex, data);
        updateMultiSelection(data);
        if (!(data.getCount() == this.mAllMembers.size() || isSearchMode())) {
            prepareList(data);
        }
        getActivity().invalidateOptionsMenu();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                if (ContactMultiselectionFragment.this.adapter != null) {
                    ContactMultiselectionFragment.this.adapter.notifyChange();
                }
            }
        }, 50);
    }

    private void prepareList(Cursor mainCursor) {
        if (mainCursor != null && !mainCursor.isClosed()) {
            int lCursorCount = mainCursor.getCount();
            if (!needToSetMaxLimit() || lCursorCount < 3000) {
                this.mTotalContacts = lCursorCount;
            } else {
                this.mTotalContacts = 3000;
            }
            try {
                synchronized (this.mAllMembers) {
                    this.mAllMembers.clear();
                    this.mAllMembersID.clear();
                    if (mainCursor.moveToFirst()) {
                        do {
                            String contactId = mainCursor.getString(0);
                            this.mAllMembers.add(Uri.withAppendedPath(Contacts.CONTENT_URI, contactId));
                            this.mAllMembersID.add(contactId);
                            if (this.mNeedResultBack) {
                                updateAllMembers(mainCursor);
                            }
                        } while (mainCursor.moveToNext());
                    }
                }
            } catch (IllegalStateException e) {
                HwLog.e("ContactMultiselectionFragment", "updateAllMembers failed!");
            }
        }
    }

    protected void setEmptyText(int resourceId) {
        CharSequence string;
        TextView emptyView = getEmptyTextView();
        emptyView.setVisibility(0);
        if (isSearchMode()) {
            string = getString(R.string.contact_list_FoundAllContactsZero);
        } else {
            string = getString(resourceId);
        }
        emptyView.setText(string);
        getListView().setVisibility(4);
    }

    protected void showCount(int partitionIndex, Cursor data) {
        if (this.mMultiListener == null) {
            return;
        }
        if (data == null || data.getCount() <= 0) {
            this.mMultiListener.onSelectionChanged(null);
            this.adapter.setSelectedContactUri(null);
            return;
        }
        Uri lookUpUri;
        data.moveToFirst();
        if (this.mCurSelectionUri != null) {
            lookUpUri = this.mCurSelectionUri;
        } else {
            if ((data instanceof MultiCursor) && (((MultiCursor) data).getCurrentCursor() instanceof HwSearchCursor)) {
                this.mDefaultSelectionLookUpUri = Contacts.getLookupUri(data.getLong(data.getColumnIndex("contact_id")), data.getString(data.getColumnIndex("lookup")));
            } else {
                this.mDefaultSelectionLookUpUri = Contacts.getLookupUri(data.getLong(data.getColumnIndex("_id")), data.getString(data.getColumnIndex("lookup")));
            }
            lookUpUri = this.mDefaultSelectionLookUpUri;
        }
        this.mMultiListener.onSelectionChanged(lookUpUri);
        this.adapter.setSelectedContactUri(lookUpUri);
    }

    public void setListener(MultiSelectListener aListener) {
        this.mMultiListener = aListener;
    }

    protected void setActionCode(int code) {
        this.mActionCode = code;
    }

    public boolean beforeActivityFinish() {
        if (!this.mHasGrayForFav) {
            return false;
        }
        Animation animation;
        if (CommonUtilMethods.isLayoutRTL()) {
            animation = AnimationUtils.loadAnimation(this.mContext, R.anim.slide_out_right);
        } else {
            animation = AnimationUtils.loadAnimation(this.mContext, R.anim.slide_out_left);
        }
        getView().findViewById(R.id.list_and_menu_layout).startAnimation(animation);
        return true;
    }

    public void afterActivityFinish() {
        if (this.mHasGrayForFav && isAdded()) {
            getActivity().overridePendingTransition(0, 0);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mAddCompanyDialog != null && this.mAddCompanyDialog.isShowing()) {
            this.mAddCompanyDialog.dismiss();
        }
        this.mMultiListener = null;
    }

    public void handleEmptyList(int aCount) {
        setEmptyText(R.string.noContacts);
        super.handleEmptyList(aCount);
    }

    private boolean needToSetMaxLimit() {
        return 204 == this.mActionCode;
    }

    private boolean addContactToShare(String aLookUpId) {
        boolean z;
        if (!needToSetMaxLimit() || this.mSelectedInShareContacts.size() < 3000) {
            z = false;
        } else {
            z = true;
        }
        if (z) {
            return false;
        }
        this.mSelectedInShareContacts.add(aLookUpId);
        return true;
    }

    private void showToast() {
        Toast.makeText(getActivity(), getString(R.string.max_contact_selected_Toast, new Object[]{Integer.valueOf(3000)}), 0).show();
    }

    public CursorLoader createCursorLoader(int id) {
        return new ProfileAndContactsLoader(this.mContext);
    }

    protected void loadData() {
        if (this.mHasGrayForFav) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (ContactMultiselectionFragment.this.isAdded()) {
                        ContactMultiselectionFragment.this.superLoadData();
                    }
                }
            }, 300);
        } else {
            superLoadData();
        }
    }

    private void superLoadData() {
        super.loadData();
    }

    private void createSplitActionBar(Activity mContext, View lView) {
        SetButtonDetails menuOptions;
        this.mSplitActionBarView = (SplitActionBarView) lView.findViewById(R.id.menu_view);
        this.mSplitActionBarView.setVisibility(0);
        if (this.mActionCode == 201) {
            menuOptions = new SetButtonDetails(R.drawable.ic_done_normal, R.string.add_label);
        } else {
            menuOptions = new SetButtonDetails(R.drawable.contact_ic_remove, R.string.remove_label);
        }
        this.mSplitActionBarView.fillDetails(menuOptions, null, null, new SetButtonDetails(R.drawable.csp_selected_all_normal, R.string.contact_menu_select_all), false);
        this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
            public boolean onCustomSplitMenuItemClick(int aMenuItem) {
                return ContactMultiselectionFragment.this.customSplitMenuItemClicked(aMenuItem);
            }

            public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                return false;
            }

            public void onPrepareOptionsMenu(Menu aMenu) {
            }
        });
    }

    protected boolean customSplitMenuItemClicked(int aMenuItem) {
        switch (aMenuItem) {
            case R.string.contact_menu_select_all:
                hideSoftKeyboard();
                if (this.mSelectedContacts.size() >= this.mTotalContacts) {
                    showToast();
                } else {
                    if (isSearchMode()) {
                        setSelectAllItemTitle(true);
                        if (this.mTitle != null) {
                            this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
                        }
                    }
                    if (this.mNeedResultBack) {
                        selectAllItemsForResultBack();
                    } else {
                        selectAllItems();
                    }
                    this.mIsAllSelected = true;
                }
                getActivity().invalidateOptionsMenu();
                break;
            case R.string.menu_select_none:
                hideSoftKeyboard();
                if (isSearchMode()) {
                    setSelectAllItemTitle(false);
                    if (this.mTitle != null) {
                        this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
                    }
                }
                if (this.mNeedResultBack) {
                    deselectAllItemsForResultBack();
                } else {
                    deselectAllItems();
                }
                this.mIsAllSelected = false;
                getActivity().invalidateOptionsMenu();
                break;
            case R.string.add_label:
            case R.string.remove_label:
                if (this.mSelectedContacts.size() > 0) {
                    doOperation();
                    break;
                }
                break;
            default:
                return false;
        }
        return true;
    }

    private void refreshSplitActionBar() {
        innerRefreshSplitActionBar(1, 4);
    }

    protected void innerRefreshSplitActionBar(int actionMenu, int selectAllMenu) {
        if (this.mSplitActionBarView != null) {
            if (this.mSplitActionBarView.isVisible(selectAllMenu) != this.mSelectAllVisible) {
                this.mSplitActionBarView.setVisibility(selectAllMenu, this.mSelectAllVisible);
            }
            if (this.mSelectAllVisible) {
                this.mSplitActionBarView.setVisibility(actionMenu, true);
            } else {
                this.mSplitActionBarView.setVisibility(actionMenu, false);
            }
            if (this.mSelectedContacts.size() > 0) {
                this.mSplitActionBarView.setEnable(actionMenu, true);
            } else {
                this.mSplitActionBarView.setEnable(actionMenu, false);
            }
            int lSelectedContactsCount = this.mSelectedContacts.size();
            if (isSearchMode()) {
                lSelectedContactsCount = this.mSelectedCountInSearch;
            }
            SplitActionBarView splitActionBarView;
            boolean z;
            if (isSearchMode()) {
                splitActionBarView = this.mSplitActionBarView;
                if (this.mContactsNumberInSearch > 0) {
                    z = true;
                } else {
                    z = false;
                }
                splitActionBarView.setEnable(selectAllMenu, z);
                if (this.mContactsNumberInSearch == 0 || this.mSelectedCountInSearch != this.mContactsNumberInSearch) {
                    setSelectAllItemTitle(false);
                    this.mIsAllSelected = false;
                } else {
                    setSelectAllItemTitle(true);
                    this.mIsAllSelected = true;
                }
            } else {
                splitActionBarView = this.mSplitActionBarView;
                if (this.mTotalContacts > 0) {
                    z = true;
                } else {
                    z = false;
                }
                splitActionBarView.setEnable(selectAllMenu, z);
                if (this.mTotalContacts == 0 || lSelectedContactsCount < this.mTotalContacts) {
                    setSelectAllItemTitle(false);
                    this.mIsAllSelected = false;
                } else {
                    setSelectAllItemTitle(true);
                    this.mIsAllSelected = true;
                }
            }
            if (this.mTitle != null) {
                this.mTitle.setCustomTitle(getMultiselectionTitle(), this.mSelectedContacts.size());
            }
        }
    }
}
