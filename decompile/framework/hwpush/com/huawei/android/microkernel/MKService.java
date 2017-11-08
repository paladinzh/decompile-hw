package com.huawei.android.microkernel;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import defpackage.aw;

public class MKService extends Service {
    private static final String TAG = "PushLog2841";
    private static Context appContext = null;

    public static Context getAppContext() {
        return appContext;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stopService() {
        try {
            if (appContext == null) {
                aw.d(TAG, " stopService,stop Pushservice ");
                stopSelf();
                return;
            }
            ComponentName componentName = new ComponentName(appContext, "com.huawei.deviceCloud.microKernel.push.PushMKService");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.setPackage(appContext.getPackageName());
            appContext.stopService(intent);
            aw.d(TAG, " stopService,stop Push Microkernel service ");
        } catch (Exception e) {
            aw.d(TAG, "Stop service fail");
        }
    }
}
