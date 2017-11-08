package com.loc;

import android.content.Context;
import android.text.TextUtils;
import java.lang.Thread.UncaughtExceptionHandler;

/* compiled from: DynamicExceptionHandler */
public class bc implements UncaughtExceptionHandler {
    private static bc a;
    private UncaughtExceptionHandler b = Thread.getDefaultUncaughtExceptionHandler();
    private Context c;
    private v d;

    private bc(Context context, v vVar) {
        this.c = context.getApplicationContext();
        this.d = vVar;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    static synchronized bc a(Context context, v vVar) {
        bc bcVar;
        synchronized (bc.class) {
            if (a == null) {
                a = new bc(context, vVar);
            }
            bcVar = a;
        }
        return bcVar;
    }

    public void uncaughtException(Thread thread, Throwable th) {
        String a = w.a(th);
        try {
            if (!TextUtils.isEmpty(a)) {
                if (a.contains("amapdynamic") && a.contains("com.amap.api")) {
                    ba.a(new aj(this.c, bd.c()), this.c, this.d);
                }
            }
        } catch (Throwable th2) {
            aa.a(th2, "DynamicExceptionHandler", "uncaughtException");
        }
        if (this.b != null) {
            this.b.uncaughtException(thread, th);
        }
    }
}
