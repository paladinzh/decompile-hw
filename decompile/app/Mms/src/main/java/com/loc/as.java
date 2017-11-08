package com.loc;

import android.content.Context;
import java.util.List;

/* compiled from: UpdateLogDBOperation */
public class as {
    private aj a = a(this.b);
    private Context b;

    public as(Context context) {
        this.b = context;
    }

    private aj a(Context context) {
        try {
            return new aj(context, am.c());
        } catch (Throwable th) {
            aa.a(th, "UpdateLogDB", "getDB");
            return null;
        }
    }

    public au a() {
        try {
            if (this.a == null) {
                this.a = a(this.b);
            }
            List c = this.a.c("1=1", new at());
            if (c.size() > 0) {
                return (au) c.get(0);
            }
        } catch (Throwable th) {
            aa.a(th, "UpdateLogDB", "getUpdateLog");
        }
        return null;
    }

    public void a(au auVar) {
        if (auVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b);
                }
                ak atVar = new at();
                atVar.a((Object) auVar);
                String str = "1=1";
                List c = this.a.c(str, new at());
                if (c != null) {
                    if (c.size() != 0) {
                        this.a.b(str, atVar);
                    }
                }
                this.a.a(atVar);
            } catch (Throwable th) {
                aa.a(th, "UpdateLogDB", "updateLog");
            }
        }
    }
}
