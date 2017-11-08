package com.huawei.systemmanager.competitorintercept;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.SimpleProcessObserver;
import com.huawei.systemmanager.comm.SimpleProcessObserver.Stub;
import com.huawei.systemmanager.comm.collections.ArrayChecker;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.mainscreen.MainScreenActivity;
import com.huawei.systemmanager.optimize.ProcessManager;
import com.huawei.systemmanager.rainbow.db.CloudDBAdapter;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.util.HwLog;

public class NotificationGuideService implements HsmService {
    private static final String ACTION_USER_CLICK_GUIDE_NOTIFICATION = "com.huawei.systemmanager.ACTION.userclickguide";
    private static final long DELAY_CANCEL_NOTIFICATION = 10000;
    private static final String KEY_COMPETITOR_PKG = "competitor_pkg";
    private static final int MSG_ACTIVIY_FOREGROUND = 2;
    private static final int MSG_CANCEL_NOTIFICATION = 3;
    private static final int MSG_INIT = 1;
    public static final String TAG = "NotificationGuideService";
    private final Context mContext;
    private Handler mHandler;
    private boolean mInited;
    private final PackageManager mPkgManager;
    private Stub mProcessObserver = new Stub() {
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) {
            if (foregroundActivities) {
                NotificationGuideService.this.mHandler.obtainMessage(2, pid, uid).sendToTarget();
            }
        }
    };
    private BroadcastReceiver mRecevier = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (Utility.checkBroadcast(context, intent)) {
                String action = intent.getAction();
                HwLog.i(NotificationGuideService.TAG, "receive action:" + action);
                if (NotificationGuideService.ACTION_USER_CLICK_GUIDE_NOTIFICATION.equals(action)) {
                    doUserClickGuildNotifi(intent);
                }
            }
        }

        private void doUserClickGuildNotifi(Intent intent) {
            Intent startIntent = new Intent("android.intent.action.MAIN");
            startIntent.addCategory("android.intent.category.LAUNCHER");
            startIntent.setClass(NotificationGuideService.this.mContext, MainScreenActivity.class);
            startIntent.setFlags(270532608);
            NotificationGuideService.this.mContext.startActivity(startIntent);
            HsmStat.statE((int) Events.E_COMPETITOR_GUIDE_BANNER_CLICK, HsmStatConst.PARAM_PKG, intent.getStringExtra(NotificationGuideService.KEY_COMPETITOR_PKG));
            if (!TextUtils.isEmpty(intent.getStringExtra(NotificationGuideService.KEY_COMPETITOR_PKG))) {
                ProcessManager.clearPackages(Lists.newArrayList(competitorPkg));
            }
        }
    };

    private class CustomHander extends Handler {
        public CustomHander(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    HwLog.i(NotificationGuideService.TAG, "handler msg MSG_INIT");
                    NotificationGuideService.this.doInitRecords();
                    return;
                case 2:
                    int uid = msg.arg2;
                    HwLog.i(NotificationGuideService.TAG, "handle MSG_ACTIVIY_FOREGROUND, uid:" + uid);
                    String[] pkgs = NotificationGuideService.this.mPkgManager.getPackagesForUid(uid);
                    if (!ArrayChecker.isEmpty(pkgs)) {
                        if (!InterceptRecords.checkAndSetElapsedTime(NotificationGuideService.this.mContext)) {
                            HwLog.i(NotificationGuideService.TAG, "checkAndSetElapsedTime false, do init here");
                            NotificationGuideService.this.doInitRecords();
                        }
                        for (String pkg : pkgs) {
                            if (NotificationGuideService.this.isCompetitor(pkg) && InterceptRecords.checkAndRecords(NotificationGuideService.this.mContext, pkg)) {
                                HwLog.i(NotificationGuideService.TAG, "show notification guide banner, pkg:" + pkg + ", uid:" + uid);
                                NotificationGuideService.this.showNotification(pkg);
                                return;
                            }
                        }
                        return;
                    }
                    return;
                case 3:
                    ((NotificationManager) NotificationGuideService.this.mContext.getSystemService("notification")).cancel(Const.NOTIFICATION_ID_GUIDE_TO_HSM);
                    return;
                default:
                    return;
            }
        }
    }

    public NotificationGuideService(Context ctx) {
        this.mContext = ctx;
        this.mPkgManager = ctx.getPackageManager();
    }

    public void init() {
        HandlerThread thread = new HandlerThread(TAG);
        thread.start();
        this.mHandler = new CustomHander(thread.getLooper());
        SimpleProcessObserver.addObserver(this.mProcessObserver);
        this.mContext.registerReceiver(this.mRecevier, new IntentFilter(ACTION_USER_CLICK_GUIDE_NOTIFICATION), "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    public void onDestroy() {
        this.mContext.unregisterReceiver(this.mRecevier);
        SimpleProcessObserver.removeObserver(this.mProcessObserver);
        this.mHandler.getLooper().quit();
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                this.mHandler.sendEmptyMessage(1);
            }
        }
    }

    private boolean isCompetitor(String pkg) {
        return CloudDBAdapter.getInstance(this.mContext.getApplicationContext()).isCompetitor(pkg);
    }

    private void doInitRecords() {
        if (this.mInited) {
            HwLog.i(TAG, "doInitRecords called, but is already init");
            return;
        }
        this.mInited = true;
        InterceptRecords.clearAllRecords(this.mContext);
    }

    private void showNotification(String competitorPkg) {
        this.mHandler.removeMessages(3);
        Intent intent = new Intent();
        intent.putExtra(KEY_COMPETITOR_PKG, competitorPkg);
        intent.setAction(ACTION_USER_CLICK_GUIDE_NOTIFICATION);
        intent.setPackage(this.mContext.getPackageName());
        ((NotificationManager) this.mContext.getSystemService("notification")).notify(Const.NOTIFICATION_ID_GUIDE_TO_HSM, new Builder(this.mContext).setSmallIcon(R.drawable.ic_launcher).setContentTitle(this.mContext.getString(R.string.notification_guide_to_systemmanager)).setContentIntent(PendingIntent.getBroadcast(this.mContext, 0, intent, 134217728)).setDefaults(2).setPriority(2).setAutoCancel(true).setShowWhen(false).build());
        HsmStat.statE((int) Events.E_COMPETITOR_GUIDE_BANNER_SHOW, HsmStatConst.PARAM_PKG, competitorPkg);
        this.mHandler.sendEmptyMessageDelayed(3, 10000);
    }
}
