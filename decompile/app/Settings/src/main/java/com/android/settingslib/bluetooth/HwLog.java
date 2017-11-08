package com.android.settingslib.bluetooth;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

public class HwLog {
    public static void v(String tag, String msg) {
        Log.v("Bluetooth_stlib", tag + ":" + msg);
    }

    public static void d(String tag, String msg) {
        i(tag, msg);
    }

    public static void i(String tag, String msg) {
        Log.i("Bluetooth_stlib", tag + ":" + msg);
    }

    public static void w(String tag, String msg) {
        Log.w("Bluetooth_stlib", tag + ":" + msg);
    }

    public static void w(String tag, String msg, Throwable tr) {
        Log.w("Bluetooth_stlib", tag + ":" + msg + '\n' + getStackTraceString(tr));
    }

    public static void e(String tag, String msg) {
        Log.e("Bluetooth_stlib", tag + ":" + msg);
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        for (Throwable t = tr; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException) {
                return "";
            }
        }
        StringWriter sw = new StringWriter();
        tr.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
