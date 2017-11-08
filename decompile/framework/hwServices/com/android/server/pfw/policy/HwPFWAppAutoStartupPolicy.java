package com.android.server.pfw.policy;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.android.server.HwConnectivityService;
import com.android.server.PPPOEStateMachine;
import com.android.server.am.ActivityRecord;
import com.android.server.pfw.autostartup.comm.AutoStartupUtil;
import com.android.server.pfw.autostartup.datamgr.AutoStartupDataMgr;
import com.android.server.pfw.autostartup.datamgr.StopPackagesThread;
import com.android.server.pfw.log.HwPFWLogger;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import huawei.android.pfw.HwPFWStartupControlScope;
import huawei.android.pfw.HwPFWStartupSetting;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HwPFWAppAutoStartupPolicy extends HwPFWPolicy {
    private static final String ACTION_STARTUP_CONFIRM = "com.huawei.android.hsm.STARTUP_CONFIRM";
    private static final String ACTION_STARTUP_RECORD = "com.huawei.android.hsm.STARTUP_RECORD";
    private static final String ACTION_STARTUP_RESULT = "com.huawei.android.hsm.STARTUP_RESULT";
    private static final boolean DEBUG_POLICY_DETAIL = false;
    private static final long DELAY_TIME_ONE_MIN = 60000;
    private static final long DELAY_TIME_TEN_SECOND = 10000;
    private static final long DELAY_TIME_THIRTY_MIN = 1800000;
    private static final String HSM_PACKAGE_NAME = "com.huawei.systemmanager";
    public static final String HW_LAUNCHER_WIDGET_UPDATE = "com.huawei.android.launcher.action.GET_WIDGET";
    private static boolean MM_HOTA_AUTOSTART_HAS_DONE = false;
    private static final int MSG_CODE_CACHE_LAUNCHER_WIDGET_LIST = 8;
    private static final int MSG_CODE_DEVICE_RESTART = 3;
    private static final int MSG_CODE_INIT_FORBIDEN_ACTIVITY_SET = 9;
    private static final int MSG_CODE_INIT_SYSTEM_UID_SET = 2;
    private static final int MSG_CODE_NOTIFY_STARTUP_CONFIRM_TO_HSM = 5;
    private static final int MSG_CODE_NOTIFY_STARTUP_RESULT_TO_HSM = 6;
    private static final int MSG_CODE_PERIOD_DUMP = 1;
    private static final int MSG_CODE_SEND_STARTUP_RECORD_TO_HSM = 7;
    private static final int MSG_CODE_WRITE_CACHE_FILE = 4;
    private static final String RECORD_TYPE_PROVIDER = "p";
    private static final String RECORD_TYPE_RECEIVER = "r";
    private static final String RECORD_TYPE_SERVICE = "s";
    private static final int SETTINGS_SECURITY_OVERSEA_OPEN = 1;
    private static final String TAG = "AppAutoStartupPolicy";
    private AutoStartupDataMgr mDataMgr = new AutoStartupDataMgr();
    private boolean mFastCacheWriteScheduled = false;
    final Object mForbidListLock = new Object();
    private List<String> mForbidRestartApps = new ArrayList();
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread("AutoStartupPolicyThread");
    private boolean mRebootHandled = false;
    private List<String> mRestrictAutoStartApps = new ArrayList();
    private StopPackagesThread mStopPkgThread = null;

    private class StartupPolicyHandler extends Handler {
        public StartupPolicyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwPFWAppAutoStartupPolicy.this.periodDumpPolicyDataToLog();
                    return;
                case 2:
                    HwPFWAppAutoStartupPolicy.this.initSystemUidSet();
                    return;
                case 3:
                    HwPFWAppAutoStartupPolicy.this.handlerDeviceRestart();
                    return;
                case 4:
                    HwPFWAppAutoStartupPolicy.this.writeHsmCacheData();
                    return;
                case 5:
                    HwPFWAppAutoStartupPolicy.this.startConfirmService(msg);
                    return;
                case 6:
                    HwPFWAppAutoStartupPolicy.this.startResultService(msg);
                    return;
                case 7:
                    HwPFWAppAutoStartupPolicy.this.startRecordService(msg);
                    return;
                case 8:
                    HwPFWAppAutoStartupPolicy.this.cacheLauncherWidgetList();
                    return;
                case 9:
                    HwPFWAppAutoStartupPolicy.this.initForbidenActivitySet();
                    return;
                default:
                    return;
            }
        }
    }

    public HwPFWAppAutoStartupPolicy(Context context) {
        boolean z = true;
        super(context);
        this.mDataMgr.initData(this.mContext);
        this.mHandlerThread.start();
        this.mHandler = new StartupPolicyHandler(this.mHandlerThread.getLooper());
        this.mHandler.sendEmptyMessage(2);
        this.mHandler.sendEmptyMessage(9);
        int overSeaStatus = Secure.getInt(this.mContext.getContentResolver(), "overseaswitch_status", 0);
        if (!(SystemProperties.get("ro.config.hw_optb", PPPOEStateMachine.PHASE_DEAD).equals("156") || 1 == overSeaStatus)) {
            z = false;
        }
        setPolicyEnabled(z);
        this.mHandler.sendEmptyMessageDelayed(3, 10000);
    }

    public void handleBroadcastIntent(Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals("android.intent.action.BOOT_COMPLETED")) {
                this.mHandler.sendEmptyMessage(3);
            } else if (action.equals(HW_LAUNCHER_WIDGET_UPDATE)) {
                this.mHandler.sendEmptyMessage(8);
            }
        }
    }

    public void printDumpInfo(PrintWriter pw) {
        this.mDataMgr.dumpToDumpsys(pw);
    }

    public void startupFilterReceiverList(Intent intent, List<ResolveInfo> receivers) {
        if (isPolicyEnabled() || isNeedRestrict()) {
            String action = intent.getAction();
            if (!this.mDataMgr.whiteReceiverForAll(action)) {
                Iterator<ResolveInfo> iterator = receivers.iterator();
                while (iterator.hasNext()) {
                    ResolveInfo resolve = (ResolveInfo) iterator.next();
                    String targetPkg = resolve.activityInfo.applicationInfo.packageName;
                    if (!this.mDataMgr.isWhiteReceiverStartupRequest(resolve.activityInfo.applicationInfo) && shouldPreventPackage(targetPkg)) {
                        HwPFWLogger.i(TAG, "prevent start receiver of package " + targetPkg + " for action " + action);
                        iterator.remove();
                    }
                }
            }
        }
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid) {
        if ((!isPolicyEnabled() && !isNeedRestrict()) || this.mDataMgr.isWhiteStartupRequest(servInfo.applicationInfo, callerUid) || !shouldPreventPackage(servInfo.applicationInfo.packageName, RECORD_TYPE_SERVICE, callerPid, callerUid)) {
            return false;
        }
        HwPFWLogger.i(TAG, "prevent start service of package " + servInfo.applicationInfo.packageName + ", serviceInfo " + servInfo.name + " by callerPid " + callerPid + ", callerUid " + callerUid);
        return true;
    }

    public boolean shouldPreventStartActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record) {
        if (!isPolicyEnabled()) {
            return false;
        }
        if ((aInfo.applicationInfo != null && !AutoStartupUtil.isAppStopped(aInfo.applicationInfo)) || !record.translucent) {
            return false;
        }
        String clsName = getClsNameFromIntent(intent);
        if (!TextUtils.isEmpty(clsName) && this.mDataMgr.checkActivityExist(clsName)) {
            return true;
        }
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid) {
        if ((!isPolicyEnabled() && !isNeedRestrict()) || this.mDataMgr.isWhiteStartupRequest(cpi.applicationInfo, callerUid) || !shouldPreventPackage(cpi.applicationInfo.packageName, RECORD_TYPE_PROVIDER, callerPid, callerUid)) {
            return false;
        }
        HwPFWLogger.i(TAG, "prevent start provider of package " + cpi.applicationInfo.packageName + ", auth: " + cpi.authority + ", name: " + cpi.name + " by callerPid " + callerPid + ", callerUid " + callerUid);
        return true;
    }

    public boolean shouldPreventSyncService(Intent intent, int userId) {
        return shouldPreventIntent(intent, userId, "syncService");
    }

    public boolean shouldPreventJobService(Intent intent, int userId) {
        return shouldPreventIntent(intent, userId, "jobService");
    }

    private boolean shouldPreventIntent(Intent intent, int userId, String scene) {
        if (!isPolicyEnabled() && !isNeedRestrict()) {
            return false;
        }
        String targetPkg = getPkgNameFromIntent(intent);
        ApplicationInfo applicationInfo = getApplicationInfoPkgName(targetPkg, userId);
        if (applicationInfo == null) {
            return false;
        }
        int callerPid = Binder.getCallingPid();
        int callerUid = Binder.getCallingUid();
        if (!shouldPreventApplication(applicationInfo, callerPid, callerUid)) {
            return false;
        }
        HwPFWLogger.i(TAG, "prevent start package " + targetPkg + ", serviceInfo " + getClsNameFromIntent(intent) + " by callerPid " + callerPid + ", callerUid " + callerUid + ", scene:" + scene);
        return true;
    }

    private boolean shouldPreventApplication(ApplicationInfo applicationInfo, int callerPid, int callerUid) {
        if (applicationInfo == null) {
            return false;
        }
        String targetPkg = applicationInfo.packageName;
        if (!this.mDataMgr.isWhiteApplicationInfo(applicationInfo) && shouldPreventPackage(targetPkg, RECORD_TYPE_SERVICE, callerPid, callerUid)) {
            return true;
        }
        return false;
    }

    public boolean shouldPreventRestartService(String pkgName) {
        return false;
    }

    public void addForbidRestartApp(String pkgName) {
        synchronized (this.mForbidListLock) {
            HwPFWLogger.i(TAG, "Other PFW policy addForbidRestartApp " + pkgName);
            this.mForbidRestartApps.add(pkgName);
        }
    }

    public void clearAllForbidRestartApp() {
        synchronized (this.mForbidListLock) {
            HwPFWLogger.i(TAG, "Other PFW policy clearAllForbidRestartApp");
            this.mForbidRestartApps.clear();
        }
    }

    public HwPFWStartupControlScope getStartupControlScope() {
        HwPFWStartupControlScope result = new HwPFWStartupControlScope();
        result.setScope(this.mDataMgr.copyOfFwkSystemBlackPkgs(), this.mDataMgr.copyOfFwkThirdWhitePkgs());
        return result;
    }

    public void appendExtStartupControlScope(HwPFWStartupControlScope scope) {
        List<String> blackList = new ArrayList();
        List<String> whiteList = new ArrayList();
        scope.copyOutScope(blackList, whiteList);
        this.mDataMgr.appendExtSystemBlackPkgs(blackList);
        this.mDataMgr.appendExtThirdWhitePkgs(whiteList);
    }

    public void updateStartupSettings(List<HwPFWStartupSetting> settings, boolean clearFirst) {
        this.mDataMgr.updateStartupSettings(settings, clearFirst);
        scheduleFastWriteHsmCache();
    }

    public void removeStartupSetting(String pkgName) {
        this.mDataMgr.removeStartupSetting(pkgName);
        scheduleFastWriteHsmCache();
    }

    private boolean shouldPreventPackage(String pkgName) {
        if (isRestrictAutoStartApp(pkgName)) {
            return true;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        if (isForbidRestartApp(pkgName)) {
            return true;
        }
        int settingValue = this.mDataMgr.retrieveStartupSettings(pkgName, 0);
        if (!(MM_HOTA_AUTOSTART_HAS_DONE || !HwConnectivityService.MM_PKG_NAME.equals(pkgName) || settingValue == 1)) {
            PackageManager mPM = this.mContext.getPackageManager();
            if (mPM != null && mPM.isUpgrade()) {
                settingValue = 1;
                HwPFWLogger.i(TAG, "we allow com.tencent.mm autostartup for MEDIA_MOUNTED when ota");
            }
            MM_HOTA_AUTOSTART_HAS_DONE = true;
        }
        switch (settingValue) {
            case 1:
                sendStartupRecordMsg(pkgName, RECORD_TYPE_RECEIVER, true, -1, -1);
                return false;
            default:
                sendStartupRecordMsg(pkgName, RECORD_TYPE_RECEIVER, false, -1, -1);
                return true;
        }
    }

    private boolean shouldPreventPackage(String pkgName, String type, int callerPid, int callerUid) {
        if (isRestrictAutoStartApp(pkgName)) {
            return true;
        }
        if (!isPolicyEnabled()) {
            return false;
        }
        if (isForbidRestartApp(pkgName)) {
            return true;
        }
        switch (this.mDataMgr.retrieveStartupSettings(pkgName, 1)) {
            case 0:
                sendStartupResultMsg(pkgName, type, callerPid, callerUid, false);
                sendStartupRecordMsg(pkgName, type, false, callerPid, callerUid);
                return true;
            case 1:
                sendStartupResultMsg(pkgName, type, callerPid, callerUid, true);
                sendStartupRecordMsg(pkgName, type, true, callerPid, callerUid);
                return false;
            case 2:
                sendStartupConfirmMsg(pkgName, type, callerPid, callerUid);
                return false;
            default:
                HwPFWLogger.w(TAG, "shouldPreventPackage un-expected default flow");
                return false;
        }
    }

    private void sendStartupConfirmMsg(String pkgName, String callerType, int callerPid, int callerUid) {
        Bundle bundle = new Bundle();
        bundle.putString("B_TARGET_PACKAGE", pkgName);
        bundle.putString("B_CALLER_TYPE", callerType);
        bundle.putInt("B_CALLER_PID", callerPid);
        bundle.putInt("B_CALLER_UID", callerUid);
        Message msg = new Message();
        msg.what = 5;
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void sendStartupResultMsg(String pkgName, String callerType, int callerPid, int callerUid, boolean allow) {
        Bundle bundle = new Bundle();
        bundle.putString("B_TARGET_PACKAGE", pkgName);
        bundle.putString("B_CALLER_TYPE", callerType);
        bundle.putInt("B_CALLER_PID", callerPid);
        bundle.putInt("B_CALLER_UID", callerUid);
        bundle.putBoolean("B_ALLOW_STARTUP", allow);
        Message msg = new Message();
        msg.what = 6;
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void sendStartupRecordMsg(String pkgName, String type, boolean result, int callerPid, int callerUid) {
        Bundle bundle = new Bundle();
        bundle.putString("B_RECORD_PACKAGE", pkgName);
        bundle.putString("B_RECORD_TYPE", type);
        bundle.putBoolean("B_RECORD_RESULT", result);
        bundle.putLong("B_RECORD_TIME", System.currentTimeMillis());
        if (!RECORD_TYPE_RECEIVER.equals(type)) {
            bundle.putInt("B_RECORD_CALLER_PID", callerPid);
            bundle.putInt("B_RECORD_CALLER_UID", callerUid);
        }
        Message msg = this.mHandler.obtainMessage(7);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void startConfirmService(Message msg) {
        Intent intentService = new Intent(ACTION_STARTUP_CONFIRM);
        intentService.setPackage(HSM_PACKAGE_NAME);
        intentService.putExtras(msg.getData());
        try {
            this.mContext.startServiceAsUser(intentService, UserHandle.CURRENT);
        } catch (Exception e) {
            HwPFWLogger.i(TAG, "startConfirmService error " + e.getMessage());
        }
    }

    private void startResultService(Message msg) {
        Intent intentService = new Intent(ACTION_STARTUP_RESULT);
        intentService.setPackage(HSM_PACKAGE_NAME);
        intentService.putExtras(msg.getData());
        try {
            this.mContext.startServiceAsUser(intentService, UserHandle.CURRENT);
        } catch (Exception e) {
            HwPFWLogger.i(TAG, "startResultService error " + e.getMessage());
        }
    }

    private void startRecordService(Message msg) {
        Intent intentService = new Intent(ACTION_STARTUP_RECORD);
        intentService.setPackage(HSM_PACKAGE_NAME);
        intentService.putExtras(msg.getData());
        try {
            this.mContext.startServiceAsUser(intentService, UserHandle.CURRENT);
        } catch (Exception e) {
            HwPFWLogger.i(TAG, "startRecordService error " + e.getMessage());
        }
    }

    private boolean isForbidRestartApp(String pkgName) {
        boolean result;
        synchronized (this.mForbidListLock) {
            result = this.mForbidRestartApps.contains(pkgName);
            if (result) {
                HwPFWLogger.w(TAG, "isForbidRestartApp will prevent startup of " + pkgName);
            }
        }
        return result;
    }

    private boolean isNeedRestrict() {
        boolean z = false;
        synchronized (this.mRestrictAutoStartApps) {
            if (this.mRestrictAutoStartApps.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    private boolean isRestrictAutoStartApp(String pkgName) {
        boolean result;
        synchronized (this.mRestrictAutoStartApps) {
            result = this.mRestrictAutoStartApps.contains(pkgName);
            if (result) {
                HwPFWLogger.i(TAG, "ras app : " + pkgName);
            }
        }
        return result;
    }

    public void addRestrictAutoStartApp(List<String> pkgs) {
        if (pkgs == null) {
            HwPFWLogger.w(TAG, "add ras pkgs is null !");
            return;
        }
        synchronized (this.mRestrictAutoStartApps) {
            for (String pkgName : pkgs) {
                if (!this.mRestrictAutoStartApps.contains(pkgName)) {
                    HwPFWLogger.i(TAG, "add ras app : " + pkgName);
                    this.mRestrictAutoStartApps.add(pkgName);
                }
            }
            HwPFWLogger.i(TAG, "all ras app : " + this.mRestrictAutoStartApps);
        }
    }

    public void removeRestrictAutoStartApp(List<String> pkgs) {
        if (pkgs == null) {
            HwPFWLogger.w(TAG, "remove ras pkgs is null !");
            return;
        }
        synchronized (this.mRestrictAutoStartApps) {
            for (String pkgName : pkgs) {
                if (this.mRestrictAutoStartApps.contains(pkgName)) {
                    HwPFWLogger.i(TAG, "remove ras app : " + pkgName);
                    this.mRestrictAutoStartApps.remove(pkgName);
                }
            }
        }
    }

    public void clearAllRestrictAutoStartApp() {
        synchronized (this.mRestrictAutoStartApps) {
            HwPFWLogger.i(TAG, "clear all ras app list.");
            this.mRestrictAutoStartApps.clear();
        }
    }

    private void initSystemUidSet() {
        this.mDataMgr.loadSystemUid(this.mContext);
    }

    private void initForbidenActivitySet() {
        this.mDataMgr.loadForbidenActivity();
    }

    private void scheduleFastWriteHsmCache() {
        if (!this.mFastCacheWriteScheduled) {
            this.mFastCacheWriteScheduled = true;
            this.mHandler.removeMessages(4);
            this.mHandler.sendEmptyMessageDelayed(4, 10000);
        }
    }

    private void writeHsmCacheData() {
        this.mFastCacheWriteScheduled = false;
        this.mDataMgr.flushCacheDataToDisk();
        this.mHandler.sendEmptyMessageDelayed(4, 1800000);
    }

    private void periodDumpPolicyDataToLog() {
        this.mDataMgr.dumpToLog();
        this.mHandler.sendEmptyMessageDelayed(1, 1800000);
    }

    private void handlerDeviceRestart() {
        if (this.mRebootHandled) {
            HwPFWLogger.d(TAG, "handlerDeviceRestart already handled!");
            return;
        }
        if (isPolicyEnabled()) {
            HwPFWLogger.d(TAG, "handlerDeviceRestart begin when policy enabled");
            if (this.mStopPkgThread == null) {
                this.mStopPkgThread = new StopPackagesThread(this.mContext, this.mDataMgr);
                this.mStopPkgThread.start();
            }
            this.mHandler.sendEmptyMessageDelayed(1, 60000);
            this.mHandler.sendEmptyMessageDelayed(8, 60000);
        } else {
            HwPFWLogger.w(TAG, "handlerDeviceRestart policy disabled.");
        }
        this.mRebootHandled = true;
    }

    private void cacheLauncherWidgetList() {
        this.mDataMgr.updateWidgetList(AutoStartupUtil.getWidgetListFromLauncher(this.mContext));
        scheduleFastWriteHsmCache();
    }

    private ApplicationInfo getApplicationInfoPkgName(String pkgName, int userId) {
        if (TextUtils.isEmpty(pkgName)) {
            return null;
        }
        try {
            return AppGlobals.getPackageManager().getApplicationInfo(pkgName, 0, userId);
        } catch (Exception e) {
            HwPFWLogger.w(TAG, "getApplicationInfoByIntent error, e:" + e.getMessage());
            return null;
        }
    }

    private String getPkgNameFromIntent(Intent intent) {
        if (intent == null) {
            return AppHibernateCst.INVALID_PKG;
        }
        ComponentName component = intent.getComponent();
        if (component == null) {
            return AppHibernateCst.INVALID_PKG;
        }
        String pkg = component.getPackageName();
        if (TextUtils.isEmpty(pkg)) {
            return AppHibernateCst.INVALID_PKG;
        }
        return pkg;
    }

    private String getClsNameFromIntent(Intent intent) {
        if (intent == null) {
            return AppHibernateCst.INVALID_PKG;
        }
        ComponentName component = intent.getComponent();
        if (component == null) {
            return AppHibernateCst.INVALID_PKG;
        }
        String target = component.getClassName();
        if (TextUtils.isEmpty(target)) {
            return AppHibernateCst.INVALID_PKG;
        }
        return target;
    }
}
