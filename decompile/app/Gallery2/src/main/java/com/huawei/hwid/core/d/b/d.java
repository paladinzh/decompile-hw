package com.huawei.hwid.core.d.b;

import android.content.Context;
import android.util.Log;
import java.io.File;

public final class d extends b {
    private static final a b = new a();
    private static d c;

    static class a extends Thread {
        private Context a;

        a(Context context) {
            this.a = context;
        }

        public void run() {
            String str = "";
            try {
                d.b.a(new File(a.a(this.a), "apphwid.txt"));
                c.a(d.b);
            } catch (Throwable e) {
                Log.e("hwid", "ArrayIndexOutOfBoundsException", e);
            } catch (Throwable e2) {
                Log.e("hwid", "Exception", e2);
            }
        }
    }

    public static synchronized d b(Context context) {
        d dVar;
        synchronized (d.class) {
            if (c == null) {
                c = new d(context);
                a = b.a(context);
            }
            dVar = c;
        }
        return dVar;
    }

    private d(Context context) {
        new a(context).start();
    }

    public void a(String str, String str2) {
        a(3, str, str2, null, 2);
        c.a(str, str2);
    }

    public void a(String str, String str2, Throwable th) {
        a(3, str, str2, th, 2);
        c.a(str, str2, th);
    }

    public void b(String str, String str2) {
        a(4, str, str2, null, 2);
        c.b(str, str2);
    }

    public void c(String str, String str2) {
        a(6, str, str2, null, 2);
        c.c(str, str2);
    }

    public void b(String str, String str2, Throwable th) {
        a(6, str, str2, th, 2);
        c.a(str, str2, th);
    }

    private static synchronized void a(int i, String str, String str2, Throwable th, int i2) {
        synchronized (d.class) {
            if (a(i)) {
                if (str2 == null) {
                    str2 = "";
                }
                try {
                    Log.println(i, a + str, str2);
                } catch (Throwable e) {
                    Log.e("hwid", "println IllegalArgumentException", e);
                } catch (Throwable e2) {
                    Log.e("hwid", "println Exception", e2);
                }
            } else {
                return;
            }
        }
    }

    private static boolean a(int i) {
        return Log.isLoggable("hwid", i);
    }
}
