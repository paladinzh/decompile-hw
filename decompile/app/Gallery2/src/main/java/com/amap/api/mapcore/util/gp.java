package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import java.lang.Thread.UncaughtExceptionHandler;

/* compiled from: DynamicExceptionHandler */
public class gp implements UncaughtExceptionHandler {
    private static gp a;
    private UncaughtExceptionHandler b = Thread.getDefaultUncaughtExceptionHandler();
    private Context c;
    private fh d;

    private gp(Context context, fh fhVar) {
        this.c = context.getApplicationContext();
        this.d = fhVar;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    static synchronized gp a(Context context, fh fhVar) {
        gp gpVar;
        synchronized (gp.class) {
            if (a == null) {
                a = new gp(context, fhVar);
            }
            gpVar = a;
        }
        return gpVar;
    }

    void a(Throwable th) {
        String a = fi.a(th);
        try {
            if (!TextUtils.isEmpty(a)) {
                if (!a.contains("amapdynamic")) {
                    if (!a.contains("admic")) {
                        if (!a.contains("com.autonavi.aps.amapapi.offline")) {
                            gn.a(new fu(this.c, gq.a()), this.c, "OfflineLocation");
                        } else if (!a.contains("com.data.carrier_v4")) {
                            gn.a(new fu(this.c, gq.a()), this.c, "Collection");
                        } else if (a.contains("com.autonavi.aps.amapapi.httpdns") || a.contains("com.autonavi.httpdns")) {
                            gn.a(new fu(this.c, gq.a()), this.c, "HttpDNS");
                        } else {
                            return;
                        }
                    }
                }
                if (a.contains("com.amap.api")) {
                    fu fuVar = new fu(this.c, gq.a());
                    if (a.contains("loc")) {
                        gn.a(fuVar, this.c, "loc");
                    }
                    if (a.contains("navi")) {
                        gn.a(fuVar, this.c, "navi");
                    }
                    if (a.contains("sea")) {
                        gn.a(fuVar, this.c, "sea");
                    }
                    if (a.contains("2dmap")) {
                        gn.a(fuVar, this.c, "2dmap");
                    }
                    if (a.contains("3dmap")) {
                        gn.a(fuVar, this.c, "3dmap");
                        return;
                    }
                    return;
                }
                if (!a.contains("com.autonavi.aps.amapapi.offline")) {
                    gn.a(new fu(this.c, gq.a()), this.c, "OfflineLocation");
                } else if (!a.contains("com.data.carrier_v4")) {
                    gn.a(new fu(this.c, gq.a()), this.c, "Collection");
                } else {
                    if (a.contains("com.autonavi.aps.amapapi.httpdns")) {
                        return;
                    }
                    gn.a(new fu(this.c, gq.a()), this.c, "HttpDNS");
                }
            }
        } catch (Throwable th2) {
            fl.a(th2, "DynamicExceptionHandler", "uncaughtException");
        }
    }

    public void uncaughtException(Thread thread, Throwable th) {
        a(th);
        if (this.b != null) {
            this.b.uncaughtException(thread, th);
        }
    }
}
