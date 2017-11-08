package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v4.view.PointerIconCompat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.wrapper.SharePrefWrapper;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.power.comm.SharedPrefKeyConst;
import com.huawei.systemmanager.power.provider.ProviderWrapper;
import com.huawei.systemmanager.power.provider.SmartProvider.ROGUE_Columns;
import com.huawei.systemmanager.power.util.Conversion;
import com.huawei.systemmanager.power.util.PowerEmuiRelativeLayout;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.power.util.SpecialAppUid;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.startupmgr.comm.NormalStartupInfo;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class DetailOfSoftConsumptionActivity extends HsmActivity {
    private static final String SCHEME = "package";
    private static final String TAG = "DetailOfSoftConsumptionActivity";
    private static final String ZEROINDOUBLE = "0.00";
    private LinkedList<RelativeLayout> basicLayoutList;
    private int enterType = -1;
    private TextView hintSection;
    private ApplicationInfo mApp;
    private boolean mAutoBootAppState;
    private Switch mAutoBootAppSwitch;
    private RelativeLayout mAutoBootupLayout;
    private TextView mCpuFgOrBgTitle;
    private RelativeLayout mCpuTimeFgOrBgLayout;
    private TextView mCpuTimeFgOrBgValue;
    private TextView mCpuTimeValue;
    private RelativeLayout mCpuTimeValueLayout;
    private RelativeLayout mDataRecvLayout;
    private TextView mDataRecvValue;
    private RelativeLayout mDataSendLayout;
    private TextView mDataSendValue;
    private AlertDialog mDialog = null;
    private Bundle mExtraData;
    private RelativeLayout mGpsTimeLayout;
    private TextView mGpsValue;
    private RelativeLayout mHintCpuWakeupSwitchLayout;
    private boolean mHintState;
    private Switch mHintSwitch;
    private RelativeLayout mHintSwitchLayout;
    private boolean mHintWakeupState;
    private Switch mHintWakeupSwitch;
    private ImageView mIconView;
    private PackageInfo mInfo;
    private MenuItem mInfoMenu;
    private boolean mIsFromSettings;
    private boolean mIsOtherUser = false;
    private boolean mKeepBgAppState;
    private Switch mKeepBgAppSwitch;
    private RelativeLayout mKeepBgLayout;
    private TextView mPkgLabel;
    private String mPkgName;
    private TextView mPkgVersion;
    private RelativeLayout mPowerConsumptionLayout;
    private TextView mPowerConsumptionValue;
    private TextView mPowerValueDes;
    private TextView mPreventSleepTime;
    private RelativeLayout mPreventSleepTimeLayout;
    private ProtectAppControl mProtectListControl;
    private LinearLayout mShareAppsLabelContainer;
    private LinearLayout mShareAppsLayout;
    private List<LabelAndPower> mSharedLabelList = Lists.newArrayList();
    private TextView mSharedUidDes;
    private boolean mShowBgView;
    private Drawable mSoftIcon;
    private String mSoftLabel;
    private String mSoftVersion;
    private int mSpecialUid;
    private MenuItem mStopMenu;
    private int mUid;
    private ProgressDialog mWaitingDialog = null;
    private RelativeLayout mWifiRunningTimeLayout;
    private TextView mWifiRunningTimeValue;
    private RelativeLayout mWlanRecvLayout;
    private TextView mWlanRecvValue;
    private RelativeLayout mWlanSendLayout;
    private TextView mWlanSendValue;
    private ArrayList<String> protectPkgNameList;
    private LinkedList<RelativeLayout> switchLayoutList;

    private class AsyncLoaderTask extends AsyncTask<Void, Void, Void> {
        private AsyncLoaderTask() {
        }

        protected Void doInBackground(Void... param) {
            DetailOfSoftConsumptionActivity.this.mProtectListControl = ProtectAppControl.getInstance(DetailOfSoftConsumptionActivity.this.getApplicationContext());
            DetailOfSoftConsumptionActivity.this.protectPkgNameList = DetailOfSoftConsumptionActivity.this.mProtectListControl.getProtectList();
            if (DetailOfSoftConsumptionActivity.this.mIsFromSettings) {
                try {
                    DetailOfSoftConsumptionActivity.this.mExtraData = DetailExtConst.getConsumeExtInfoByPackageName(DetailOfSoftConsumptionActivity.this.getApplicationContext(), DetailOfSoftConsumptionActivity.this.mPkgName, DetailOfSoftConsumptionActivity.this.mUid);
                    Preconditions.checkNotNull(DetailOfSoftConsumptionActivity.this.mExtraData);
                    DetailOfSoftConsumptionActivity.this.mShowBgView = DetailOfSoftConsumptionActivity.this.mExtraData.getBoolean(SoftDetailKey.EXT_SOFT_DETAIL_SHOW_BG_VIEW);
                    HwLog.d(DetailOfSoftConsumptionActivity.TAG, "mExtraData= " + DetailOfSoftConsumptionActivity.this.mExtraData);
                } catch (Exception e) {
                    HwLog.e(DetailOfSoftConsumptionActivity.TAG, "getConsumeExtInfoByPackageName catch exception: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            if (DetailOfSoftConsumptionActivity.this.mExtraData == null) {
                HwLog.i(DetailOfSoftConsumptionActivity.TAG, "mExtraData is null, generate a new Bundle");
                DetailOfSoftConsumptionActivity.this.mExtraData = new Bundle();
            }
            if (!Strings.isNullOrEmpty(DetailOfSoftConsumptionActivity.this.mPkgName)) {
                try {
                    DetailOfSoftConsumptionActivity.this.mInfo = PackageManagerWrapper.getPackageInfo(DetailOfSoftConsumptionActivity.this.getApplicationContext().getPackageManager(), DetailOfSoftConsumptionActivity.this.mPkgName, 0);
                    DetailOfSoftConsumptionActivity.this.mApp = DetailOfSoftConsumptionActivity.this.mInfo.applicationInfo;
                } catch (NameNotFoundException e2) {
                    e2.printStackTrace();
                    HwLog.w(DetailOfSoftConsumptionActivity.TAG, "doInBackground catch NameNotFoundException: " + DetailOfSoftConsumptionActivity.this.mPkgName);
                }
            }
            if (DetailOfSoftConsumptionActivity.this.mApp == null) {
                if (DetailOfSoftConsumptionActivity.this.mUid == 0) {
                    DetailOfSoftConsumptionActivity.this.mSoftLabel = DetailOfSoftConsumptionActivity.this.getApplicationContext().getString(R.string.android_os_new);
                    DetailOfSoftConsumptionActivity.this.mSoftIcon = DetailOfSoftConsumptionActivity.this.getResources().getDrawable(R.drawable.ic_robot_battery);
                }
                if (PointerIconCompat.TYPE_ALL_SCROLL == DetailOfSoftConsumptionActivity.this.mUid) {
                    DetailOfSoftConsumptionActivity.this.mSoftLabel = DetailOfSoftConsumptionActivity.this.getApplicationContext().getString(R.string.media_server);
                    DetailOfSoftConsumptionActivity.this.mSoftIcon = DetailOfSoftConsumptionActivity.this.getResources().getDrawable(R.drawable.ic_multimedia_battery);
                }
            } else {
                DetailOfSoftConsumptionActivity.this.mUid = DetailOfSoftConsumptionActivity.this.mApp.uid;
                DetailOfSoftConsumptionActivity.this.mSoftLabel = DetailOfSoftConsumptionActivity.this.mApp.loadLabel(DetailOfSoftConsumptionActivity.this.getPackageManager()).toString().replaceAll("\\s", "");
                DetailOfSoftConsumptionActivity.this.mSoftIcon = DetailOfSoftConsumptionActivity.this.mApp.loadIcon(DetailOfSoftConsumptionActivity.this.getPackageManager());
                if (DetailOfSoftConsumptionActivity.this.mInfo.versionName != null) {
                    DetailOfSoftConsumptionActivity.this.mSoftVersion = DetailOfSoftConsumptionActivity.this.getApplicationContext().getString(R.string.version, new Object[]{DetailOfSoftConsumptionActivity.this.mInfo.versionName});
                }
                DetailOfSoftConsumptionActivity.this.loadSharedUidPackageNameList(DetailOfSoftConsumptionActivity.this.mUid);
            }
            DetailOfSoftConsumptionActivity.this.invalidateOptionsMenu();
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            DetailOfSoftConsumptionActivity.this.setAllViewStateAndValues();
            if (DetailOfSoftConsumptionActivity.this.mIsFromSettings) {
                DetailOfSoftConsumptionActivity.this.hideWaitingDialog();
            }
        }
    }

    private static class Cmp implements Comparator<LabelAndPower>, Serializable {
        private static final long serialVersionUID = 3340775350811075891L;

        private Cmp() {
        }

        public int compare(LabelAndPower arg0, LabelAndPower arg1) {
            return Conversion.invertedCompare(arg0.power, arg1.power);
        }
    }

    private static class LabelAndPower {
        String label;
        double power;

        private LabelAndPower() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_software_list);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.consuption_detail_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.show();
        retrieveIntentData();
        findAllViews();
    }

    private void updateShareUidLableLayout() {
        List<LabelAndPower> mSharedLabelListNew = new ArrayList();
        mSharedLabelListNew.addAll(this.mSharedLabelList);
        int listSize = mSharedLabelListNew.size();
        if (1 < listSize) {
            this.mShareAppsLabelContainer.removeAllViews();
            this.mShareAppsLayout.setVisibility(0);
            if (this.enterType == 1) {
                this.mSharedUidDes.setText(getString(R.string.shareuid_background_app_title));
                this.mShareAppsLabelContainer.setVisibility(8);
                this.mSharedUidDes.setVisibility(8);
            } else if (this.enterType == 2) {
                this.mSharedUidDes.setText(getString(R.string.shareuid_app_title));
            }
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService("layout_inflater");
            for (int i = 0; i < listSize; i++) {
                View v = layoutInflater.inflate(R.layout.shareuid_app_content, null);
                TextView powerTV = (TextView) v.findViewById(R.id.power);
                ((TextView) v.findViewById(R.id.app_name)).setText(((LabelAndPower) mSharedLabelListNew.get(i)).label);
                if (ZEROINDOUBLE.equals(Conversion.formatPower(((LabelAndPower) mSharedLabelListNew.get(i)).power))) {
                    powerTV.setText(getString(R.string.power_consume_detail_shared_uid_empty_data));
                } else {
                    powerTV.setText(String.format(getString(R.string.mah), new Object[]{power}));
                }
                this.mShareAppsLabelContainer.addView(v);
            }
        }
        mSharedLabelListNew.clear();
    }

    private void setInitSwitchState() {
        this.mKeepBgAppState = true;
        this.mKeepBgAppSwitch.setChecked(this.mKeepBgAppState);
        if (this.protectPkgNameList != null) {
            if (this.protectPkgNameList.contains(this.mPkgName)) {
                this.mKeepBgAppState = false;
            } else {
                this.mKeepBgAppState = true;
            }
        }
        this.mKeepBgAppSwitch.setChecked(this.mKeepBgAppState);
        boolean bAppAutoBoot = false;
        NormalStartupInfo startupInfo = StartupDataMgrHelper.querySingleNormalStartupInfo(getApplicationContext(), this.mPkgName);
        if (startupInfo != null) {
            bAppAutoBoot = true;
            this.mAutoBootAppState = startupInfo.getStatus();
            this.mAutoBootAppSwitch.setChecked(this.mAutoBootAppState);
        }
        if (this.mShowBgView) {
            this.mKeepBgLayout.setVisibility(0);
            if (bAppAutoBoot && CustomizeWrapper.isBootstartupEnabled()) {
                this.mAutoBootupLayout.setVisibility(0);
            } else {
                this.mAutoBootupLayout.setVisibility(8);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_power_detail_two_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        this.mStopMenu = menu.findItem(R.id.power_detail_menu_one);
        this.mInfoMenu = menu.findItem(R.id.power_detail_menu_two);
        String[] pkgs = getPackageManager().getPackagesForUid(this.mUid);
        Set<Integer> cacheUidSet = SysCoreUtils.getRunningUids(this);
        boolean isRunning;
        if (this.mIsOtherUser) {
            if (cacheUidSet.contains(Integer.valueOf(this.mSpecialUid))) {
                isRunning = true;
            } else {
                isRunning = false;
            }
        } else if (cacheUidSet.contains(Integer.valueOf(this.mUid))) {
            isRunning = true;
        } else {
            isRunning = false;
        }
        if (this.mApp == null || (this.mApp.flags & 8) != 0 || ((pkgs != null && 2 <= pkgs.length) || !isRunning)) {
            this.mStopMenu.setEnabled(false);
        } else {
            this.mStopMenu.setEnabled(true);
        }
        if (this.mApp == null) {
            this.mInfoMenu.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        addPointOfMenuItem(item);
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            case R.id.power_detail_menu_one:
                ActivityManager activityManager = (ActivityManager) getSystemService("activity");
                if (activityManager != null) {
                    if (this.mIsOtherUser) {
                        activityManager.forceStopPackageAsUser(this.mPkgName, UserHandle.getUserId(this.mSpecialUid));
                    } else {
                        activityManager.forceStopPackage(this.mPkgName);
                    }
                    ProviderWrapper.updateWakeupNumDBSingle(getApplicationContext(), this.mPkgName);
                    this.mStopMenu.setEnabled(false);
                    invalidateOptionsMenu();
                    return true;
                }
                break;
            case R.id.power_detail_menu_two:
                Intent intent = new Intent();
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                intent.setData(Uri.fromParts("package", this.mPkgName, null));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    HwLog.e(TAG, "ActivityNotFoundException error!");
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addPointOfMenuItem(MenuItem item) {
        String statParam;
        if (this.mUid == 0) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, DetailExtConst.SOFTWARE_ANDROID_OS);
        } else if (PointerIconCompat.TYPE_ALL_SCROLL == this.mUid) {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, DetailExtConst.SOFTWARE_MEDIA);
        } else {
            statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, this.mPkgName);
        }
        switch (item.getItemId()) {
            case R.id.power_detail_menu_one:
                HsmStat.statE((int) Events.E_POWER_DETAIL_CLOSE_CLICK, statParam);
                return;
            case R.id.power_detail_menu_two:
                HsmStat.statE((int) Events.E_POWER_DETAIL_INFO_CLICK, statParam);
                return;
            default:
                return;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.mPkgName != null) {
            try {
                this.mInfo = PackageManagerWrapper.getPackageInfo(getPackageManager(), this.mPkgName, 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
                finish();
            }
        }
        if (this.mExtraData == null) {
            finish();
            return;
        }
        invalidateOptionsMenu();
        if (this.mIsFromSettings) {
            showWaitingDialog();
        }
        new AsyncLoaderTask().execute(new Void[0]);
    }

    private void retrieveIntentData() {
        this.mIsFromSettings = false;
        try {
            Intent intent = getIntent();
            Preconditions.checkNotNull(intent);
            this.mExtraData = intent.getExtras();
            Preconditions.checkNotNull(this.mExtraData);
            if (-1 != this.mExtraData.getInt("uid", -1)) {
                this.mIsFromSettings = true;
                this.mPkgName = this.mExtraData.getString("package");
                this.mUid = this.mExtraData.getInt("uid");
                HwLog.i(TAG, "intent is come from Settings, mPkgName= " + this.mPkgName + " , mUid= " + this.mUid);
            } else {
                this.enterType = this.mExtraData.getInt(DetailExtConst.ENTER_INTO_DETAIL_TYPE_KEY);
                this.mPkgName = this.mExtraData.getString(SoftDetailKey.EXT_SOFT_DETAIL_PACKAGE_NAME);
                this.mShowBgView = this.mExtraData.getBoolean(SoftDetailKey.EXT_SOFT_DETAIL_SHOW_BG_VIEW);
                this.mUid = this.mExtraData.getInt(SoftDetailKey.EXT_SOFT_DETAIL_UID);
                HwLog.i(TAG, "intent is come from HwSystemManger, mPkgName= " + this.mPkgName + " , mUid= " + this.mUid + " , mShowBgView= " + this.mShowBgView + ",enterType= " + this.enterType);
            }
        } catch (Exception ex) {
            HwLog.e(TAG, "retrieveIntent catch exception: " + ex.getMessage());
            ex.printStackTrace();
        }
        if (SpecialAppUid.isOtherUserUid(SpecialAppUid.collapseUidsTogether(this.mUid, ActivityManager.getCurrentUser()))) {
            this.mSpecialUid = this.mUid;
            this.mIsOtherUser = true;
        }
    }

    private void findAllViews() {
        findBasicViews();
        findSwitchViews();
    }

    private void findBasicViews() {
        this.basicLayoutList = new LinkedList();
        this.mPkgLabel = (TextView) findViewById(R.id.app_name);
        this.mPkgVersion = (TextView) findViewById(R.id.app_version);
        this.mIconView = (ImageView) findViewById(R.id.app_icon);
        this.mPreventSleepTime = (TextView) findViewById(R.id.prevent_system_sleep_value);
        this.mPreventSleepTimeLayout = (RelativeLayout) findViewById(R.id.prevent_system_sleep_layout);
        this.mGpsTimeLayout = (RelativeLayout) findViewById(R.id.gps_layout);
        this.mGpsValue = (TextView) findViewById(R.id.gps_value);
        this.mWifiRunningTimeLayout = (RelativeLayout) findViewById(R.id.wifi_layout);
        this.mWifiRunningTimeValue = (TextView) findViewById(R.id.wifi_value);
        this.mCpuTimeFgOrBgLayout = (RelativeLayout) findViewById(R.id.cpu_time_front_layout);
        this.mCpuFgOrBgTitle = (TextView) findViewById(R.id.cpu_time_front);
        this.mCpuTimeFgOrBgValue = (TextView) findViewById(R.id.cpu_time_front_value);
        this.mCpuTimeValue = (TextView) findViewById(R.id.cpu_time_value);
        this.mCpuTimeValueLayout = (RelativeLayout) findViewById(R.id.cpu_time_layout);
        this.mHintSwitch = (Switch) findViewById(R.id.consumption_hint_switch);
        this.mHintWakeupSwitch = (Switch) findViewById(R.id.cpu_wakeup_switch);
        this.mDataSendLayout = (RelativeLayout) findViewById(R.id.data_send_layout);
        this.mDataSendValue = (TextView) findViewById(R.id.data_send_value);
        this.mDataRecvLayout = (RelativeLayout) findViewById(R.id.data_recv_layout);
        this.mDataRecvValue = (TextView) findViewById(R.id.data_recv_value);
        this.mWlanSendLayout = (RelativeLayout) findViewById(R.id.wlan_send_layout);
        this.mWlanSendValue = (TextView) findViewById(R.id.wlan_send_value);
        this.mWlanRecvLayout = (RelativeLayout) findViewById(R.id.wlan_recv_layout);
        this.mWlanRecvValue = (TextView) findViewById(R.id.wlan_recv_value);
        this.mShareAppsLayout = (LinearLayout) findViewById(R.id.shareAppsLayout);
        this.mShareAppsLabelContainer = (LinearLayout) findViewById(R.id.shareAppsLabelContainer);
        this.mPowerConsumptionLayout = (RelativeLayout) findViewById(R.id.power_consumption_layout);
        this.mPowerConsumptionValue = (TextView) findViewById(R.id.power_consumption_value);
        this.mPowerValueDes = (TextView) findViewById(R.id.power_consumption);
        this.hintSection = (TextView) findViewById(R.id.hint_switch_textview);
        this.mSharedUidDes = (TextView) findViewById(R.id.shareAppsTitle);
        this.basicLayoutList.add(this.mCpuTimeValueLayout);
        this.basicLayoutList.add(this.mCpuTimeFgOrBgLayout);
        this.basicLayoutList.add(this.mGpsTimeLayout);
        this.basicLayoutList.add(this.mWifiRunningTimeLayout);
        this.basicLayoutList.add(this.mPreventSleepTimeLayout);
        this.basicLayoutList.add(this.mDataSendLayout);
        this.basicLayoutList.add(this.mDataRecvLayout);
        this.basicLayoutList.add(this.mWlanSendLayout);
        this.basicLayoutList.add(this.mWlanRecvLayout);
        this.basicLayoutList.add(this.mPowerConsumptionLayout);
    }

    private void findSwitchViews() {
        this.switchLayoutList = new LinkedList();
        this.mAutoBootupLayout = (RelativeLayout) findViewById(R.id.auto_boot_switch_layout);
        this.mKeepBgLayout = (RelativeLayout) findViewById(R.id.keep_running_switch_layout);
        this.mHintSwitchLayout = (RelativeLayout) findViewById(R.id.hint_switch_layout);
        this.mHintCpuWakeupSwitchLayout = (RelativeLayout) findViewById(R.id.cpu_wakeup_switch_layout);
        this.switchLayoutList.add(this.mHintSwitchLayout);
        this.switchLayoutList.add(this.mHintCpuWakeupSwitchLayout);
        this.switchLayoutList.add(this.mAutoBootupLayout);
        this.switchLayoutList.add(this.mKeepBgLayout);
    }

    private void setAppSwitchListener() {
        this.mKeepBgAppSwitch = (Switch) findViewById(R.id.keep_running_switch);
        this.mAutoBootAppSwitch = (Switch) findViewById(R.id.auto_boot_switch);
        this.mKeepBgLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DetailOfSoftConsumptionActivity.this.mKeepBgAppSwitch.isEnabled()) {
                    DetailOfSoftConsumptionActivity.this.mKeepBgAppSwitch.setChecked(!DetailOfSoftConsumptionActivity.this.mKeepBgAppState);
                }
            }
        });
        this.mKeepBgAppSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (DetailOfSoftConsumptionActivity.this.mProtectListControl != null) {
                        DetailOfSoftConsumptionActivity.this.mProtectListControl.setNoProtect(DetailOfSoftConsumptionActivity.this.mPkgName);
                    }
                } else if (DetailOfSoftConsumptionActivity.this.mProtectListControl != null) {
                    DetailOfSoftConsumptionActivity.this.mProtectListControl.setProtect(DetailOfSoftConsumptionActivity.this.mPkgName);
                }
                if (DetailOfSoftConsumptionActivity.this.mKeepBgAppState != isChecked) {
                    String[] strArr = new String[4];
                    strArr[0] = HsmStatConst.PARAM_KEY;
                    strArr[1] = DetailOfSoftConsumptionActivity.this.mPkgName;
                    strArr[2] = HsmStatConst.PARAM_VAL;
                    strArr[3] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_POWER_DETAIL_SCREEN_OFF_RUN, HsmStatConst.constructJsonParams(strArr));
                    DetailOfSoftConsumptionActivity.this.mKeepBgAppState = isChecked;
                }
            }
        });
        this.mAutoBootupLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DetailOfSoftConsumptionActivity.this.mAutoBootAppSwitch.isEnabled()) {
                    DetailOfSoftConsumptionActivity.this.mAutoBootAppSwitch.setChecked(!DetailOfSoftConsumptionActivity.this.mAutoBootAppState);
                }
            }
        });
        this.mAutoBootAppSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StartupDataMgrHelper.modifyNormalStartupInfoStatus(DetailOfSoftConsumptionActivity.this.getApplicationContext(), DetailOfSoftConsumptionActivity.this.mPkgName, isChecked);
                if (DetailOfSoftConsumptionActivity.this.mAutoBootAppState != isChecked) {
                    String[] strArr = new String[4];
                    strArr[0] = HsmStatConst.PARAM_KEY;
                    strArr[1] = DetailOfSoftConsumptionActivity.this.mPkgName;
                    strArr[2] = HsmStatConst.PARAM_VAL;
                    strArr[3] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_POWER_DETAIL_AUTO_STARTUP, HsmStatConst.constructJsonParams(strArr));
                    DetailOfSoftConsumptionActivity.this.mAutoBootAppState = isChecked;
                }
            }
        });
    }

    private void setAllViewStateAndValues() {
        if (this.mShowBgView) {
            this.hintSection.setVisibility(0);
        }
        setAppSwitchListener();
        setPackageBasicViews();
        setSwitchSettings();
        setStatisticData();
        updateShareUidLableLayout();
        modifyLayout();
    }

    private void setPackageBasicViews() {
        this.mPkgLabel.setText(this.mSoftLabel);
        this.mPkgVersion.setText(this.mSoftVersion);
        setIcon();
    }

    private void setIcon() {
        if (this.mIsOtherUser) {
            Drawable drawable = SpecialAppUid.getBadgedIconOfSpecialUidApp(this.mSoftIcon, UserHandle.getUserId(this.mSpecialUid));
            if (drawable != null) {
                this.mSoftIcon = drawable;
            }
        }
        this.mIconView.setImageDrawable(this.mSoftIcon);
    }

    private void setSwitchSettings() {
        if (Strings.isNullOrEmpty(this.mPkgName)) {
            this.mHintSwitchLayout.setVisibility(8);
            this.mHintCpuWakeupSwitchLayout.setVisibility(8);
            return;
        }
        setInitSwitchState();
        setHintSwtich();
        setHighWakeFreqState();
    }

    private void setStatisticData() {
        setConsumeAnalyseTimeItem(this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_PREVENT_SLEEP_TIME), this.mPreventSleepTimeLayout, this.mPreventSleepTime);
        long data_send_bytes = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_DATA_SEND);
        setViewVisible(data_send_bytes == 0, this.mDataSendLayout);
        setByteTransfer(data_send_bytes, this.mDataSendValue);
        long data_recv_bytes = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_DATA_RECV);
        setViewVisible(data_recv_bytes == 0, this.mDataRecvLayout);
        setByteTransfer(data_recv_bytes, this.mDataRecvValue);
        long wlan_send_bytes = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_WLAN_SEND);
        setViewVisible(wlan_send_bytes == 0, this.mWlanSendLayout);
        setByteTransfer(wlan_send_bytes, this.mWlanSendValue);
        long wlan_recv_bytes = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_WLAN_RECV);
        setViewVisible(wlan_recv_bytes == 0, this.mWlanRecvLayout);
        setByteTransfer(wlan_recv_bytes, this.mWlanRecvValue);
        long wifi_running_time = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_WIFI_RUNNING_TIME);
        if (wifi_running_time < 1000) {
            this.mWifiRunningTimeValue.setText(String.format(getResources().getString(R.string.power_time_under_s_new), new Object[]{Integer.valueOf(1)}));
            this.mWifiRunningTimeLayout.setVisibility(0);
        } else {
            setConsumeAnalyseTimeItem(wifi_running_time, this.mWifiRunningTimeLayout, this.mWifiRunningTimeValue);
        }
        long gps_time = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_GPS_TIME);
        HwLog.i(TAG, "gps_time = " + gps_time);
        if (gps_time < 1000) {
            this.mGpsValue.setText(String.format(getResources().getString(R.string.power_time_under_s_new), new Object[]{Integer.valueOf(1)}));
            this.mGpsTimeLayout.setVisibility(0);
        } else {
            setConsumeAnalyseTimeItem(gps_time, this.mGpsTimeLayout, this.mGpsValue);
        }
        long cpuTime = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_CPU_TIME);
        if (cpuTime < 1000) {
            this.mCpuTimeValue.setText(String.format(getResources().getString(R.string.power_time_under_s_new), new Object[]{Integer.valueOf(1)}));
            this.mCpuTimeValueLayout.setVisibility(0);
        } else {
            setConsumeAnalyseTimeItem(cpuTime, this.mCpuTimeValueLayout, this.mCpuTimeValue);
        }
        if (this.enterType == 1) {
            this.mCpuTimeValueLayout.setVisibility(8);
        }
        long cpuFgTime = this.mExtraData.getLong(SoftDetailKey.EXT_SOFT_DETAIL_CPU_FRONT_TIME);
        if (this.mShowBgView) {
            long cpuBgTime = cpuTime - cpuFgTime;
            this.mCpuFgOrBgTitle.setText(getString(R.string.cpu_bg));
            setConsumeAnalyseTimeItem(cpuBgTime, this.mCpuTimeFgOrBgLayout, this.mCpuTimeFgOrBgValue);
        } else {
            setConsumeAnalyseTimeItem(cpuFgTime, this.mCpuTimeFgOrBgLayout, this.mCpuTimeFgOrBgValue);
        }
        if (this.mUid == 0) {
            setShowConsumePowerValue();
        } else {
            if (1013 == this.mUid) {
                setShowConsumePowerValue();
            } else {
                String[] packages = getPackageManager().getPackagesForUid(this.mUid);
                if (packages == null) {
                    HwLog.e(TAG, "get packages from mUid is null!" + this.mUid);
                } else {
                    if (1 == packages.length) {
                        setShowConsumePowerValue();
                    }
                }
            }
        }
    }

    private void modifyLayout() {
        checkLastLayout(this.switchLayoutList);
        if (this.mSharedUidDes.isShown()) {
            checkLastLayout(this.basicLayoutList);
        }
    }

    private void checkLastLayout(LinkedList<RelativeLayout> list) {
        if (list != null && list.size() != 0) {
            while (list.size() > 0 && list.getLast() != null) {
                PowerEmuiRelativeLayout pl = (PowerEmuiRelativeLayout) list.getLast();
                if (pl.isShown()) {
                    pl.setBottomLineVisibility(8);
                    return;
                }
                list.removeLast();
            }
        }
    }

    private void setShowConsumePowerValue() {
        this.mPowerConsumptionLayout.setVisibility(0);
        if (this.enterType == 1) {
            this.mPowerValueDes.setText(getString(R.string.power_background_consumption_size));
            this.mPowerConsumptionLayout.setVisibility(8);
        } else if (this.enterType == 2) {
            this.mPowerValueDes.setText(getString(R.string.power_consumption_size));
        }
        if (ZEROINDOUBLE.equals(Conversion.formatPower(this.mExtraData.getDouble(SoftDetailKey.EXT_SOFT_DETAIL_POWER_VALUE)))) {
            this.mPowerConsumptionValue.setText(getString(R.string.power_consume_detail_shared_uid_empty_data));
            return;
        }
        this.mPowerConsumptionValue.setText(String.format(getString(R.string.mah), new Object[]{power}));
    }

    private void setConsumeAnalyseTimeItem(long millisecond, RelativeLayout layout, TextView tv) {
        long second = millisecond / 1000;
        setViewVisible(second == 0, layout);
        if (0 != second) {
            tv.setText(formatSecondToTime(second));
        }
    }

    private void setViewVisible(boolean isInVisible, RelativeLayout layout) {
        layout.setVisibility(isInVisible ? 8 : 0);
    }

    private void setByteTransfer(long load, TextView view) {
        view.setText(Formatter.formatFileSize(this, load));
    }

    private void switchWakeupDialog(boolean isChecked) {
        if (isChecked) {
            SavingSettingUtil.setRogue(getContentResolver(), this.mPkgName, 5, Integer.valueOf(0));
        } else {
            this.mDialog = new Builder(this).setTitle(getResources().getString(R.string.power_cpu_wakeup_dialog_title)).setMessage(getResources().getString(R.string.power_cpu_wakeup_dialog_msg)).setPositiveButton(getResources().getString(R.string.power_cpu_wakeup_dialog_close), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SavingSettingUtil.setRogue(DetailOfSoftConsumptionActivity.this.getContentResolver(), DetailOfSoftConsumptionActivity.this.mPkgName, 5, Integer.valueOf(1));
                }
            }).setNegativeButton(getString(R.string.alert_dialog_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    DetailOfSoftConsumptionActivity.this.mHintWakeupSwitch.setChecked(true);
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    DetailOfSoftConsumptionActivity.this.mHintWakeupSwitch.setChecked(true);
                }
            }).show();
        }
    }

    private void setHintSwtich() {
        Integer ignore = (Integer) SavingSettingUtil.getRogue(getContentResolver(), this.mPkgName, 1);
        this.mHintState = true;
        if (ignore == null) {
            this.mHintState = true;
            ContentValues values = new ContentValues();
            values.put("pkgname", this.mPkgName);
            values.put(ROGUE_Columns.ISROGUE, Integer.valueOf(0));
            values.put(ROGUE_Columns.IGNORE, Integer.valueOf(0));
            values.put(ROGUE_Columns.CLEAR, Integer.valueOf(0));
            values.put(ROGUE_Columns.PRESETBLACKAPP, Integer.valueOf(0));
            values.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(0));
            values.put(ROGUE_Columns.IGNOREWAKEUPAPP, Integer.valueOf(0));
            SavingSettingUtil.insertRogue(getContentResolver(), this.mPkgName, values);
        } else if (ignore.intValue() == 1) {
            this.mHintState = false;
            HwLog.d(TAG, this.mPkgName + " PACKAGE_FIELD_IGNORE=" + ignore.intValue());
        }
        this.mHintSwitch.setChecked(this.mHintState);
        this.mHintSwitch.setEnabled(SharePrefWrapper.getPrefValue(getApplicationContext(), SharedPrefKeyConst.POWER_SETTINGS_SHAREDPREF_NAME, SharedPrefKeyConst.POWER_INTENSIVE_PRMOPT_SWITCH_KEY, true));
        if (this.mShowBgView) {
            this.mHintSwitchLayout.setVisibility(0);
        }
        this.mHintSwitchLayout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DetailOfSoftConsumptionActivity.this.mHintSwitch.isEnabled()) {
                    DetailOfSoftConsumptionActivity.this.mHintSwitch.setChecked(!DetailOfSoftConsumptionActivity.this.mHintState);
                }
            }
        });
        this.mHintSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SavingSettingUtil.setRogue(DetailOfSoftConsumptionActivity.this.getContentResolver(), DetailOfSoftConsumptionActivity.this.mPkgName, 1, Integer.valueOf(0));
                } else {
                    SavingSettingUtil.setRogue(DetailOfSoftConsumptionActivity.this.getContentResolver(), DetailOfSoftConsumptionActivity.this.mPkgName, 1, Integer.valueOf(1));
                }
                if (DetailOfSoftConsumptionActivity.this.mHintState != isChecked) {
                    String[] strArr = new String[4];
                    strArr[0] = HsmStatConst.PARAM_KEY;
                    strArr[1] = DetailOfSoftConsumptionActivity.this.mPkgName;
                    strArr[2] = HsmStatConst.PARAM_VAL;
                    strArr[3] = isChecked ? "1" : "0";
                    HsmStat.statE((int) Events.E_POWER_DETAIL_HIGHPOWER_REMINDER, HsmStatConst.constructJsonParams(strArr));
                }
                DetailOfSoftConsumptionActivity.this.mHintState = isChecked;
            }
        });
    }

    private void setHighWakeFreqState() {
        Integer highwakefreq = (Integer) SavingSettingUtil.getRogue(getContentResolver(), this.mPkgName, 4);
        this.mHintWakeupState = true;
        if (highwakefreq == null) {
            this.mHintWakeupState = true;
            ContentValues values = new ContentValues();
            values.put("pkgname", this.mPkgName);
            values.put(ROGUE_Columns.ISROGUE, Integer.valueOf(0));
            values.put(ROGUE_Columns.IGNORE, Integer.valueOf(0));
            values.put(ROGUE_Columns.CLEAR, Integer.valueOf(0));
            values.put(ROGUE_Columns.PRESETBLACKAPP, Integer.valueOf(0));
            values.put(ROGUE_Columns.HIGHWAKEUPFREQ, Integer.valueOf(0));
            values.put(ROGUE_Columns.IGNOREWAKEUPAPP, Integer.valueOf(0));
            SavingSettingUtil.insertRogue(getContentResolver(), this.mPkgName, values);
        } else if (highwakefreq.intValue() == 1) {
            Integer ignorewakeup = (Integer) SavingSettingUtil.getRogue(getContentResolver(), this.mPkgName, 5);
            if (ignorewakeup != null && ignorewakeup.intValue() == 1) {
                this.mHintWakeupState = false;
                HwLog.d(TAG, this.mPkgName + " PACKAGE_FIELD_IGNOREWAKEUP=" + ignorewakeup.intValue());
            }
            if (this.mDialog == null || !this.mDialog.isShowing()) {
                this.mHintWakeupSwitch.setChecked(this.mHintWakeupState);
            }
            HwLog.d(TAG, "mHintWakeupSwitch mHintWakeupState" + this.mHintWakeupState);
            this.mHintCpuWakeupSwitchLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (DetailOfSoftConsumptionActivity.this.mHintWakeupSwitch.isEnabled()) {
                        DetailOfSoftConsumptionActivity.this.mHintWakeupSwitch.setChecked(!DetailOfSoftConsumptionActivity.this.mHintWakeupState);
                    }
                }
            });
            this.mHintWakeupSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DetailOfSoftConsumptionActivity.this.switchWakeupDialog(isChecked);
                    if (DetailOfSoftConsumptionActivity.this.mHintWakeupState != isChecked) {
                        String[] strArr = new String[4];
                        strArr[0] = HsmStatConst.PARAM_KEY;
                        strArr[1] = DetailOfSoftConsumptionActivity.this.mPkgName;
                        strArr[2] = HsmStatConst.PARAM_VAL;
                        strArr[3] = isChecked ? "1" : "0";
                        HsmStat.statE((int) Events.E_POWER_DETAIL_TIME_WAKEUP, HsmStatConst.constructJsonParams(strArr));
                    }
                    DetailOfSoftConsumptionActivity.this.mHintWakeupState = isChecked;
                }
            });
            if (this.mShowBgView) {
                this.mHintCpuWakeupSwitchLayout.setVisibility(0);
            }
        } else {
            this.mHintCpuWakeupSwitchLayout.setVisibility(8);
        }
    }

    private boolean isLogUploadAppPackageName(String pkgname) {
        return "com.huawei.logupload".equals(pkgname);
    }

    private void loadSharedUidPackageNameList(int uid) {
        this.mSharedLabelList.clear();
        String[] packages = getPackageManager().getPackagesForUid(uid);
        if (packages == null) {
            HwLog.e(TAG, "get packages from uid is null!" + uid);
            return;
        }
        for (String pkgname : packages) {
            if (!isLogUploadAppPackageName(pkgname)) {
                LabelAndPower labelAndPower = new LabelAndPower();
                String label = HsmPackageManager.getInstance().getLabel(pkgname);
                labelAndPower.label = label;
                if (-1.0d != this.mExtraData.getDouble(pkgname, -1.0d)) {
                    labelAndPower.power = this.mExtraData.getDouble(pkgname);
                } else {
                    labelAndPower.power = 0.0d;
                }
                this.mSharedLabelList.add(labelAndPower);
                HwLog.d(TAG, "pkgname = " + pkgname + "label = " + label + "power = " + labelAndPower.power);
            }
        }
        Collections.sort(this.mSharedLabelList, new Cmp());
    }

    private String formatSecondToTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        String string;
        Object[] objArr;
        if (h > 0) {
            string = getResources().getString(R.string.power_time_h_m_s_connect);
            objArr = new Object[3];
            objArr[0] = getResources().getQuantityString(R.plurals.power_time_hour_array, (int) h, new Object[]{Long.valueOf(h)});
            objArr[1] = getResources().getQuantityString(R.plurals.power_time_min_array, (int) m, new Object[]{Long.valueOf(m)});
            objArr[2] = getResources().getQuantityString(R.plurals.power_time_s_array, (int) s, new Object[]{Long.valueOf(s)});
            return String.format(string, objArr);
        } else if (m > 0) {
            string = getResources().getString(R.string.power_time_connect);
            objArr = new Object[2];
            objArr[0] = getResources().getQuantityString(R.plurals.power_time_min_array, (int) m, new Object[]{Long.valueOf(m)});
            objArr[1] = getResources().getQuantityString(R.plurals.power_time_s_array, (int) s, new Object[]{Long.valueOf(s)});
            return String.format(string, objArr);
        } else {
            return getResources().getQuantityString(R.plurals.power_time_s_array, (int) s, new Object[]{Long.valueOf(s)});
        }
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void hideWaitingDialog() {
        if (this.mWaitingDialog != null) {
            if (!(!this.mWaitingDialog.isShowing() || isFinishing() || isDestroyed())) {
                this.mWaitingDialog.dismiss();
            }
            this.mWaitingDialog = null;
        }
    }
}
