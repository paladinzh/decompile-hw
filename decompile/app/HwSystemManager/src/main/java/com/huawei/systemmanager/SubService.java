package com.huawei.systemmanager;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

public abstract class SubService {

    public static class HsmBinder {
        private IBinder binder;
        private String binderName;

        public HsmBinder(String binderName, IBinder binder) {
            this.binderName = binderName;
            this.binder = binder;
        }

        public String getBinderName() {
            return this.binderName;
        }

        public IBinder getBinder() {
            return this.binder;
        }
    }

    public abstract HsmBinder onBind();

    public void onCreate() {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void onDestroy() {
    }
}
