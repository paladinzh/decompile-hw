package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.maps.AMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/* compiled from: TaskManager */
public class bg {
    private static bg a;
    private hl b;
    private LinkedHashMap<String, hm> c = new LinkedHashMap();
    private boolean d = true;

    public static bg a(int i) {
        return a(true, i);
    }

    private static synchronized bg a(boolean z, int i) {
        bg bgVar;
        synchronized (bg.class) {
            try {
                if (a != null) {
                    if (z) {
                        if (a.b == null) {
                            a.b = hl.a(i);
                        }
                    }
                    bgVar = a;
                } else {
                    a = new bg(z, i);
                    bgVar = a;
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return bgVar;
    }

    private bg(boolean z, int i) {
        if (z) {
            try {
                this.b = hl.a(i);
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
                    ((bc) entry.getValue()).b();
                }
                this.c.clear();
                return;
            }
        }
    }

    public void a(bf bfVar) {
        synchronized (this.c) {
            bc bcVar = (bc) this.c.get(bfVar.b());
            if (bcVar != null) {
                bcVar.b();
                return;
            }
        }
    }

    public void a(bf bfVar, Context context, AMap aMap) throws ex {
        if (this.b != null) {
        }
        if (!this.c.containsKey(bfVar.b())) {
            bc bcVar = new bc((bv) bfVar, context.getApplicationContext(), aMap);
            synchronized (this.c) {
                this.c.put(bfVar.b(), bcVar);
            }
        }
        this.b.a((hm) this.c.get(bfVar.b()));
    }

    public void b() {
        a();
        hl hlVar = this.b;
        hl.a();
        this.b = null;
        a = null;
    }

    public void b(bf bfVar) {
        bc bcVar = (bc) this.c.get(bfVar.b());
        if (bcVar != null) {
            synchronized (this.c) {
                bcVar.c();
                this.c.remove(bfVar.b());
            }
        }
    }
}
