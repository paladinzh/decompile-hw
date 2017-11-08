package com.huawei.systemmanager.rainbow.client.background.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

public class IntelligentCompleteService extends Service {
    private static final int MSG_BASE = 1;
    private static final int MSG_NOTIFICATION_COMPLETE = 2;
    private static final String TAG = "IntelligentCompleteService";
    private static final long TIME_DELEAY = 20000;
    private NotificationHandler mHandler;
    private HandlerThread mHandlerThread;
    private NotificationManager mNotificationManager;

    private class NotificationHandler extends Handler {
        private Context mContext;

        public NotificationHandler(Context context, Looper looper) {
            super(looper);
            this.mContext = context;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    IntelligentCompleteService.this.sendCompleteNotification(this.mContext);
                    IntelligentCompleteService.this.stopSelf(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        this.mHandlerThread = new HandlerThread(TAG);
        this.mHandlerThread.start();
        this.mHandler = new NotificationHandler(getApplicationContext(), this.mHandlerThread.getLooper());
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf(startId);
            return 2;
        }
        handlerAction(intent, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandlerThread.quit();
    }

    private void handlerAction(Intent intent, int startId) {
        this.mHandler.removeMessages(2);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, startId, 0), 20000);
    }

    private void sendCompleteNotification(Context context) {
    }
}
