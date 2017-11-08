package com.android.settings.bluetooth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.android.settings.MLog;
import com.android.settingslib.bluetooth.LocalBluetoothAdapter;

class BluetoothExtUtils {
    BluetoothExtUtils() {
    }

    public static void handleAdapterStateChange(LocalBluetoothAdapter adapter, int state, Context context) {
        if (context == null || adapter == null) {
            MLog.e("BluetoothExtUtils", "in handleAdapterStateChange, params is error!");
        } else {
            new BluetoothPlatformImp().handleAdapterStateChange(adapter, state, context);
        }
    }

    public static void setBeamPushUrisCallback(Activity activity) {
        if (activity == null) {
            MLog.e("BluetoothExtUtils", "in setBeamPushUrisCallback, activity is null!");
        } else {
            new BluetoothPlatformImp().setBeamPushUrisCallback(activity);
        }
    }

    public static int getDiscoverableTimeOut(Intent intent, Context context) {
        if (intent == null || context == null) {
            return 120;
        }
        return new BluetoothPlatformImp().getDiscoverableTimeOut(intent, context);
    }
}
