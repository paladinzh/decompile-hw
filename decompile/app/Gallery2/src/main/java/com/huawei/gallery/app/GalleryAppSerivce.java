package com.huawei.gallery.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.gallery3d.util.GalleryLog;

public class GalleryAppSerivce extends Service {
    public IBinder onBind(Intent intent) {
        GalleryLog.d("GalleryAppSerivce", "onBind");
        return null;
    }

    public void onCreate() {
        GalleryLog.d("GalleryAppSerivce", "onCreate");
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        GalleryLog.d("GalleryAppSerivce", "onStartCommand startId:" + startId);
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        GalleryLog.d("GalleryAppSerivce", "onDestroy");
        super.onDestroy();
    }

    public boolean onUnbind(Intent intent) {
        GalleryLog.d("GalleryAppSerivce", "onUnbind");
        return super.onUnbind(intent);
    }
}
