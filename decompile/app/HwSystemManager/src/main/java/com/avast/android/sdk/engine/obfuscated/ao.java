package com.avast.android.sdk.engine.obfuscated;

import com.avast.android.sdk.engine.EngineLoggerInterface;

/* compiled from: Unknown */
public class ao {
    private static EngineLoggerInterface a;

    public static synchronized void a(EngineLoggerInterface engineLoggerInterface) {
        synchronized (ao.class) {
            a = engineLoggerInterface;
        }
    }

    public static synchronized void a(String str) {
        synchronized (ao.class) {
            if (a != null) {
                a.d(str);
            }
        }
    }

    public static synchronized void a(String str, Throwable th) {
        synchronized (ao.class) {
            if (a != null) {
                a.d(str, th);
            }
        }
    }

    public static synchronized void b(String str) {
        synchronized (ao.class) {
            if (a != null) {
                a.i(str);
            }
        }
    }

    public static synchronized void b(String str, Throwable th) {
        synchronized (ao.class) {
            if (a != null) {
                a.i(str, th);
            }
        }
    }

    public static synchronized void c(String str) {
        synchronized (ao.class) {
            if (a != null) {
                a.w(str);
            }
        }
    }

    public static synchronized void c(String str, Throwable th) {
        synchronized (ao.class) {
            if (a != null) {
                a.w(str, th);
            }
        }
    }

    public static synchronized void d(String str) {
        synchronized (ao.class) {
            if (a != null) {
                a.e(str);
            }
        }
    }

    public static synchronized void d(String str, Throwable th) {
        synchronized (ao.class) {
            if (a != null) {
                a.e(str, th);
            }
        }
    }

    public static synchronized void e(String str) {
        synchronized (ao.class) {
            if (a != null) {
                a.a(str);
            }
        }
    }
}
