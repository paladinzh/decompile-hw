package com.huawei.gallery.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import com.android.gallery3d.util.GalleryLog;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public abstract class AsyncService extends Service implements Callback {
    private HandlerThread mHandlerThread;
    protected Handler mServiceHandler;

    protected abstract void decorateMsg(Message message, Intent intent, int i);

    protected abstract String getServiceTag();

    public void onCreate() {
        this.mHandlerThread = new HandlerThread(getServiceTag());
        this.mHandlerThread.start();
        this.mServiceHandler = new Handler(this.mHandlerThread.getLooper(), this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            GalleryLog.e("AsyncService", "Intent is null in onStartCommand");
            return 2;
        }
        Message msg = this.mServiceHandler.obtainMessage();
        decorateMsg(msg, intent, startId);
        this.mServiceHandler.sendMessage(msg);
        return 3;
    }

    @SuppressWarnings({"UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR"})
    public void onDestroy() {
        this.mHandlerThread.quit();
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
