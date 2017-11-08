package com.huawei.gallery.phonestatus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.huawei.gallery.media.CloudLocalSyncService;
import com.huawei.gallery.media.StoryAlbumService;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.watermark.manager.parse.util.ParseJson;

public class PowerStateReceiver extends BroadcastReceiver {
    private static final MyPrinter LOG = new MyPrinter("PowerStateReceiver");
    private static BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                int level = PowerStateReceiver.getCurrentBattery(intent);
                PhoneState.setBatteryLevel(level);
                PowerStateReceiver.LOG.d("battery current is " + level);
                if (level >= 10) {
                    PowerStateReceiver.unRegisterBatteryBroadcastReceiver(context);
                    if (!CloudLocalSyncService.isSyncWorking()) {
                        PowerStateReceiver.startSelfBootService(context);
                    }
                }
            }
        }
    };
    private static boolean sRegisterState = false;

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LOG.d("onReceive intent=" + intent + " action=" + action);
        if ("android.intent.action.ACTION_POWER_CONNECTED".equals(action)) {
            PhoneState.setChargeState(0);
            registerBatteryBroadcastReceiver(context);
        } else if ("android.intent.action.ACTION_POWER_DISCONNECTED".equals(action)) {
            PhoneState.setChargeState(1);
            unRegisterBatteryBroadcastReceiver(context);
        }
    }

    private static void startSelfBootService(Context context) {
        StoryAlbumService.startStoryService(context, 1);
    }

    public static synchronized void registerBatteryBroadcastReceiver(Context context) {
        synchronized (PowerStateReceiver.class) {
            if (sRegisterState) {
                return;
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.BATTERY_CHANGED");
            Intent batteryIntent = context.getApplicationContext().registerReceiver(batteryLevelReceiver, filter);
            if (isChargePlugIn(batteryIntent)) {
                PhoneState.setChargeState(0);
            } else {
                PhoneState.setChargeState(1);
            }
            PhoneState.setBatteryLevel(getCurrentBattery(batteryIntent));
            sRegisterState = true;
        }
    }

    private static void unRegisterBatteryBroadcastReceiver(Context context) {
        if (sRegisterState) {
            context.getApplicationContext().unregisterReceiver(batteryLevelReceiver);
            sRegisterState = false;
        }
    }

    private static boolean isChargePlugIn(Intent intent) {
        if (intent.getIntExtra("plugged", -1) != 0) {
            return true;
        }
        return false;
    }

    private static int getCurrentBattery(Intent intent) {
        int battery = (int) ((((float) intent.getIntExtra(ParseJson.LEVEL, -1)) / ((float) intent.getIntExtra("scale", -1))) * 100.0f);
        LOG.d("battery = " + battery);
        return battery;
    }
}
