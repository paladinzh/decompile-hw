package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;

/* compiled from: ExceptionLogUpDateProcessor */
class bd extends bg {
    private static boolean a = true;

    protected bd(Context context) {
        super(context);
    }

    protected String a() {
        return bf.b;
    }

    protected int b() {
        return 1;
    }

    protected boolean a(Context context) {
        if (z.g(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            aq aqVar = new aq(context);
            as a = aqVar.a();
            if (a == null) {
                return true;
            } else if (a.b()) {
                a.b(false);
                aqVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
