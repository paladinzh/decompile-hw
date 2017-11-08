package com.android.contacts.hap.util;

import android.os.Handler;
import android.os.Message;

public class GenericHandler extends Handler {
    public synchronized void requestDelayedExecution(Runnable aRunnable, long aDelayInMillis) {
        Message msg = obtainMessage();
        msg.obj = aRunnable;
        sendMessageDelayed(msg, aDelayInMillis);
    }

    public void clearAllMsg() {
        removeMessages(0);
    }

    public void handleMessage(Message aMsg) {
        Runnable runnable = aMsg.obj;
        if (runnable != null) {
            runnable.run();
        }
    }
}
