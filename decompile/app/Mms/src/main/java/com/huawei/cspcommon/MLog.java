package com.huawei.cspcommon;

import android.os.SystemProperties;
import android.util.Log;
import com.huawei.cspcommon.ex.ErrorMonitor;

public class MLog {
    protected static final String LINE_END = System.lineSeparator();
    private static int sCallCounter = 1;
    private static Object sCallCounterLock = new Object();
    static boolean sDebugMode = false;
    static boolean sHwInfo = false;
    static boolean sModLog = false;
    static boolean sPerfLog = false;
    static boolean sSysLog = false;

    public static class LogEx {
        public static String getCallerInfo(int level) {
            StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            sb.append("in thread ").append(Thread.currentThread().getId());
            if (stacks.length > level) {
                StackTraceElement stack = stacks[level];
                sb.append(" call from -->").append(stack.getClassName()).append(":").append(stack.getMethodName()).append("(").append(stack.getFileName()).append(":").append(stack.getLineNumber()).append(")");
            } else {
                sb.append("can't get caller info");
            }
            return sb.toString();
        }

        public static String getTraceInfo(Thread thr, int fromLevel, int maxLevel, String msg, Object... format) {
            if (!MLog.sDebugMode) {
                return "";
            }
            if (thr == null) {
                return "getTraceInfo for null thread ? ";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Thread ").append(thr.getName()).append(" Id[").append(thr.getId()).append("]. Msg ");
            sb.append(String.format(msg, format));
            getTraceInfo(thr.getStackTrace(), fromLevel, maxLevel, sb);
            return sb.toString();
        }

        public static void getTraceInfo(StackTraceElement[] stack, int fromLevel, int maxLevel, StringBuilder sb) {
            sb.append(" > Call stack -->").append("  ");
            int stop = stack.length > maxLevel ? maxLevel : stack.length;
            for (int i = fromLevel; i < stop; i++) {
                sb.append(MLog.format("   %02d at", Integer.valueOf(i))).append(stack[i].getClassName()).append(":").append(stack[i].getMethodName()).append("(").append(stack[i].getFileName()).append(":").append(stack[i].getLineNumber()).append(")").append("  ");
            }
        }
    }

    static {
        initLog(true);
    }

    public static void initLog(boolean debug) {
        boolean z = true;
        if (debug) {
            sModLog = true;
            sSysLog = true;
            sPerfLog = true;
            sDebugMode = true;
            sHwInfo = true;
            return;
        }
        boolean z2;
        sDebugMode = SystemProperties.get("config.hw.enable_debug_csp", "false").equals("true");
        if (sDebugMode) {
            z2 = true;
        } else {
            z2 = SystemProperties.get("config.hw.debug_csp_performance", "false").equals("true");
        }
        sPerfLog = z2;
        if (sDebugMode) {
            z2 = true;
        } else {
            z2 = SystemProperties.get("ro.config.hw_log", "false").equals("true");
        }
        sSysLog = z2;
        if (sDebugMode) {
            z2 = true;
        } else {
            z2 = SystemProperties.get("ro.config.hw_module_log", "false").equals("true");
        }
        sModLog = z2;
        if (!SystemProperties.get("ro.debuggable", "false").equals("true")) {
            z = SystemProperties.get("persist.sys.huawei.debug.on", "false").equals("true");
        }
        sHwInfo = z;
    }

    public static final int v(String tag, String msg) {
        return sSysLog ? Log.v(tag, msg) : 0;
    }

    public static final int v(String tag, String format, Object... args) {
        return sSysLog ? Log.v(tag, format(format, args)) : 0;
    }

    public static final int d(String tag, String msg) {
        return sSysLog ? Log.d(tag, msg) : 0;
    }

    public static final int d(String tag, String msg, Throwable tr) {
        return sSysLog ? Log.d(tag, msg, tr) : 0;
    }

    public static final int d(String tag, String format, Object... args) {
        return sSysLog ? Log.d(tag, format(format, args)) : 0;
    }

    public static final int i(String tag, String msg) {
        return sSysLog ? Log.i(tag, msg) : 0;
    }

    public static final int i(String tag, String format, Object... args) {
        return sSysLog ? Log.i(tag, format(format, args)) : 0;
    }

    public static final int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static final int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static final int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static final int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    public static final int e(String tag, String format, Object... args) {
        return Log.e(tag, format(format, args));
    }

    public static final int wtf(String tag, String msg) {
        int ret = Log.wtf(tag, msg, null);
        ErrorMonitor.reportErrorInfo(1, msg, null);
        return ret;
    }

    public static final int wtf(String tag, String msg, Throwable tr) {
        int ret = Log.wtf(tag, msg, tr);
        ErrorMonitor.reportErrorInfo(1, msg, tr);
        return ret;
    }

    public static final boolean isLoggable(String tag, int level) {
        if (sDebugMode) {
            return true;
        }
        return (sSysLog || sModLog) ? Log.isLoggable(tag, level) : false;
    }

    public static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }

    public static final boolean isHwLoggable() {
        return sHwInfo;
    }

    protected static final String format(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (Exception e) {
            return "Exception in format Log >> " + format + "<< !!";
        }
    }

    public static final int logPerformance(String tag, String msg) {
        return sPerfLog ? Log.d(tag, msg + " @" + System.nanoTime()) : 0;
    }

    public static final int logCallMethod(String tag) {
        if (!sPerfLog) {
            return 0;
        }
        int i;
        synchronized (sCallCounterLock) {
            i = sCallCounter;
            sCallCounter = i + 1;
            i = logMethodTracing(tag, i);
        }
        return i;
    }

    public static final int logCallMethod(String tag, int counter) {
        if (sPerfLog) {
            return logMethodTracing(tag, counter);
        }
        return 0;
    }

    private static final int logMethodTracing(String tag, int counter) {
        StackTraceElement method = Thread.currentThread().getStackTrace()[4];
        Log.d(tag, "MTL [" + counter + "] #" + method.getClassName() + '.' + method.getMethodName() + " @" + System.nanoTime());
        return counter;
    }
}
