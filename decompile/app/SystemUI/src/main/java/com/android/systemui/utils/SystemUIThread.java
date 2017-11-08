package com.android.systemui.utils;

import android.os.Handler;
import android.os.HandlerThread;

public class SystemUIThread {
    private static Handler mSubThread = null;
    private static Handler mUIThread = null;

    public static abstract class SimpleAsyncTask {
        public boolean runInThread() {
            return true;
        }

        public void runInUI() {
        }
    }

    public static void init() {
        HandlerThread mSubHandlerThread = new HandlerThread("SystemUIApplication_subThread");
        mSubHandlerThread.start();
        mSubThread = new Handler(mSubHandlerThread.getLooper());
        mUIThread = new Handler();
        SystemUIIdleHandler.init();
    }

    public static void runAsync(final SimpleAsyncTask async) {
        mSubThread.post(new Runnable() {
            public void run() {
                if (async.runInThread()) {
                    Handler -get0 = SystemUIThread.mUIThread;
                    final SimpleAsyncTask simpleAsyncTask = async;
                    -get0.post(new Runnable() {
                        public void run() {
                            simpleAsyncTask.runInUI();
                        }
                    });
                }
            }
        });
    }
}
