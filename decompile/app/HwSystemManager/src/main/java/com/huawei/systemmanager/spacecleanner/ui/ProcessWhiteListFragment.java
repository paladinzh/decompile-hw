package com.huawei.systemmanager.spacecleanner.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.component.SelectListFragment;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.CommonSwitchController;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.optimize.process.AbsProtectAppControl;
import com.huawei.systemmanager.optimize.process.IDataChangedListener;
import com.huawei.systemmanager.optimize.process.IDataChangedListener.SimpleDataChangeListenr;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.optimize.process.ProtectAppItem;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProcessWhiteListFragment extends SelectListFragment<ProcessWhiteListItem> implements MessageHandler, OnCheckedChangeListener {
    private static final int MSG_PACKAGE_ADD = 4;
    private static final int MSG_PACKAGE_REMOVE = 5;
    private static final int MSG_START_TO_LOAD_TASK = 6;
    private static final String TAG = "ProcessWhiteListFragment";
    private IDataChangedListener mDataSetChangeListener = new SimpleDataChangeListenr() {
        public void onPackageAdded(String pkgName, boolean protect) {
            HwLog.i(ProcessWhiteListFragment.TAG, "onPackageAdded, pkgName:" + pkgName);
            HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
            if (info == null) {
                HwLog.i(ProcessWhiteListFragment.TAG, "can not find pkg:" + pkgName);
                return;
            }
            ProcessWhiteListFragment.this.mHandler.obtainMessage(4, new ProcessWhiteListItem(info, protect, SavingSettingUtil.getRogueAppSet(ProcessWhiteListFragment.this.getApplicationContext()).contains(pkgName))).sendToTarget();
        }

        public void onPackageRemoved(String pkgName) {
            HwLog.i(ProcessWhiteListFragment.TAG, "onPackageRemoved, pkgName:" + pkgName);
            if (!TextUtils.isEmpty(pkgName)) {
                ProcessWhiteListFragment.this.mHandler.obtainMessage(5, pkgName).sendToTarget();
            }
        }

        public void onProtectedAppRefresh() {
            HwLog.i(ProcessWhiteListFragment.TAG, "onProtectedAppRefresh");
            ProcessWhiteListFragment.this.mHandler.removeMessages(6);
            ProcessWhiteListFragment.this.mHandler.sendEmptyMessage(6);
        }
    };
    private GridView mGridView = null;
    private Handler mHandler = new GenericHandler(this);
    private final HsmSingleExecutor mLoadDataExecutor = new HsmSingleExecutor();
    private DataLoaderTask mLoadTask;
    private CommonSwitchController mMainSwitch;
    private OnCheckedChangeListener mOnCheckedChangeListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            ProcessWhiteListItem item = (ProcessWhiteListItem) buttonView.getTag();
            if (item == null || ProcessWhiteListFragment.this.mProtectAppControl == null) {
                HwLog.e(ProcessWhiteListFragment.TAG, "onCheckedChanged, but item is null!");
                return;
            }
            if (item.isProtect()) {
                ProcessWhiteListFragment.this.mProtectAppControl.setProtect(item, false);
            } else {
                ProcessWhiteListFragment.this.mProtectAppControl.setProtect(item, true);
            }
            ProcessWhiteListFragment.this.updateSelectState();
            buttonView.sendAccessibilityEvent(8);
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_PKG;
            strArr[1] = item.getPackageName();
            strArr[2] = HsmStatConst.PARAM_OP;
            strArr[3] = item.isProtect() ? "1" : "0";
            HsmStat.statE((int) Events.E_PROTECTED_APP_SET, HsmStatConst.constructJsonParams(strArr));
        }
    };
    private List<ProcessWhiteListItem> mProcessWhiteList = Lists.newArrayList();
    private volatile ProtectAppControl mProtectAppControl;
    private ScrollView mScrollView;
    private TextView mTipsView;

    class DataLoaderTask extends AsyncTask<Void, Void, List<ProcessWhiteListItem>> {
        DataLoaderTask() {
        }

        protected List<ProcessWhiteListItem> doInBackground(Void... params) {
            if (ProcessWhiteListFragment.this.mProtectAppControl == null) {
                ProcessWhiteListFragment.this.mProtectAppControl = ProtectAppControl.getInstance(ProcessWhiteListFragment.this.getApplicationContext());
            }
            if (isCancelled()) {
                HwLog.i(ProcessWhiteListFragment.TAG, "MyDataLoaderTask doInBackground is canceled");
                return null;
            }
            List<ProtectAppItem> originData = ProcessWhiteListFragment.this.mProtectAppControl.getProtectAppItems(false);
            List<ProcessWhiteListItem> result = Lists.newArrayListWithCapacity(originData.size());
            for (ProtectAppItem item : originData) {
                item.getIcon();
                item.getName();
                result.add(new ProcessWhiteListItem(item));
            }
            Collections.sort(result, ProtectAppItem.WHITE_LIST_COMPARATOR);
            HwLog.i(ProcessWhiteListFragment.TAG, "load data complete, size:" + result.size());
            return result;
        }

        protected void onPostExecute(List<ProcessWhiteListItem> result) {
            if (isCancelled() || result == null) {
                HwLog.i(ProcessWhiteListFragment.TAG, "MyDataLoaderTask doInBackground is canceled");
                return;
            }
            ProcessWhiteListFragment.this.mProcessWhiteList.clear();
            ProcessWhiteListFragment.this.mProcessWhiteList.addAll(result);
            if (!(ProcessWhiteListFragment.this.isDetached() || ProcessWhiteListFragment.this.getActivity() == null)) {
                ProcessWhiteListFragment.this.initScreenOrientation(ProcessWhiteListFragment.this.getResources().getConfiguration());
                ProcessWhiteListFragment.this.swapAdapterData(result);
            }
        }
    }

    public static class ProcessWhiteListActivity extends SingleFragmentActivity {
        protected Fragment buildFragment() {
            return new ProcessWhiteListFragment();
        }

        protected boolean useHsmActivityHelper() {
            return false;
        }
    }

    public static class ProcessWhiteListItem extends ProtectAppItem {
        public ProcessWhiteListItem(ProtectAppItem item) {
            super(item.getPkgInfo());
            this.mProtected = item.isProtect();
            this.mHighPower = item.isHighPower();
        }

        public ProcessWhiteListItem(HsmPkgInfo info, boolean protect, boolean highPower) {
            super(info, protect, highPower);
        }

        public boolean isChecked() {
            return isProtect();
        }

        public void setChecked(boolean checked) {
            setProtect(checked);
        }
    }

    private static class ViewHolder {
        ImageView mIcon;
        TextView mLabel;
        ImageView mLockIcon;
        TextView mStatus;
        Switch mSwitch;

        private ViewHolder() {
        }
    }

    private class WhiteListAdapter extends CommonAdapter<ProcessWhiteListItem> {
        private final int mTitleMaxWidth;

        public WhiteListAdapter(Context context, LayoutInflater inflater) {
            super(context, inflater);
            this.mTitleMaxWidth = ProcessWhiteListFragment.this.getResourcesEx().getDimensionPixelSize(R.dimen.phone_optimize_protecte_item_title_maxwidth);
        }

        protected View newView(int position, ViewGroup parent, ProcessWhiteListItem item) {
            View convertView = this.mInflater.inflate(R.layout.spaceclean_process_white_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.mIcon = (ImageView) convertView.findViewById(R.id.white_app_item_icon);
            holder.mLabel = (TextView) convertView.findViewById(R.id.white_app_item_name);
            holder.mStatus = (TextView) convertView.findViewById(R.id.sub_title);
            holder.mSwitch = (Switch) convertView.findViewById(R.id.switcher);
            holder.mLockIcon = (ImageView) convertView.findViewById(R.id.lock_icon);
            convertView.setTag(holder);
            holder.mLabel.setMaxWidth(this.mTitleMaxWidth);
            return convertView;
        }

        protected void bindView(int position, View view, ProcessWhiteListItem item) {
            int i;
            ViewHolder holder = (ViewHolder) view.getTag();
            String label = item.getName();
            holder.mIcon.setImageDrawable(item.getIcon());
            holder.mLabel.setText(label);
            holder.mSwitch.setOnCheckedChangeListener(null);
            holder.mSwitch.setChecked(item.isProtect());
            holder.mSwitch.setTag(item);
            holder.mSwitch.setOnCheckedChangeListener(ProcessWhiteListFragment.this.mOnCheckedChangeListener);
            boolean protect = item.isProtect();
            holder.mLockIcon.setVisibility(protect ? 0 : 4);
            TextView textView = holder.mStatus;
            if (protect) {
                i = R.string.space_clean_whitelist_select;
            } else {
                i = R.string.space_clean_whitelist_unselect;
            }
            textView.setText(i);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onResume() {
        super.onResume();
        startLoadData();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setListAdapter(new WhiteListAdapter(getApplicationContext(), inflater));
        return inflater.inflate(R.layout.spaceclean_process_whitelist, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        boolean isLand = true;
        super.onViewCreated(view, savedInstanceState);
        this.mTipsView = (TextView) view.findViewById(R.id.tips);
        RelativeLayout rlAllSwitcher = (RelativeLayout) view.findViewById(R.id.rl_all_switcher);
        this.mMainSwitch = new CommonSwitchController(rlAllSwitcher, (Switch) rlAllSwitcher.findViewById(R.id.main_switcher), true);
        this.mMainSwitch.setOnCheckedChangeListener(this);
        AbsProtectAppControl.registerListener(this.mDataSetChangeListener);
        this.mScrollView = (ScrollView) view.findViewById(R.id.list_scroll_view);
        this.mScrollView.setOverScrollMode(2);
        this.mGridView = (GridView) view.findViewById(R.id.content_list);
        if (2 != getResources().getConfiguration().orientation) {
            isLand = false;
        }
        if (isLand) {
            initScreenOrientation(getResources().getConfiguration());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initScreenOrientation(newConfig);
    }

    private void initScreenOrientation(Configuration newConfig) {
        if (!(2 == newConfig.orientation) || this.mProcessWhiteList.size() <= 1) {
            this.mGridView.setNumColumns(1);
        } else {
            this.mGridView.setNumColumns(2);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        AbsProtectAppControl.unregisterListener(this.mDataSetChangeListener);
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Switch sw = (Switch) v.findViewById(R.id.switcher);
        if (sw != null) {
            sw.performClick();
        }
    }

    private void doSelectAll(boolean isProtect) {
        if (this.mProtectAppControl == null) {
            HwLog.i(TAG, "doSelectAll, mProtectAppControl is null");
            return;
        }
        List<ProcessWhiteListItem> data = getData();
        List<String> pkgList = Lists.newArrayList();
        for (ProcessWhiteListItem info : data) {
            if ((info.isProtect() ^ isProtect) != 0) {
                pkgList.add(info.getPackageName());
                info.setProtect(isProtect);
            }
        }
        if (isProtect) {
            this.mProtectAppControl.setProtect(pkgList);
        } else {
            this.mProtectAppControl.setNoProtect(pkgList);
        }
        updateSelectState();
    }

    protected void onCheckNumChanged(int allNum, int checkedAbleNum, int checkedNum, boolean allChecked) {
        if (this.mTipsView != null) {
            this.mTipsView.setText(getResourcesEx().getQuantityString(R.plurals.space_clean_white_list_title_change, checkedNum, new Object[]{Integer.valueOf(checkedNum)}));
        }
        updateMainSwitch(allChecked);
    }

    public void updateEmptyView() {
        super.updateEmptyView();
    }

    private void updateMainSwitch(boolean allChecked) {
        if (this.mMainSwitch == null) {
            HwLog.e(TAG, "updateMainSwitch, mMainSwitch == null");
        } else {
            this.mMainSwitch.updateCheckState(allChecked);
        }
    }

    public void onHandleMessage(Message msg) {
        List<ProcessWhiteListItem> dataList;
        switch (msg.what) {
            case 4:
                ProcessWhiteListItem item = msg.obj;
                dataList = Lists.newArrayList(getData());
                dataList.add(item);
                swapAdapterData(dataList);
                return;
            case 5:
                String pkgName = msg.obj;
                dataList = Lists.newArrayList(getData());
                Iterator<ProcessWhiteListItem> it = dataList.iterator();
                while (it.hasNext()) {
                    if (Objects.equal(pkgName, ((ProcessWhiteListItem) it.next()).getPackageName())) {
                        it.remove();
                        swapAdapterData(dataList);
                        return;
                    }
                }
                swapAdapterData(dataList);
                return;
            case 6:
                startLoadData();
                return;
            default:
                return;
        }
    }

    private void startLoadData() {
        HwLog.i(TAG, "start to LoadData");
        if (this.mLoadTask != null) {
            this.mLoadTask.cancel(true);
        }
        this.mLoadTask = new DataLoaderTask();
        this.mLoadTask.executeOnExecutor(this.mLoadDataExecutor, new Void[0]);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String str;
        if (isChecked) {
            HwLog.i(TAG, "User click allow all");
            doSelectAll(true);
        } else {
            HwLog.i(TAG, "User click restrict all");
            doSelectAll(false);
        }
        if (isChecked) {
            str = "1";
        } else {
            str = "0";
        }
        SpaceStatsUtils.reportMemoryAcceleratorListSelectAllOp(str);
    }
}
