package com.huawei.systemmanager.applock.datacenter;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import android.support.v4.content.IntentCompat;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.db.DBHelper;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.AuthSuccessPackageAllProvider;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.AuthSuccessPackageProvider;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.LockedPackageProvider;
import com.huawei.systemmanager.applock.password.AuthEnterRelockSelfActivity;
import com.huawei.systemmanager.applock.utils.ActivityIntentUtils;
import com.huawei.systemmanager.applock.utils.AppLockFilterOutUtils;
import com.huawei.systemmanager.applock.utils.MultiWinServiceWrapper;
import com.huawei.systemmanager.applock.utils.compatibility.AppLockPwdUtils;
import com.huawei.systemmanager.applock.utils.sp.FunctionSwitchUtils;
import com.huawei.systemmanager.applock.utils.sp.LockingPackageUtils;
import com.huawei.systemmanager.applock.utils.sp.ReloadSwitchUtils;
import com.huawei.systemmanager.applock.utils.sp.RelockActivityUtils;
import com.huawei.systemmanager.comm.misc.CursorHelper;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.List;

public class AppLockService extends Service implements IPackageChangeListener {
    private static final int HANDLER_EXTERNAL_AVAILABLE = 2;
    private static final int HANDLER_LOOP_LOCK = 3;
    public static final int INIT_DATA_DEALY = 0;
    public static final int INIT_DATA_IMMEDIATELY = 1;
    private static final int MAX_LOOP = 5;
    private static final int RECHECK_DELAY_MS = 3000;
    private static final String TAG = "AppLockService";
    private static final int TIMEOUT = 10000;
    private Context mAppContext = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread("AppLockServiceHandlerThread");
    boolean mIsShutdown = false;
    private boolean mLoaded = false;
    private IProcessObserver mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (AppLockPwdUtils.isPasswordSet(AppLockService.this.mAppContext)) {
                if (foregroundActivities) {
                    AppLockService.this.mHandler.removeMessages(3);
                    AppLockService.this.mHandler.sendMessageDelayed(AppLockService.this.mHandler.obtainMessage(3), 200);
                } else {
                    AppLockService.this.mHandler.removeMessages(3);
                    AppLockService.this.mHandler.sendMessageDelayed(AppLockService.this.mHandler.obtainMessage(3), 250);
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };
    private ContentObserver mProvisionObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            AppLockService.this.startLoadingMonitorListThread();
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context != null && intent != null && !TextUtils.isEmpty(intent.getAction())) {
                try {
                    if (AppLockPwdUtils.isPasswordSet(AppLockService.this.mAppContext)) {
                        String action = intent.getAction();
                        if ("android.intent.action.SCREEN_OFF".equals(action)) {
                            HwLog.v(AppLockService.TAG, "receive ACTION_SCREEN_OFF");
                            new Thread("applock_screen_off") {
                                public void run() {
                                    AppLockService.this.handleScreenOff();
                                }
                            }.start();
                        } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                            AppLockService.this.mHandler.removeMessages(3);
                            AppLockService.this.mHandler.sendMessage(AppLockService.this.mHandler.obtainMessage(3));
                        } else if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                            AppLockService.this.mIsShutdown = true;
                        } else if (IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action)) {
                            AppLockService.this.fillDB();
                        }
                    }
                } catch (Exception ex) {
                    HwLog.e(AppLockService.TAG, "onReceive catch exception: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    };

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    AppLockService.this.handleExternalAvailable(msg.obj);
                    return;
                case 3:
                    AppLockService.this.handleLoopLock();
                    return;
                default:
                    HwLog.d(AppLockService.TAG, "invalid handle message");
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
        new CleanTrashDataThread(this.mAppContext).start();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.v(TAG, "onStartCommand");
        if (intent == null) {
            return 1;
        }
        HwLog.i(TAG, "is first enter applock == " + intent.getFlags());
        if (intent.getFlags() == 1) {
            fillDB();
        } else if (intent.getFlags() == 0) {
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    AppLockService.this.fillDB();
                }
            }, DBHelper.HISTORY_MAX_SIZE);
        }
        return 1;
    }

    public void onDestroy() {
        HwLog.v(TAG, "onDestroy");
        unregisterObserverAndReceiver();
        super.onDestroy();
    }

    public void onPackagedAdded(String pkgName) {
        if (!MultiUserUtils.isInMultiUserMode()) {
            if (AppLockFilterOutUtils.needFilterOut(this.mAppContext, pkgName)) {
                HwLog.d(TAG, "onPackagedAdded filter out package:" + pkgName);
            } else if (hasLauncherActivity(pkgName)) {
                AppLockDBHelper.getInstance(this.mAppContext).replaceLockStatus(pkgName, 0);
                ReloadSwitchUtils.setApplicationListNeedReload(this.mAppContext);
            } else {
                HwLog.w(TAG, "onPackageAdded pkg " + pkgName + " does not have launcher activity");
            }
        }
    }

    public void onPackageRemoved(String pkgName) {
        if (!MultiUserUtils.isInMultiUserMode()) {
            AppLockDBHelper.getInstance(this.mAppContext).deleteLockData(pkgName);
            ReloadSwitchUtils.setApplicationListNeedReload(this.mAppContext);
        }
    }

    public void onPackageChanged(String pkgName) {
    }

    public void onExternalChanged(String[] packages, boolean available) {
        HwLog.d(TAG, "onExternalChanged:" + packages.length + ", available:" + available);
        for (String str : packages) {
            HwLog.d(TAG, "onExternalChanged: " + str);
        }
        if (available) {
            this.mHandler.removeMessages(2);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(2, Lists.newArrayList((Object[]) packages)));
        }
        ReloadSwitchUtils.setApplicationListNeedReload(this.mAppContext);
    }

    private void registerObserverAndReceiver() {
        HwLog.d(TAG, "registerObserver");
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterObserver RemoteException!");
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.USER_PRESENT");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN");
        filter.addAction(IntentCompat.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        registerReceiver(this.mReceiver, filter);
        getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, this.mProvisionObserver);
        HsmPackageManager.registerListener(this);
    }

    private void unregisterObserverAndReceiver() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterObserver RemoteException!");
        }
        unregisterReceiver(this.mReceiver);
        this.mAppContext.getContentResolver().unregisterContentObserver(this.mProvisionObserver);
        HsmPackageManager.unregisterListener(this);
    }

    private boolean hasLauncherActivity(String pkgName) {
        Intent resolveIntent = new Intent("android.intent.action.MAIN", null);
        resolveIntent.addCategory("android.intent.category.LAUNCHER");
        resolveIntent.setPackage(pkgName);
        for (ResolveInfo temp : PackageManagerWrapper.queryIntentActivities(this.mAppContext.getPackageManager(), resolveIntent, 0)) {
            if (pkgName.equals(temp.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    private void startLoadingMonitorListThread() {
        try {
            if (1 == Global.getInt(this.mAppContext.getContentResolver(), "device_provisioned")) {
                HwLog.d(TAG, "startLoadingMonitorListThread provisioned");
                new LoadingMonitorListThread(this.mAppContext).start();
                return;
            }
            HwLog.d(TAG, "startLoadingMonitorListThread not provisioned");
        } catch (SettingNotFoundException ex) {
            HwLog.d(TAG, "deviceProvisioned catch SettingNotFoundException: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private RunningTaskInfo getTopTaskInfo() {
        List<RunningTaskInfo> taskInfos = ((ActivityManager) getSystemService("activity")).getRunningTasks(1);
        if (taskInfos != null && !taskInfos.isEmpty()) {
            return (RunningTaskInfo) taskInfos.get(0);
        }
        HwLog.e(TAG, "Invalid result of getTopTaskInfo");
        return null;
    }

    private boolean checkIsLockedApp(String pkgName) {
        return checkDbPackageExist(pkgName, LockedPackageProvider.CONTENT_URI, "packageName");
    }

    private boolean checkIsAuthSuccessPackage(String pkgName) {
        return checkDbPackageExist(pkgName, AuthSuccessPackageProvider.CONTENT_URI, "packageName");
    }

    private boolean checkDbPackageExist(String pkgName, Uri queryUri, String colName) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(queryUri, new String[]{colName}, colName + " = ?", new String[]{pkgName}, null);
            if (cursor != null && cursor.getCount() != 0) {
                return true;
            }
            CursorHelper.closeCursor(cursor);
            return false;
        } catch (Exception ex) {
            HwLog.e(TAG, "Database exception!" + ex.getMessage());
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    private synchronized void fillDB() {
        if (!this.mLoaded) {
            HwLog.d(TAG, "AppLockService receiver");
            startLoadingMonitorListThread();
            new TransPreferenceToDBThread(this.mAppContext).start();
            this.mLoaded = true;
        }
    }

    private boolean checkRelockSelf(String pkgName, String baseClassName) {
        if (!pkgName.equals(this.mAppContext.getPackageName()) || !RelockActivityUtils.shouldRelock(this.mAppContext) || !RelockActivityUtils.isRelockActivity(this.mAppContext, baseClassName)) {
            return false;
        }
        startActivity(ActivityIntentUtils.getRelockSelfActivityIntent(this.mAppContext));
        RelockActivityUtils.setRelockFlag(this.mAppContext, false);
        return true;
    }

    private boolean checkAndStartUnlockActivity(String pkgName) {
        if (!FunctionSwitchUtils.getFunctionSwitchStatus(this.mAppContext)) {
            return true;
        }
        if (pkgName.equals(this.mAppContext.getPackageName()) || !checkIsLockedApp(pkgName)) {
            return false;
        }
        if (checkIsAuthSuccessPackage(pkgName)) {
            HwLog.i(TAG, "checkAndStartUnlockActivity already auth success!");
        } else {
            HwLog.i(TAG, "checkAndStartUnlockActivity going to start auth activity!");
            LockingPackageUtils.setLockingPackageName(this.mAppContext, pkgName);
            startActivity(ActivityIntentUtils.getStartLaunchAppAuthActivityIntent(this.mAppContext));
            HwLog.i(TAG, "checkAndStartUnlockActivity send 2 seconds delay recheck msg");
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 3000);
        }
        return true;
    }

    private void handleLoopLock() {
        KeyguardManager keyguard = (KeyguardManager) getSystemService("keyguard");
        int i = 0;
        while (i < 5) {
            if (!MultiUserUtils.currentUserIsOwner()) {
                HwLog.d(TAG, "current user is not owner! ");
                return;
            } else if (this.mIsShutdown) {
                HwLog.d(TAG, "checkAndStartUnlockActivity system is shutting down!");
                return;
            } else if (!isNotNeedAuthEnterRelockSelf()) {
                if (keyguard.isKeyguardLocked() || !checkTopAndStartLockActivity()) {
                    if (i != 4) {
                        SystemClock.sleep(500);
                    }
                    i++;
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private boolean isNotNeedAuthEnterRelockSelf() {
        String relockSelfActivity = AuthEnterRelockSelfActivity.class.getName();
        RunningTaskInfo taskInfo = getTopTaskInfo();
        if (taskInfo != null) {
            return relockSelfActivity.equals(taskInfo.topActivity.getClassName());
        }
        HwLog.e(TAG, "handleLoopLock invalid top taskInfo");
        return true;
    }

    private boolean checkTopAndStartLockActivity() {
        RunningTaskInfo taskInfo = getTopTaskInfo();
        if (taskInfo == null) {
            HwLog.e(TAG, "handleUserPresent invalid top taskInfo");
            return false;
        }
        String topPkgName = taskInfo.topActivity.getPackageName();
        String topTaskBaseClassName = taskInfo.topActivity.getClassName();
        if (!topPkgName.equals(this.mAppContext.getPackageName())) {
            RelockActivityUtils.setRelockFlag(this.mAppContext, true);
        }
        boolean isMwPrevTaskLockActivity = false;
        if (isSplitWindowEnable() && isPartOfSplitWindow(taskInfo.id)) {
            RunningTaskInfo prevToptaskInfo = getPrevTopTaskInfo();
            if (prevToptaskInfo != null && isPartOfSplitWindow(prevToptaskInfo.id) && checkAndStartUnlockActivity(prevToptaskInfo.topActivity.getPackageName())) {
                isMwPrevTaskLockActivity = true;
            }
        }
        return checkRelockSelf(topPkgName, topTaskBaseClassName) || checkAndStartUnlockActivity(topPkgName) || isMwPrevTaskLockActivity;
    }

    private void handleScreenOff() {
        HwLog.v(TAG, "handleScreenOff");
        ProviderUtils.deleteAll(getApplicationContext(), AuthSuccessPackageAllProvider.CONTENT_URI);
        RelockActivityUtils.setRelockFlag(this.mAppContext, true);
    }

    private void handleExternalAvailable(Object obj) {
        if (!MultiUserUtils.isInMultiUserMode()) {
            List<String> pkgList = (List) obj;
            if (pkgList == null || pkgList.isEmpty()) {
                HwLog.e(TAG, "handleExternalAvailable: pkgList is empty or null");
                return;
            }
            for (String pkgName : pkgList) {
                if (!AppLockFilterOutUtils.needFilterOut(this.mAppContext, pkgName)) {
                    AppLockDBHelper.getInstance(this.mAppContext).insertDefaultLockStatus(pkgName);
                }
            }
            ReloadSwitchUtils.setApplicationListNeedReload(this.mAppContext);
        }
    }

    private boolean isSplitWindowEnable() {
        return MultiWinServiceWrapper.getMWMaintained();
    }

    private boolean isPartOfSplitWindow(int aTaskId) {
        return MultiWinServiceWrapper.isPartOfMultiWindow(aTaskId);
    }

    private RunningTaskInfo getPrevTopTaskInfo() {
        List<RunningTaskInfo> taskInfos = ((ActivityManager) getSystemService("activity")).getRunningTasks(2);
        if (taskInfos == null || taskInfos.size() != 2) {
            return null;
        }
        return (RunningTaskInfo) taskInfos.get(1);
    }
}
