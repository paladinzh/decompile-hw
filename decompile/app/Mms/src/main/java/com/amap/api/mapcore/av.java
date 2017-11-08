package com.amap.api.mapcore;

import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: MapMessageQueue */
class av {
    AMapDelegateImp a;
    private CopyOnWriteArrayList<p> b = new CopyOnWriteArrayList();
    private CopyOnWriteArrayList<au> c = new CopyOnWriteArrayList();

    public av(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public synchronized void a(au auVar) {
        this.a.f(false);
        this.c.add(auVar);
        this.a.f(false);
    }

    public au a() {
        if (b() == 0) {
            return null;
        }
        au auVar = (au) this.c.get(0);
        this.c.remove(auVar);
        return auVar;
    }

    public synchronized int b() {
        return this.c.size();
    }

    public void a(p pVar) {
        this.a.f(false);
        this.b.add(pVar);
        this.a.f(false);
    }

    public p c() {
        if (d() == 0) {
            return null;
        }
        p pVar = (p) this.b.get(0);
        this.b.remove(pVar);
        this.a.f(false);
        return pVar;
    }

    public int d() {
        return this.b.size();
    }

    public void e() {
        this.b.clear();
    }
}
