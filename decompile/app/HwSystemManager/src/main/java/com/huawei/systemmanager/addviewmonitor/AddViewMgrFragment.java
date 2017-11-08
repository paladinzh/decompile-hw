package com.huawei.systemmanager.addviewmonitor;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.DimensionUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AddViewMgrFragment extends Fragment implements IAppChangeListener {
    private static final String TAG = "AddViewMgrFragment";
    private boolean isMeasured = false;
    private AddViewMonitorAdapter mAdapter;
    private View mAddView;
    private AddViewAppManager mAddViewAppManager;
    private List<AddViewAppInfo> mAppShowList = new ArrayList();
    private Context mContext = null;
    private MyDataLoaderTask mDataLoadingTask = null;
    private DefListener mExternalStorageListener = new DefListener() {
        public void onExternalChanged(String[] packages, boolean available) {
            if (packages != null && packages.length != 0) {
                new MyDataLoaderTask().execute(new Void[0]);
            }
        }
    };
    private int mHeight1;
    private int mHeight2;
    private OnItemClickListener mItemClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            AddViewAppInfo appInfo = (AddViewAppInfo) AddViewMgrFragment.this.mAppShowList.get(position);
            if (appInfo.mAddViewAllow) {
                appInfo.mAddViewAllow = false;
            } else {
                appInfo.mAddViewAllow = true;
            }
            AddViewMgrFragment.this.mAddViewAppManager.singleOpsChange(appInfo);
            AddViewMgrFragment.this.onListDataChanged();
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_PKG;
            strArr[1] = appInfo.mPkgName;
            strArr[2] = HsmStatConst.PARAM_OP;
            strArr[3] = appInfo.mAddViewAllow ? "1" : "0";
            HsmStat.statE((int) Events.E_ADDVIEW_SET, HsmStatConst.constructJsonParams(strArr));
        }
    };
    private Switch mMainSwitch;
    private OnCheckedChangeListener mMainSwitchListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String statParam = "";
            if (isChecked) {
                AddViewMgrFragment.this.doSelectAll();
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "1");
            } else {
                AddViewMgrFragment.this.doCancelAll();
                statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "0");
            }
            AddViewMgrFragment.this.onListDataChanged();
            if (!TextUtils.isEmpty(statParam)) {
                HsmStat.statE((int) Events.E_ADDVIEW_SET_ALL, statParam);
            }
        }
    };
    private View mNoAddviewAppLayout = null;
    private TextView mTipsTitleView;

    class MyDataLoaderTask extends AsyncTask<Void, Void, List<AddViewAppInfo>> {
        MyDataLoaderTask() {
        }

        protected List<AddViewAppInfo> doInBackground(Void... params) {
            return AddViewMgrFragment.this.mAddViewAppManager.initAddViewAppList();
        }

        protected void onPostExecute(List<AddViewAppInfo> result) {
            if (AddViewMgrFragment.this.mContext == null) {
                HwLog.w(AddViewMgrFragment.TAG, "onPostExecute: Invalid context");
            } else if (!isCancelled()) {
                AddViewMgrFragment.this.mAppShowList.clear();
                AddViewMgrFragment.this.mAppShowList.addAll(result);
                AddViewMgrFragment.this.reSortList();
                AddViewMgrFragment.this.onListDataChanged();
                AddViewMgrFragment.this.mDataLoadingTask = null;
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        HsmPackageManager.registerListener(this.mExternalStorageListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        View fragmentView = inflater.inflate(R.layout.addview_app_fragment, container, false);
        initFragment(fragmentView);
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                if (!AddViewMgrFragment.this.isMeasured) {
                    AddViewMgrFragment.this.mHeight1 = DimensionUtils.getAreaOne(AddViewMgrFragment.this.getActivity());
                    AddViewMgrFragment.this.mHeight2 = DimensionUtils.getAreaThree(AddViewMgrFragment.this.getActivity());
                    AddViewMgrFragment.this.isMeasured = true;
                    LayoutParams layoutParams = (LayoutParams) AddViewMgrFragment.this.mNoAddviewAppLayout.getLayoutParams();
                    layoutParams.topMargin = ((int) (((double) AddViewMgrFragment.this.mHeight1) * 0.3d)) - (AddViewMgrFragment.this.mHeight1 - AddViewMgrFragment.this.mHeight2);
                    AddViewMgrFragment.this.mNoAddviewAppLayout.setLayoutParams(layoutParams);
                }
                return true;
            }
        });
        return fragmentView;
    }

    private void initFragment(View fragmentView) {
        this.mTipsTitleView = (TextView) fragmentView.findViewById(R.id.addview_explain_textview);
        this.mAddView = fragmentView.findViewById(R.id.addview_app);
        this.mMainSwitch = (Switch) fragmentView.findViewById(R.id.main_switcher);
        this.mMainSwitch.setOnCheckedChangeListener(this.mMainSwitchListener);
        this.mAdapter = new AddViewMonitorAdapter(this.mContext);
        ListView appListView = (ListView) fragmentView.findViewById(R.id.addview_app_list);
        appListView.setOnItemClickListener(this.mItemClickListener);
        appListView.setAdapter(this.mAdapter);
        this.mAddViewAppManager = AddViewAppManager.getInstance(this.mContext);
        this.mAddViewAppManager.registerListener(this);
        this.mNoAddviewAppLayout = fragmentView.findViewById(R.id.no_addview_app_layout);
    }

    public void onResume() {
        super.onResume();
        if (this.mDataLoadingTask == null) {
            this.mDataLoadingTask = new MyDataLoaderTask();
            this.mDataLoadingTask.execute(new Void[0]);
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        HsmPackageManager.unregisterListener(this.mExternalStorageListener);
        this.mAddViewAppManager.unregisterListener(this);
        this.mContext = null;
        if (this.mDataLoadingTask != null) {
            this.mDataLoadingTask.cancel(false);
            this.mDataLoadingTask = null;
        }
        super.onDestroy();
    }

    private void onListDataChanged() {
        notifyAdapter();
        updateCheckedNumber();
        checkEmptyView();
    }

    private void updateCheckedNumber() {
        int number = 0;
        for (AddViewAppInfo info : this.mAppShowList) {
            if (info.mAddViewAllow) {
                number++;
            }
        }
        this.mMainSwitch.setOnCheckedChangeListener(null);
        if (number == this.mAppShowList.size()) {
            this.mMainSwitch.setChecked(true);
        } else {
            this.mMainSwitch.setChecked(false);
        }
        this.mMainSwitch.setOnCheckedChangeListener(this.mMainSwitchListener);
        this.mTipsTitleView.setText(this.mContext.getResources().getQuantityString(R.plurals.addview_app_explain, number, new Object[]{Integer.valueOf(number)}));
    }

    private void notifyAdapter() {
        if (this.mAdapter != null) {
            this.mAdapter.setData(this.mAppShowList);
        }
    }

    private void reSortList() {
        Collections.sort(this.mAppShowList, AddViewAppInfo.ADDVIEW_APP_ALP_COMPARATOR);
    }

    private void checkEmptyView() {
        int i;
        int i2 = 0;
        boolean isShowNoView = this.mAppShowList.size() == 0;
        View view = this.mAddView;
        if (isShowNoView) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        View view2 = this.mNoAddviewAppLayout;
        if (!isShowNoView) {
            i2 = 8;
        }
        view2.setVisibility(i2);
        getActivity().invalidateOptionsMenu();
    }

    private void doSelectAll() {
        List<AddViewAppInfo> appChangeList = new ArrayList();
        for (AddViewAppInfo info : this.mAppShowList) {
            if (!info.mAddViewAllow) {
                info.mAddViewAllow = true;
                appChangeList.add(new AddViewAppInfo(info));
            }
        }
        this.mAddViewAppManager.listOpsChange(appChangeList);
    }

    private void doCancelAll() {
        List<AddViewAppInfo> appChangeList = new ArrayList();
        for (AddViewAppInfo info : this.mAppShowList) {
            if (info.mAddViewAllow) {
                info.mAddViewAllow = false;
                appChangeList.add(new AddViewAppInfo(info));
            }
        }
        this.mAddViewAppManager.listOpsChange(appChangeList);
    }

    public void onPackageAdded(String pkgName, AddViewAppInfo appInfo) {
        if (pkgName != null) {
            this.mAppShowList.add(new AddViewAppInfo(appInfo));
            onListDataChanged();
        }
    }

    public void onPackageRemoved(String pkgName) {
        if (pkgName != null) {
            for (AddViewAppInfo info : this.mAppShowList) {
                if (pkgName.equals(info.mPkgName)) {
                    this.mAppShowList.remove(info);
                    break;
                }
            }
            onListDataChanged();
        }
    }
}
