package com.amap.api.mapcore.util;

/* compiled from: CityStateImp */
public abstract class ca {
    protected int a;
    protected aw b;

    public abstract void c();

    public ca(int i, aw awVar) {
        this.a = i;
        this.b = awVar;
    }

    public int b() {
        return this.a;
    }

    public boolean a(ca caVar) {
        return caVar.b() == b();
    }

    public void b(ca caVar) {
        bu.a(b() + " ==> " + caVar.b() + "   " + getClass() + "==>" + caVar.getClass());
    }

    public void d() {
        bu.a("Wrong call start()  State: " + b() + "  " + getClass());
    }

    public void e() {
        bu.a("Wrong call continueDownload()  State: " + b() + "  " + getClass());
    }

    public void f() {
        bu.a("Wrong call pause()  State: " + b() + "  " + getClass());
    }

    public void a() {
        bu.a("Wrong call delete()  State: " + b() + "  " + getClass());
    }

    public void a(int i) {
        bu.a("Wrong call fail()  State: " + b() + "  " + getClass());
    }

    public void g() {
        bu.a("Wrong call hasNew()  State: " + b() + "  " + getClass());
    }

    public void h() {
        bu.a("Wrong call complete()  State: " + b() + "  " + getClass());
    }
}
