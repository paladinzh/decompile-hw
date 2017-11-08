package com.android.settingslib.bluetooth;

import android.content.Context;
import android.util.Log;
import java.util.List;

class BluetoothExtUtils {
    BluetoothExtUtils() {
    }

    public static void handleAdapterStateChange(LocalBluetoothAdapter adapter, int state, Context context) {
        if (context == null || adapter == null) {
            Log.e("BluetoothExtUtils", "in handleAdapterStateChange, params is error!");
        } else {
            new BluetoothPlatformImp().handleAdapterStateChange(adapter, state, context);
        }
    }

    public static boolean checkForDeviceRemoval(List<CachedBluetoothDevice> allCachedDevices, CachedBluetoothDevice cachedDevice) {
        if (allCachedDevices != null && cachedDevice != null) {
            return new BluetoothPlatformImp().checkForDeviceRemoval(allCachedDevices, cachedDevice);
        }
        Log.e("BluetoothExtUtils", "in checkForDeviceRemoval, params is error!");
        return false;
    }

    public static void broadcastAllDevicesRemoved(List<CachedBluetoothDevice> allCachedDevices, Context context) {
        if (allCachedDevices == null || context == null) {
            Log.e("BluetoothExtUtils", "in broadcastAllDevicesRemoved, params is error!");
        } else {
            new BluetoothPlatformImp().broadcastAllDevicesRemoved(allCachedDevices, context);
        }
    }
}
