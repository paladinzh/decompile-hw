package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;

/* compiled from: CrashLogUpDateProcessor */
class az extends be {
    private static boolean a = true;

    protected az(Context context) {
        super(context);
    }

    protected String a() {
        return bd.c;
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
            bv bvVar = new bv(context);
            bx a = bvVar.a();
            if (a == null) {
                return true;
            } else if (a.a()) {
                a.a(false);
                bvVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
