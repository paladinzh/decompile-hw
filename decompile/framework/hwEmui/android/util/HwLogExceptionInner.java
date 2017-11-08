package android.util;

public class HwLogExceptionInner implements LogException {
    public static final int LEVEL_A = 65;
    public static final int LEVEL_B = 66;
    public static final int LEVEL_C = 67;
    public static final int LEVEL_D = 68;
    private static final int LOG_ID_EXCEPTION = 5;
    private static LogException mLogExceptionInner = null;

    public static native int println_exception_native(String str, int i, String str2, String str3);

    static {
        System.loadLibrary("hwlog_jni");
    }

    public static synchronized LogException getInstance() {
        LogException logException;
        synchronized (HwLogExceptionInner.class) {
            if (mLogExceptionInner == null) {
                mLogExceptionInner = new HwLogExceptionInner();
            }
            logException = mLogExceptionInner;
        }
        return logException;
    }

    public int cmd(String tag, String contain) {
        return println_exception_native(tag, 0, "command", contain);
    }

    public int msg(String category, int level, String header, String body) {
        return println_exception_native(category, level, "message", header + '\n' + body);
    }

    public int msg(String category, int level, int mask, String header, String body) {
        return println_exception_native(category, level, "message", "mask=" + mask + ";" + header + '\n' + body);
    }
}
