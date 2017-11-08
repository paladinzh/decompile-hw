package com.fyusion.sdk.viewer.internal.b.b.a;

import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.d;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* compiled from: Unknown */
final class c {
    private final Map<e, a> a = new HashMap();
    private final b b = new b();

    /* compiled from: Unknown */
    private static class a {
        final Lock a;
        int b;

        private a() {
            this.a = new ReentrantLock();
        }
    }

    /* compiled from: Unknown */
    private static class b {
        private final Queue<a> a;

        private b() {
            this.a = new ArrayDeque();
        }

        a a() {
            a aVar;
            synchronized (this.a) {
                aVar = (a) this.a.poll();
            }
            return aVar != null ? aVar : new a();
        }

        void a(a aVar) {
            synchronized (this.a) {
                if (this.a.size() < 10) {
                    this.a.offer(aVar);
                }
            }
        }
    }

    c() {
    }

    void a(e eVar) {
        a aVar;
        synchronized (this) {
            aVar = (a) this.a.get(eVar);
            if (aVar == null) {
                aVar = this.b.a();
                this.a.put(eVar, aVar);
            }
            aVar.b++;
        }
        aVar.a.lock();
    }

    void b(e eVar) {
        a aVar;
        synchronized (this) {
            aVar = (a) d.a(this.a.get(eVar));
            if (aVar.b >= 1) {
                aVar.b--;
                if (aVar.b == 0) {
                    a aVar2 = (a) this.a.remove(eVar);
                    if (aVar2.equals(aVar)) {
                        this.b.a(aVar2);
                    } else {
                        throw new IllegalStateException("Removed the wrong lock, expected to remove: " + aVar + ", but actually removed: " + aVar2 + ", key: " + eVar);
                    }
                }
            } else {
                throw new IllegalStateException("Cannot release a lock that is not held, key: " + eVar + ", interestedThreads: " + aVar.b);
            }
        }
        aVar.a.unlock();
    }
}
