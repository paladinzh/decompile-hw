package com.android.settings.bluetooth;

import android.app.QueuedWork;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

final class LocalBluetoothPreferences {
    private LocalBluetoothPreferences() {
    }

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("bluetooth_settings", 0);
    }

    static long getDiscoverableEndTimestamp(Context context) {
        return getSharedPreferences(context).getLong("discoverable_end_timestamp", 0);
    }

    static boolean shouldShowDialogInForeground(Context context, String deviceAddress, String deviceName) {
        LocalBluetoothManager manager = Utils.getLocalBtManager(context);
        if (manager == null) {
            HwLog.e("LocalBluetoothPreferences", "manager == null - do not show dialog.");
            return false;
        } else if (manager.isForegroundActivity()) {
            return true;
        } else {
            if ((context.getResources().getConfiguration().uiMode & 5) == 5) {
                HwLog.d("LocalBluetoothPreferences", "in appliance mode - do not show dialog.");
                return false;
            }
            long currentTimeMillis = System.currentTimeMillis();
            SharedPreferences sharedPreferences = getSharedPreferences(context);
            if (60000 + sharedPreferences.getLong("discoverable_end_timestamp", 0) > currentTimeMillis) {
                return true;
            }
            LocalBluetoothAdapter adapter = manager.getBluetoothAdapter();
            if (adapter != null && adapter.isDiscovering()) {
                return true;
            }
            if (sharedPreferences.getLong("last_discovering_time", 0) + 60000 > currentTimeMillis) {
                return true;
            }
            if (deviceAddress != null) {
                if (deviceAddress.equals(sharedPreferences.getString("last_selected_device", null)) && 60000 + sharedPreferences.getLong("last_selected_device_time", 0) > currentTimeMillis) {
                    return true;
                }
            }
            if (!TextUtils.isEmpty(deviceName)) {
                if (deviceName.equals(context.getString(17039468))) {
                    HwLog.v("LocalBluetoothPreferences", "showing dialog for packaged keyboard");
                    return true;
                }
            }
            HwLog.d("LocalBluetoothPreferences", "Found no reason to show the dialog - do not show dialog.");
            return false;
        }
    }

    static void persistSelectedDeviceInPicker(Context context, String deviceAddress) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putString("last_selected_device", deviceAddress);
        editor.putLong("last_selected_device_time", System.currentTimeMillis());
        editor.apply();
    }

    static void persistDiscoverableEndTimestamp(Context context, long endTimestamp) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putLong("discoverable_end_timestamp", endTimestamp);
        editor.apply();
    }

    static void persistDiscoveringTimestamp(final Context context) {
        QueuedWork.singleThreadExecutor().submit(new Runnable() {
            public void run() {
                Editor editor = LocalBluetoothPreferences.getSharedPreferences(context).edit();
                editor.putLong("last_discovering_time", System.currentTimeMillis());
                editor.apply();
            }
        });
    }

    static boolean hasDockAutoConnectSetting(Context context, String addr) {
        return getSharedPreferences(context).contains("auto_connect_to_dock" + addr);
    }

    static boolean getDockAutoConnectSetting(Context context, String addr) {
        return getSharedPreferences(context).getBoolean("auto_connect_to_dock" + addr, false);
    }

    static void saveDockAutoConnectSetting(Context context, String addr, boolean autoConnect) {
        Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean("auto_connect_to_dock" + addr, autoConnect);
        editor.apply();
    }

    static void removeDockAutoConnectSetting(Context context, String addr) {
        Editor editor = getSharedPreferences(context).edit();
        editor.remove("auto_connect_to_dock" + addr);
        editor.apply();
    }
}
