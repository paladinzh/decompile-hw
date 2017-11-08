package com.huawei.gallery.phonestatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.gallery.media.CloudLocalSyncService;
import com.huawei.gallery.media.StoryAlbumService;
import com.huawei.gallery.util.MyPrinter;

public class ScreenStateReceiver {
    private static final MyPrinter LOG = new MyPrinter("ScreenStateReceiver");
    private static BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                ScreenStateReceiver.LOG.d("screen off");
                ScreenStateReceiver.unRegisterScreenOffBroadcast(context);
                if (!CloudLocalSyncService.isSyncWorking()) {
                    ScreenStateReceiver.startScreenOffService(context);
                }
            }
        }
    };
    private static boolean sScreenOffRegister = false;

    public static void registerScreenOffBroadcast(Context context) {
        if (!sScreenOffRegister) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.SCREEN_OFF");
            context.getApplicationContext().registerReceiver(mScreenOffReceiver, filter);
            sScreenOffRegister = true;
        }
    }

    private static void unRegisterScreenOffBroadcast(Context context) {
        if (sScreenOffRegister) {
            context.getApplicationContext().unregisterReceiver(mScreenOffReceiver);
            sScreenOffRegister = false;
        }
    }

    private static void startScreenOffService(Context context) {
        StoryAlbumService.startStoryService(context, 1);
    }
}
