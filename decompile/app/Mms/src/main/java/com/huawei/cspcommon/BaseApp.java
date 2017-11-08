package com.huawei.cspcommon;

import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.AbstractWindowedCursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.huawei.cspcommon.ex.ExceptionMonitor;
import com.huawei.cspcommon.ex.MemCollector;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cspcommon.ex.TmoMonitor;

public class BaseApp extends Application {
    private static Application sApp = null;
    private static long sMainThreadId;
    private EventReceiver mEventReceiver;

    private class EventReceiver extends BroadcastReceiver {
        private EventReceiver() {
        }

        public void registe() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SCREEN_OFF");
            intentFilter.addAction("android.intent.action.SCREEN_ON");
            BaseApp.this.registerReceiver(this, intentFilter);
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                MLog.w("CspApp", "Got an error intent " + intent);
            }
            if ("android.intent.action.SCREEN_OFF".equals(action)) {
                BaseApp.this.onScreenOff();
            } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                BaseApp.this.onScreenOn();
            }
        }
    }

    public void onCreate() {
        super.onCreate();
        setApplication(this);
        MemCollector.addCriticalClass(AbstractWindowedCursor.class);
        MemCollector.addCriticalClass(Bitmap.class);
        MemCollector.addCriticalClass(Activity.class);
        MemCollector.addCriticalClass(Fragment.class);
        TmoMonitor tmo = TmoMonitor.getInst();
        tmo.addWatchTarget("ThreadEx's defualtExecutor", ThreadEx.getDefaultExecutor());
        tmo.addWatchTarget("ThreadEx's defualtExecutor", ThreadEx.getSerialExecutor());
        tmo.addWatchTarget("AsyncTask.THREAD_POOL_EXECUTOR", AsyncTask.THREAD_POOL_EXECUTOR);
        tmo.addWatchTarget("AsyncTask.SERIAL_EXECUTOR", AsyncTask.SERIAL_EXECUTOR);
        ExceptionMonitor.init();
        this.mEventReceiver = new EventReceiver();
        this.mEventReceiver.registe();
        setMainThreadId(Thread.currentThread().getId());
    }

    private static void setMainThreadId(long id) {
        sMainThreadId = id;
    }

    public static boolean isInMainThread() {
        return Thread.currentThread().getId() == sMainThreadId;
    }

    public static synchronized Application getApplication() {
        Application application;
        synchronized (BaseApp.class) {
            application = sApp;
        }
        return application;
    }

    private static synchronized void setApplication(Application app) {
        synchronized (BaseApp.class) {
            sApp = app;
        }
    }

    public void onScreenOff() {
        TmoMonitor.getInst().stopWatch();
    }

    public void onScreenOn() {
        TmoMonitor.getInst().startWatch();
    }
}
