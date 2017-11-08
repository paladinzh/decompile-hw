package com.huawei.systemmanager.comm.component;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.lang.ref.WeakReference;

public class GenericHandler extends Handler {
    private WeakReference<MessageHandler> mReference;

    public interface MessageHandler {
        void onHandleMessage(Message message);
    }

    public GenericHandler(MessageHandler messageHander) {
        this.mReference = new WeakReference(messageHander);
    }

    public GenericHandler(MessageHandler messageHandler, Looper looper) {
        super(looper);
        this.mReference = new WeakReference(messageHandler);
    }

    public void quiteLooper() {
        removeCallbacksAndMessages(null);
        Looper looper = getLooper();
        if (looper != null && looper != Looper.getMainLooper()) {
            looper.quit();
        }
    }

    public void handleMessage(Message msg) {
        MessageHandler handler = (MessageHandler) this.mReference.get();
        if (handler != null) {
            handler.onHandleMessage(msg);
        }
        super.handleMessage(msg);
    }
}
