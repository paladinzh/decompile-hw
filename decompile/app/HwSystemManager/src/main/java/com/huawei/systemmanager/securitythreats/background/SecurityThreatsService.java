package com.huawei.systemmanager.securitythreats.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.huawei.systemmanager.comm.SimpleProcessObserver;
import com.huawei.systemmanager.comm.SimpleProcessObserver.Stub;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;

public class SecurityThreatsService implements HsmService {
    private static final int HANDLER_PROCESS_DIED = 4;
    private static final int HANDLER_PROCESS_START_UP = 2;
    private static final int HANDLER_UNKNOW = -1;
    private static final int HANDLER_VIRUS_NEW_INSTALL = 1;
    private static final int HANDLER_VIRUS_NOTIFY_FINISH = 3;
    public static final String TAG = "SecurityThreatsService";
    private Context mAppContext = null;
    private Handler mHandler = null;
    private HandlerThread mHandlerThread = new HandlerThread("SecurityThreatsHandlerThread");
    private Stub mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            HwLog.v(SecurityThreatsService.TAG, "onForegroundActivitiesChanged: " + pid + SqlMarker.COMMA_SEPARATE + uid + SqlMarker.COMMA_SEPARATE + foregroundActivities);
            if (foregroundActivities) {
                Message msg = SecurityThreatsService.this.mHandler.obtainMessage(2);
                msg.arg1 = pid;
                msg.arg2 = uid;
                SecurityThreatsService.this.mHandler.sendMessage(msg);
            }
        }

        public void onProcessDied(int pid, int uid) {
            Message msg = SecurityThreatsService.this.mHandler.obtainMessage(4);
            msg.arg1 = pid;
            msg.arg2 = uid;
            SecurityThreatsService.this.mHandler.sendMessage(msg);
        }
    };
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwLog.i(SecurityThreatsService.TAG, "onReceive action=" + action);
                int message = -1;
                if (SecurityThreatsConst.ACTION_VIRUS_NEW_INSTALL.equals(action)) {
                    message = 1;
                } else if (SecurityThreatsConst.ACTION_VIRUS_NOTIFY_FINISH.equals(action)) {
                    message = 3;
                } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    SecurityThreatsService.this.handlerPackageRemoved(intent);
                }
                if (-1 != message) {
                    Message msg = SecurityThreatsService.this.mHandler.obtainMessage(message);
                    msg.setData(intent.getExtras());
                    SecurityThreatsService.this.mHandler.sendMessage(msg);
                }
            }
        }
    };

    private class InnerHandler extends Handler {
        public InnerHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            HwLog.d(SecurityThreatsService.TAG, "handleMessage msg=" + msg.what);
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    SecurityThreatsService.this.newInstallVirus(msg.getData());
                    return;
                case 2:
                    SecurityThreatsService.this.notifyVirusWhenStartUp(msg.arg1, msg.arg2);
                    return;
                case 3:
                    SecurityThreatsService.this.notifyVirusFinish(msg.getData());
                    return;
                case 4:
                    SecurityThreatsService.this.onVirusDied(msg.arg1, msg.arg2);
                    return;
                default:
                    return;
            }
        }
    }

    public SecurityThreatsService(Context context) {
        this.mAppContext = context.getApplicationContext();
    }

    public void init() {
        HwLog.v(TAG, "init");
        this.mHandlerThread.start();
        this.mHandler = new InnerHandler(this.mHandlerThread.getLooper());
        registerObserverAndReceiver();
    }

    public void onDestroy() {
        HwLog.v(TAG, "onDestroy");
        this.mHandlerThread.quit();
        unregisterObserverAndReceiver();
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    private void registerObserverAndReceiver() {
        HwLog.d(TAG, "registerObserver");
        SimpleProcessObserver.addObserver(this.mProcessObserver);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SecurityThreatsConst.ACTION_VIRUS_NOTIFY_FINISH);
        filter.addAction(SecurityThreatsConst.ACTION_VIRUS_NEW_INSTALL);
        this.mAppContext.registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction("android.intent.action.PACKAGE_REMOVED");
        filter2.addDataScheme("package");
        this.mAppContext.registerReceiver(this.mReceiver, filter2);
    }

    private void unregisterObserverAndReceiver() {
        SimpleProcessObserver.removeObserver(this.mProcessObserver);
        this.mAppContext.unregisterReceiver(this.mReceiver);
    }

    private void newInstallVirus(Bundle bundle) {
        if (bundle == null) {
            HwLog.w(TAG, "newInstallVirus bundle is null");
            return;
        }
        String pkg = bundle.getString("package_name", "");
        int level = bundle.getInt(SecurityThreatsConst.BUNDLE_KEY_VIRUS_LEVEL, 2);
        HwLog.i(TAG, "newInstallVirus pkg=" + pkg);
        VirusNotifyControl.getInstance(this.mAppContext).addInstallVirus(this.mAppContext, pkg, level);
    }

    private void notifyVirusWhenStartUp(int pid, int uid) {
        VirusNotifyControl.getInstance(this.mAppContext).notifyVirusWhenStartUp(pid, uid);
    }

    private void onVirusDied(int pid, int uid) {
        VirusNotifyControl.getInstance(this.mAppContext).onVirusDied(pid, uid);
    }

    private void notifyVirusFinish(Bundle bundle) {
        if (bundle == null) {
            HwLog.w(TAG, "notifyVirusFinish bundle is null");
            return;
        }
        String pkg = bundle.getString("package_name", "");
        HwLog.i(TAG, "notifyVirusFinish pkg=" + pkg);
        VirusNotifyControl.getInstance(this.mAppContext).removeInstallVirus(this.mAppContext, pkg);
    }

    private void handlerPackageRemoved(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            HwLog.w(TAG, "handlerPackageRemoved uri is null");
            return;
        }
        String pkg = uri.getSchemeSpecificPart();
        HwLog.i(TAG, "handlerPackageRemoved pkg=" + pkg);
        VirusNotifyControl.getInstance(this.mAppContext).removeInstallVirus(this.mAppContext, pkg);
    }
}
