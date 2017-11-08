package com.huawei.notificationmanager.ui;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.notificationmanager.common.ConstValues;
import com.huawei.notificationmanager.db.DBAdapter;
import com.huawei.notificationmanager.util.Const;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotificationCenterFragment extends Fragment {
    private static final String TAG = "NotificationCenterFragment";
    private List<NotificationCfgInfo> mAppCfgList = null;
    private ListView mAppListView = null;
    private AppListAdapter mApplistAdapter;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                HwLog.d(NotificationCenterFragment.TAG, "context:" + context + " intent:" + intent);
                return;
            }
            String action = intent.getAction();
            if (ConstValues.CFG_CHANGE_INTENT.equals(action) || ConstValues.CFG_CHANGE_BACKGROUND_INTENT.equals(action)) {
                NotificationCenterFragment.this.mCfgChange = Boolean.valueOf(intent.getBooleanExtra(ConstValues.CFG_CHANGE_VALUE, NotificationCenterFragment.this.mCfgChange.booleanValue()));
            }
        }
    };
    private Boolean mCfgChange = Boolean.valueOf(false);
    private OnDataLoadedListener mDataLoadedListener;
    private DefListener mExternalStorageListener = new DefListener() {
        public void onExternalChanged(String[] packages, boolean available) {
            if (packages != null && packages.length != 0) {
                NotificationCenterFragment.this.refurbishAppList();
            }
        }
    };
    private View mFragmentView;
    private LayoutInflater mInflater;
    private Boolean mIsOnFirstResume = Boolean.valueOf(true);
    private Boolean mIsOnPause = Boolean.valueOf(false);
    private OnItemClickListener mItemListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            NotificationCfgInfo cfgInfo = (NotificationCfgInfo) NotificationCenterFragment.this.mAppCfgList.get(position);
            if (cfgInfo == null) {
                HwLog.e(NotificationCenterFragment.TAG, "startActivityforSettings: Invalid config info");
            } else {
                NotificationCenterFragment.this.startActivityforSettings(cfgInfo);
            }
        }
    };
    private RuleLoadingTask mLoadingTask = null;
    private ProgressBar mProgressBar = null;

    class AppListAdapter extends BaseAdapter {
        public int getCount() {
            return NotificationCenterFragment.this.mAppCfgList.size();
        }

        public Object getItem(int position) {
            return NotificationCenterFragment.this.mAppCfgList.get(position);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            NotificationCfgInfo appCfgInfo = (NotificationCfgInfo) NotificationCenterFragment.this.mAppCfgList.get(position);
            if (appCfgInfo == null) {
                return null;
            }
            ListItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = NotificationCenterFragment.this.mInflater.inflate(R.layout.common_list_item_twolines_image_arrow, null);
                viewHolder = new ListItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListItemViewHolder) convertView.getTag();
            }
            setAppViewContent(viewHolder, appCfgInfo);
            return convertView;
        }

        private void setAppViewContent(ListItemViewHolder holder, NotificationCfgInfo appCfgInfo) {
            holder.appIcon.setImageDrawable(appCfgInfo.mAppIcon);
            holder.tvAppLabel.setText(appCfgInfo.mAppLabel);
            String statusValues = NotificationCenterFragment.this.getNotificationStatusString(appCfgInfo);
            if (TextUtils.isEmpty(statusValues)) {
                holder.tvNotificationStatusCfg.setVisibility(8);
                return;
            }
            holder.tvNotificationStatusCfg.setText(statusValues);
            holder.tvNotificationStatusCfg.setSingleLine(false);
            holder.tvNotificationStatusCfg.setMaxLines(2);
            holder.tvNotificationStatusCfg.setVisibility(0);
        }
    }

    private static class ListItemViewHolder {
        ImageView appIcon = null;
        TextView tvAppLabel = null;
        TextView tvNotificationStatusCfg = null;

        public ListItemViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.image);
            this.tvAppLabel = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
            this.tvNotificationStatusCfg = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
        }
    }

    protected interface OnDataLoadedListener {
        void doLoaded(Bundle bundle);

        void refresh(Bundle bundle);
    }

    private class RuleLoadingTask extends AsyncTask<Void, Void, List<NotificationCfgInfo>> {
        private boolean mIsLoadonReset = false;

        public RuleLoadingTask(boolean isLoadOnReset) {
            this.mIsLoadonReset = isLoadOnReset;
        }

        protected List<NotificationCfgInfo> doInBackground(Void... voidParams) {
            try {
                return new DBAdapter(NotificationCenterFragment.this.getActivity()).getCfgList();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List<NotificationCfgInfo> appCfgList) {
            if (isCancelled()) {
                HwLog.w(NotificationCenterFragment.TAG, "onPostExecute, The task is canceled");
                return;
            }
            if (NotificationCenterFragment.this.mAppCfgList == null) {
                NotificationCenterFragment.this.mAppCfgList = new ArrayList();
            }
            if (appCfgList == null) {
                HwLog.w(NotificationCenterFragment.TAG, "Fail to load notification rule list");
            } else {
                NotificationCenterFragment.this.mAppCfgList.clear();
                if (appCfgList.size() > 0) {
                    if (Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) {
                        appCfgList = NotificationCenterFragment.this.removeTelePkgName(appCfgList);
                    }
                    NotificationCenterFragment.this.mAppCfgList.addAll(appCfgList);
                }
                if (!this.mIsLoadonReset) {
                    Helper.setCfgChangeFlag(NotificationCenterFragment.this.getActivity(), false);
                }
            }
            NotificationCenterFragment.this.notifyAppListAdapter();
            NotificationCenterFragment.this.mLoadingTask = null;
            NotificationCenterFragment.this.mIsOnFirstResume = Boolean.valueOf(false);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        HsmPackageManager.registerListener(this.mExternalStorageListener);
        super.onCreate(savedInstanceState);
        IntentFilter cfgStateReceiver = new IntentFilter();
        cfgStateReceiver.addAction(ConstValues.CFG_CHANGE_INTENT);
        cfgStateReceiver.addAction(ConstValues.CFG_CHANGE_BACKGROUND_INTENT);
        getActivity().registerReceiver(this.mBroadcastReceiver, cfgStateReceiver, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        this.mFragmentView = this.mInflater.inflate(R.layout.notification_main_fragment, container, false);
        initView();
        return this.mFragmentView;
    }

    private void initView() {
        this.mAppListView = (ListView) this.mFragmentView.findViewById(R.id.app_rule_list_view);
        this.mAppListView.setItemsCanFocus(false);
        this.mProgressBar = (ProgressBar) this.mFragmentView.findViewById(R.id.rule_loading_progressbar);
        this.mAppCfgList = new ArrayList();
        this.mApplistAdapter = new AppListAdapter();
        this.mAppListView.setAdapter(this.mApplistAdapter);
        this.mAppListView.setOnItemClickListener(this.mItemListener);
    }

    private void startActivityforSettings(NotificationCfgInfo cfgInfo) {
        Intent intent = new Intent();
        intent.putExtra("packageName", cfgInfo.mPkgName);
        intent.putExtra(Const.KEY_LABEL, cfgInfo.mAppLabel);
        intent.setClass(getActivity(), NotificationSettingsActivity.class);
        getActivity().startActivityAsUser(intent, new UserHandle(UserHandle.getUserId(cfgInfo.mUid)));
    }

    protected void refurbishAppList() {
        if (this.mLoadingTask == null) {
            showLoadingProcess(Boolean.valueOf(true));
            this.mLoadingTask = new RuleLoadingTask(false);
            this.mLoadingTask.execute(new Void[0]);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mIsOnFirstResume.booleanValue() || ((this.mIsOnPause.booleanValue() && Helper.getCfgChangeFlag(getActivity())) || (this.mIsOnPause.booleanValue() && this.mCfgChange.booleanValue()))) {
            refurbishAppList();
        } else {
            notifyAppListAdapter();
        }
        this.mIsOnPause = Boolean.valueOf(false);
    }

    public void onPause() {
        this.mIsOnPause = Boolean.valueOf(true);
        super.onPause();
    }

    public void onDestroy() {
        if (this.mBroadcastReceiver != null) {
            getActivity().unregisterReceiver(this.mBroadcastReceiver);
        }
        this.mBroadcastReceiver = null;
        if (this.mLoadingTask != null) {
            this.mLoadingTask.cancel(false);
            this.mLoadingTask = null;
        }
        HsmPackageManager.unregisterListener(this.mExternalStorageListener);
        super.onDestroy();
    }

    private void notifyAppListAdapter() {
        showLoadingProcess(Boolean.valueOf(false));
        this.mApplistAdapter.notifyDataSetChanged();
    }

    private void showLoadingProcess(Boolean bShow) {
        if (this.mProgressBar != null) {
            int i;
            ProgressBar progressBar = this.mProgressBar;
            if (bShow.booleanValue()) {
                i = 0;
            } else {
                i = 8;
            }
            progressBar.setVisibility(i);
        }
    }

    private String getNotificationStatusString(NotificationCfgInfo appCfgInfo) {
        String notificationStatusString = "";
        if (appCfgInfo == null) {
            return notificationStatusString;
        }
        if (!appCfgInfo.isMainNotificationEnabled()) {
            return getString(R.string.notification_not_allow);
        }
        List<String> statusList = new ArrayList();
        if (appCfgInfo.isStatusbarNotificationEnabled()) {
            statusList.add(getString(R.string.status_bar_str));
        }
        if (appCfgInfo.isHeadsupNotificationEnabled()) {
            statusList.add(getString(R.string.banner_str));
        }
        if (appCfgInfo.isLockscreenNotificationEnabled()) {
            statusList.add(getString(R.string.lock_screen_str));
        }
        int listSize = statusList.size();
        HwLog.d(TAG, "getNotificationStatusString  listSize:" + listSize);
        switch (listSize) {
            case 0:
                notificationStatusString = "";
                break;
            case 1:
                notificationStatusString = (String) statusList.get(0);
                break;
            case 2:
                notificationStatusString = getString(R.string.notification_details2, new Object[]{statusList.get(0), statusList.get(1)});
                break;
            case 3:
                notificationStatusString = getString(R.string.notification_details3, new Object[]{statusList.get(0), statusList.get(1), statusList.get(2)});
                break;
        }
        return notificationStatusString;
    }

    protected void setOnDataLoadedListener(OnDataLoadedListener listener) {
        this.mDataLoadedListener = listener;
    }

    private List<NotificationCfgInfo> removeTelePkgName(List<NotificationCfgInfo> appCfgList) {
        String TELE_PACKAGE_NAME = "com.android.server.telecom";
        Iterator<NotificationCfgInfo> iter = appCfgList.iterator();
        while (iter.hasNext()) {
            NotificationCfgInfo info = (NotificationCfgInfo) iter.next();
            if ((Utility.isWifiOnlyMode() || Utility.isDataOnlyMode()) && info != null && !TextUtils.isEmpty(info.mPkgName) && TELE_PACKAGE_NAME.equals(info.mPkgName)) {
                iter.remove();
            }
        }
        return appCfgList;
    }
}
