package com.amap.api.services.core;

import android.content.Context;
import java.util.List;

/* compiled from: SDKDBOperation */
public class bt {
    private bj a;
    private Context b;

    public bt(Context context, boolean z) {
        this.b = context;
        this.a = a(this.b, z);
    }

    private bj a(Context context, boolean z) {
        try {
            return new bj(context, bp.c());
        } catch (Throwable th) {
            if (z) {
                th.printStackTrace();
                return null;
            }
            av.a(th, "SDKDB", "getDB");
            return null;
        }
    }

    public void a(ar arVar) {
        if (arVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b, false);
                }
                bk buVar = new bu();
                buVar.a((Object) arVar);
                String a = bu.a(arVar.a());
                List c = this.a.c(a, new bu());
                if (c != null) {
                    if (c.size() != 0) {
                        this.a.b(a, buVar);
                    }
                }
                this.a.a(buVar);
            } catch (Throwable th) {
                av.a(th, "SDKDB", "insert");
                th.printStackTrace();
            }
        }
    }

    public List<ar> a() {
        try {
            bk buVar = new bu();
            return this.a.b(bu.c(), buVar, true);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }
}
