package com.amap.api.services.core;

import android.content.Context;
import java.util.List;

/* compiled from: UpdateLogDBOperation */
public class bv {
    private bj a = a(this.b);
    private Context b;

    public bv(Context context) {
        this.b = context;
    }

    private bj a(Context context) {
        try {
            return new bj(context, bp.c());
        } catch (Throwable th) {
            av.a(th, "UpdateLogDB", "getDB");
            th.printStackTrace();
            return null;
        }
    }

    public bx a() {
        try {
            if (this.a == null) {
                this.a = a(this.b);
            }
            List c = this.a.c("1=1", new bw());
            if (c.size() <= 0) {
                return null;
            }
            return (bx) c.get(0);
        } catch (Throwable th) {
            av.a(th, "UpdateLogDB", "getUpdateLog");
            th.printStackTrace();
            return null;
        }
    }

    public void a(bx bxVar) {
        if (bxVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b);
                }
                bk bwVar = new bw();
                bwVar.a((Object) bxVar);
                String str = "1=1";
                List c = this.a.c(str, new bw());
                if (c != null) {
                    if (c.size() != 0) {
                        this.a.b(str, bwVar);
                    }
                }
                this.a.a(bwVar);
            } catch (Throwable th) {
                av.a(th, "UpdateLogDB", "updateLog");
                th.printStackTrace();
            }
        }
    }
}
