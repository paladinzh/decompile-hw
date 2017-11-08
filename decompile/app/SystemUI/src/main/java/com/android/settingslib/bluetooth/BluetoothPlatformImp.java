package com.android.settingslib.bluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;
import java.util.List;

public class BluetoothPlatformImp {
    public void handleAdapterStateChange(LocalBluetoothAdapter adapter, int state, Context context) {
        if (context == null || adapter == null) {
            Log.e("BluetoothPlatformImp", "handleAdapterStateChange, illegal argument!");
        } else if (12 == state && SystemProperties.getBoolean("ro.config.bt_discovery_default", true)) {
            makeDeviceDiscoverable(adapter, PreferenceManager.getDefaultSharedPreferences(context).getInt("bt_discoverable_timeout_number", 120), context);
        }
    }

    private boolean makeDeviceDiscoverable(LocalBluetoothAdapter adapter, int timeout, Context context) {
        if (!adapter.setScanMode(23, timeout)) {
            return false;
        }
        long endTime = System.currentTimeMillis() + (((long) timeout) * 1000);
        persistDiscoverableEndTimestamp(context, endTime);
        if (timeout > 0) {
            BluetoothDiscoverableTimeoutReceiver.setDiscoverableAlarm(context, endTime);
        }
        return true;
    }

    static void persistDiscoverableEndTimestamp(Context context, long endTimestamp) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putLong("discoverable_end_timestamp", endTimestamp);
        editor.apply();
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("bluetooth_settings", 0);
    }

    public boolean checkForDeviceRemoval(List<CachedBluetoothDevice> allCachedDevices, CachedBluetoothDevice cachedDevice) {
        synchronized (allCachedDevices) {
            if (cachedDevice.getBondState() != 10 || cachedDevice.isVisible()) {
                return false;
            }
            allCachedDevices.remove(cachedDevice);
            return true;
        }
    }

    public void broadcastAllDevicesRemoved(List<CachedBluetoothDevice> allCachedDevices, Context context) {
        if (allCachedDevices == null || context == null) {
            Log.w("BluetoothPlatformImp", "in broadcastAllDevicesRemoved, params is error!");
        } else if ("com.android.systemui".equals(context.getPackageName())) {
            Log.w("BluetoothPlatformImp", "in broadcastAllDevicesRemoved, systemui cannot allowed to send broadcast!");
        } else {
            synchronized (allCachedDevices) {
                for (int i = allCachedDevices.size() - 1; i >= 0; i--) {
                    CachedBluetoothDevice cachedDevice = (CachedBluetoothDevice) allCachedDevices.get(i);
                    if (cachedDevice != null) {
                        Intent intent = new Intent("android.bluetooth.device.action.DISAPPEARED");
                        intent.putExtra("android.bluetooth.device.extra.DEVICE", cachedDevice.getDevice());
                        context.sendBroadcast(intent, "android.permission.BLUETOOTH");
                    }
                }
            }
        }
    }
}
