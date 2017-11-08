package com.android.systemui.utils;

import android.annotation.SuppressLint;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import java.util.HashMap;
import java.util.Iterator;

@SuppressLint({"NewApi"})
public class SystemUIIdleHandler {
    private static final IdleHandler WAIT_TO_DEPOSE_IDLERHANDLER = new IdleHandler() {
        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean queueIdle() {
            while (true) {
                synchronized (SystemUIIdleHandler.mMessageArray) {
                    Iterator<Runnable> it = SystemUIIdleHandler.mMessageArray.values().iterator();
                    if (it.hasNext()) {
                        Runnable runnable = (Runnable) it.next();
                        it.remove();
                    } else {
                        SystemUIIdleHandler.releaseIdleHandler();
                        return false;
                    }
                }
            }
        }
    };
    private static final HashMap<Integer, Runnable> mMessageArray = new HashMap();
    private static MessageQueue mainMessageQueue = null;

    public static void addToIdleMessage(Runnable runnable, int type) {
        if (isMainLoopQueueIdle()) {
            runnable.run();
            return;
        }
        synchronized (mMessageArray) {
            if (mainMessageQueue == null) {
                HwLog.e("SystemUIIdleHandler", "when addToIdleMessage mainMessageQueue is not init!!");
                return;
            }
            HwLog.i("SystemUIIdleHandler", "enter addToIdleMessage(type:" + type + "), mainloop is busy, add to idleMessage");
            mMessageArray.put(Integer.valueOf(type), runnable);
            addToIdleHandler();
        }
    }

    private static boolean isMainLoopQueueIdle() {
        boolean isIdle;
        synchronized (mMessageArray) {
            isIdle = mainMessageQueue != null ? mainMessageQueue.isIdle() : false;
        }
        return isIdle;
    }

    private static void addToIdleHandler() {
        synchronized (mMessageArray) {
            if (mMessageArray.size() != 0) {
                mainMessageQueue.addIdleHandler(WAIT_TO_DEPOSE_IDLERHANDLER);
            } else {
                HwLog.e("SystemUIIdleHandler", "when addIdle to Handler, mMessageSet is empty!");
            }
        }
    }

    private static void releaseIdleHandler() {
        synchronized (mMessageArray) {
            if (mMessageArray.size() == 0) {
                mainMessageQueue.removeIdleHandler(WAIT_TO_DEPOSE_IDLERHANDLER);
            } else {
                HwLog.e("SystemUIIdleHandler", "when remove Idle handler, mMessageSet = " + mMessageArray.size());
            }
        }
    }

    protected static void init() {
        synchronized (mMessageArray) {
            mainMessageQueue = Looper.myQueue();
        }
    }
}
