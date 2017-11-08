package com.huawei.systemmanager;

import android.os.HandlerThread;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;

public abstract class Task implements MessageHandler {
    protected GenericHandler mHandler;
    protected HandlerThread mHandlerThread;

    public abstract String getName();

    public abstract void registerListener();

    public abstract void unRegisterListener();

    public void init() {
        this.mHandlerThread = new HandlerThread(getName(), 10);
        this.mHandlerThread.start();
        this.mHandler = new GenericHandler(this, this.mHandlerThread.getLooper());
        registerListener();
    }

    public void destory() {
        unRegisterListener();
        this.mHandlerThread.quit();
    }
}
