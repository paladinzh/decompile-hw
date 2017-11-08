package com.autonavi.amap.mapcore;

import java.util.Hashtable;

/* compiled from: TilesProcessingCtrl */
class d {
    int a = 0;
    long b;
    boolean c = true;
    private Hashtable<String, c> d = new Hashtable();

    public void a(String str) {
        this.d.remove(str);
    }

    public boolean b(String str) {
        return this.d.get(str) != null;
    }

    public void c(String str) {
        this.d.put(str, new c(str, 0));
    }

    public void a() {
        this.d.clear();
    }

    public d() {
        b();
    }

    public void b() {
        this.b = System.currentTimeMillis();
    }
}
