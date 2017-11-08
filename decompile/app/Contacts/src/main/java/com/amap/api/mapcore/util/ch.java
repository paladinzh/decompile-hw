package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.util.List;

/* compiled from: ExceptionLogProcessor */
public class ch extends ci {
    private static boolean a = true;

    protected ch(int i) {
        super(i);
    }

    protected boolean a(Context context) {
        if (bq.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            cv cvVar = new cv(context);
            cw a = cvVar.a();
            if (a == null) {
                return true;
            } else if (a.b()) {
                a.b(false);
                cvVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }

    protected String a(List<bv> list) {
        return null;
    }
}
