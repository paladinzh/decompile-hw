package com.huawei.systemmanager.comm.component;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class HomeWatcherReceiver extends HsmBroadcastReceiver {
    private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
    private static final String TAG = "HomeWatcherReceiver";
    private static HomeWatcherReceiver sInstance = null;
    private static IHomePressListener sListener = null;
    private static final Object sLock = new Object();

    public static void register(Context context, IHomePressListener listener) {
        if (context == null) {
            HwLog.w(TAG, "onReceive: Invalid context");
            return;
        }
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new HomeWatcherReceiver();
            }
            sListener = listener;
            context.registerReceiver(sInstance, new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS"), "android.permission.INJECT_EVENTS", null);
        }
    }

    public static void unregister(Context context) {
        if (context == null) {
            HwLog.w(TAG, "onReceive: Invalid context");
            return;
        }
        synchronized (sLock) {
            if (sInstance == null) {
                return;
            }
            context.unregisterReceiver(sInstance);
            sInstance = null;
            sListener = null;
        }
    }

    public void onReceive(Context context, Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            HwLog.w(TAG, "onReceive: Invalid params");
            return;
        }
        String action = intent.getAction();
        HwLog.d(TAG, "onReceive: action = " + action);
        if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action)) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            HwLog.i(TAG, "onReceive: reason = " + reason);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                HwLog.i(TAG, "onReceive: homekey is pressed");
                if (sListener == null) {
                    HwLog.w(TAG, "doInBackground: Invalid listener");
                    return;
                }
                sListener.onHomePressed();
            }
        }
    }

    public void doInBackground(Context context, Intent intent) {
        synchronized (sLock) {
            if (sListener == null) {
                HwLog.w(TAG, "doInBackground: Invalid listener");
                return;
            }
            sListener.onHomePressed();
        }
    }
}
