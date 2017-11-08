package com.android.deskclock;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.android.util.HwLog;

public class BootstrapService extends Service {
    private final String TAG = "BootstrapService";
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BootstrapService.this.stopSelf();
        }
    };

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        HwLog.i("BootstrapService", "onCreate");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        HwLog.i("BootstrapService", "onStartCommand");
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 120000);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        super.onDestroy();
        HwLog.i("BootstrapService", "onDestroy");
        this.mHandler.removeCallbacksAndMessages(null);
    }

    public static void startBootstrapService(Context context) {
        if (context != null) {
            context.startService(new Intent(context, BootstrapService.class));
        }
    }
}
