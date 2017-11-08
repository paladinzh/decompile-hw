package com.huawei.hwid.core.a;

import android.content.Context;
import com.huawei.hwid.core.c.b.a;
import com.huawei.hwid.core.model.http.request.u;

/* compiled from: OpLogUtil */
public class d {
    private static boolean a = true;
    private static volatile int b = 0;

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void a(c cVar, Context context) {
        synchronized (d.class) {
            a.b("OpLogUtil", "recordOpLog");
            if (!(cVar == null || context == null)) {
                if (b == 0) {
                    b.a(context).a(cVar);
                    b(context);
                } else if (1 == b) {
                    b.a(context).b(cVar);
                }
            }
        }
    }

    private static synchronized void b(Context context) {
        synchronized (d.class) {
            if (a) {
                if (com.huawei.hwid.core.c.d.a(context) && !b.a(context).b().isEmpty()) {
                    com.huawei.hwid.core.model.http.a uVar = new u(b.a(context).toString());
                    uVar.a(context, uVar, null, null);
                    b = 1;
                }
            }
        }
    }

    public static synchronized void a(Context context) {
        synchronized (d.class) {
            if (!b.a(context).c().isEmpty()) {
                b.a(context).d();
                b(context);
            }
        }
    }

    public static synchronized void a(int i) {
        synchronized (d.class) {
            b = i;
        }
    }
}
