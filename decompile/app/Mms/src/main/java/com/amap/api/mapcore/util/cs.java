package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.List;

/* compiled from: LogDBOperation */
public class cs {
    private ck a;

    public cs(Context context) {
        try {
            this.a = new ck(context, ck.a(cr.class));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e2) {
            e2.printStackTrace();
        }
    }

    public void a(String str, Class<? extends ct> cls) {
        try {
            c(str, cls);
        } catch (Throwable th) {
            cb.a(th, "LogDB", "delLog");
        }
    }

    public void b(String str, Class<? extends ct> cls) {
        try {
            c(str, cls);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void c(String str, Class<? extends ct> cls) {
        this.a.a(ct.c(str), (Class) cls);
    }

    public List<? extends ct> a(int i, Class<? extends ct> cls) {
        try {
            return this.a.b(ct.c(i), cls);
        } catch (Throwable th) {
            cb.a(th, "LogDB", "ByState");
            return null;
        }
    }

    public void a(ct ctVar) {
        if (ctVar != null) {
            String c = ct.c(ctVar.b());
            List a = this.a.a(c, ctVar.getClass(), true);
            if (a == null || a.size() == 0) {
                this.a.a((Object) ctVar, true);
            } else {
                Object obj = (ct) a.get(0);
                if (ctVar.a() != 0) {
                    obj.b(0);
                } else {
                    obj.b(obj.c() + 1);
                }
                this.a.a(c, obj);
            }
        }
    }

    public void b(ct ctVar) {
        try {
            this.a.a(ct.c(ctVar.b()), (Object) ctVar);
        } catch (Throwable th) {
            cb.a(th, "LogDB", "updateLogInfo");
        }
    }
}
