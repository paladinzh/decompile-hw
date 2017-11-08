package com.amap.api.services.core;

import android.content.Context;
import android.os.Looper;

/* compiled from: ANRLogUpDateProcessor */
class aw extends be {
    private static boolean a = true;

    protected aw(Context context) {
        super(context);
    }

    protected String a() {
        return bd.d;
    }

    protected int b() {
        return 2;
    }

    protected boolean a(Context context) {
        if (an.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            bv bvVar = new bv(context);
            bx a = bvVar.a();
            if (a == null) {
                return true;
            } else if (a.c()) {
                a.c(false);
                bvVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
