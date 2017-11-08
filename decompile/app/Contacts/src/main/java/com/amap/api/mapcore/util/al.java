package com.amap.api.mapcore.util;

/* compiled from: CityStateImp */
public abstract class al {
    protected int a;
    protected g b;

    public abstract void c();

    public al(int i, g gVar) {
        this.a = i;
        this.b = gVar;
    }

    public int b() {
        return this.a;
    }

    public boolean a(al alVar) {
        return alVar.b() == b();
    }

    public void b(al alVar) {
        af.a(b() + " ==> " + alVar.b() + "   " + getClass() + "==>" + alVar.getClass());
    }

    public void d() {
        af.a("Wrong call start()  State: " + b() + "  " + getClass());
    }

    public void e() {
        af.a("Wrong call continueDownload()  State: " + b() + "  " + getClass());
    }

    public void f() {
        af.a("Wrong call pause()  State: " + b() + "  " + getClass());
    }

    public void a() {
        af.a("Wrong call delete()  State: " + b() + "  " + getClass());
    }

    public void g() {
        af.a("Wrong call fail()  State: " + b() + "  " + getClass());
    }

    public void h() {
        af.a("Wrong call hasNew()  State: " + b() + "  " + getClass());
    }

    public void i() {
        af.a("Wrong call complete()  State: " + b() + "  " + getClass());
    }
}
