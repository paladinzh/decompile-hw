package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.List;

/* compiled from: LogDBOperation */
public class gc {
    private fu a;

    public gc(Context context) {
        try {
            this.a = new fu(context, fu.a(gb.class));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void a(String str, Class<? extends gd> cls) {
        try {
            c(str, cls);
        } catch (Throwable th) {
            fl.a(th, "LogDB", "delLog");
        }
    }

    public void b(String str, Class<? extends gd> cls) {
        try {
            c(str, cls);
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private void c(String str, Class<? extends gd> cls) {
        this.a.a(gd.c(str), (Class) cls);
    }

    public List<? extends gd> a(int i, Class<? extends gd> cls) {
        try {
            return this.a.b(gd.c(i), cls);
        } catch (Throwable th) {
            fl.a(th, "LogDB", "ByState");
            return null;
        }
    }

    public void a(gd gdVar) {
        if (gdVar != null) {
            String c = gd.c(gdVar.b());
            List a = this.a.a(c, gdVar.getClass(), true);
            if (a == null || a.size() == 0) {
                this.a.a((Object) gdVar, true);
            } else {
                Object obj = (gd) a.get(0);
                if (gdVar.a() != 0) {
                    obj.b(0);
                } else {
                    obj.b(obj.c() + 1);
                }
                this.a.a(c, obj, true);
            }
        }
    }

    public void b(gd gdVar) {
        try {
            this.a.a(gd.c(gdVar.b()), (Object) gdVar);
        } catch (Throwable th) {
            fl.a(th, "LogDB", "updateLogInfo");
        }
    }
}
