package com.huawei.systemmanager.util.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.procpolicy.ProcessPolicy;

public abstract class HsmBroadcastReceiver extends BroadcastReceiver {
    public static final String EXTRA_ORIGINAL_CLASS = "com.huawei.systemmanager.util.content.original_class";
    public static final String EXTRA_ORIGINAL_INTENT = "com.huawei.systemmanager.util.content.original_intent";
    public static final String METHOD_DO_IN_BACKGROUND = "doInBackground";
    private static final String TAG = "HsmBroadcastReceiver";

    public final void sendToBackground(Context context, Intent origIntent) {
        if (origIntent == null) {
            HwLog.w(TAG, "try to handle a null intent broadcast background, ignore it.");
            return;
        }
        Class<?> serviceClass = new ProcessPolicy().getIntentServiceClass();
        if (serviceClass == null) {
            HwLog.w(TAG, "can't get corret service, usually because get process name fail.");
            dispatchToThread(context, origIntent);
        } else if (origIntent.hasFileDescriptors()) {
            HwLog.w(TAG, "can't start service because the intent has fd, dispatch to thread.");
            dispatchToThread(context, origIntent);
            sendIdleWorkToService(context, serviceClass);
        } else {
            try {
                sendIntentToService(context, origIntent, serviceClass);
            } catch (Exception e) {
                HwLog.e(TAG, "sendIntentToService occur exception", e);
                dispatchToThread(context, origIntent);
                sendIdleWorkToService(context, serviceClass);
            }
        }
    }

    private void sendIdleWorkToService(Context context, Class<?> serviceClass) {
        sendIntentToService(context, null, serviceClass);
    }

    private void sendIntentToService(Context context, Intent origIntent, Class<?> serviceClass) {
        Intent i = new Intent(context, serviceClass);
        i.putExtra(EXTRA_ORIGINAL_INTENT, origIntent);
        i.putExtra(EXTRA_ORIGINAL_CLASS, getClass().getName());
        context.startServiceAsUser(i, UserHandle.CURRENT);
    }

    private void dispatchToThread(final Context context, final Intent origIntent) {
        new Thread("util_receiver_background") {
            public void run() {
                HsmBroadcastReceiver.this.doInBackground(context, origIntent);
            }
        }.start();
    }

    public void doInBackground(Context context, Intent intent) {
    }
}
