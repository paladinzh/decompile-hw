package com.huawei.gallery.media;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.huawei.gallery.util.MyPrinter;

public class CloudLocalSyncService {
    private static final MyPrinter LOG = new MyPrinter("CloudLocalSyncService");
    private static int cloudSync = -1;
    private static int localSync = -1;
    private static Runnable sCloudSyncTimeExceed = new Runnable() {
        public void run() {
            CloudLocalSyncService.LOG.d("--schedule-- cloud sync time exceed ...");
            if (CloudLocalSyncService.sContext != null) {
                CloudLocalSyncService.stopCloudSync(CloudLocalSyncService.sContext);
            }
        }
    };
    private static Context sContext;
    private static Handler sTimeWaitHandler = new Handler(Looper.getMainLooper());

    public static synchronized void startCloudSync(Context context) {
        synchronized (CloudLocalSyncService.class) {
            sTimeWaitHandler.removeCallbacks(sCloudSyncTimeExceed);
            sTimeWaitHandler.postDelayed(sCloudSyncTimeExceed, 30000);
            if (cloudSync == 0) {
                return;
            }
            LOG.d("--schedule-- start cloud sync");
            sContext = context;
            cloudSync = 0;
            stopChildService(context);
        }
    }

    public static synchronized void startLocalSync(Context context) {
        synchronized (CloudLocalSyncService.class) {
            if (localSync == 0) {
                return;
            }
            LOG.d("--schedule-- start local sync");
            localSync = 0;
            stopChildService(context);
        }
    }

    public static synchronized void stopCloudSync(Context context) {
        synchronized (CloudLocalSyncService.class) {
            LOG.d("--schedule-- stop cloud sync");
            cloudSync = 1;
            sTimeWaitHandler.removeCallbacks(sCloudSyncTimeExceed);
            startChildService(context);
        }
    }

    public static synchronized void stopLocalSync(Context context) {
        synchronized (CloudLocalSyncService.class) {
            LOG.d("--schedule-- stop local sync");
            localSync = 1;
            startChildService(context);
        }
    }

    public static synchronized boolean isSyncWorking() {
        boolean z = true;
        synchronized (CloudLocalSyncService.class) {
            LOG.d("localSync = " + localSync + "  cloudSync = " + cloudSync);
            if (!(localSync == 0 || cloudSync == 0)) {
                z = false;
            }
        }
        return z;
    }

    private static void startChildService(Context context) {
        if (!isSyncWorking()) {
            LOG.d("--schedule-- start child service");
            context.startService(new Intent().setClass(context, GeoService.class));
            StoryAlbumService.startStoryService(context, 2);
        }
    }

    private static void stopChildService(Context context) {
        LOG.d("--schedule-- stop child service");
        context.stopService(new Intent().setClass(context, GeoService.class));
        StoryAlbumService.stopStoryService(context);
        RecycleClearService.stopRecycleClearService(context);
    }
}
