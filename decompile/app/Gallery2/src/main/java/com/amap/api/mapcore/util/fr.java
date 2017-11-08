package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.util.List;

/* compiled from: ExceptionLogProcessor */
public class fr extends fs {
    private static boolean a = true;

    protected fr(int i) {
        super(i);
    }

    protected boolean a(Context context) {
        if (fc.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            gf gfVar = new gf(context);
            gg a = gfVar.a();
            if (a == null) {
                return true;
            } else if (a.b()) {
                a.b(false);
                gfVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }

    protected String a(List<fh> list) {
        return null;
    }
}
