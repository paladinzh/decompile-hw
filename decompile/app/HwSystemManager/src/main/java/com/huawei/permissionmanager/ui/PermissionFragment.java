package com.huawei.permissionmanager.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ProgressBar;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.ui.permissionlist.GeneralItem;
import com.huawei.permissionmanager.ui.permissionlist.LabelItem;
import com.huawei.permissionmanager.ui.permissionlist.PermItem;
import com.huawei.permissionmanager.ui.permissionlist.PermissionAdapter;
import com.huawei.permissionmanager.ui.permissionlist.PermissionAdapter.Position;
import com.huawei.permissionmanager.utils.IRecommendChangeListener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.addviewmonitor.AddViewAppManager;
import com.huawei.systemmanager.comm.component.ListItem;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import java.util.ArrayList;
import java.util.List;

public class PermissionFragment extends PermissionBaseFragment {
    private static final long DELAY_SELECT_KEY_ITEM = 0;
    private String LOG_TAG = "PermissionFragment";
    private int mAddViewCount = 0;
    private OnChildClickListener mChildClicker = new OnChildClickListener() {
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            PermissionFragment.this.handlerClick((ListItem) v.getTag(R.id.convertview_tag_item));
            return false;
        }
    };
    private DefListener mExternalStorageListener = new DefListener() {
        public void onExternalChanged(String[] packages, boolean available) {
            if (packages != null && packages.length != 0) {
                new AsynctaskUpdateDB().execute(new Void[0]);
            }
        }

        public void onPackageRemoved(String pkgName) {
            HwLog.i(PermissionFragment.this.LOG_TAG, "onPackageRemoved : " + pkgName);
            new AsynctaskUpdateDB().execute(new Void[0]);
        }

        public void onPackagedAdded(String pkgName) {
            HwLog.i(PermissionFragment.this.LOG_TAG, "onPackagedAdded : " + pkgName);
            new AsynctaskUpdateDB().execute(new Void[0]);
        }

        public void onPackageChanged(String pkgName) {
            HwLog.i(PermissionFragment.this.LOG_TAG, "onPackageChanged : " + pkgName);
            new AsynctaskUpdateDB().execute(new Void[0]);
        }
    };
    private View mFragmentView = null;
    private OnGroupClickListener mGroupClicker = new OnGroupClickListener() {
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            ListItem item = (ListItem) v.getTag(R.id.convertview_tag_item);
            if (item instanceof LabelItem) {
                return true;
            }
            PermissionFragment.this.handlerClick(item);
            return false;
        }
    };
    private Handler mHandler = new Handler();
    private int mHeightOffSet = 0;
    private LayoutInflater mInflater = null;
    private String mKeyItem;
    private int mListViewPos = 0;
    private PermissionTableManager mPerManager = null;
    private PermissionAdapter mPermissionAdapter = null;
    private ExpandableListView mPermissionListView = null;
    private List<Permission> mPermissionTable = null;
    private ProgressBar mProgressBar = null;
    private IRecommendChangeListener mRecommendChangeListener = new IRecommendChangeListener() {
        public void onApplicationFragmentRecommendAppsChange() {
            PermissionFragment.this.removeRecommendHeader();
        }

        public void onPermissionFragmentRecommendAppsChange() {
        }

        public String toString() {
            return "PermissionFragment";
        }
    };
    private OnScrollListener mScrollListener = new OnScrollListener() {
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0) {
                PermissionFragment.this.mListViewPos = PermissionFragment.this.mPermissionListView.getFirstVisiblePosition();
                View topView = PermissionFragment.this.mPermissionListView.getChildAt(0);
                if (topView != null) {
                    PermissionFragment.this.mHeightOffSet = topView.getTop();
                }
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };
    private Runnable mSelectListRunnable = new Runnable() {
        public void run() {
            if (PermissionFragment.this.mPermissionListView != null && PermissionFragment.this.mPermissionAdapter != null) {
                Position position = PermissionFragment.this.mPermissionAdapter.findPositionByKey(PermissionFragment.this.mKeyItem);
                try {
                    if (position.mGroupPos > 2) {
                        if (position.mChildPos < 0) {
                            PermissionFragment.this.mPermissionListView.setSelectedGroup(position.mGroupPos);
                        } else {
                            PermissionFragment.this.mPermissionListView.setSelectedChild(position.mGroupPos, position.mChildPos, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private class AsynctaskUpdateDB extends AsyncTask<Void, Void, List<AppInfoWrapper>> {
        private AsynctaskUpdateDB() {
        }

        protected List<AppInfoWrapper> doInBackground(Void... params) {
            HwLog.i(PermissionFragment.this.LOG_TAG, "permissionList display Asynctask doInBackground start");
            PermissionFragment.this.mAddViewCount = AddViewAppManager.getInstance(PermissionFragment.this.mContext).initAddViewAppList().size();
            DBAdapter.getInstance(PermissionFragment.this.mContext).getPermissionCounts(PermissionFragment.this.mPermissionTable);
            return AppInfoWrapper.updatePureAppInfoWrapperList(PermissionFragment.this.mContext, "permission fragment", false);
        }

        protected void onPostExecute(List<AppInfoWrapper> result) {
            super.onPostExecute(result);
            HwLog.i(PermissionFragment.this.LOG_TAG, "permissionList display Asynctask onPostExecute start");
            PermissionFragment.this.mPermissonAppsList.clear();
            PermissionFragment.this.mPermissonAppsList.addAll(result);
            PermissionFragment.this.updateRecommendUI();
            PermissionFragment.this.updateUI();
            PermissionFragment.this.showLoadingProcess(Boolean.valueOf(false));
            HwLog.i(PermissionFragment.this.LOG_TAG, "permissionList display Asynctask onPostExecute end");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HsmPackageManager.registerListener(this.mExternalStorageListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        this.mInflater = inflater;
        this.mFragmentView = this.mInflater.inflate(R.layout.permission_list_tab, container, false);
        init();
        return this.mFragmentView;
    }

    public void onResume() {
        super.onResume();
        new AsynctaskUpdateDB().execute(new Void[0]);
    }

    private void init() {
        this.mPermissionListView = (ExpandableListView) this.mFragmentView.findViewById(16908298);
        this.mPermissionListView.setOnScrollListener(this.mScrollListener);
        this.mPermissionListView.setOnGroupClickListener(this.mGroupClicker);
        this.mPermissionListView.setOnChildClickListener(this.mChildClicker);
        View footerView = this.mInflater.inflate(R.layout.blank_footer_view, this.mPermissionListView, false);
        this.mPermissionListView.setFooterDividersEnabled(false);
        this.mPermissionListView.addFooterView(footerView, null, false);
        this.mProgressBar = (ProgressBar) this.mFragmentView.findViewById(R.id.loading_progressbar);
        this.mPerManager = PermissionTableManager.getInstance(this.mContext.getApplicationContext());
        this.mPermissionTable = this.mPerManager.getPermissionTable();
        this.mPermissonAppsList = new ArrayList();
        this.mRecommendManager = RecommendManager.getInstance();
        this.mRecommendManager.registerListener(this.mRecommendChangeListener);
        showLoadingProcess(Boolean.valueOf(true));
    }

    private void showLoadingProcess(Boolean bShow) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setVisibility(bShow.booleanValue() ? 0 : 8);
        }
    }

    public void onDestroy() {
        if (this.mRecommendManager != null) {
            this.mRecommendManager.unregisterListener(this.mRecommendChangeListener);
        }
        super.onDestroy();
        HsmPackageManager.unregisterListener(this.mExternalStorageListener);
    }

    private void updateUI() {
        if (this.mPermissionAdapter == null) {
            this.mPermissionAdapter = new PermissionAdapter(this.mContext);
            this.mPermissionAdapter.initData(this.mPerManager, this.mAddViewCount);
            this.mPermissionListView.setAdapter(this.mPermissionAdapter);
            for (Integer intValue : this.mPermissionAdapter.getExpandablePos()) {
                this.mPermissionListView.expandGroup(intValue.intValue());
            }
            scrollToKeyItem();
        } else {
            this.mPermissionAdapter.changeAddViewCount(this.mAddViewCount);
            this.mPermissionAdapter.notifyDataSetChanged();
        }
        this.mPermissionListView.setSelectionFromTop(this.mListViewPos, this.mHeightOffSet);
    }

    private void updateRecommendUI() {
        updateRecommendAppsInfo();
    }

    private void handlerClick(ListItem item) {
        if (item != null) {
            if (item instanceof GeneralItem) {
                startActiviySafe(((GeneralItem) item).getIntent(this.mContext));
            } else if (item instanceof PermItem) {
                HsmStat.statPerssmisonSelectAction(((PermItem) item).getKey());
                startActiviySafe(((PermItem) item).getIntent(this.mContext));
            }
        }
    }

    private void startActiviySafe(Intent intent) {
        if (intent == null) {
            HwLog.e(this.LOG_TAG, "startActiviySafe, intent is null!");
            return;
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void removeRecommendHeader() {
    }

    protected void updateUIAfterRecommendOperation(List<AppInfoWrapper> result) {
        removeRecommendHeader();
        if (this.mRecommendManager != null) {
            this.mRecommendManager.permissionFragmentChange();
        }
        this.mPermissonAppsList.clear();
        this.mPermissonAppsList.addAll(result);
        updateUI();
    }

    private void scrollToKeyItem() {
        Activity ac = getActivity();
        if (ac != null && (ac instanceof MainActivity)) {
            this.mKeyItem = ((MainActivity) ac).getSelectItemKey();
        }
        if (!TextUtils.isEmpty(this.mKeyItem)) {
            this.mHandler.postDelayed(this.mSelectListRunnable, 0);
        }
    }
}
