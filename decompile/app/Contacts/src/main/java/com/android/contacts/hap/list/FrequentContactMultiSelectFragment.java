package com.android.contacts.hap.list;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.activities.ContactAndGroupMultiSelectionActivity;
import com.android.contacts.hap.activities.ContactMultiSelectionActivity;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.performance.PLog;
import com.huawei.cspcommon.util.ViewUtil;
import com.huawei.cust.HwCustUtils;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.android.widget.SubTabWidget.SubTabListener;
import java.util.HashMap;

public class FrequentContactMultiSelectFragment extends ListFragment implements LoaderCallbacks<Cursor>, SubTabListener {
    private ActionBar mActionBar;
    private FrequentContactSelectAdapter mAdapter;
    private ContactsRequest mContactRequest;
    private HwCustFrequentContactMultiSelectFragment mCust = null;
    ActionBarCustom mCustActionBar;
    Handler mHideHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int type = msg.what;
            if (FrequentContactMultiSelectFragment.this.mParentActivity != null) {
                try {
                    View view = FrequentContactMultiSelectFragment.this.getView();
                    if (view != null) {
                        switch (type) {
                            case 1:
                                CommonUtilMethods.hideSoftKeyboard(FrequentContactMultiSelectFragment.this.mParentActivity, view);
                                break;
                        }
                    }
                } catch (RuntimeException e) {
                    HwLog.w("FrequentContactMultiSelectFragment", "view is null ");
                }
            }
        }
    };
    private boolean mIsAllSelected;
    private ListView mListView;
    private int mMaxLimit = -1;
    protected Activity mParentActivity;
    private Drawable mSelectAllDrawable;
    private MenuItem mSelectAllMenuItem;
    private boolean mSelectAllMenuVisable = false;
    private Drawable mSelectNoneDrawable;
    private int mSelectedItemCount;
    private int mTotalContacts = 0;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mParentActivity = activity;
        this.mActionBar = this.mParentActivity.getActionBar();
        if (!EmuiVersion.isSupportEmui() && this.mCustActionBar == null) {
            this.mCustActionBar = new ActionBarCustom(this.mParentActivity, this.mActionBar);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mSelectedItemCount = savedInstanceState.getInt("selected_item_count");
        }
        this.mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(getActivity());
        this.mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(getActivity());
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustFrequentContactMultiSelectFragment) HwCustUtils.createObj(HwCustFrequentContactMultiSelectFragment.class, new Object[]{getActivity()});
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PLog.d(0, "FrequentContactMultiSelectFragment onCreate begin");
        View view = inflater.inflate(R.layout.frequent_contact_mutil_select_fragment, container, false);
        this.mListView = (ListView) view.findViewById(16908298);
        this.mListView.setFastScrollEnabled(true);
        this.mAdapter = new FrequentContactSelectAdapter(this.mParentActivity, null, false, this.mListView);
        setListAdapter(this.mAdapter);
        this.mListView.setChoiceMode(2);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mListView, getActivity());
        this.mListView.setFooterDividersEnabled(false);
        setHasOptionsMenu(true);
        this.mMaxLimit = ((ContactMultiSelectionActivity) this.mParentActivity).mMaxLimit;
        PLog.d(0, "FrequentContactMultiSelectFragment onCreate end");
        return view;
    }

    public void onStart() {
        super.onStart();
        startLoading();
    }

    public void onResume() {
        super.onResume();
        if (this.mAdapter != null) {
            this.mAdapter.upateSimpleDisplayMode();
        }
    }

    private void startLoading() {
        if (this.mAdapter != null) {
            Bundle bundle = new Bundle();
            if (ContactAndGroupMultiSelectionActivity.isPrivateModeChange()) {
                getLoaderManager().restartLoader(1, bundle, this);
            } else {
                getLoaderManager().initLoader(1, bundle, this);
            }
        }
    }

    public void setContactsRequest(ContactsRequest request) {
        this.mContactRequest = request;
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        PLog.d(0, "FrequentContactMultiSelectFragment onCreateLoader");
        FrequntContactMultiselectListLoader loader = new FrequntContactMultiselectListLoader(this.mParentActivity, Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "contacts/frequent"), null, null, null, null);
        loader.setDataFilter(getFilterForRequest());
        return loader;
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        PLog.d(0, "FrequentContactMultiSelectFragment onLoadFinished");
        if (isAdded()) {
            if (data != null) {
                this.mTotalContacts = data.getCount() > this.mMaxLimit ? this.mMaxLimit : data.getCount();
                this.mAdapter.changeCursor(data);
                refreshListViewDelayed();
            } else {
                this.mTotalContacts = 0;
            }
            if (this.mTotalContacts == 0) {
                this.mSelectAllMenuVisable = false;
            } else {
                this.mSelectAllMenuVisable = true;
            }
            handleEmptyList(this.mTotalContacts);
            this.mParentActivity.invalidateOptionsMenu();
        }
    }

    private void refreshListViewDelayed() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (FrequentContactMultiSelectFragment.this.mAdapter != null) {
                    FrequentContactMultiSelectFragment.this.mAdapter.notifyDataSetChanged();
                }
            }
        }, 50);
    }

    private void handleEmptyList(int count) {
        View loadingLayout = getView().findViewById(R.id.loadingcontacts);
        if (loadingLayout != null) {
            loadingLayout.setVisibility(8);
        }
        View emptyView = getView().findViewById(R.id.content_empty);
        if (count == 0) {
            getListView().setVisibility(8);
            emptyView.setVisibility(0);
            setEmptyText();
            return;
        }
        getListView().setVisibility(0);
        emptyView.setVisibility(8);
    }

    private void setEmptyText() {
        TextView emptyTextView = (TextView) this.mParentActivity.findViewById(R.id.frequent_list_empty);
        emptyTextView.setText(getString(getEmptyTextId()));
        Activity activity = getActivity();
        if (activity != null) {
            boolean isPor = getResources().getConfiguration().orientation == 1;
            int subTabHeight = getResources().getDimensionPixelSize(R.dimen.suspention_view_height);
            if (isPor) {
                MarginLayoutParams params = (MarginLayoutParams) emptyTextView.getLayoutParams();
                params.topMargin = CommonUtilMethods.getMarginTopPix(activity, 0.3d, isPor) - subTabHeight;
                emptyTextView.setLayoutParams(params);
                return;
            }
            int paddingBottom = CommonUtilMethods.getActionBarAndStatusHeight(activity, isPor) + subTabHeight;
            ((LinearLayout) this.mParentActivity.findViewById(R.id.content_empty)).setGravity(17);
            emptyTextView.setPadding(0, 0, 0, paddingBottom);
        }
    }

    private int getEmptyTextId() {
        switch (this.mContactRequest.getActionCode()) {
            case 208:
            case 210:
                if (this.mCust == null || !this.mCust.getEnableEmailContactInMms()) {
                    return R.string.contact_frequent_noNumbers;
                }
                return this.mCust.getNoPhoneNumbersOrEmailsTextId();
            case 209:
            case 212:
                return R.string.contact_frequent_noEmails;
            case 211:
                return R.string.contact_frequent_noNumbers;
            default:
                return R.string.contacts_no_frequent_contacts;
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        boolean isChecked = this.mListView.isItemChecked(position);
        if (!isChecked || this.mMaxLimit <= 0 || ((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size() < this.mMaxLimit) {
            checkItem(position, isChecked);
            updateCacheWithCheckedState(position, isChecked);
            getActivity().invalidateOptionsMenu();
            return;
        }
        this.mListView.setItemChecked(position, false);
        showReachMaxToast();
    }

    private void updateCacheWithCheckedState(int position, boolean isChecked) {
        Uri selectedUri = this.mAdapter.getSelectedDataUri(position);
        String selectedData = this.mAdapter.getSelectedData(position);
        long selectedContactId = this.mAdapter.getSelectedContactId(position);
        if (isChecked) {
            addToCache(selectedUri, selectedData, selectedContactId);
        } else {
            removeFromCache(selectedUri, selectedData, selectedContactId);
        }
    }

    private void addToCache(Uri selectedUri, String selectedData, long selectedContactId) {
        ContactMultiSelectionActivity parentActivity = this.mParentActivity;
        if (this.mMaxLimit <= 0 || ((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size() < this.mMaxLimit) {
            if (!parentActivity.mSelectedDataUris.contains(selectedUri)) {
                if (selectedUri != null && selectedUri.toString().startsWith("content://call_log/calls")) {
                    parentActivity.mCallLogCount++;
                }
                parentActivity.mSelectedDataUris.add(selectedUri);
            }
            if (!parentActivity.mSelectedData.contains(selectedData)) {
                parentActivity.mSelectedData.add(selectedData);
            }
            if (!parentActivity.mSelectedContactId.contains(Long.valueOf(selectedContactId))) {
                parentActivity.mSelectedContactId.add(Long.valueOf(selectedContactId));
            }
            return;
        }
        showReachMaxToast();
    }

    private void removeFromCache(Uri selectedUri, String selectedData, long selectedContactId) {
        ContactMultiSelectionActivity parentActivity = this.mParentActivity;
        if (selectedUri != null && selectedUri.toString().startsWith("content://call_log/calls") && parentActivity.mCallLogCount > 0) {
            parentActivity.mCallLogCount--;
        }
        parentActivity.mSelectedDataUris.remove(selectedUri);
        parentActivity.mSelectedData.remove(selectedData);
        parentActivity.mSelectedContactId.remove(Long.valueOf(selectedContactId));
    }

    private void showReachMaxToast() {
        Toast.makeText(this.mParentActivity, getString(R.string.max_contact_selected_Toast, new Object[]{Integer.valueOf(this.mMaxLimit)}), 0).show();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        this.mSelectAllMenuItem = menu.findItem(R.id.menu_action_selectall);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (this.mSelectAllMenuVisable) {
            this.mSelectAllMenuItem.setVisible(true);
            if (this.mSelectedItemCount >= this.mTotalContacts) {
                setSelectAllItemTitle(true);
                this.mIsAllSelected = true;
            } else {
                setSelectAllItemTitle(false);
                this.mIsAllSelected = false;
            }
        } else {
            this.mSelectAllMenuItem.setVisible(false);
        }
        reConfigureActionBarTitle();
    }

    private void setSelectAllItemTitle(boolean isSelected) {
        if (this.mSelectAllMenuItem != null) {
            if (isSelected) {
                this.mSelectAllMenuItem.setTitle(R.string.menu_select_none);
                this.mSelectAllMenuItem.setIcon(this.mSelectNoneDrawable);
            } else {
                this.mSelectAllMenuItem.setTitle(R.string.contact_menu_select_all);
                this.mSelectAllMenuItem.setIcon(this.mSelectAllDrawable);
            }
            this.mSelectAllMenuItem.setChecked(isSelected);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_selectall:
                if (this.mIsAllSelected) {
                    deselectAllItems();
                    setSelectAllItemTitle(false);
                    this.mIsAllSelected = false;
                } else {
                    optionsItemSelectedAll();
                    setSelectAllItemTitle(true);
                    this.mIsAllSelected = true;
                }
                this.mParentActivity.invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void optionsItemSelectedAll() {
        if (this.mParentActivity.checkIsSelectAllWithDef()) {
            switch (this.mContactRequest.getActionCode()) {
                case 208:
                    selectAllItemsWithDef(2);
                    return;
                case 209:
                case 212:
                    selectAllItemsWithDef(4);
                    return;
                case 210:
                    selectAllItems();
                    return;
                default:
                    selectAllItems();
                    return;
            }
        }
        selectAllItems();
    }

    private void selectAllItemsWithDef(int aPhoneType) {
        ContactAndGroupMultiSelectionActivity parentActivity = (ContactAndGroupMultiSelectionActivity) this.mParentActivity;
        long lDefaultSelectedId = -1;
        String lDefaultSelectedData = null;
        long previousId = -1;
        long lDefaultTimes = -1;
        HashMap<Long, Long> frequentDataMap = parentActivity.getFrequentDataMap();
        boolean isPrimaryFound = false;
        boolean isMobileNumberFound = false;
        boolean isDefaultSelectedNumberFound = false;
        for (int i = 0; i < getListView().getCount(); i++) {
            Uri dataUri;
            long ContactId = this.mAdapter.getSelectedContactId(i);
            if (!(previousId == -1 || ContactId == previousId)) {
                if (this.mMaxLimit <= 0 || ((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size() < this.mMaxLimit) {
                    if (!(lDefaultSelectedId == -1 || lDefaultSelectedData == null)) {
                        dataUri = Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(lDefaultSelectedId));
                        if (!parentActivity.mSelectedDataUris.contains(dataUri)) {
                            this.mSelectedItemCount++;
                            addToCache(dataUri, lDefaultSelectedData, lDefaultSelectedId);
                        }
                    }
                    isPrimaryFound = false;
                    isMobileNumberFound = false;
                    isDefaultSelectedNumberFound = false;
                    lDefaultSelectedId = -1;
                    lDefaultSelectedData = null;
                    lDefaultTimes = -1;
                } else {
                    showReachMaxToast();
                    return;
                }
            }
            previousId = ContactId;
            long selectedId = this.mAdapter.getSelectedDataId(i);
            String aSelectedData = this.mAdapter.getSelectedData(i);
            int phoneType = this.mAdapter.getDataType(i);
            int isPrimary = this.mAdapter.getDataPrimary(i);
            long currentTimes = frequentDataMap.containsKey(Long.valueOf(selectedId)) ? ((Long) frequentDataMap.get(Long.valueOf(selectedId))).longValue() : 0;
            if (isPrimary == 1 && !isPrimaryFound) {
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
            dataUri = Uri.withAppendedPath(Data.CONTENT_URI, String.valueOf(lDefaultSelectedId));
            if (!parentActivity.mSelectedDataUris.contains(dataUri)) {
                this.mSelectedItemCount++;
                addToCache(dataUri, lDefaultSelectedData, lDefaultSelectedId);
            }
        }
        this.mAdapter.notifyDataSetChanged();
    }

    private void selectAllItems() {
        ContactMultiSelectionActivity parentActivity = this.mParentActivity;
        this.mSelectedItemCount = 0;
        for (int i = 0; i < this.mListView.getCount() - this.mListView.getFooterViewsCount(); i++) {
            if (this.mMaxLimit > 0 && parentActivity.mSelectedDataUris.size() >= this.mMaxLimit) {
                showReachMaxToast();
                break;
            }
            Uri selectedUri = this.mAdapter.getSelectedDataUri(i);
            String selectedData = this.mAdapter.getSelectedData(i);
            long selectedContactId = this.mAdapter.getSelectedContactId(i);
            this.mSelectedItemCount++;
            addToCache(selectedUri, selectedData, selectedContactId);
        }
        this.mAdapter.notifyDataSetChanged();
    }

    private void deselectAllItems() {
        for (int i = 0; i < this.mListView.getCount() - this.mListView.getFooterViewsCount(); i++) {
            removeFromCache(this.mAdapter.getSelectedDataUri(i), this.mAdapter.getSelectedData(i), this.mAdapter.getSelectedContactId(i));
        }
        this.mSelectedItemCount = 0;
        this.mAdapter.notifyDataSetChanged();
    }

    private void checkItem(int position, boolean isChecked) {
        if (HwLog.HWDBG) {
            HwLog.d("FrequentContactMultiSelectFragment", "checkItem,position=" + position + ",mSelectedItemCount=" + this.mSelectedItemCount);
        }
        this.mListView.setItemChecked(position, isChecked);
        if (isChecked) {
            this.mSelectedItemCount++;
        } else {
            this.mSelectedItemCount--;
        }
    }

    private void reConfigureActionBarTitle() {
        if (this.mActionBar != null) {
            ActionBarCustomTitle title = new ActionBarCustomTitle(getActivity());
            if (EmuiVersion.isSupportEmui()) {
                ActionBarEx.setCustomTitle(this.mActionBar, title.getTitleLayout());
            } else {
                this.mCustActionBar.setCustomTitle(title.getTitleLayout());
            }
            if (((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size() == 0) {
                title.setCustomTitle(getResources().getString(R.string.contacts_not_selected_text), ((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size());
            } else {
                title.setCustomTitle(getResources().getString(R.string.contacts_selected_text), ((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.size());
            }
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private DataListFilter getFilterForRequest() {
        int filterType = 0;
        long groupId = -1;
        Uri groupUri;
        switch (this.mContactRequest.getActionCode()) {
            case 208:
                filterType = -1;
                groupUri = (Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri");
                if (groupUri != null) {
                    groupId = Long.parseLong(groupUri.getLastPathSegment());
                    break;
                }
                break;
            case 209:
                filterType = -2;
                groupUri = (Uri) this.mParentActivity.getIntent().getParcelableExtra("extra_group_uri");
                if (groupUri != null) {
                    groupId = Long.parseLong(groupUri.getLastPathSegment());
                    break;
                }
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
            case VTMCDataCache.MAX_EXPIREDTIME /*300*/:
                if (!EmuiFeatureManager.isRcsFeatureEnable()) {
                    filterType = 0;
                    break;
                }
                filterType = -50;
                break;
        }
        return new DataListFilter(filterType, null, null, null, null, groupId, null, false, null);
    }

    public void onSubTabReselected(SubTab subTab, FragmentTransaction ft) {
    }

    public void onSubTabSelected(SubTab subTab, FragmentTransaction ft) {
        Message msg = new Message();
        msg.what = 1;
        this.mHideHandler.sendMessage(msg);
        if (this.mAdapter != null && this.mAdapter.getCursor() != null) {
            this.mSelectedItemCount = 0;
            for (int i = 0; i < this.mAdapter.getCursor().getCount(); i++) {
                addItemFromTab(i, ((ContactMultiSelectionActivity) this.mParentActivity).mSelectedDataUris.contains(this.mAdapter.getSelectedDataUri(i)));
            }
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void addItemFromTab(int position, boolean isChecked) {
        if (isChecked) {
            this.mSelectedItemCount++;
        }
    }

    public void onSubTabUnselected(SubTab subTab, FragmentTransaction ft) {
    }

    public void setCustActionBar(ActionBarCustom custActionBar) {
        this.mCustActionBar = custActionBar;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_item_count", this.mSelectedItemCount);
    }
}
