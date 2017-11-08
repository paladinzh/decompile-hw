package com.huawei.gallery.util;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.media.MediaSyncerService;
import com.huawei.gallery.provider.GalleryMediaObserver;

public class MediaSyncerHelper {
    private static GalleryMediaObserver sObserver;
    private static volatile boolean sTerminateMediaSync = false;

    public static synchronized void terminateMediaSyncerService() {
        synchronized (MediaSyncerHelper.class) {
            sTerminateMediaSync = true;
            GalleryLog.d("MediaSyncerHelper", "terminate media sync service");
        }
    }

    public static synchronized void startMediaSyncerService(Context context) {
        synchronized (MediaSyncerHelper.class) {
            if (context == null) {
                return;
            }
            sTerminateMediaSync = false;
            context.startService(new Intent(context, MediaSyncerService.class));
            GalleryLog.d("MediaSyncerHelper", "restore media sync batch");
        }
    }

    public static synchronized boolean isMediaSyncerTerminated() {
        boolean z;
        synchronized (MediaSyncerHelper.class) {
            z = sTerminateMediaSync;
        }
        return z;
    }

    public static synchronized void registerMediaObserver(Context context) {
        synchronized (MediaSyncerHelper.class) {
            if (sObserver != null) {
                return;
            }
            sObserver = new GalleryMediaObserver(context, new Handler(Looper.getMainLooper()));
            sObserver.register();
            GalleryLog.i("MediaSyncerHelper", "register media observer");
        }
    }
}
