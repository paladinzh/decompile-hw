package com.amap.api.mapcore.util;

import android.content.Context;
import java.util.List;

/* compiled from: UpdateLogDBOperation */
public class cv {
    private ck a = a(this.b);
    private Context b;

    public cv(Context context) {
        this.b = context;
    }

    private ck a(Context context) {
        try {
            return new ck(context, ck.a(cr.class));
        } catch (Throwable th) {
            cb.a(th, "UpdateLogDB", "getDB");
            return null;
        }
    }

    public cw a() {
        try {
            if (this.a == null) {
                this.a = a(this.b);
            }
            List b = this.a.b("1=1", cw.class);
            if (b.size() <= 0) {
                return null;
            }
            return (cw) b.get(0);
        } catch (Throwable th) {
            cb.a(th, "UpdateLogDB", "getUpdateLog");
            return null;
        }
    }

    public void a(cw cwVar) {
        if (cwVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b);
                }
                String str = "1=1";
                List b = this.a.b(str, cw.class);
                if (b != null) {
                    if (b.size() != 0) {
                        this.a.a(str, (Object) cwVar);
                    }
                }
                this.a.a((Object) cwVar);
            } catch (Throwable th) {
                cb.a(th, "UpdateLogDB", "updateLog");
            }
        }
    }
}
