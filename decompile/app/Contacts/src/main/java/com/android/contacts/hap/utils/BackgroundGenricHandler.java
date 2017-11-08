package com.android.contacts.hap.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class BackgroundGenricHandler extends Handler {
    private static BackgroundGenricHandler sInstance;
    private static boolean sIsRunning;
    private Looper mLooper;

    public static BackgroundGenricHandler getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        HandlerThread thread = new HandlerThread("BackgroundGenericHandler", 10);
        thread.start();
        sInstance = new BackgroundGenricHandler(thread.getLooper());
        sIsRunning = true;
        return sInstance;
    }

    private BackgroundGenricHandler(Looper aLooper) {
        super(aLooper);
        this.mLooper = aLooper;
    }

    public void cleanUp() {
        if (this.mLooper != null) {
            this.mLooper.quit();
            this.mLooper = null;
        }
    }

    public static void destroy() {
        if (sInstance != null && sIsRunning) {
            sIsRunning = false;
            sInstance.cleanUp();
            sInstance = null;
        }
    }
}
