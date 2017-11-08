package com.huawei.systemmanager.optimize;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.IProcessObserver;
import android.app.IProcessObserver.Stub;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hsm.MediaTransactWrapper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.permission.HoldServiceConst;
import com.huawei.systemmanager.comm.concurrent.HsmSingleExecutor;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.AbroadUtils;
import com.huawei.systemmanager.optimize.MemoryManager.HsmMemoryInfo;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.optimize.trimmer.ProcessTrimer;
import com.huawei.systemmanager.optimize.trimmer.TrimParam;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.widget.WidgetCleanManager;
import java.util.List;

public class ProcessTrimService implements HsmService {
    private static final String ACTION_REPLY_ONEKEYCLEAN = "com.huawei.systemmanager.action.REPLY_ONEKEYCLEAN";
    private static final String ACTION_REPLY_TRIM_ALL = "com.huawei.systemmanager.action.REPLY_TRIM_ALL";
    private static final String ACTION_REQUEST_ONEKEYCLEAN = "com.huawei.systemmanager.action.REQUEST_ONEKEYCLEAN";
    private static final String ACTION_REQUEST_TRIM_ALL = "com.huawei.systemmanager.action.REQUEST_TRIM_ALL";
    private static final String KEY_MEMORY_PERCENT = "memory_percent";
    private static final String KEY_REQUEST_ID = "request_id";
    private static final String KEY_REQUEST_TYPE = "request_type";
    private static final String KEY_SENDER_PKG = "sender_pkg";
    private static final String KEY_START_TIME = "start_time";
    private static final String KEY_TOAST_INFO = "toast_info";
    private static final String LAUNCHER_PERMISSION = "com.huawei.android.launcher.permission.ONEKEYCLEAN";
    private static final String SYSTEMUI_PERMISSION = "com.android.systemui.permission.removeTask";
    public static final String TAG = "ProcessTrimService";
    private int mBatteryLevel;
    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    ProcessTrimService.this.mBatteryLevel = intent.getIntExtra("level", 0);
                }
            }
        }
    };
    private final Context mContext;
    private HsmSingleExecutor mExecutor = new HsmSingleExecutor();
    private IProcessObserver mProcessObserver = new Stub() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                List<TrimParam> list = Lists.newArrayList();
                synchronized (ProcessTrimService.this.mTrimers) {
                    if (ProcessTrimService.this.mTrimers.isEmpty()) {
                        return;
                    }
                    list.addAll(ProcessTrimService.this.mTrimers);
                }
            }
        }

        public void onProcessDied(int pid, int uid) {
            MediaTransactWrapper.musicPausedOrStopped(uid, pid);
        }

        public void onImportanceChanged(int pid, int uid, int importance) {
        }

        public void onProcessStateChanged(int pid, int uid, int procState) {
        }
    };
    private ProcessBroadcastReceiver mReceiver;
    private final List<TrimParam> mTrimers = Lists.newArrayList();

    private class EAssistantProcessRunnable implements Runnable {
        private EAssistantProcessRunnable() {
        }

        public void run() {
            long memBeforeClean = WidgetCleanManager.getMemoryAvailSize(ProcessTrimService.this.mContext);
            int cleanCount = WidgetCleanManager.doOneKeyCleanTask(ProcessTrimService.this.mContext, true);
            long memAfterClean = WidgetCleanManager.getMemoryAvailSize(ProcessTrimService.this.mContext);
            String resText = WidgetCleanManager.getToastMessage(cleanCount, memAfterClean - memBeforeClean, 0, 0, ProcessTrimService.this.mContext);
            HwLog.i(ProcessTrimService.TAG, "memAfterClean - memBeforeClean = " + (memAfterClean - memBeforeClean) + "; count = " + cleanCount);
            Intent intent = new Intent(HoldServiceConst.ADD_VIEW_TOAST_ACTION);
            intent.putExtra(HoldServiceConst.ADD_VIEW_TOAST_CONTENT, resText);
            intent.setPackage(ProcessTrimService.this.mContext.getPackageName());
            ProcessTrimService.this.mContext.sendBroadcastAsUser(intent, UserHandle.OWNER);
        }
    }

    private class OnekeyCleanRunnalbe implements Runnable {
        private final TrimParam mParam;
        private final int mRequestId;
        private final String mRequestType;

        public OnekeyCleanRunnalbe(Intent intent) {
            int requestId = intent.getIntExtra("request_id", -1);
            String requestType = intent.getStringExtra(ProcessTrimService.KEY_REQUEST_TYPE);
            if (requestType == null) {
                requestType = "";
            }
            this.mRequestId = requestId;
            this.mRequestType = requestType;
            this.mParam = TrimParam.createOnekeycleanParam(ProcessTrimService.this.mContext, false);
            HwLog.i(ProcessTrimService.TAG, "OnekeyCleanRunnalbe, requestId:" + requestId + ", requestType:" + requestType);
        }

        public void run() {
            HwLog.i(ProcessTrimService.TAG, "CleanRunnalbe begin ,request id:" + this.mRequestId);
            long freeMemBeforeClean = WidgetCleanManager.getMemoryAvailSize(ProcessTrimService.this.mContext);
            int batteryBeforeClean = WidgetCleanManager.getBattery(ProcessTrimService.this.mBatteryLevel, ProcessTrimService.this.mContext);
            TrimParam p = this.mParam;
            ProcessTrimService.this.addTrimParam(p);
            int appNum = WidgetCleanManager.doOneKeyCleanTask(p);
            ProcessTrimService.this.removeTrimParam(p);
            HsmMemoryInfo info = MemoryManager.getMemoryInfo(ProcessTrimService.this.mContext);
            int memPercent = info.getUsedPercent();
            long saveMem = info.getFree() - freeMemBeforeClean;
            int saveBattery = WidgetCleanManager.getBattery(ProcessTrimService.this.mBatteryLevel, ProcessTrimService.this.mContext) - batteryBeforeClean;
            String resText = WidgetCleanManager.getToastMessage(appNum, saveMem, saveBattery, 0, ProcessTrimService.this.mContext);
            HwLog.i(ProcessTrimService.TAG, "Toast data:appNum = " + appNum + ", saveMem = " + saveMem + ", saveBattery = " + saveBattery + ", memory percent:" + memPercent);
            Intent intent = new Intent(ProcessTrimService.ACTION_REPLY_ONEKEYCLEAN);
            intent.putExtra("request_id", this.mRequestId);
            intent.putExtra(ProcessTrimService.KEY_MEMORY_PERCENT, memPercent);
            intent.putExtra(ProcessTrimService.KEY_TOAST_INFO, resText);
            intent.putExtra(ProcessTrimService.KEY_REQUEST_TYPE, this.mRequestType);
            HwLog.i(ProcessTrimService.TAG, "send reply broadcast, mRequestId:" + this.mRequestId + ", mRequestType" + this.mRequestType);
            ProcessTrimService.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, ProcessTrimService.LAUNCHER_PERMISSION);
        }
    }

    private class ProcessBroadcastReceiver extends BroadcastReceiver {
        private ProcessBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                HwLog.i(ProcessTrimService.TAG, "receive action: " + action);
                if (ProcessTrimService.ACTION_REQUEST_TRIM_ALL.equals(action)) {
                    ProcessTrimService.this.mExecutor.execute(new SystemUiTrimRunnable(intent));
                } else if (Const.ACTION_REMOVE_PACKAGES.equals(action)) {
                    ProcessTrimService.this.mExecutor.execute(new TrimSpecifyPackageRunnable(intent));
                } else if (ProcessTrimService.ACTION_REQUEST_ONEKEYCLEAN.equals(action)) {
                    ProcessTrimService.this.mExecutor.execute(new OnekeyCleanRunnalbe(intent));
                } else if (Const.ACTION_EASSISTANT_CLEAN_MEMORY_REQUEST.equals(action)) {
                    ProcessTrimService.this.mExecutor.execute(new EAssistantProcessRunnable());
                } else {
                    HwLog.e(ProcessTrimService.TAG, "systemmanager will not hand this action! " + action);
                }
            }
        }
    }

    private class SystemUiTrimRunnable implements Runnable {
        private long mRequestId;
        private long startTime;

        public SystemUiTrimRunnable(Intent intent) {
            this.mRequestId = intent.getLongExtra("request_id", -1);
            this.startTime = intent.getLongExtra(ProcessTrimService.KEY_START_TIME, System.currentTimeMillis());
            HwLog.i(ProcessTrimService.TAG, "TrimAllRunnable, mRequestId:" + this.mRequestId + ", mStartTime" + this.startTime);
        }

        public void run() {
            HwLog.i(ProcessTrimService.TAG, "start SystemUiTrimRunnable, mRequestId:" + this.mRequestId);
            Context ctx = GlobalContext.getContext();
            ProcessTrimer trimer = new ProcessTrimer();
            TrimParam p = TrimParam.createSystemuiTrimParam(ctx, this.startTime);
            ProcessTrimService.this.addTrimParam(p);
            trimer.doTrim(p);
            ProcessTrimService.this.removeTrimParam(p);
            Intent intent = new Intent(ProcessTrimService.ACTION_REPLY_TRIM_ALL);
            intent.putExtra("request_id", this.mRequestId);
            HwLog.i(ProcessTrimService.TAG, "send reply broadcast:com.huawei.systemmanager.action.REPLY_TRIM_ALL, mRequestId:" + this.mRequestId);
            ProcessTrimService.this.mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT, ProcessTrimService.SYSTEMUI_PERMISSION);
        }
    }

    private class TrimSpecifyPackageRunnable implements Runnable {
        private final String mPkg;
        private final int mUserId;

        public TrimSpecifyPackageRunnable(Intent intent) {
            String pkgName = intent.getStringExtra("pkg_name");
            if (TextUtils.isEmpty(pkgName)) {
                HwLog.i(ProcessTrimService.TAG, "TrimSpecifyPackageRunnable, pkg name is empty");
            }
            int userid = intent.getIntExtra(Const.KEY_UID, -1);
            if (-1 == userid) {
                HwLog.i(ProcessTrimService.TAG, "TrimSpecifyPackageRunnable, userid is empty");
            }
            this.mPkg = pkgName;
            this.mUserId = userid;
            String senderPkg = intent.getStringExtra(ProcessTrimService.KEY_SENDER_PKG);
            String str = ProcessTrimService.TAG;
            StringBuilder append = new StringBuilder().append(" TrimSpecifyPackageRunnable, pkg name is: ").append(pkgName).append(" userId is: ").append(this.mUserId).append(", sender:");
            if (senderPkg == null) {
                senderPkg = "";
            }
            HwLog.i(str, append.append(senderPkg).toString());
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            if (!TextUtils.isEmpty(this.mPkg) && !AbroadUtils.isAbroad() && this.mUserId != -1) {
                long start = SystemClock.elapsedRealtime();
                HwLog.i(ProcessTrimService.TAG, "begin trim single app");
                ProcessManager.trimApps(ProcessTrimService.this.mContext, Lists.newArrayList(this.mPkg), Lists.newArrayList(Integer.valueOf(this.mUserId)));
                HwLog.i(ProcessTrimService.TAG, "end trim single app, cost time:" + (SystemClock.elapsedRealtime() - start));
            }
        }
    }

    public ProcessTrimService(Context ctx) {
        this.mContext = ctx;
    }

    public void init() {
        this.mReceiver = new ProcessBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Const.ACTION_TRIM_PROCESS);
        filter.addAction(Const.ACTION_REMOVE_PACKAGES);
        filter.addAction(Const.ACTION_EASSISTANT_CLEAN_MEMORY_REQUEST);
        filter.addAction(ACTION_REQUEST_TRIM_ALL);
        filter.addAction(ACTION_REQUEST_ONEKEYCLEAN);
        this.mContext.registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        this.mContext.registerReceiver(this.mBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.registerProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterObserver RemoteException!");
        }
    }

    public void onDestroy() {
        this.mContext.unregisterReceiver(this.mReceiver);
        this.mContext.unregisterReceiver(this.mBatteryReceiver);
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            if (am != null) {
                am.unregisterProcessObserver(this.mProcessObserver);
            }
        } catch (RemoteException e) {
            HwLog.e(TAG, "unregisterObserver RemoteException!");
        }
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    private void addTrimParam(TrimParam p) {
        synchronized (this.mTrimers) {
            this.mTrimers.add(p);
            p.updateRecentTask();
        }
    }

    private void removeTrimParam(TrimParam p) {
        synchronized (this.mTrimers) {
            this.mTrimers.remove(p);
        }
    }
}
