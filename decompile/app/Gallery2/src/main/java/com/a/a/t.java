package com.a.a;

import android.os.SystemClock;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* compiled from: Unknown */
public class t {
    public static String a = "Volley";
    public static boolean b = Log.isLoggable(a, 2);

    /* compiled from: Unknown */
    static class a {
        public static final boolean a = t.b;
        private final List<a> b = new ArrayList();
        private boolean c = false;

        /* compiled from: Unknown */
        private static class a {
            public final String a;
            public final long b;
            public final long c;

            public a(String str, long j, long j2) {
                this.a = str;
                this.b = j;
                this.c = j2;
            }
        }

        a() {
        }

        private long a() {
            if (this.b.size() == 0) {
                return 0;
            }
            return ((a) this.b.get(this.b.size() - 1)).c - ((a) this.b.get(0)).c;
        }

        public synchronized void a(String str) {
            Object obj = 1;
            synchronized (this) {
                this.c = true;
                if (a() <= 0) {
                    obj = null;
                }
                if (obj == null) {
                    return;
                }
                long j = ((a) this.b.get(0)).c;
                t.b("(%-4d ms) %s", Long.valueOf(r2), str);
                long j2 = j;
                for (a aVar : this.b) {
                    t.b("(+%-4d) [%2d] %s", Long.valueOf(aVar.c - j2), Long.valueOf(aVar.b), aVar.a);
                    j2 = aVar.c;
                }
            }
        }

        public synchronized void a(String str, long j) {
            if (this.c) {
                throw new IllegalStateException("Marker added to finished log");
            }
            this.b.add(new a(str, j, SystemClock.elapsedRealtime()));
        }

        protected void finalize() throws Throwable {
            if (!this.c) {
                a("Request on the loose");
                t.c("Marker log finalized without finish() - uncaught exit point for request", new Object[0]);
            }
        }
    }

    public static void a(String str, Object... objArr) {
        if (b) {
            Log.v(a, e(str, objArr));
        }
    }

    public static void a(Throwable th, String str, Object... objArr) {
        Log.e(a, e(str, objArr), th);
    }

    public static void b(String str, Object... objArr) {
        Log.d(a, e(str, objArr));
    }

    public static void c(String str, Object... objArr) {
        Log.e(a, e(str, objArr));
    }

    public static void d(String str, Object... objArr) {
        Log.wtf(a, e(str, objArr));
    }

    private static String e(String str, Object... objArr) {
        String str2;
        if (objArr != null) {
            str = String.format(Locale.US, str, objArr);
        }
        StackTraceElement[] stackTrace = new Throwable().fillInStackTrace().getStackTrace();
        String str3 = "<unknown>";
        for (int i = 2; i < stackTrace.length; i++) {
            if (!stackTrace[i].getClass().equals(t.class)) {
                str3 = stackTrace[i].getClassName();
                str3 = str3.substring(str3.lastIndexOf(46) + 1);
                str2 = str3.substring(str3.lastIndexOf(36) + 1) + "." + stackTrace[i].getMethodName();
                break;
            }
        }
        str2 = str3;
        return String.format(Locale.US, "[%d] %s: %s", new Object[]{Long.valueOf(Thread.currentThread().getId()), str2, str});
    }
}
