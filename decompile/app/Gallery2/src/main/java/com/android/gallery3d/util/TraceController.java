package com.android.gallery3d.util;

import android.os.SystemProperties;
import android.os.Trace;

public final class TraceController {
    private static final String CLASS_NAME = TraceController.class.getName();
    private static boolean DEBUG_INFO = SystemProperties.getBoolean("gallery.debug.switch", false);
    private static int STATE_ENDED = -2;
    private static int STATE_READY = -1;
    private static int TIME_LIMIT = 5000;
    private static long sState = ((long) STATE_READY);

    private TraceController() {
    }

    public static void traceBegin(String methodName) {
        Trace.traceBegin(8, methodName);
    }

    public static void traceEnd() {
        Trace.traceEnd(8);
    }

    public static void beginSection(String section) {
        printDebugInfo("BEGIN", section);
        Trace.traceBegin(8, section);
    }

    public static void endSection() {
        printDebugInfo("END", "");
        Trace.traceEnd(8);
    }

    public static void printDebugInfo(String msg) {
        printDebugInfo("", msg);
    }

    public static void printDebugInfo(String pre, String msg) {
        if (DEBUG_INFO && sState != ((long) STATE_ENDED)) {
            if (sState == ((long) STATE_READY)) {
                sState = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - sState >= ((long) TIME_LIMIT)) {
                sState = (long) STATE_ENDED;
                return;
            }
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            long tid = Thread.currentThread().getId();
            boolean foundName = false;
            int len = stack.length;
            for (int i = 2; i < len; i++) {
                if (foundName) {
                    if (!CLASS_NAME.equals(stack[i].getClassName())) {
                        GalleryLog.d("Trace", String.format("%5s TID:%5s %s(%s:%d) time: %s  %s", new Object[]{pre, Long.valueOf(tid), stack[i].getMethodName(), stack[i].getFileName(), Integer.valueOf(stack[i].getLineNumber()), Long.valueOf(runTime), msg}));
                        break;
                    }
                } else {
                    foundName = CLASS_NAME.equals(stack[i].getClassName());
                }
            }
        }
    }
}
