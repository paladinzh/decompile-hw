package com.huawei.systemmanager.startupmgr.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.optimize.bootstart.BootStartManager;
import com.huawei.systemmanager.rainbow.client.base.GetAppListBasic.CloudUpdateAction;
import com.huawei.systemmanager.startupmgr.comm.AbsRecordInfo;
import com.huawei.systemmanager.startupmgr.comm.AwakedStartupInfo;
import com.huawei.systemmanager.startupmgr.comm.FwkAwakedStartInfo;
import com.huawei.systemmanager.startupmgr.comm.StartupBinderAccess;
import com.huawei.systemmanager.startupmgr.comm.StartupFwkConst;
import com.huawei.systemmanager.startupmgr.comm.SysCallUtils;
import com.huawei.systemmanager.startupmgr.db.AwakedRecordTable;
import com.huawei.systemmanager.startupmgr.db.NormalRecordTable;
import com.huawei.systemmanager.startupmgr.db.StartupDataMgrHelper;
import com.huawei.systemmanager.startupmgr.localize.LocalizePackageWrapper;
import com.huawei.systemmanager.startupmgr.ui.StartupAwakedAppListActivity;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener;

public class StartupResidentService extends Service implements IPackageChangeListener {
    static final String AWAKE_NOTIFICATION_CLICK = "com.huawei.android.hsm.AWAKE_NOTIFICATION_CLICK";
    static final String AWAKE_NOTIFICATION_DELETE = "com.huawei.android.hsm.AWAKE_NOTIFICATION_DELETE";
    private static final int MSG_AWAKED_STARTUP_FIRST_CONFIRM = 8;
    private static final int MSG_CODE_PACKAGE_ADDED = 3;
    private static final int MSG_CODE_PACKAGE_INSTALL_SET = 10;
    private static final int MSG_CODE_PACKAGE_REMOVED = 4;
    private static final int MSG_CODE_PACKAGE_REPLACED = 5;
    private static final int MSG_CODE_POLICY_ENABLED = 6;
    private static final int MSG_CODE_STARTUP_CLOUD_DATE_UPDATE = 9;
    private static final int MSG_CONSISTENCY_CHECK = 1;
    private static final int MSG_LOAD_DEFAULT_VALUE = 0;
    private static final int MSG_RECORD_AWAKE_STARTUP_RESULT = 2;
    private static final int MSG_STARTUP_RECORD = 7;
    static final String PACKAGE_INSTALL_SET_ACTION = "com.huawei.install.permission";
    private static final String TAG = "StartupResidentService";
    private Context mAppContext = null;
    private AwakedFirstConfirm mAwakedConfirm = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread("StartupService");
    private StartupPreSettingData mPreData = null;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                if (CloudUpdateAction.STARTUP_DATA_UPDATE_ACTION.equals(action)) {
                    StartupResidentService.this.mHandler.sendEmptyMessage(9);
                } else if (StartupResidentService.PACKAGE_INSTALL_SET_ACTION.equals(action)) {
                    if (SysCallUtils.checkUser()) {
                        StartupResidentService.this.mHandler.sendMessageDelayed(StartupResidentService.this.mHandler.obtainMessage(10, intent.getIntExtra("APP_STARTUP_SET", -1), 0, intent.getStringExtra("packageName")), 2000);
                    } else {
                        HwLog.w(StartupResidentService.TAG, "check user failed, when receive action:" + action);
                        return;
                    }
                }
                return;
            }
            HwLog.w(StartupResidentService.TAG, "onReceive invalid intent, ignore it");
        }
    };
    private int mRecordMsgCount = 0;

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            HwLog.i(StartupResidentService.TAG, "InnerHandler Message:" + msg.what);
            switch (msg.what) {
                case 0:
                    StartupResidentService.this.loadPreSettingData();
                    return;
                case 1:
                    StartupResidentService.this.checkConsistency();
                    return;
                case 2:
                    StartupResidentService.this.recordAwakeStartupResult((FwkAwakedStartInfo) msg.obj);
                    return;
                case 3:
                    StartupResidentService.this.handlePackageAdd((String) msg.obj);
                    return;
                case 4:
                    StartupResidentService.this.handlePackageRemoved((String) msg.obj);
                    return;
                case 5:
                    StartupResidentService.this.handlePackageChanged((String) msg.obj);
                    return;
                case 6:
                    StartupResidentService.this.handleSetPolicyEnabled();
                    return;
                case 7:
                    StartupResidentService.this.handleStartupRecord((AbsRecordInfo) msg.obj);
                    return;
                case 8:
                    StartupResidentService.this.handleAwakedConfirm((FwkAwakedStartInfo) msg.obj);
                    return;
                case 9:
                    StartupResidentService.this.handleStartupCloudDataUpdate();
                    return;
                case 10:
                    StartupResidentService.this.handlePackageInstallSet(msg.arg1, (String) msg.obj);
                    return;
                default:
                    return;
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        HwLog.v(TAG, "onCreate");
        super.onCreate();
        registerObserverAndReceiver();
        this.mAppContext = getApplicationContext();
        this.mHandlerThread.start();
        this.mHandler = new InnerHandler(this.mHandlerThread.getLooper());
        this.mHandler.sendEmptyMessage(0);
        this.mHandler.sendEmptyMessage(1);
        this.mHandler.sendEmptyMessage(6);
        this.mAwakedConfirm = new AwakedFirstConfirm(this.mHandler);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            Bundle bundle;
            if (StartupFwkConst.ACTION_AWAKED_STARTUP_RESULT.equals(action)) {
                bundle = intent.getExtras();
                if (bundle != null) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(2, new FwkAwakedStartInfo(bundle.getString(StartupFwkConst.KEY_TARGET_PACKAGE), bundle.getString("B_RECORD_TYPE"), bundle.getInt(StartupFwkConst.KEY_CALLER_PID), bundle.getInt(StartupFwkConst.KEY_CALLER_UID), bundle.getBoolean(StartupFwkConst.KEY_ALLOW_STARTUP))));
                }
            } else if (StartupFwkConst.ACTION_STARTUP_RECORD.equals(action)) {
                bundle = intent.getExtras();
                if (bundle != null) {
                    AbsRecordInfo record = AbsRecordInfo.createRecordFromBundle(getApplicationContext(), bundle);
                    if (record != null) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, record));
                    } else {
                        HwLog.w(TAG, "AbsRecordInfo.createFromBundle null return, ignore this action.");
                    }
                }
            } else if (StartupFwkConst.ACTION_AWAKED_STARTUP_CONFIRM.equals(action)) {
                bundle = intent.getExtras();
                if (bundle != null) {
                    HwLog.d(TAG, "onStartCommand got valid action: com.huawei.android.hsm.STARTUP_CONFIRM");
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(8, new FwkAwakedStartInfo(bundle.getString(StartupFwkConst.KEY_TARGET_PACKAGE), bundle.getString("B_RECORD_TYPE"), bundle.getInt(StartupFwkConst.KEY_CALLER_PID), bundle.getInt(StartupFwkConst.KEY_CALLER_UID))));
                }
            } else if (AWAKE_NOTIFICATION_CLICK.equals(action)) {
                HwLog.d(TAG, "onStartCommand of com.huawei.android.hsm.AWAKE_NOTIFICATION_CLICK");
                this.mAwakedConfirm.cleanConfirmedCached();
                this.mAwakedConfirm.cancelAwakedNotification(this.mAppContext);
                Intent jumpActivity = new Intent(this.mAppContext, StartupAwakedAppListActivity.class);
                jumpActivity.setFlags(335544320);
                this.mAppContext.startActivity(jumpActivity);
            } else if (AWAKE_NOTIFICATION_DELETE.equals(action)) {
                HwLog.d(TAG, "onStartCommand of com.huawei.android.hsm.AWAKE_NOTIFICATION_DELETE");
                this.mAwakedConfirm.cleanConfirmedCached();
                this.mAwakedConfirm.cancelAwakedNotification(this.mAppContext);
            }
        }
        return 1;
    }

    public void onDestroy() {
        HwLog.v(TAG, "onDestroy");
        unregisterObserverAndReceiver();
        super.onDestroy();
    }

    public void onPackagedAdded(String pkgName) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, pkgName));
    }

    public void onPackageRemoved(String pkgName) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, pkgName));
    }

    public void onPackageChanged(String pkgName) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, pkgName));
    }

    public void onExternalChanged(String[] packages, boolean available) {
    }

    private void registerObserverAndReceiver() {
        HsmPackageManager.registerListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(CloudUpdateAction.STARTUP_DATA_UPDATE_ACTION);
        filter.addAction(PACKAGE_INSTALL_SET_ACTION);
        registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void unregisterObserverAndReceiver() {
        unregisterReceiver(this.mReceiver);
        HsmPackageManager.unregisterListener(this);
    }

    private void loadPreSettingData() {
        if (this.mPreData == null) {
            this.mPreData = new StartupPreSettingData();
        }
        this.mPreData.loadPreSetting(this.mAppContext);
    }

    private void checkConsistency() {
        HwLog.i(TAG, "checkConsistency called");
        BootStartManager.getInstance(this).onCreate();
        new StartupConsistency(this.mPreData).checkConsistency(this.mAppContext);
        LocalizePackageWrapper.resetAllLocalizeTableData(this.mAppContext);
    }

    private void recordAwakeStartupResult(FwkAwakedStartInfo info) {
        AwakedStartupInfo toUpdateInfo = StartupDataMgrHelper.querySingleAwakedStartupInfo(this.mAppContext, info.mPkgName);
        if (toUpdateInfo != null) {
            toUpdateInfo.setLastCaller(SysCallUtils.getPackageByPidUid(this.mAppContext, info.mCallerPid, info.mCallerUid));
            if (toUpdateInfo.validInfo()) {
                toUpdateInfo.updateCallerInfoToDB(this.mAppContext);
                return;
            } else {
                HwLog.w(TAG, "recordAwakeStartupResult invalid AwakedStartupInfo " + toUpdateInfo);
                return;
            }
        }
        HwLog.w(TAG, "recordAwakeStartupResult null return while query from database: " + info);
    }

    private void handlePackageAdd(String pkgName) {
        HwLog.i(TAG, "handlePackageAdd, pkg:" + pkgName);
        new StartupConsistency(this.mPreData).loadNewPackageDefaultData(this.mAppContext, pkgName);
        LocalizePackageWrapper.resetSingleLocalizeInfo(this.mAppContext, pkgName);
    }

    private void handlePackageRemoved(String pkgName) {
        HwLog.i(TAG, "handlePackageRemoved, pkg:" + pkgName);
        StartupDataMgrHelper.deleteNotExistPackageData(this.mAppContext, pkgName);
        StartupBinderAccess.removeStartupSetting(pkgName);
        this.mAwakedConfirm.removeSingleConfirmedPackage(pkgName);
    }

    private void handlePackageChanged(String pkgName) {
        HwLog.i(TAG, "handlePackageChanged, pkg:" + pkgName);
        new StartupConsistency(this.mPreData).loadAndUpdateExistPackageData(this.mAppContext, pkgName);
    }

    private void handleSetPolicyEnabled() {
        StartupBinderAccess.setAutoStartupPolicyEnabled(CustomizeWrapper.isBootstartupEnabled());
    }

    private void handleStartupRecord(AbsRecordInfo info) {
        info.insertOrUpdateRecordCountToDB(this.mAppContext);
        int i = this.mRecordMsgCount + 1;
        this.mRecordMsgCount = i;
        if (1 == i % 1000) {
            StartupDataMgrHelper.checkRecordTable(this.mAppContext, NormalRecordTable.TABLE_NAME);
            StartupDataMgrHelper.checkRecordTable(this.mAppContext, AwakedRecordTable.TABLE_NAME);
        }
    }

    private void handleAwakedConfirm(FwkAwakedStartInfo info) {
        this.mAwakedConfirm.handlePkgFirstAwaked(this.mAppContext, info);
    }

    private void handleStartupCloudDataUpdate() {
        HwLog.i(TAG, "handleStartupCloudDataUpdate");
        new StartupConsistency(this.mPreData).handleCloudDataUpdate(this.mAppContext);
    }

    private void handlePackageInstallSet(int state, String pkgName) {
        boolean z = true;
        HwLog.i(TAG, "handlePackageInstallSet, pkg:" + pkgName + ", state:" + state);
        if (-1 == state || TextUtils.isEmpty(pkgName)) {
            HwLog.e(TAG, "handlePackageInstallSet invalid input " + state + SqlMarker.COMMA_SEPARATE + pkgName);
            return;
        }
        Context context = this.mAppContext;
        if (1 != state) {
            z = false;
        }
        StartupDataMgrHelper.modifyNormalStartupInfoStatus(context, pkgName, z);
    }
}
