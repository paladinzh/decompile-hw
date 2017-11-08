package com.huawei.powergenie.integration.eventhub;

import android.content.Context;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.util.Log;
import java.util.HashMap;
import java.util.Map.Entry;

public final class SysBroadcastReceiver extends BaseBroadcastReceiver {
    private static BaseBroadcastReceiver mBaseBroadcastReceiver;
    private static final HashMap<String, Integer> mMsgActionsIds = new HashMap<String, Integer>() {
        {
            put("android.intent.action.BOOT_COMPLETED", Integer.valueOf(302));
            put("android.intent.action.LOCKED_BOOT_COMPLETED", Integer.valueOf(302));
            put("android.intent.action.ACTION_SHUTDOWN", Integer.valueOf(303));
            put("android.intent.action.SCREEN_ON", Integer.valueOf(300));
            put("android.intent.action.SCREEN_OFF", Integer.valueOf(301));
            put("android.intent.action.USER_PRESENT", Integer.valueOf(304));
            put("android.intent.action.USER_SWITCHED", Integer.valueOf(359));
            put("android.intent.action.ACTION_POWER_CONNECTED", Integer.valueOf(310));
            put("android.intent.action.ACTION_POWER_DISCONNECTED", Integer.valueOf(311));
            put("android.intent.action.BATTERY_CHANGED", Integer.valueOf(308));
            put("android.intent.action.BATTERY_LOW", Integer.valueOf(319));
            put("android.intent.action.BATTERY_OKAY", Integer.valueOf(327));
            put("android.intent.action.SIM_STATE_CHANGED", Integer.valueOf(330));
            put("android.intent.action.AIRPLANE_MODE", Integer.valueOf(317));
            put("android.net.wifi.WIFI_STATE_CHANGED", Integer.valueOf(315));
            put("android.net.wifi.STATE_CHANGE", Integer.valueOf(314));
            put("android.net.conn.CONNECTIVITY_CHANGE", Integer.valueOf(312));
            put("android.net.conn.TETHER_STATE_CHANGED", Integer.valueOf(313));
            put("android.bluetooth.device.action.ACL_CONNECTED", Integer.valueOf(325));
            put("android.bluetooth.device.action.ACL_DISCONNECTED", Integer.valueOf(326));
            put("android.bluetooth.adapter.action.STATE_CHANGED", Integer.valueOf(336));
            put("android.hardware.usb.action.USB_STATE", Integer.valueOf(309));
            put("android.intent.action.WALLPAPER_CHANGED", Integer.valueOf(318));
            put("android.intent.action.HEADSET_PLUG", Integer.valueOf(335));
            put("android.nfc.action.ADAPTER_STATE_CHANGED", Integer.valueOf(337));
            put("android.net.wifi.WIFI_AP_STATE_CHANGED", Integer.valueOf(338));
            put("android.intent.action.PACKAGE_ADDED", Integer.valueOf(305));
            put("android.intent.action.PACKAGE_REMOVED", Integer.valueOf(307));
            put("android.intent.action.PACKAGE_CHANGED", Integer.valueOf(306));
        }
    };

    protected Integer getMsgId(String action) {
        if (action != null) {
            return (Integer) mMsgActionsIds.get(action);
        }
        return null;
    }

    protected static void init(Context context) {
        if (mBaseBroadcastReceiver != null) {
            Log.i("MsgHub", "SYS Receiver has been inited.");
            return;
        }
        mBaseBroadcastReceiver = new SysBroadcastReceiver();
        IntentFilter filterAsOwner = new IntentFilter();
        IntentFilter filterAllUser = new IntentFilter();
        for (Entry entry : mMsgActionsIds.entrySet()) {
            String action = (String) entry.getKey();
            if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                filterAsOwner.addAction(action);
            } else if (!("android.intent.action.BOOT_COMPLETED".equals(action) || "android.intent.action.LOCKED_BOOT_COMPLETED".equals(action))) {
                if ("android.intent.action.PACKAGE_ADDED".equals(action) || "android.intent.action.PACKAGE_CHANGED".equals(action) || "android.intent.action.PACKAGE_REMOVED".equals(action)) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(action);
                    filter.addDataScheme("package");
                    context.registerReceiverAsUser(mBaseBroadcastReceiver, UserHandle.ALL, filter, null, null);
                } else {
                    filterAllUser.addAction(action);
                }
            }
        }
        context.registerReceiverAsUser(mBaseBroadcastReceiver, UserHandle.ALL, filterAllUser, null, null);
        context.registerReceiverAsUser(new SysBroadcastReceiver(), UserHandle.OWNER, filterAsOwner, null, null);
    }
}
