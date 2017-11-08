package com.android.deskclock;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import java.lang.Thread.State;

public final class AsyncHandler {
    private static final Handler sHandler;
    private static final HandlerThread sHandlerThread = new HandlerThread("AsyncHandler");

    static {
        Handler handler;
        sHandlerThread.start();
        Looper lp = sHandlerThread.getLooper();
        if (lp == null) {
            handler = new Handler();
        } else {
            handler = new Handler(lp);
        }
        sHandler = handler;
    }

    public static void post(Runnable r) {
        sHandler.post(r);
    }

    public static State getState() {
        return sHandlerThread.getState();
    }

    public static void quit() {
        sHandlerThread.quit();
    }

    private AsyncHandler() {
    }
}
