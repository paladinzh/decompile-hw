package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.List;

/* compiled from: SDKDBOperation */
public class cu {
    private ck a;
    private Context b;

    public cu(Context context, boolean z) {
        this.b = context;
        this.a = a(this.b, z);
    }

    private ck a(Context context, boolean z) {
        try {
            return new ck(context, ck.a(cr.class));
        } catch (Throwable th) {
            if (z) {
                th.printStackTrace();
                return null;
            }
            cb.a(th, "SDKDB", "getDB");
            return null;
        }
    }

    public void a(bv bvVar) {
        if (bvVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b, false);
                }
                String a = bv.a(bvVar.a());
                List b = this.a.b(a, bv.class);
                if (b != null) {
                    if (b.size() != 0) {
                        this.a.a(a, (Object) bvVar);
                    }
                }
                this.a.a((Object) bvVar);
            } catch (Throwable th) {
                cb.a(th, "SDKDB", "insert");
                th.printStackTrace();
            }
        }
    }

    public List<bv> a() {
        try {
            return this.a.a(bv.f(), bv.class, true);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
