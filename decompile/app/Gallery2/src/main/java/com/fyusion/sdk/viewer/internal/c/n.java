package com.fyusion.sdk.viewer.internal.c;

import com.fyusion.sdk.viewer.internal.f.e;
import com.fyusion.sdk.viewer.internal.request.b;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/* compiled from: Unknown */
public class n {
    private final Set<b> a = Collections.newSetFromMap(new WeakHashMap());
    private final List<b> b = new ArrayList();
    private boolean c;

    public void a(b bVar) {
        this.a.add(bVar);
        if (this.c) {
            this.b.add(bVar);
        } else {
            bVar.a();
        }
    }

    public boolean a() {
        return this.c;
    }

    public void b() {
        this.c = true;
        for (b bVar : e.a(this.a)) {
            if (bVar.d()) {
                bVar.b();
                this.b.add(bVar);
            }
        }
    }

    public boolean b(b bVar) {
        boolean z = false;
        if (bVar != null) {
            if (this.a.remove(bVar) || this.b.remove(bVar)) {
                z = true;
            }
        }
        if (z) {
            bVar.c();
            bVar.g();
        }
        return z;
    }

    public void c() {
        this.c = false;
        for (b bVar : e.a(this.a)) {
            if (!(bVar.e() || bVar.f() || bVar.d())) {
                bVar.a();
            }
        }
        this.b.clear();
    }

    public void d() {
        for (b b : e.a(this.a)) {
            b(b);
        }
        this.b.clear();
    }

    public String toString() {
        return super.toString() + "{numRequests=" + this.a.size() + ", isPaused=" + this.c + "}";
    }
}
