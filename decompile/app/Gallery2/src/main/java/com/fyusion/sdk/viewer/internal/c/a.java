package com.fyusion.sdk.viewer.internal.c;

import com.fyusion.sdk.viewer.internal.f.e;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/* compiled from: Unknown */
class a implements h {
    private final Set<i> a = Collections.newSetFromMap(new WeakHashMap());
    private boolean b;
    private boolean c;

    a() {
    }

    void a() {
        this.b = true;
        for (i onStart : e.a(this.a)) {
            onStart.onStart();
        }
    }

    public void a(i iVar) {
        this.a.add(iVar);
        if (this.c) {
            iVar.onDestroy();
        } else if (this.b) {
            iVar.onStart();
        } else {
            iVar.onStop();
        }
    }

    void b() {
        this.b = false;
        for (i onStop : e.a(this.a)) {
            onStop.onStop();
        }
    }

    public void b(i iVar) {
        this.a.remove(iVar);
    }

    void c() {
        this.c = true;
        for (i onDestroy : e.a(this.a)) {
            onDestroy.onDestroy();
        }
    }
}
