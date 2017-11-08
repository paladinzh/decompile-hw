package com.huawei.systemmanager.netassistant.netapp.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.netassistant.cardmanager.SimCardManager;
import com.huawei.netassistant.common.ParcelableAppItem;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.netassistant.netapp.bean.ILockScreenAppFactory;
import com.huawei.systemmanager.netassistant.netapp.bean.LockScreenApp;
import com.huawei.systemmanager.netassistant.traffic.appdetail.AppDetailActivity;
import com.huawei.systemmanager.netassistant.traffic.appdetail.Constant;
import com.huawei.systemmanager.netassistant.traffic.appinfo.SpecialUid;
import com.huawei.systemmanager.spacecleanner.view.EnsureCheckBox;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;

public class LockScreenAppListActivity extends Activity implements LoaderCallbacks<List<LockScreenApp>>, OnClickListener, OnItemClickListener {
    private static final String TAG = LockScreenAppListActivity.class.getSimpleName();
    LockScreenListAdapter mAdapter;
    Context mContext;

    private static class LockScreenAppTaskLoader extends AsyncTaskLoader<List<LockScreenApp>> {
        private Bundle bundle;
        private List<LockScreenApp> mApps;

        public LockScreenAppTaskLoader(Context context, Bundle args) {
            super(context);
            this.bundle = args;
        }

        public List<LockScreenApp> loadInBackground() {
            List<LockScreenApp> lockList = Lists.newArrayList();
            if (this.bundle == null) {
                return lockList;
            }
            List<ParcelableAppItem> list = this.bundle.getParcelableArrayList(NotificationUtil.KEY_RESULT_LIST);
            if (list == null) {
                HwLog.d(LockScreenAppListActivity.TAG, "LockScreenAppTaskLoader loadInBackground list is null");
                return Lists.newArrayList();
            }
            for (ParcelableAppItem item : list) {
                lockList.add(ILockScreenAppFactory.createLockScreenApp(item.key, item.wifitotal, item.mobiletotal));
            }
            Collections.sort(lockList, LockScreenApp.NETASSISTANT_LOCKSCREEN_APP_COMPARATOR);
            Collections.sort(lockList, LockScreenApp.TRAFFIC_APP_MOBILE_COMPARATOR);
            return lockList;
        }

        public void deliverResult(List<LockScreenApp> apps) {
            if (isReset() && apps != null) {
                onReleaseResources(apps);
            }
            List<LockScreenApp> oldApps = apps;
            this.mApps = apps;
            if (isStarted()) {
                super.deliverResult(apps);
            }
            if (apps != null) {
                onReleaseResources(apps);
            }
        }

        protected void onStopLoading() {
            cancelLoad();
        }

        protected void onStartLoading() {
            if (this.mApps != null) {
                deliverResult(this.mApps);
            }
            if (takeContentChanged() || this.mApps == null) {
                forceLoad();
            }
        }

        public void onCanceled(List<LockScreenApp> apps) {
            super.onCanceled(apps);
            onReleaseResources(apps);
        }

        protected void onReset() {
            super.onReset();
            onStopLoading();
            if (this.mApps != null) {
                onReleaseResources(this.mApps);
                this.mApps = null;
            }
        }

        protected void onReleaseResources(List<LockScreenApp> list) {
        }
    }

    private class LockScreenListAdapter extends CommonAdapter<LockScreenApp> {
        static final int VIEW_TYPE_INSTALLED = 0;
        static final int VIEW_TYPE_SYSTEM_ACCOUNT = 2;
        static final int VIEW_TYPE_SYSTEM_CORE = 1;
        Context context;

        private class ViewHolder {
            ImageView icon;
            EnsureCheckBox mobileBox;
            TextView mobileTraffic;
            TextView summary;
            TextView title;
            EnsureCheckBox wifiBox;

            private ViewHolder() {
            }
        }

        public LockScreenListAdapter(Context ctx) {
            super(ctx);
            this.context = ctx;
        }

        public LockScreenApp getItem(int position) {
            return (LockScreenApp) super.getItem(position);
        }

        public int getItemViewType(int position) {
            LockScreenApp lockScreenApp = getItem(position);
            if (lockScreenApp == null || !SpecialUid.isWhiteListUid(lockScreenApp.getUid())) {
                return 0;
            }
            if (SpecialUid.isSystemAccount(lockScreenApp.getUid())) {
                return 2;
            }
            return 1;
        }

        public int getViewTypeCount() {
            return 3;
        }

        protected View newView(int position, ViewGroup parent, LockScreenApp item) {
            ViewHolder vh = new ViewHolder();
            View view = this.mInflater.inflate(R.layout.lockscreen_traffic_ranking_list_item, null);
            view.findViewById(R.id.ll_wifi_checkbox).setVisibility(8);
            vh.icon = (ImageView) view.findViewById(R.id.icon);
            vh.title = (TextView) view.findViewById(R.id.title);
            vh.summary = (TextView) view.findViewById(R.id.more_app);
            vh.mobileTraffic = (TextView) view.findViewById(R.id.mobile_data);
            vh.mobileBox = (EnsureCheckBox) view.findViewById(R.id.mobile_checkbox);
            vh.wifiBox = (EnsureCheckBox) view.findViewById(R.id.wifi_checkbox);
            vh.mobileBox.setOnClickListener(LockScreenAppListActivity.this);
            vh.wifiBox.setOnClickListener(LockScreenAppListActivity.this);
            view.setTag(vh);
            return view;
        }

        protected void bindView(int position, View view, LockScreenApp item) {
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.icon.setImageDrawable(item.getIcon());
            vh.title.setText(item.getLabel());
            vh.mobileTraffic.setText(CommonMethodUtil.formatBytes(this.context, item.getMobileTraffic()));
            String summary = item.getMoreAppSummary();
            if (TextUtils.isEmpty(summary)) {
                vh.summary.setVisibility(8);
            } else {
                vh.summary.setVisibility(0);
                vh.summary.setText(summary);
            }
            vh.mobileBox.setChecked(item.isMobileAccess());
            vh.wifiBox.setChecked(item.isWifiAccess());
            vh.mobileBox.setTag(item);
            vh.wifiBox.setTag(item);
            if (getItemViewType(position) == 1) {
                HwLog.d(LockScreenAppListActivity.TAG, "this is system core uid");
            }
            if (getItemViewType(position) == 2) {
                vh.mobileBox.setVisibility(8);
                vh.wifiBox.setVisibility(8);
                return;
            }
            vh.mobileBox.setVisibility(0);
            vh.wifiBox.setVisibility(0);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lockscreen_notify_layout);
        this.mContext = this;
        this.mAdapter = new LockScreenListAdapter(this.mContext);
        ListView list = (ListView) findViewById(R.id.listview);
        findViewById(R.id.ll_headview_wifi).setVisibility(8);
        list.setAdapter(this.mAdapter);
        list.setOnItemClickListener(this);
    }

    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            String totalUsed = intent.getStringExtra(NotificationUtil.KEY_TOTAL_MOBILE_BYTES);
            ((TextView) findViewById(R.id.netassistant_explain)).setText(getString(R.string.netassistant_lockscreen_explain, new Object[]{totalUsed}));
            getLoaderManager().restartLoader(0, intent.getExtras(), this);
        }
    }

    public Loader<List<LockScreenApp>> onCreateLoader(int id, Bundle args) {
        return new LockScreenAppTaskLoader(this.mContext, args);
    }

    public void onLoadFinished(Loader<List<LockScreenApp>> loader, List<LockScreenApp> arg1) {
        this.mAdapter.swapData(arg1);
    }

    public void onLoaderReset(Loader<List<LockScreenApp>> loader) {
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mobile_checkbox:
                CheckBox checkBox = (CheckBox) v;
                LockScreenApp appMobile = (LockScreenApp) v.getTag();
                if (SpecialUid.isWhiteListUid(appMobile.getUid()) && checkBox.isChecked()) {
                    showWarnningDialog(appMobile);
                } else if (appMobile.isMobileAccess()) {
                    appMobile.denyMobile();
                } else {
                    appMobile.accessMobile();
                }
                this.mAdapter.notifyDataSetChanged();
                return;
            case R.id.wifi_checkbox:
                LockScreenApp appWifi = (LockScreenApp) v.getTag();
                if (((CheckBox) v).isChecked()) {
                    appWifi.accessWifi();
                    return;
                } else {
                    appWifi.denyWifi();
                    return;
                }
            default:
                return;
        }
    }

    private void showWarnningDialog(final LockScreenApp appMobile) {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.net_assistant_net_app_network_dialog_title);
        builder.setMessage(SpecialUid.getWarningTextId(appMobile.getUid()));
        builder.setPositiveButton(R.string.power_cpu_wakeup_dialog_close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (appMobile.isMobileAccess()) {
                    appMobile.denyMobile();
                } else {
                    appMobile.accessMobile();
                }
                LockScreenAppListActivity.this.mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.common_cancel, null);
        builder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
        LockScreenApp absNetAppInfo = (LockScreenApp) parentView.getItemAtPosition(position);
        if (absNetAppInfo != null) {
            Intent intent = new Intent();
            intent.putExtra(Constant.EXTRA_ACTIVITY_FROM, 0);
            intent.putExtra(Constant.EXTRA_APP_LABEL, absNetAppInfo.getLabel());
            intent.putExtra("uid", absNetAppInfo.getUid());
            intent.putExtra(Constant.EXTRA_IMSI, SimCardManager.getInstance().getPreferredDataSubscriberId());
            intent.setClass(this, AppDetailActivity.class);
            startActivity(intent);
        }
    }
}
