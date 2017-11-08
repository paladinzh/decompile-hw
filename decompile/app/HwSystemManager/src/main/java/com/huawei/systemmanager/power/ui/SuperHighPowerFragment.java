package com.huawei.systemmanager.power.ui;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.huawei.notificationmanager.util.Helper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SuperHighPowerBean;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SuperHighPowerFragment extends Fragment {
    private static final String TAG = "SuperHighPowerFragment";
    private List<SuperHighPowerBean> mAppCfgList = null;
    private ListView mAppListView = null;
    private AppListAdapter mApplistAdapter;
    private Context mContext = null;
    private View mFragmentView;
    private LayoutInflater mInflater;
    private Boolean mIsOnFirstResume = Boolean.valueOf(true);
    private Boolean mIsOnPause = Boolean.valueOf(false);
    private SuperHighPowerLoadingTask mLoadingTask = null;
    private ProgressBar mProgressBar = null;
    private LinearLayout noAppLayout;
    private LinearLayout superHighPowerMainLayout;

    class AppListAdapter extends BaseAdapter {
        public Object getItem(int position) {
            return SuperHighPowerFragment.this.mAppCfgList.get(position);
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public int getCount() {
            return SuperHighPowerFragment.this.mAppCfgList.size();
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            SuperHighPowerBean appCfgInfo = (SuperHighPowerBean) SuperHighPowerFragment.this.mAppCfgList.get(position);
            if (appCfgInfo == null) {
                return null;
            }
            ListItemViewHolder viewHolder;
            if (convertView == null) {
                convertView = SuperHighPowerFragment.this.mInflater.inflate(R.layout.super_high_power_main_fragment_item, null);
                viewHolder = new ListItemViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ListItemViewHolder) convertView.getTag();
            }
            setAppViewContent(viewHolder, appCfgInfo);
            return convertView;
        }

        private void setAppViewContent(ListItemViewHolder holder, SuperHighPowerBean appCfgInfo) {
            holder.appIcon.setImageDrawable(appCfgInfo.getmAppIcon());
            holder.tvAppLabel.setText(appCfgInfo.getmLabel());
            holder.clearTime.setText(SuperHighPowerFragment.this.displayTime(appCfgInfo.getmTime()));
        }
    }

    static class ListItemViewHolder {
        ImageView appIcon = null;
        TextView clearTime = null;
        TextView tvAppLabel = null;

        public ListItemViewHolder(View view) {
            this.appIcon = (ImageView) view.findViewById(R.id.NotificationRuleAppIcon);
            this.tvAppLabel = (TextView) view.findViewById(R.id.NotificationRuleAppName);
            this.clearTime = (TextView) view.findViewById(R.id.clearTime);
        }
    }

    private class SuperHighPowerLoadingTask extends AsyncTask<Void, Void, List<SuperHighPowerBean>> {
        private boolean mIsLoadonReset = false;

        public SuperHighPowerLoadingTask(boolean isLoadOnReset) {
            this.mIsLoadonReset = isLoadOnReset;
        }

        protected List<SuperHighPowerBean> doInBackground(Void... voidParams) {
            try {
                HwLog.w(SuperHighPowerFragment.TAG, "SavingSettingUtil.querySuperHighPowerAppsFromDB(mContext)");
                return SavingSettingUtil.querySuperHighPowerAppsFromDB(SuperHighPowerFragment.this.mContext);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        protected void onPostExecute(List<SuperHighPowerBean> appCfgList) {
            if (isCancelled()) {
                HwLog.w(SuperHighPowerFragment.TAG, "onPostExecute, The task is canceled");
                return;
            }
            if (SuperHighPowerFragment.this.mAppCfgList == null) {
                SuperHighPowerFragment.this.mAppCfgList = new ArrayList();
            }
            if (appCfgList == null) {
                HwLog.w(SuperHighPowerFragment.TAG, "Fail to load notification rule list");
            } else {
                SuperHighPowerFragment.this.mAppCfgList.clear();
                if (appCfgList.size() > 0) {
                    SuperHighPowerFragment.this.mAppCfgList.addAll(appCfgList);
                }
                if (!this.mIsLoadonReset) {
                    Helper.setCfgChangeFlag(SuperHighPowerFragment.this.mContext, false);
                }
            }
            SuperHighPowerFragment.this.updateAllViews();
            SuperHighPowerFragment.this.notifyAppListAdapter();
            SuperHighPowerFragment.this.mLoadingTask = null;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        this.mInflater = inflater;
        this.mFragmentView = this.mInflater.inflate(R.layout.super_high_power_main_fragment, container, false);
        initView();
        return this.mFragmentView;
    }

    private void initView() {
        this.superHighPowerMainLayout = (LinearLayout) this.mFragmentView.findViewById(R.id.super_high_power_fragment_layout);
        this.mAppListView = (ListView) this.mFragmentView.findViewById(R.id.super_high_power_list_view);
        this.mAppListView.setItemsCanFocus(false);
        this.mProgressBar = (ProgressBar) this.mFragmentView.findViewById(R.id.loading_progressbar);
        this.noAppLayout = (LinearLayout) this.mFragmentView.findViewById(R.id.no_app_layout);
        this.mAppCfgList = new ArrayList();
        this.mApplistAdapter = new AppListAdapter();
        this.mAppListView.setAdapter(this.mApplistAdapter);
    }

    private void updateAllViews() {
        if (this.mApplistAdapter.getCount() <= 0) {
            this.noAppLayout.setVisibility(0);
            this.superHighPowerMainLayout.setVisibility(8);
            return;
        }
        this.noAppLayout.setVisibility(8);
        this.superHighPowerMainLayout.setVisibility(0);
    }

    private void refurbishAppList() {
        if (this.mLoadingTask == null) {
            showLoadingProcess(Boolean.valueOf(true));
            this.mLoadingTask = new SuperHighPowerLoadingTask(false);
            this.mLoadingTask.execute(new Void[0]);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mIsOnFirstResume.booleanValue() || (this.mIsOnPause.booleanValue() && Helper.getCfgChangeFlag(this.mContext))) {
            refurbishAppList();
        } else {
            notifyAppListAdapter();
        }
        this.mIsOnPause = Boolean.valueOf(false);
        this.mIsOnFirstResume = Boolean.valueOf(false);
    }

    public void onPause() {
        this.mIsOnPause = Boolean.valueOf(true);
        super.onPause();
    }

    public void onDestroy() {
        if (this.mLoadingTask != null) {
            this.mLoadingTask.cancel(false);
            this.mLoadingTask = null;
        }
        this.mContext = null;
        super.onDestroy();
    }

    private void notifyAppListAdapter() {
        showLoadingProcess(Boolean.valueOf(false));
        this.mApplistAdapter.notifyDataSetChanged();
    }

    private void showLoadingProcess(Boolean bShow) {
        if (this.mProgressBar != null) {
            this.mProgressBar.setVisibility(bShow.booleanValue() ? 0 : 8);
        }
    }

    private String displayTime(long timestamp) {
        String str = "";
        Date d = new Date(timestamp);
        return DateUtils.isToday(timestamp) ? DateFormat.getTimeFormat(this.mContext).format(d) : DateFormat.getDateFormat(this.mContext).format(d);
    }
}
