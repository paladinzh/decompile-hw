package com.loc;

import android.content.Context;
import java.util.List;

/* compiled from: SDKDBOperation */
public class aq {
    private aj a;
    private Context b;

    public aq(Context context, boolean z) {
        this.b = context;
        this.a = a(this.b, z);
    }

    private aj a(Context context, boolean z) {
        try {
            return new aj(context, am.c());
        } catch (Throwable th) {
            if (z) {
                th.printStackTrace();
            } else {
                aa.a(th, "SDKDB", "getDB");
            }
            return null;
        }
    }

    public List<v> a() {
        try {
            ak arVar = new ar();
            return this.a.b(ar.c(), arVar, true);
        } catch (Throwable th) {
            th.printStackTrace();
            return null;
        }
    }

    public void a(v vVar) {
        if (vVar != null) {
            try {
                if (this.a == null) {
                    this.a = a(this.b, false);
                }
                ak arVar = new ar();
                arVar.a((Object) vVar);
                String a = ar.a(vVar.a());
                List c = this.a.c(a, new ar());
                if (c != null) {
                    if (c.size() != 0) {
                        this.a.b(a, arVar);
                    }
                }
                this.a.a(arVar);
            } catch (Throwable th) {
                aa.a(th, "SDKDB", "insert");
                th.printStackTrace();
            }
        }
    }
}
