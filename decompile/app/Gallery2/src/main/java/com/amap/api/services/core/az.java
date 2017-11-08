package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;

/* compiled from: ANRLogUpDateProcessor */
class az extends bg {
    private static boolean a = true;

    protected az(Context context) {
        super(context);
    }

    protected String a() {
        return bf.d;
    }

    protected int b() {
        return 2;
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
            } else if (a.c()) {
                a.c(false);
                aqVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
