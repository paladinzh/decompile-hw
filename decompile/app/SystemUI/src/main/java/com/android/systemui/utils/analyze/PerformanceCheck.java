package com.android.systemui.utils.analyze;

import com.android.systemui.utils.HwLog;
import java.io.PrintWriter;
import java.io.StringWriter;

public class PerformanceCheck {
    public static boolean callingInWorkThread() {
        long tid = Thread.currentThread().getId();
        if (1 != tid) {
            HwLog.d("PerformanceCheck", "callingInThread in work thread tid: " + tid);
            return true;
        }
        HwLog.w("PerformanceCheck", "callingInThread in main thread");
        return false;
    }

    public static void radarIfCallingNotInWorkThread() {
        if (!callingInWorkThread()) {
            RadarLog.logRadar(104, 1, stackTraceToString(new Throwable("radarIfCallingNotInWorkThread")));
        }
    }

    public static void enforceCallingInWorkThread() {
        if (!callingInWorkThread()) {
            throw new RuntimeException("SystemUI Checking Thread Running Exception");
        }
    }

    private static String stackTraceToString(Throwable t) {
        StringWriter error = new StringWriter();
        t.printStackTrace(new PrintWriter(error));
        return error.toString();
    }
}
