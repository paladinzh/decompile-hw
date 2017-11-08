package com.amap.api.services.core;

import android.content.Context;
import java.util.List;

/* compiled from: UpdateLogDBOperation */
public class aq {
    private ai a = a(this.b);
    private Context b;

    public aq(Context context) {
        this.b = context;
    }

    private ai a(Context context) {
        try {
            return new ai(context);
        } catch (Throwable th) {
            ay.a(th, "UpdateLogDB", "getDB");
            th.printStackTrace();
            return null;
        }
    }

    public as a() {
        try {
            if (this.a == null) {
                this.a = a(this.b);
            }
            List c = this.a.c("1=1", new ar());
            if (c.size() <= 0) {
                return null;
            }
            return (as) c.get(0);
        } catch (Throwable th) {
            ay.a(th, "UpdateLogDB", "getUpdateLog");
            th.printStackTrace();
            return null;
        }
    }

    public void a(as asVar) {
        if (asVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b);
                }
                ap arVar = new ar();
                arVar.a(asVar);
                String str = "1=1";
                List c = this.a.c(str, new ar());
                if (c != null) {
                    if (c.size() != 0) {
                        this.a.b(str, arVar);
                    }
                }
                this.a.a(arVar);
            } catch (Throwable th) {
                ay.a(th, "UpdateLogDB", "updateLog");
                th.printStackTrace();
            }
        }
    }
}
