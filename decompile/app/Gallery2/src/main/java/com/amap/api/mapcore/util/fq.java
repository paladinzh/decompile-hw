package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Looper;
import java.util.Date;
import java.util.List;

/* compiled from: CrashLogProcessor */
public class fq extends fs {
    private static boolean a = true;

    protected fq(int i) {
        super(i);
    }

    protected boolean a(Context context) {
        if (!a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            gf gfVar = new gf(context);
            gg a = gfVar.a();
            if (a == null) {
                return true;
            } else if (a.a()) {
                a.a(false);
                gfVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }

    protected String a(String str) {
        return fe.c(str + fi.a(new Date().getTime()));
    }

    protected String a(List<fh> list) {
        return null;
    }
}
