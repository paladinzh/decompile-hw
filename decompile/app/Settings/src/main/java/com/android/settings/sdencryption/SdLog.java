package com.android.settings.sdencryption;

import android.util.Log;

public class SdLog {
    public static void d(String tag, String msg) {
        Log.d("Sdencryption", tag + " - " + msg);
    }

    public static void i(String tag, String msg) {
        Log.i("Sdencryption", tag + " - " + msg);
    }

    public static void e(String tag, String msg) {
        Log.e("Sdencryption", tag + " - " + msg);
    }
}
