package com.huawei.gallery.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.GalleryLog;

public class MediaMountReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (ApiHelper.HAS_MULTI_USER_STORAGE) {
            GalleryLog.d("MediaMountReceiver", "MediaMountReceiver initializeStorageVolume");
            context.startService(new Intent(context, MediaMountService.class));
        }
    }
}
