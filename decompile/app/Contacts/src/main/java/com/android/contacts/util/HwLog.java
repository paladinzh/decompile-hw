package com.android.contacts.util;

import android.util.Log;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;

public final class HwLog {
    public static final boolean HWDBG = getDebugFlags();
    public static final boolean HWFLOW = getFlowFlags();

    public static void v(String TAG, String msg) {
        if (HWDBG) {
            Log.v("Contacts", TAG + HwCustPreloadContacts.EMPTY_STRING + msg);
        }
    }

    public static void d(String TAG, String msg) {
        if (HWDBG) {
            Log.d("Contacts", TAG + HwCustPreloadContacts.EMPTY_STRING + msg);
        }
    }

    public static void i(String TAG, String msg) {
        if (HWFLOW) {
            Log.i("Contacts", TAG + HwCustPreloadContacts.EMPTY_STRING + msg);
        }
    }

    public static void w(String tag, String msg) {
        Log.w("Contacts", tag + HwCustPreloadContacts.EMPTY_STRING + msg);
    }

    public static void w(String tag, String msg, Throwable error) {
        Log.w("Contacts", tag + HwCustPreloadContacts.EMPTY_STRING + msg, error);
    }

    public static void e(String tag, String msg) {
        Log.e("Contacts", tag + HwCustPreloadContacts.EMPTY_STRING + msg);
    }

    public static void e(String tag, String msg, Throwable error) {
        Log.e("Contacts", tag + HwCustPreloadContacts.EMPTY_STRING + msg, error);
    }

    public static void wtf(String tag, String msg) {
        Log.wtf("Contacts", tag + HwCustPreloadContacts.EMPTY_STRING + msg);
    }

    public static void wtf(String tag, String msg, Throwable error) {
        Log.wtf("Contacts", tag + HwCustPreloadContacts.EMPTY_STRING + msg, error);
    }

    static boolean getDebugFlags() {
        boolean z = false;
        try {
            Class<?> log = Class.forName("android.util.Log");
            boolean HWLog = log.getField("HWLog").getBoolean(null);
            boolean HWModuleLog = log.getField("HWModuleLog").getBoolean(null);
            if (HWLog) {
                z = true;
            } else if (HWModuleLog) {
                z = Log.isLoggable("CSP", 3);
            }
            return z;
        } catch (Throwable th) {
            return false;
        }
    }

    static boolean getFlowFlags() {
        boolean z = false;
        try {
            Class<?> log = Class.forName("android.util.Log");
            boolean HWModuleLog = log.getField("HWModuleLog").getBoolean(null);
            if (log.getField("HWINFO").getBoolean(null)) {
                z = true;
            } else if (HWModuleLog) {
                z = Log.isLoggable("CSP", 4);
            }
            return z;
        } catch (Throwable th) {
            return false;
        }
    }
}
