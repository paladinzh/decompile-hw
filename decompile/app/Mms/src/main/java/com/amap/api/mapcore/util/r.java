package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.maps.AMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: TaskManager */
public class r {
    private static r a;
    private dn b;
    private LinkedHashMap<String, dp> c = new LinkedHashMap();
    private boolean d = true;

    public static r a(int i) {
        return a(true, i);
    }

    private static synchronized r a(boolean z, int i) {
        r rVar;
        synchronized (r.class) {
            try {
                if (a != null) {
                    if (z) {
                        if (a.b == null) {
                            a.b = dn.a(i);
                        }
                    }
                    rVar = a;
                } else {
                    a = new r(z, i);
                    rVar = a;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return rVar;
    }

    private r(boolean z, int i) {
        if (z) {
            try {
                this.b = dn.a(i);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    public void a() {
        synchronized (this.c) {
            if (this.c.size() >= 1) {
                for (Entry entry : this.c.entrySet()) {
                    entry.getKey();
                    ((n) entry.getValue()).b();
                }
                this.c.clear();
                return;
            }
        }
    }

    public void a(q qVar) {
        synchronized (this.c) {
            n nVar = (n) this.c.get(qVar.b());
            if (nVar != null) {
                nVar.b();
                return;
            }
        }
    }

    public void a(q qVar, Context context, AMap aMap) throws bk {
        if (this.b != null) {
        }
        if (!this.c.containsKey(qVar.b())) {
            n nVar = new n((ag) qVar, context.getApplicationContext(), aMap);
            synchronized (this.c) {
                this.c.put(qVar.b(), nVar);
            }
        }
        this.b.a((dp) this.c.get(qVar.b()));
    }

    public void b() {
        a();
        dn dnVar = this.b;
        dn.a();
        this.b = null;
        a = null;
    }

    public void b(q qVar) {
        n nVar = (n) this.c.get(qVar.b());
        if (nVar != null) {
            synchronized (this.c) {
                nVar.c();
                this.c.remove(qVar.b());
            }
        }
    }
}
