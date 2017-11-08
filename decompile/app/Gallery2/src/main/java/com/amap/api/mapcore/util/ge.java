package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.List;

/* compiled from: SDKDBOperation */
public class ge {
    private fu a;
    private Context b;

    public ge(Context context, boolean z) {
        this.b = context;
        this.a = a(this.b, z);
    }

    private fu a(Context context, boolean z) {
        try {
            return new fu(context, fu.a(gb.class));
        } catch (Throwable th) {
            if (z) {
                th.printStackTrace();
                return null;
            }
            fl.a(th, "SDKDB", "getDB");
            return null;
        }
    }

    public void a(fh fhVar) {
        if (fhVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b, false);
                }
                String a = fh.a(fhVar.a());
                List b = this.a.b(a, fh.class);
                if (b != null) {
                    if (b.size() != 0) {
                        this.a.a(a, (Object) fhVar);
                    }
                }
                this.a.a((Object) fhVar);
            } catch (Throwable th) {
                fl.a(th, "SDKDB", "insert");
                th.printStackTrace();
            }
        }
    }

    public List<fh> a() {
        try {
            return this.a.a(fh.g(), fh.class, true);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
