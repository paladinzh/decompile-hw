package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

/* compiled from: ClassLoaderFactory */
public class gl {
    private static final gl a = new gl();
    private final Map<String, gk> b = new HashMap();

    private gl() {
    }

    public static gl a() {
        return a;
    }

    synchronized gk a(Context context, fh fhVar) throws Exception {
        gk gkVar;
        if (a(fhVar) && context != null) {
            String a = fhVar.a();
            gkVar = (gk) this.b.get(a);
            if (gkVar == null) {
                try {
                    gk goVar = new go(context.getApplicationContext(), fhVar, true);
                    try {
                        this.b.put(a, goVar);
                        gp.a(context, fhVar);
                        gkVar = goVar;
                    } catch (Throwable th) {
                        gkVar = goVar;
                    }
                } catch (Throwable th2) {
                }
            }
        } else {
            throw new Exception("sdkInfo or context referance is null");
        }
        return gkVar;
    }

    private boolean a(fh fhVar) {
        if (fhVar == null || TextUtils.isEmpty(fhVar.b()) || TextUtils.isEmpty(fhVar.a())) {
            return false;
        }
        return true;
    }
}
