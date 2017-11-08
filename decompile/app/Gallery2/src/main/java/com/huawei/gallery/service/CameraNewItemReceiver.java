package com.huawei.gallery.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.android.gallery3d.util.GalleryLog;

public class CameraNewItemReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getData();
        if (uri == null || !"content".equals(uri.getScheme())) {
            GalleryLog.e("CameraNewItemReceiver", "uri is invalidate:" + uri);
            return;
        }
        GalleryLog.d("CameraNewItemReceiver", "receive new item:" + uri);
        Intent serviceIntent = new Intent(context, CameraNewItemThumbnailService.class);
        serviceIntent.setData(uri);
        context.startService(serviceIntent);
    }
}
