package com.huawei.systemmanager.spacecleanner;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public class CommonHandler extends Handler {
    private WeakReference<MessageHandler> mMessageHandler;

    public interface MessageHandler {
        void handleMessage(Message message);
    }

    public CommonHandler(MessageHandler msgHandler) {
        this.mMessageHandler = new WeakReference(msgHandler);
    }

    public void handleMessage(Message msg) {
        MessageHandler realHandler = (MessageHandler) this.mMessageHandler.get();
        if (realHandler != null) {
            realHandler.handleMessage(msg);
        }
    }
}
