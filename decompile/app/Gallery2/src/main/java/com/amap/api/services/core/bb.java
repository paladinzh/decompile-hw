package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;

/* compiled from: CrashLogUpDateProcessor */
class bb extends bg {
    private static boolean a = true;

    protected bb(Context context) {
        super(context);
    }

    protected String a() {
        return bf.c;
    }

    protected int b() {
        return 0;
    }

    protected boolean a(Context context) {
        if (!a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            aq aqVar = new aq(context);
            as a = aqVar.a();
            if (a == null) {
                return true;
            } else if (a.a()) {
                a.a(false);
                aqVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
