package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.SpaceClear;
import com.huawei.systemmanager.spacecleanner.SpaceCleanActivity;
import com.huawei.systemmanager.spacecleanner.utils.AppCleanUpAndStorageNotifyUtils;
import com.huawei.systemmanager.util.HwLog;

public class AppCleanUpService extends Service {
    private static final String TAG = "AppCleanUpService";
    private Context mContext;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            HwLog.e(AppCleanUpService.TAG, "msg is " + msg.what);
            super.handleMessage(msg);
            switch (msg.what) {
                case 2:
                    new Thread(new Runnable() {
                        public void run() {
                            AppCleanUpAndStorageNotifyUtils.sendLackOfMemoryNotification(AppCleanUpService.this.mContext);
                        }
                    }, "AppCleanUpService_setLackSpaceAlarm_thread").start();
                    return;
                case 4:
                    HwLog.i(AppCleanUpService.TAG, "ACTION_NOTIFICATION_BUTTON_CLICK_LEFT received !");
                    Intent leftOnClickIntent = new Intent();
                    leftOnClickIntent.setClass(AppCleanUpService.this, SpaceCleanActivity.class);
                    leftOnClickIntent.setFlags(335544320);
                    leftOnClickIntent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, SpaceClear.ACTION_CLICK_LOW_STORAGE_NOTIFICATION);
                    leftOnClickIntent.putExtra(HsmStatConst.KEY_SHOULD_STAT, false);
                    AppCleanUpService.this.mContext.startActivity(leftOnClickIntent);
                    AppCleanUpAndStorageNotifyUtils.collapseStatusBar(AppCleanUpService.this.mContext);
                    ((NotificationManager) AppCleanUpService.this.mContext.getSystemService("notification")).cancel(R.string.lack_space_notify_content);
                    return;
                case 5:
                    HwLog.i(AppCleanUpService.TAG, "ACTION_NOTIFICATION_BUTTON_CLICK_RIGHT received !");
                    Intent rightOnClickIntent = new Intent();
                    rightOnClickIntent.setAction("android.settings.MEMORY_CARD_SETTINGS");
                    rightOnClickIntent.setFlags(335544320);
                    rightOnClickIntent.putExtra(HsmStatConst.KEY_NOTFICATION_EVENT, SpaceClear.ACTION_CLICK_LOW_STORAGE_NOTIFICATION);
                    rightOnClickIntent.putExtra(HsmStatConst.KEY_SHOULD_STAT, false);
                    AppCleanUpService.this.mContext.startActivity(rightOnClickIntent);
                    AppCleanUpAndStorageNotifyUtils.collapseStatusBar(AppCleanUpService.this.mContext);
                    ((NotificationManager) AppCleanUpService.this.mContext.getSystemService("notification")).cancel(R.string.lack_space_notify_content);
                    return;
                default:
                    return;
            }
        }
    };

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.mContext = getApplicationContext();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.d(TAG, "onStartCommand() in!");
        if (intent == null) {
            HwLog.d(TAG, "null == intent");
            return 2;
        }
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return 2;
        }
        Message msg = this.mHandler.obtainMessage(bundle.getInt(AppCleanUpAndStorageNotifyUtils.SERVICE_INTENT_ARGS));
        msg.obj = bundle.getString(AppCleanUpAndStorageNotifyUtils.PACKAGE_NAME_ARGS);
        this.mHandler.sendMessage(msg);
        HwLog.d(TAG, "onStartCommand() out!");
        return 2;
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
