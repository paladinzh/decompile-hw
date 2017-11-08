package com.android.contacts.hap.calllog;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CallLog.Calls;
import android.telephony.PhoneNumberUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import com.android.contacts.GeoUtil;
import com.android.contacts.calllog.CallLogAdapter.CallFetcher;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.calllog.CallLogQueryHandler;
import com.android.contacts.calllog.CallLogQueryHandler.Listener;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.DeleteCallLogDialog;
import com.android.contacts.calllog.ExtendedCursor;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.util.ActionBarCustom;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.hap.widget.SplitActionBarView;
import com.android.contacts.hap.widget.SplitActionBarView.OnCustomMenuListener;
import com.android.contacts.hap.widget.SplitActionBarView.SetButtonDetails;
import com.android.contacts.util.ActionBarCustomTitle;
import com.android.contacts.util.EmuiVersion;
import com.android.contacts.util.HwLog;
import com.android.contacts.widget.ActionBarEx;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;
import java.util.HashSet;

public class CallLogMultiSelectionFragment extends HwBaseFragment implements CallFetcher, OnItemClickListener, OnCheckedChangeListener, Listener {
    private OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            int idCencel;
            if (EmuiVersion.isSupportEmui()) {
                idCencel = 16908295;
            } else {
                idCencel = R.id.icon1;
            }
            if (v.getId() == idCencel) {
                CallLogMultiSelectionFragment.this.getActivity().finish();
            }
        }
    };
    private CallLogMultiSelectionActivity mActivityRef;
    private CallLogMultiSelectionAdapter mAdapter;
    private ArrayList<Long> mAllIdsList = new ArrayList();
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            if (CallLogMultiSelectionFragment.this.mAdapter != null) {
                CallLogMultiSelectionFragment.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private ListView mCallLogListView;
    private CallLogQueryHandler mCallLogQueryHandler;
    private int mCallTypeFilter = 0;
    private Activity mContext;
    private MenuItem mDeleteMenu;
    private ArrayList<String> mEmergencynumbers = null;
    private int mFirstVisiblePosition = 0;
    private float mFriction = 0.0075f;
    private boolean mIsAllSelected;
    private int mNetworkTypeFilter = 2;
    private Drawable mSelectAllDrawable;
    private Drawable mSelectNoneDrawable;
    private ActionBarCustomTitle mTitle;
    private float mVelocityScale = 0.65f;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivityRef = (CallLogMultiSelectionActivity) activity;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            this.mActivityRef.mSelectedIds = (HashSet) savedInstanceState.getSerializable("selected_entries");
            this.mIsAllSelected = savedInstanceState.getBoolean("all_selected");
            this.mCallTypeFilter = savedInstanceState.getInt("call_type_filter");
            this.mNetworkTypeFilter = savedInstanceState.getInt("network_type_filter");
            this.mFirstVisiblePosition = savedInstanceState.getInt("first_visible_position", 0);
        }
        this.mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(getActivity());
        this.mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(getActivity());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SIM_STATE_CHANGED");
        if (getActivity() != null) {
            getActivity().registerReceiver(this.mBroadcastReceiver, filter);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("selected_entries", this.mActivityRef.mSelectedIds);
        outState.putBoolean("all_selected", this.mIsAllSelected);
        outState.putInt("call_type_filter", this.mCallTypeFilter);
        outState.putInt("network_type_filter", this.mNetworkTypeFilter);
        if (this.mCallLogListView != null) {
            outState.putInt("first_visible_position", this.mCallLogListView.getFirstVisiblePosition());
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        ActionBar mActionBar = this.mContext.getActionBar();
        if (EmuiVersion.isSupportEmui()) {
            ActionBarEx.setStartIcon(mActionBar, true, null, this.mActionBarListener);
            ActionBarEx.setEndIcon(mActionBar, false, null, null);
            this.mTitle = new ActionBarCustomTitle(this.mContext, inflater);
            ActionBarEx.setCustomTitle(mActionBar, this.mTitle.getTitleLayout());
        } else {
            this.mTitle = new ActionBarCustomTitle(this.mContext, inflater);
            ActionBarCustom mCustActionBar = new ActionBarCustom(this.mContext, mActionBar);
            mCustActionBar.init();
            mCustActionBar.setStartIcon(true, null, this.mActionBarListener);
            mCustActionBar.setEndIcon(false, null, null);
            mCustActionBar.setCustomTitle(this.mTitle.getTitleLayout());
        }
        this.mAdapter = new CallLogMultiSelectionAdapter(this.mActivityRef, this, new ContactInfoHelper(this.mActivityRef, GeoUtil.getCurrentCountryIso(getActivity())));
        View lView = inflater.inflate(R.layout.calllog_multi_select_list, null);
        this.mCallLogListView = (ListView) lView.findViewById(R.id.calllog_multi_list);
        this.mCallLogListView.setFriction(this.mFriction);
        this.mCallLogListView.setVelocityScale(this.mVelocityScale);
        this.mCallLogListView.setAdapter(this.mAdapter);
        this.mCallLogListView.setItemsCanFocus(true);
        this.mCallLogListView.setChoiceMode(2);
        this.mCallLogListView.setOnItemClickListener(this);
        this.mCallLogListView.setOnScrollListener(this.mAdapter);
        this.mCallLogListView.setFastScrollEnabled(true);
        CommonUtilMethods.addFootEmptyViewPortrait(this.mCallLogListView, this.mContext);
        this.mCallLogQueryHandler = new CallLogQueryHandler(this.mContext, getActivity().getContentResolver(), this);
        startCallsQuery();
        this.mTitle.setCustomTitle(getCallLogMultiSelectionTitle(), this.mActivityRef.mSelectedIds.size());
        if (CommonUtilMethods.calcIfNeedSplitScreen() && getResources().getConfiguration().orientation == 2) {
            createSplitBar(lView);
        }
        return lView;
    }

    private void createSplitBar(View root) {
        this.mSplitActionBarView = (SplitActionBarView) root.findViewById(R.id.menu_view);
        this.mSplitActionBarView.setVisibility(0);
        this.mSplitActionBarView.fillDetails(new SetButtonDetails(R.drawable.ic_trash_normal, R.string.menu_deleteContact), null, null, new SetButtonDetails(R.drawable.csp_selected_all_highlight, R.string.contact_menu_select_all), false);
        this.mSplitActionBarView.setOnCustomMenuListener(new OnCustomMenuListener() {
            public void onPrepareOptionsMenu(Menu aMenu) {
            }

            public boolean onCustomSplitMenuItemClick(int menuStringResId) {
                switch (menuStringResId) {
                    case R.string.menu_deleteContact:
                        if (CallLogMultiSelectionFragment.this.mActivityRef.mSelectedIds.size() > 0) {
                            CallLogMultiSelectionFragment.this.doOperation();
                            break;
                        }
                        break;
                    case R.string.contact_menu_select_all:
                    case R.string.menu_select_none:
                        CallLogMultiSelectionFragment.this.onSelectAllClick();
                        break;
                    default:
                        return false;
                }
                return true;
            }

            public boolean onCustomMenuItemClick(MenuItem aMenuItem) {
                return false;
            }
        });
    }

    private void onPrepareSplitBar() {
        if (this.mSplitActionBarView != null) {
            if (this.mAllIdsList.size() == 0 || this.mActivityRef.mSelectedIds.size() == 0) {
                this.mSplitActionBarView.setEnable(1, false);
            } else {
                this.mSplitActionBarView.setEnable(1, true);
            }
            if (this.mIsAllSelected) {
                this.mSplitActionBarView.refreshDetail(4, new SetButtonDetails(R.drawable.csp_selected_all_highlight, R.string.menu_select_none));
                this.mSplitActionBarView.refreshIcon(4, this.mSelectNoneDrawable);
            } else {
                this.mSplitActionBarView.refreshDetail(4, new SetButtonDetails(R.drawable.csp_selected_all_highlight, R.string.contact_menu_select_all));
                this.mSplitActionBarView.refreshIcon(4, this.mSelectAllDrawable);
            }
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (CommonUtilMethods.calcIfNeedSplitScreen() && getResources().getConfiguration().orientation == 2) {
            onPrepareSplitBar();
            return;
        }
        this.mDeleteMenu = menu.findItem(R.id.menu_action_delete);
        ImmersionUtils.setImmersionMommonMenu(this.mContext, this.mDeleteMenu);
        ViewUtil.setMenuItemStateListIcon(this.mContext, this.mDeleteMenu);
        MenuItem lSelectAllMenu = menu.findItem(R.id.menu_action_selectall);
        setDeleteMenuEnable();
        if (this.mIsAllSelected) {
            lSelectAllMenu.setTitle(getString(R.string.menu_select_none));
            lSelectAllMenu.setIcon(this.mSelectNoneDrawable);
        } else {
            lSelectAllMenu.setTitle(getString(R.string.contact_menu_select_all));
            lSelectAllMenu.setIcon(this.mSelectAllDrawable);
        }
        lSelectAllMenu.setChecked(this.mIsAllSelected);
    }

    private void setDeleteMenuEnable() {
        if (this.mAllIdsList != null) {
            if (this.mAllIdsList.size() == 0 || this.mActivityRef.mSelectedIds.size() == 0) {
                this.mDeleteMenu.setEnabled(false);
            } else {
                this.mDeleteMenu.setEnabled(true);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_selectall:
                onSelectAllClick();
                break;
            case R.id.menu_action_delete:
                if (this.mActivityRef.mSelectedIds.size() > 0) {
                    doOperation();
                    return true;
                }
                break;
            case R.id.menu_select_cancel:
                if (getActivity() != null) {
                    getActivity().finish();
                    break;
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSelectAllClick() {
        if (this.mIsAllSelected) {
            deselectAllItems();
            this.mIsAllSelected = false;
        } else {
            selectAllItems();
            this.mIsAllSelected = true;
        }
        this.mTitle.setCustomTitle(getCallLogMultiSelectionTitle(), this.mActivityRef.mSelectedIds.size());
        if (this.mDeleteMenu != null) {
            this.mDeleteMenu.setEnabled(this.mIsAllSelected);
        }
        if (this.mSplitActionBarView != null) {
            this.mSplitActionBarView.setEnable(1, this.mIsAllSelected);
        }
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private String getCallLogMultiSelectionTitle() {
        if (this.mContext == null) {
            return null;
        }
        String title = this.mContext.getString(R.string.contacts_not_selected_text);
        if (!(this.mActivityRef == null || this.mActivityRef.mSelectedIds == null)) {
            title = CommonUtilMethods.getMultiSelectionTitle(this.mContext, this.mActivityRef.mSelectedIds.size());
        }
        return title;
    }

    private void doOperation() {
        HashSet<Long> lSelectedIds = this.mActivityRef.mSelectedIds;
        long[] ids = new long[lSelectedIds.size()];
        int i = 0;
        for (Long longValue : lSelectedIds) {
            int i2 = i + 1;
            ids[i] = longValue.longValue();
            i = i2;
        }
        DeleteCallLogDialog.show(getFragmentManager(), ids, this.mCallLogListView.getCheckedItemCount(), this.mIsAllSelected);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mAdapter != null) {
            this.mAdapter.stopRequestProcessing();
            this.mAdapter.destroyEmergencyNumberHelper();
            this.mAdapter.changeCursor(null);
        }
        if (this.mContext != null && this.mBroadcastReceiver != null) {
            this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        }
    }

    public void fetchCalls() {
        if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
            startCallsQuery();
        }
    }

    private void deselectAllItems() {
        int lListItemCount = this.mCallLogListView.getCount() - this.mCallLogListView.getFooterViewsCount();
        for (int i = 0; i < lListItemCount; i++) {
            this.mCallLogListView.setItemChecked(i, false);
        }
        this.mActivityRef.mSelectedIds.clear();
        this.mAdapter.notifyDataSetChanged();
    }

    private void selectAllItems() {
        int lListItemCount = this.mCallLogListView.getCount() - this.mCallLogListView.getFooterViewsCount();
        for (int i = 0; i < lListItemCount; i++) {
            this.mCallLogListView.setItemChecked(i, true);
        }
        HashSet<Long> lSelectedIds = this.mActivityRef.mSelectedIds;
        lSelectedIds.clear();
        lSelectedIds.addAll(this.mAllIdsList);
        this.mAdapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        SparseBooleanArray checkedItems = this.mCallLogListView.getCheckedItemPositions();
        boolean isSelectionChecked = false;
        if (checkedItems != null) {
            isSelectionChecked = checkedItems.get(position);
        }
        this.mCallLogListView.setItemChecked(position, isSelectionChecked);
        CallLogListItemViews views = (CallLogListItemViews) view.getTag();
        IntentProvider intentProvider = (IntentProvider) views.secondaryActionViewLayout.getTag();
        new long[1][0] = -1;
        if (intentProvider != null) {
            Intent lIntent = intentProvider.getIntent(getActivity());
            if (lIntent != null) {
                long[] ids = lIntent.getLongArrayExtra("EXTRA_CALL_LOG_IDS");
                if (ids != null && ids.length > 0) {
                    views.getCheckBox().setChecked(isSelectionChecked);
                    for (long lId : ids) {
                        if (isSelectionChecked) {
                            this.mActivityRef.mSelectedIds.add(Long.valueOf(lId));
                        } else {
                            this.mActivityRef.mSelectedIds.remove(Long.valueOf(lId));
                        }
                    }
                }
            }
        }
        int lListCount = this.mAllIdsList.size();
        boolean z = lListCount != 0 && lListCount == this.mActivityRef.mSelectedIds.size();
        this.mIsAllSelected = z;
        this.mTitle.setCustomTitle(getCallLogMultiSelectionTitle(), this.mActivityRef.mSelectedIds.size());
        getActivity().invalidateOptionsMenu();
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            selectAllItems();
        } else {
            deselectAllItems();
        }
        if (getActivity() != null) {
            getActivity().invalidateOptionsMenu();
        }
    }

    private void setSelectedItemsInListview(ArrayList<ArrayList<Long>> groupIdList) {
        HashSet<Integer> lSelectablePositionsInList = new HashSet();
        int lCursroPosition = 0;
        int lItemPositionInListView = -1;
        for (ArrayList<Long> array : groupIdList) {
            lItemPositionInListView++;
            HashSet<Long> lSelectedIds = this.mActivityRef.mSelectedIds;
            boolean lIsAlreadySelectedId = false;
            for (Long longValue : array) {
                if (lSelectedIds.contains(Long.valueOf(longValue.longValue()))) {
                    lIsAlreadySelectedId = true;
                    break;
                }
            }
            if (lIsAlreadySelectedId) {
                lSelectedIds.addAll(array);
                lSelectablePositionsInList.add(Integer.valueOf(lItemPositionInListView));
            }
            lCursroPosition += array.size();
        }
        if (HwLog.HWDBG) {
            HwLog.d("CallLogMultiSelectionFragment", "where loop end :" + lCursroPosition);
        }
        this.mAdapter.notifyDataSetChanged();
        if (HwLog.HWDBG) {
            HwLog.d("CallLogMultiSelectionFragment", "notifyDataSetChanged end and start setItemCheck loop");
        }
        for (int i = 0; i <= lItemPositionInListView; i++) {
            this.mCallLogListView.setItemChecked(i, lSelectablePositionsInList.contains(Integer.valueOf(i)));
        }
        if (HwLog.HWDBG) {
            HwLog.d("CallLogMultiSelectionFragment", "setItemCheck loop end!");
        }
        if (this.mFirstVisiblePosition != 0) {
            this.mCallLogListView.setSelection(this.mFirstVisiblePosition);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mAdapter != null) {
            this.mAdapter.updateCustSetting();
            this.mAdapter.updateSimStatesAndResources();
            this.mAdapter.notifyDataSetChanged();
        }
    }

    public void onVoicemailStatusFetched(Cursor statusCursor) {
    }

    public void onCallsFetched(Cursor cursor) {
        if (cursor != null) {
            Cursor lExtendedCursor = new ExtendedCursor(cursor, "section", Integer.valueOf(1));
            this.mAdapter.invalidateCache();
            this.mAdapter.changeCursor(lExtendedCursor);
            this.mAllIdsList.clear();
            if (cursor.moveToFirst()) {
                do {
                    this.mAllIdsList.add(Long.valueOf(cursor.getLong(0)));
                } while (cursor.moveToNext());
            }
            ArrayList<ArrayList<Long>> obj = cursor.getExtras().getSerializable("GROUP_ID_LIST");
            if (obj != null && (obj instanceof ArrayList)) {
                setSelectedItemsInListview(obj);
            }
            cursor.moveToFirst();
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
    }

    public void setCallTypeFilter(int callType) {
        this.mCallTypeFilter = callType;
    }

    public void setNetworkTypeFilter(int networkType) {
        this.mNetworkTypeFilter = networkType;
    }

    private void startCallsQuery() {
        this.mCallLogQueryHandler.setFilterMode(this.mNetworkTypeFilter, this.mCallTypeFilter);
        if (this.mCallTypeFilter == 6) {
            callLogNumbers();
        } else {
            this.mCallLogQueryHandler.fetchCalls(60, -1, this.mEmergencynumbers);
        }
    }

    private void callLogNumbers() {
        new AsyncTask<Void, Void, Void>() {
            public Void doInBackground(Void... params) {
                Cursor lCursor = CallLogMultiSelectionFragment.this.mContext.getContentResolver().query(Calls.CONTENT_URI, new String[]{"number"}, null, null, null);
                if (lCursor == null || lCursor.getCount() <= 0) {
                    if (lCursor != null) {
                        lCursor.close();
                    }
                    return null;
                }
                CallLogMultiSelectionFragment.this.mEmergencynumbers = new ArrayList();
                lCursor.moveToFirst();
                do {
                    if (!CallLogMultiSelectionFragment.this.mEmergencynumbers.contains(lCursor.getString(0)) && PhoneNumberUtils.isLocalEmergencyNumber(CallLogMultiSelectionFragment.this.mContext, lCursor.getString(0))) {
                        CallLogMultiSelectionFragment.this.mEmergencynumbers.add(lCursor.getString(0));
                    }
                } while (lCursor.moveToNext());
                if (lCursor != null) {
                    lCursor.close();
                }
                return null;
            }

            protected void onPostExecute(Void result) {
                CallLogMultiSelectionFragment.this.mCallLogQueryHandler.fetchCalls(60, -1, CallLogMultiSelectionFragment.this.mEmergencynumbers);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }
}
