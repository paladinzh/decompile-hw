package com.huawei.systemmanager.applock.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.huawei.android.app.ActionBarEx;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.fingerprint.FingerprintAdapter;
import com.huawei.systemmanager.applock.fingerprint.FingerprintAuthUtils;
import com.huawei.systemmanager.applock.fingerprint.IFingerprintAuth;
import com.huawei.systemmanager.applock.taskstack.ActivityStackUtils;
import com.huawei.systemmanager.applock.utils.ProviderWrapperUtils;
import com.huawei.systemmanager.applock.utils.sp.FingerprintBindUtils;
import com.huawei.systemmanager.applock.utils.sp.FunctionSwitchUtils;
import com.huawei.systemmanager.applock.utils.sp.ReloadSwitchUtils;
import com.huawei.systemmanager.applock.view.abs.AppLockRelockBaseActivity;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comparator.AlpComparator;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ApplicationListActivity extends AppLockRelockBaseActivity {
    private static final String ASSET_XML_ATTR = "name";
    private static final String ASSET_XML_TAG = "package";
    private static final String HIGH_PRIORITY_SORT_LIST_ASSET_FILE = "applock/high_priority_unlock_apps.xml";
    private static final int MSG_WHAT = 1;
    private static final String TAG = "ApplicationListActivity";
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            HsmStat.statE(Events.E_APPLOCK_ENTER_SETTING_CLICKED);
            ApplicationListActivity.this.startActivity(new Intent(ApplicationListActivity.this.getApplicationContext(), AppLockSettingActivity.class));
        }
    };
    private ImageView mActionBarSettingImg = null;
    private LockedListAdapter mAdapter = null;
    private Context mAppContext = null;
    private AlertDialog mBindRemindDialog = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    ApplicationListActivity.this.showReverseBindDialog();
                    return;
                default:
                    return;
            }
        }
    };
    private AppLockListLoadingTask mLoadingTask = null;
    private GridView mLockedAppGridView = null;
    private int mLockedCount = 0;
    private View mLockedCountLayout = null;
    private TextView mLockedCountTextView = null;
    private Button mOpenFuncBtn = null;
    private View mOpenFunctionLayout = null;
    private View mProgressBarLayout = null;
    private int mResumeCount = 0;
    private boolean mSwitchStat = true;

    private static class AppLockItemInfo {
        Drawable mAppIcon;
        String mAppLocalName;
        boolean mIsLocked;
        String mPkgName;

        private AppLockItemInfo() {
        }
    }

    private class AppLockListLoadingTask extends AsyncTask<Void, Void, List<AppLockItemInfo>> {
        private AppLockListLoadingTask() {
        }

        protected List<AppLockItemInfo> doInBackground(Void... params) {
            HwLog.v(ApplicationListActivity.TAG, "LoadingTask:doInBackground...");
            List<AppLockItemInfo> allApps = Lists.newArrayList();
            Set<String> lockedPkgSet = ProviderWrapperUtils.getLockedApps(ApplicationListActivity.this.mAppContext);
            Set<String> unlockPkgSet = ProviderWrapperUtils.getUnLockedApps(ApplicationListActivity.this.mAppContext);
            Set<String> highPriorityPkgSet = Sets.newHashSet();
            highPriorityPkgSet.addAll(XmlParsers.xmlAttrValueList(ApplicationListActivity.this.mAppContext, null, ApplicationListActivity.HIGH_PRIORITY_SORT_LIST_ASSET_FILE, XmlParsers.getTagAttrMatchPredicate("package", "name"), XmlParsers.getRowToAttrValueFunc("name")));
            allApps.addAll(sortedAppLockItems(lockedPkgSet, true));
            ApplicationListActivity.this.mLockedCount = allApps.size();
            allApps.addAll(sortedUnlockedApps(unlockPkgSet, highPriorityPkgSet));
            return allApps;
        }

        protected void onPostExecute(List<AppLockItemInfo> result) {
            if (!isCancelled()) {
                HwLog.v(ApplicationListActivity.TAG, "onPostExecute:" + result.size());
                ApplicationListActivity.this.mAdapter.swapDataAndRefresh(result);
                ApplicationListActivity.this.updateActivityViewContent();
            }
        }

        private List<AppLockItemInfo> sortedUnlockedApps(Set<String> pkgSet, Set<String> highPriorityPkgSet) {
            Set<String> intersectionPkgSet = Sets.intersection(pkgSet, highPriorityPkgSet);
            Set<String> differencePkgSet = Sets.difference(pkgSet, highPriorityPkgSet);
            List<AppLockItemInfo> allUnlockItems = Lists.newArrayList();
            allUnlockItems.addAll(sortedAppLockItems(intersectionPkgSet, false));
            allUnlockItems.addAll(sortedAppLockItems(differencePkgSet, false));
            return allUnlockItems;
        }

        private List<AppLockItemInfo> sortedAppLockItems(Set<String> pkgSet, boolean isLocked) {
            List<AppLockItemInfo> itemList = Lists.newArrayList();
            for (String pkgName : pkgSet) {
                if (isCancelled()) {
                    break;
                } else if (HsmPackageManager.getInstance().packageExists(pkgName, 0)) {
                    itemList.add(createAppItemInfo(pkgName, isLocked));
                }
            }
            Collections.sort(itemList, new AlpComparator<AppLockItemInfo>() {
                public String getStringKey(AppLockItemInfo t) {
                    return t.mAppLocalName;
                }
            });
            return itemList;
        }

        private AppLockItemInfo createAppItemInfo(String pkgName, boolean isLocked) {
            AppLockItemInfo oneLockedInfo = new AppLockItemInfo();
            oneLockedInfo.mPkgName = pkgName;
            oneLockedInfo.mAppIcon = HsmPackageManager.getInstance().getIcon(pkgName);
            oneLockedInfo.mAppLocalName = HsmPackageManager.getInstance().getLabel(pkgName);
            oneLockedInfo.mIsLocked = isLocked;
            return oneLockedInfo;
        }
    }

    private static class LockedAppViewHolder {
        ImageView mAppIcon = null;
        TextView mAppLabel = null;
        TextView mAppLockStatus = null;
        Switch mLockSwitch = null;

        public LockedAppViewHolder(View view) {
            this.mAppIcon = (ImageView) view.findViewById(R.id.image);
            this.mAppLabel = (TextView) view.findViewById(ViewUtil.HWID_TEXT_1);
            this.mAppLockStatus = (TextView) view.findViewById(ViewUtil.HWID_TEXT_2);
            this.mLockSwitch = (Switch) view.findViewById(R.id.switcher);
        }
    }

    public class LockedListAdapter extends BaseAdapter {
        private List<AppLockItemInfo> mAdapterLockedApps = null;
        private Context mContext = null;
        private LayoutInflater mLayoutInflater = null;

        public LockedListAdapter(Context context, LayoutInflater layoutInflater) {
            this.mContext = context;
            this.mLayoutInflater = layoutInflater;
            this.mAdapterLockedApps = Lists.newArrayList();
        }

        public int getCount() {
            return this.mAdapterLockedApps.size();
        }

        public Object getItem(int position) {
            return this.mAdapterLockedApps.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        public boolean isEnabled(int position) {
            return ApplicationListActivity.this.mSwitchStat;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AppLockItemInfo lockedApp = (AppLockItemInfo) getItem(position);
            if (lockedApp == null) {
                HwLog.e(ApplicationListActivity.TAG, "LockedListAdapter:getView can't find a valid item");
                return null;
            }
            LockedAppViewHolder viewHolder;
            if (convertView == null) {
                convertView = this.mLayoutInflater.inflate(R.layout.common_list_item_twolines_image_switch, null);
                viewHolder = new LockedAppViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (LockedAppViewHolder) convertView.getTag();
            }
            setViewHoldContent(viewHolder, lockedApp, position);
            return convertView;
        }

        private void setViewHoldContent(LockedAppViewHolder viewHolder, AppLockItemInfo lockedApp, final int position) {
            int i;
            int iPos = position;
            viewHolder.mAppIcon.setImageDrawable(lockedApp.mAppIcon);
            viewHolder.mAppLabel.setText(lockedApp.mAppLocalName);
            viewHolder.mLockSwitch.setOnCheckedChangeListener(null);
            viewHolder.mLockSwitch.setChecked(lockedApp.mIsLocked);
            TextView textView = viewHolder.mAppLockStatus;
            if (lockedApp.mIsLocked) {
                i = R.string.ListViewSecondLine_AppLockMainView_Tips01;
            } else {
                i = R.string.ListViewSecondLine_AppLockMainView_Tips02;
            }
            textView.setText(i);
            viewHolder.mLockSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    HwLog.d(ApplicationListActivity.TAG, "onCheckedChanged: " + position + SqlMarker.COMMA_SEPARATE + isChecked);
                    LockedListAdapter.this.modifyAppLockStatus(position, isChecked);
                    ApplicationListActivity.this.updateActivityViewContent();
                }
            });
            viewHolder.mLockSwitch.setEnabled(ApplicationListActivity.this.mSwitchStat);
        }

        private void modifyAppLockStatus(int index, boolean isLocked) {
            AppLockItemInfo appInfo = (AppLockItemInfo) this.mAdapterLockedApps.get(index);
            if (appInfo == null) {
                HwLog.e(ApplicationListActivity.TAG, "modifyAppLockStatus get null object");
                return;
            }
            int i;
            String[] strArr = new String[1];
            String[] strArr2 = new String[4];
            strArr2[0] = HsmStatConst.PARAM_PKG;
            strArr2[1] = appInfo.mPkgName;
            strArr2[2] = HsmStatConst.PARAM_OP;
            strArr2[3] = isLocked ? "1" : "0";
            strArr[0] = HsmStatConst.constructJsonParams(strArr2);
            HsmStat.statE((int) Events.E_APPLOCK_MODIFY_LOCK_STATUS, strArr);
            Context context = this.mContext;
            String str = appInfo.mPkgName;
            if (isLocked) {
                i = 1;
            } else {
                i = 0;
            }
            ProviderWrapperUtils.replaceAppLockStatus(context, str, i);
            appInfo.mIsLocked = isLocked;
            ApplicationListActivity.this.updateLockedCount(isLocked);
            if (!isLocked) {
                ProviderWrapperUtils.delAuthSuccessPackage(this.mContext, appInfo.mPkgName);
            }
            notifyDataSetChanged();
        }

        public void swapDataAndRefresh(List<AppLockItemInfo> lockData) {
            this.mAdapterLockedApps.clear();
            this.mAdapterLockedApps.addAll(lockData);
            HSMConst.doMultiply(ApplicationListActivity.this.getApplicationContext(), ApplicationListActivity.this.getResources().getConfiguration().orientation == 2, ApplicationListActivity.this.mLockedAppGridView);
            ReloadSwitchUtils.setApplicationListAlreadyReload(ApplicationListActivity.this.mAppContext);
            notifyDataSetChanged();
        }
    }

    private static class SwitchItemClickListener implements OnItemClickListener {
        private SwitchItemClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
            Switch sw = (Switch) arg1.findViewById(R.id.switcher);
            if (sw != null) {
                sw.performClick();
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppContext = getApplicationContext();
        setContentView(R.layout.app_lock_all_app_list);
        ActionBarEx.setEndIcon(getActionBar(), true, getDrawable(R.drawable.settings_menu_btn_selector), this.mActionBarListener);
        ActionBarEx.setEndContentDescription(getActionBar(), getString(R.string.net_assistant_setting_title));
        setTitle(R.string.ActionBar_EnterAppLock_Title);
        initViewsAndAdapter();
        ActivityStackUtils.addActivity(this);
    }

    protected void onDestroy() {
        ActivityStackUtils.removeFromStack(this);
        if (this.mLoadingTask != null) {
            this.mLoadingTask.cancel(false);
            this.mLoadingTask = null;
        }
        super.onDestroy();
    }

    protected void onPause() {
        closeBindRemindDialog();
        super.onPause();
    }

    protected void concreteOnResume() {
        updateSwitchStatus(FunctionSwitchUtils.getFunctionSwitchStatus(this.mAppContext));
        increaseResumeCount();
        if (firstResume() || ReloadSwitchUtils.isApplicationListNeedReload(this.mAppContext)) {
            this.mProgressBarLayout.setVisibility(0);
            this.mLoadingTask = new AppLockListLoadingTask();
            this.mLoadingTask.execute(new Void[0]);
        }
        updateTipViews(this.mSwitchStat);
        judgeAndshowBindFingerprintDialog();
    }

    protected String baseClassName() {
        return ApplicationListActivity.class.getName();
    }

    private void initViewsAndAdapter() {
        this.mLockedCountTextView = (TextView) findViewById(R.id.app_lock_applist_locked_count_tip);
        this.mOpenFuncBtn = (Button) findViewById(R.id.app_lock_applist_btn_open_func);
        this.mLockedCountLayout = findViewById(R.id.app_lock_applist_locked_count_layout);
        this.mOpenFunctionLayout = findViewById(R.id.app_lock_applist_function_closed_layout);
        this.mProgressBarLayout = findViewById(R.id.app_lock_applist_loading_progress_bar);
        this.mOpenFuncBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HsmStat.statE(Events.E_APPLOCK_REOPEN_IN_APPLIST);
                FunctionSwitchUtils.setFunctionSwitchStatus(ApplicationListActivity.this.mAppContext, true);
                ApplicationListActivity.this.updateSwitchStatus(true);
                ApplicationListActivity.this.updateTipViews(true);
                ApplicationListActivity.this.mAdapter.notifyDataSetChanged();
            }
        });
        this.mAdapter = new LockedListAdapter(this.mAppContext, getLayoutInflater());
        this.mLockedAppGridView = (GridView) findViewById(R.id.app_lock_applist_grid_view);
        HSMConst.doMultiply(getApplicationContext(), 2 == getResources().getConfiguration().orientation, this.mLockedAppGridView);
        this.mLockedAppGridView.setAdapter(this.mAdapter);
        this.mLockedAppGridView.setOnItemClickListener(new SwitchItemClickListener());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HSMConst.doMultiply(getApplicationContext(), newConfig.orientation == 2, this.mLockedAppGridView);
    }

    private void showProgressBar(boolean show) {
        if (this.mProgressBarLayout != null) {
            this.mProgressBarLayout.setVisibility(show ? 0 : 8);
        }
    }

    private void updateActivityViewContent() {
        HwLog.v(TAG, "updateViewContent");
        showProgressBar(false);
        updateTopTipContent();
    }

    private void updateTopTipContent() {
        this.mLockedCountTextView.setText(this.mAppContext.getResources().getQuantityString(R.plurals.ListViewFirstLine_AppLockMainView_Tips, this.mLockedCount, new Object[]{Integer.valueOf(this.mLockedCount)}));
    }

    private void updateTipViews(boolean appLockSwitch) {
        int i;
        int i2 = 0;
        boolean showFuncLayout = !appLockSwitch;
        View view = this.mOpenFunctionLayout;
        if (showFuncLayout) {
            i = 0;
        } else {
            i = 8;
        }
        view.setVisibility(i);
        View view2 = this.mLockedCountLayout;
        if (showFuncLayout) {
            i2 = 8;
        }
        view2.setVisibility(i2);
    }

    private void updateLockedCount(boolean isAdd) {
        if (isAdd) {
            this.mLockedCount++;
        } else {
            this.mLockedCount--;
        }
    }

    private void judgeAndshowBindFingerprintDialog() {
        if (firstResume() && !FingerprintBindUtils.getFingerprintBindStatus(this.mAppContext) && FunctionSwitchUtils.getBindRemindStatus(this.mAppContext)) {
            IFingerprintAuth authMgr = FingerprintAdapter.create(this.mAppContext);
            try {
                if (FingerprintAuthUtils.checkFingerprintReadyNotClose(authMgr)) {
                    this.mHandler.sendEmptyMessage(1);
                    FingerprintAuthUtils.closeAuth(authMgr);
                    return;
                }
                HwLog.w(TAG, "showBindFingerprintDialog finger print device not ready");
            } finally {
                FingerprintAuthUtils.closeAuth(authMgr);
            }
        } else {
            HwLog.w(TAG, "judgeAndshowBindFingerprintDialog condition not match");
        }
    }

    private void showReverseBindDialog() {
        Builder builder = new Builder(this);
        builder.setTitle(R.string.app_lock_reverse_bind_fingerprint_dialog_title);
        builder.setMessage(getString(R.string.app_lock_reverse_bind_fingerprint_dialog_message));
        builder.setPositiveButton(R.string.app_lock_reverse_bind_fingerprint_dialog_bind, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                FingerprintBindUtils.setFingerprintBindStatus(ApplicationListActivity.this.mAppContext, true);
                Toast.makeText(ApplicationListActivity.this.mAppContext, R.string.app_lock_reverse_bind_fingerprint_success_toast, 0).show();
            }
        });
        builder.setNegativeButton(R.string.app_lock_reverse_bind_fingerprint_dialog_cancel, null);
        builder.setCancelable(false);
        this.mBindRemindDialog = builder.create();
        this.mBindRemindDialog.show();
        FunctionSwitchUtils.clearBindRemindStatus(this.mAppContext);
    }

    private void increaseResumeCount() {
        this.mResumeCount++;
    }

    private boolean firstResume() {
        return 1 == this.mResumeCount;
    }

    private void closeBindRemindDialog() {
        if (this.mBindRemindDialog != null) {
            this.mBindRemindDialog.hide();
        }
    }

    private void updateSwitchStatus(boolean newStat) {
        this.mSwitchStat = newStat;
    }
}
