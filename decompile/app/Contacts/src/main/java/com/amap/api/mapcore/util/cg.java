package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.util.Date;
import java.util.List;

/* compiled from: CrashLogProcessor */
public class cg extends ci {
    private static boolean a = true;

    protected cg(int i) {
        super(i);
    }

    protected boolean a(Context context) {
        if (!a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            cv cvVar = new cv(context);
            cw a = cvVar.a();
            if (a == null) {
                return true;
            } else if (a.a()) {
                a.a(false);
                cvVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }

    protected String a(String str) {
        return bs.c(str + bx.a(new Date().getTime()));
    }

    protected String a(List<bv> list) {
        return null;
    }
}
