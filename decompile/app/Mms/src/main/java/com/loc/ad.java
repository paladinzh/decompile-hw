package com.loc;

import android.content.Context;
import android.os.Looper;
import java.util.Date;
import java.util.List;

/* compiled from: CrashLogProcessor */
public class ad extends ag {
    private static boolean a = true;

    protected ad(int i) {
        super(i);
    }

    protected String a(String str) {
        return s.c(str + w.a(new Date().getTime()));
    }

    protected String a(List<v> list) {
        return null;
    }

    protected boolean a(Context context) {
        if (!a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            as asVar = new as(context);
            au a = asVar.a();
            if (a == null) {
                return true;
            } else if (a.a()) {
                a.a(false);
                asVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
