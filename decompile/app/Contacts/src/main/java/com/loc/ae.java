package com.loc;

import android.content.Context;
import android.os.Looper;
import java.util.List;

/* compiled from: ExceptionLogProcessor */
public class ae extends ag {
    private static boolean a = true;

    protected ae(int i) {
        super(i);
    }

    protected String a(List<v> list) {
        return null;
    }

    protected boolean a(Context context) {
        if (q.m(context) != 1 || !a) {
            return false;
        }
        a = false;
        synchronized (Looper.getMainLooper()) {
            as asVar = new as(context);
            au a = asVar.a();
            if (a == null) {
                return true;
            } else if (a.b()) {
                a.b(false);
                asVar.a(a);
                return true;
            } else {
                return false;
            }
        }
    }
}
