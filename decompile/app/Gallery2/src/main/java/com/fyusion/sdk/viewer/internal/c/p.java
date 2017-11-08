package com.fyusion.sdk.viewer.internal.c;

import com.fyusion.sdk.viewer.internal.f.e;
import com.fyusion.sdk.viewer.internal.request.target.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

/* compiled from: Unknown */
public final class p implements i {
    private final Set<Target<?>> a = Collections.newSetFromMap(new WeakHashMap());

    public List<Target<?>> a() {
        return new ArrayList(this.a);
    }

    public void a(Target<?> target) {
        this.a.add(target);
    }

    public void a(Object obj) {
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            if (((Target) it.next()).getWrappedObject() == obj) {
                it.remove();
            }
        }
    }

    public void b() {
        this.a.clear();
    }

    public void b(Target<?> target) {
        this.a.remove(target);
    }

    public void onDestroy() {
        for (Target onDestroy : e.a(this.a)) {
            onDestroy.onDestroy();
        }
    }

    public void onStart() {
        for (Target onStart : e.a(this.a)) {
            onStart.onStart();
        }
    }

    public void onStop() {
        for (Target onStop : e.a(this.a)) {
            onStop.onStop();
        }
    }
}
