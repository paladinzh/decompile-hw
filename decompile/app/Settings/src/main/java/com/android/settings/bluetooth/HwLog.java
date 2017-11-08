package com.android.settings.bluetooth;

import android.util.Log;

public class HwLog {
    public static void v(String tag, String msg) {
        Log.v("Bluetooth_settings", tag + ":" + msg);
    }

    public static void d(String tag, String msg) {
        i(tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i("Bluetooth_settings", tag + ":" + msg);
    }

    public static void w(String tag, String msg) {
        Log.w("Bluetooth_settings", tag + ":" + msg);
    }

    public static void e(String tag, String msg) {
        Log.e("Bluetooth_settings", tag + ":" + msg);
    }
}
