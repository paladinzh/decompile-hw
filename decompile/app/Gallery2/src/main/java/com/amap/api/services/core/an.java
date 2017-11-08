package com.amap.api.services.core;

import android.content.Context;
import java.util.List;

/* compiled from: SDKDBOperation */
public class an {
    private ai a = a(this.b);
    private Context b;

    public an(Context context) {
        this.b = context;
    }

    private ai a(Context context) {
        try {
            return new ai(context);
        } catch (Throwable th) {
            ay.a(th, "SDKDB", "getDB");
            th.printStackTrace();
            return null;
        }
    }

    public void a(ad adVar) {
        if (adVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b);
                }
                ap aoVar = new ao();
                aoVar.a(adVar);
                String a = ao.a(adVar.a());
                List c = this.a.c(a, new ao());
                if (c != null) {
                    if (c.size() != 0) {
                        this.a.b(a, aoVar);
                    }
                }
                this.a.a(aoVar);
            } catch (Throwable th) {
                ay.a(th, "SDKDB", "insert");
                th.printStackTrace();
            }
        }
    }

    public List<ad> a() {
        try {
            ap aoVar = new ao();
            return this.a.c(ao.c(), aoVar);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
