package com.huawei.hwid.core.a;

import android.content.Context;
import com.huawei.hwid.core.b.a.a;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;

public class c {
    private static boolean a = true;
    private static int b = 0;
    private static volatile int c = 0;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a(b bVar, Context context) {
        synchronized (c.class) {
            e.b("OpLogUtil", "recordOpLog");
            if (!(bVar == null || context == null)) {
                b = bVar.a();
                if (c == 0) {
                    a.a(context).a(bVar);
                    b(context);
                } else if (1 == c) {
                    a.a(context).b(bVar);
                }
            }
        }
    }

    private static synchronized void b(Context context) {
        synchronized (c.class) {
            if (a) {
                if (b.a(context) && !a.a(context).b().isEmpty()) {
                    a bVar = new com.huawei.hwid.core.b.a.a.b(a.a(context).toString());
                    if (b > 0) {
                        if (b <= 999) {
                            bVar.c(b);
                        }
                    }
                    bVar.a(context, bVar, null, null);
                    c = 1;
                }
            }
        }
    }

    public static synchronized void a(Context context) {
        synchronized (c.class) {
            if (!a.a(context).c().isEmpty()) {
                a.a(context).d();
                b(context);
            }
        }
    }

    public static synchronized void a(int i) {
        synchronized (c.class) {
            c = i;
        }
    }
}
