package com.huawei.optimizer.base;

import android.os.Handler;
import android.os.Message;
import java.lang.ref.WeakReference;

public abstract class NoLeakHandler<T> extends Handler {
    private final WeakReference<T> mContext;

    protected abstract void processMessage(T t, Message message);

    public NoLeakHandler(T context) {
        this.mContext = new WeakReference(context);
    }

    public void handleMessage(Message msg) {
        T context = this.mContext.get();
        if (context != null) {
            processMessage(context, msg);
        }
    }
}
