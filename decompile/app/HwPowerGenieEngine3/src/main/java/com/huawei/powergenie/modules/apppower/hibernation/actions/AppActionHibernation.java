package com.huawei.powergenie.modules.apppower.hibernation.actions;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Process;
import android.os.SystemClock;
import com.huawei.powergenie.debugtest.DbgUtils;
import com.huawei.powergenie.debugtest.LogUtils;
import com.huawei.powergenie.modules.apppower.hibernation.ASHLog;
import com.huawei.powergenie.modules.apppower.hibernation.states.AppStateRecord;
import java.util.ArrayList;

public class AppActionHibernation extends AppAction {
    private static boolean mInitWD = false;
    private static Thread mMyThread = null;
    private static WatchDogHandler mWDHandler = null;
    private static WakeLock mWakeLock = null;
    private ArrayList<String> mFilterNotChangeAlarm = new ArrayList<String>() {
        {
            add("com.huawei.health");
        }
    };
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 200:
                    if (AppActionHibernation.this.mAppRecord.getMmPushPid() <= 0 || AppActionHibernation.this.getProcUTime() - AppActionHibernation.this.mMmPushUTime <= 0) {
                        AppActionHibernation.this.delayPerformAction();
                        return;
                    }
                    AppActionHibernation.this.mMmPushUTime = AppActionHibernation.this.getProcUTime();
                    ASHLog.i("utime is active to delay! utime=" + AppActionHibernation.this.mMmPushUTime);
                    AppActionHibernation.this.mHandler.sendMessageDelayed(AppActionHibernation.this.mHandler.obtainMessage(200), 1000);
                    return;
                case 201:
                    boolean dropAll = false;
                    if (AppActionHibernation.this.mAppRecord.getPids().size() <= 0) {
                        ASHLog.i(AppActionHibernation.this.mAppRecord.getPkgName() + " all process exit, drop all bc");
                        AppActionHibernation.this.dropPkgBC(AppActionHibernation.this.mAppRecord.getPkgName(), null);
                        dropAll = true;
                    } else {
                        ASHLog.i(AppActionHibernation.this.mAppRecord.getPkgName() + " process exit, drop bc pid: " + msg.arg1);
                        AppActionHibernation.this.dropProcessBC(msg.arg1, null);
                    }
                    AppActionHibernation.this.unproxyAppBroadcast();
                    if (dropAll) {
                        AppActionHibernation.this.dropPkgBC(AppActionHibernation.this.mAppRecord.getPkgName(), new ArrayList());
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsAddNetListener = false;
    private boolean mIsExtrProxyAllBC = false;
    private boolean mIsRestrictNetwork = false;
    private int mMmPushUTime = -1;

    private static class WatchDogHandler extends Handler {
        public WatchDogHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10000:
                    int pid = Process.myPid();
                    StringBuilder sb = new StringBuilder();
                    sb.append("KILL").append("\t").append(pid);
                    LogUtils.c("WATCH_DOG", sb.toString());
                    ASHLog.w("*** WATCHDOG KILLING PG PROCESS: " + pid);
                    if (AppActionHibernation.mMyThread != null) {
                        for (StackTraceElement element : AppActionHibernation.mMyThread.getStackTrace()) {
                            ASHLog.w("    at " + element);
                        }
                    }
                    ASHLog.w("*** GOODBYE!");
                    Process.killProcess(pid);
                    System.exit(10);
                    return;
                default:
                    return;
            }
        }
    }

    public AppActionHibernation(AppStateRecord record) {
        super(record);
        if (!mInitWD) {
            mInitWD = true;
            HandlerThread handlerThread = new HandlerThread("Watch Dog");
            handlerThread.start();
            mWDHandler = new WatchDogHandler(handlerThread.getLooper());
            mMyThread = Thread.currentThread();
            mWakeLock = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "hibernation");
        }
    }

    public void performAction() {
        ASHLog.i("perform hibernation actions!");
        long start = SystemClock.uptimeMillis();
        startWatchDog();
        setUnprotectedAppInactive();
        if (this.mFilterNotChangeAlarm.contains(this.mAppRecord.getPkgName())) {
            ASHLog.i("not perform alarm action:" + this.mAppRecord.getPkgName());
        } else if ((isImTypeApp() || isProtectAppByUser()) && !getBastetProxyState() && isConnected()) {
            startUnifiedHeartbeat("hibernation");
        } else if (!this.mAppRecord.isAlarmClockApp()) {
            pendingAppAlarms();
        }
        if (this.mAppRecord.isScreenOff() && this.mAppRecord.isScrOffRejectMsgApp()) {
            if (!this.mIsExtrProxyAllBC) {
                proxyPackageAllBC();
                this.mIsExtrProxyAllBC = true;
                ASHLog.i("proxy all bc :" + this.mAppRecord.getPkgName());
            }
        } else if (this.mIsExtrProxyAllBC) {
            clearProxyBCConfig();
            this.mIsExtrProxyAllBC = false;
            ASHLog.i("clear bc config :" + this.mAppRecord.getPkgName());
        }
        long delay = proxyAppBroadcast();
        if (this.mAppRecord.getMmPushPid() > 0) {
            this.mMmPushUTime = getProcUTime();
            delay = Math.max(delay, 1000);
        }
        stopWatchDog();
        if (delay > 0) {
            ASHLog.i("perform hibernation actions delay: " + delay);
            this.mHandler.removeMessages(200);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(200), delay);
        } else {
            delayPerformAction();
        }
        if (DbgUtils.DBG_USB) {
            ASHLog.i("perform hibernation expend: " + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }

    private void delayPerformAction() {
        mWakeLock.acquire();
        startWatchDog();
        proxyApp();
        freezeAppProcess();
        if (this.mAppRecord.isScreenOff() && this.mAppRecord.isScrOffRejectMsgApp()) {
            ASHLog.i("Scr off in extreme mode no need listen net packet.");
        } else {
            notifyBastetProxy();
            if (isImTypeApp() || isProtectAppByUser()) {
                if (this.mAppRecord.isListenerNetPackets()) {
                    addNetPacketListener();
                    this.mIsAddNetListener = true;
                } else {
                    ASHLog.i(this.mAppRecord.getPkgName() + " is not ctrl socket white app, don't listener packet");
                }
            }
        }
        proxyWakeLock();
        forceReleaseWakeLock();
        if (!this.mIsAddNetListener && this.mAppRecord.isPermitRestrictNetApp() && this.mAppRecord.isConnected()) {
            setAppNetworkRestrict();
            this.mIsRestrictNetwork = true;
        }
        stopWatchDog();
        mWakeLock.release();
    }

    public void clearAction() {
        ASHLog.i("clear hibernation actions!");
        long start = SystemClock.uptimeMillis();
        startWatchDog();
        this.mHandler.removeMessages(200);
        forceRestoreWakeLock();
        unproxyWakeLock();
        mWakeLock.acquire();
        unFreezeAppProcess();
        mWakeLock.release();
        notifyBastetUnProxy();
        int pid = this.mAppRecord.getDeadPid();
        if (pid > 0) {
            ASHLog.i(pid + " exit, delay to unproxy broadcast");
            this.mHandler.removeMessages(201);
            Message msg = this.mHandler.obtainMessage(201);
            msg.arg1 = pid;
            this.mHandler.sendMessageDelayed(msg, 1000);
        } else {
            unproxyAppBroadcast();
        }
        if (this.mAppRecord.getScrOffDuration() <= 10000) {
            cancelUnifiedHeartbeat("left H");
        }
        if (!this.mAppRecord.isAlarmClockApp()) {
            unPendingAppAlarms();
        }
        unproxyApp();
        setAppActiveIfNeeded();
        if (this.mIsAddNetListener) {
            removeNetPacketListener();
            this.mIsAddNetListener = false;
        }
        if (this.mIsRestrictNetwork) {
            removeAppNetworkRestrict();
            this.mIsRestrictNetwork = false;
        }
        stopWatchDog();
        if (DbgUtils.DBG_USB) {
            ASHLog.i("clear hibernation expend: " + (SystemClock.uptimeMillis() - start) + "ms");
        }
    }

    public void handleBastetProxyState(boolean ready) {
        ASHLog.i("handleBastetProxyState, ready: " + ready);
        if (ready) {
            notifyBastetProxy();
            if (isUnifiedHeartbeat()) {
                cancelUnifiedHeartbeat("left bastet");
                pendingAppAlarms();
            }
        } else if ((isImTypeApp() || isProtectAppByUser()) && isConnected()) {
            if (isPendingAppAlarms()) {
                unPendingAppAlarms();
                startUnifiedHeartbeat("bastet");
            }
            if (this.mAppRecord.isListenerNetPackets()) {
                addNetPacketListener();
                this.mIsAddNetListener = true;
                return;
            }
            ASHLog.i(this.mAppRecord.getPkgName() + " is not ctrl socket white app, don't listener packet");
        }
    }

    private void startWatchDog() {
        if (mWDHandler != null) {
            mWDHandler.sendMessageDelayed(mWDHandler.obtainMessage(10000), 20000);
        }
    }

    private void stopWatchDog() {
        if (mWDHandler != null) {
            mWDHandler.removeMessages(10000);
        }
    }
}
