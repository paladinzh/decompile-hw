package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.List;

/* compiled from: UpdateLogDBOperation */
public class gf {
    private fu a = a(this.b);
    private Context b;

    public gf(Context context) {
        this.b = context;
    }

    private fu a(Context context) {
        try {
            return new fu(context, fu.a(gb.class));
        } catch (Throwable th) {
            fl.a(th, "UpdateLogDB", "getDB");
            return null;
        }
    }

    public gg a() {
        try {
            if (this.a == null) {
                this.a = a(this.b);
            }
            List b = this.a.b("1=1", gg.class);
            if (b.size() <= 0) {
                return null;
            }
            return (gg) b.get(0);
        } catch (Throwable th) {
            fl.a(th, "UpdateLogDB", "getUpdateLog");
            return null;
        }
    }

    public void a(gg ggVar) {
        if (ggVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b);
                }
                String str = "1=1";
                List b = this.a.b(str, gg.class);
                if (b != null) {
                    if (b.size() != 0) {
                        this.a.a(str, (Object) ggVar);
                    }
                }
                this.a.a((Object) ggVar);
            } catch (Throwable th) {
                fl.a(th, "UpdateLogDB", "updateLog");
            }
        }
    }
}
