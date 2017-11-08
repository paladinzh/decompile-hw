package com.amap.api.mapcore;

import android.os.Handler;
import android.os.RemoteException;

/* compiled from: UiSettingsDelegateImp */
class bp implements aq {
    final Handler a = new bq(this);
    private ab b;
    private boolean c = true;
    private boolean d = true;
    private boolean e = true;
    private boolean f = false;
    private boolean g = true;
    private boolean h = true;
    private boolean i = true;
    private boolean j = false;
    private int k = 0;
    private int l = 1;
    private boolean m = true;

    bp(ab abVar) {
        this.b = abVar;
    }

    public boolean a() throws RemoteException {
        return this.m;
    }

    public void a(boolean z) throws RemoteException {
        this.m = z;
        this.a.obtainMessage(4).sendToTarget();
    }

    public void b(boolean z) throws RemoteException {
        this.j = z;
        this.a.obtainMessage(1).sendToTarget();
    }

    public void c(boolean z) throws RemoteException {
        this.h = z;
        this.a.obtainMessage(0).sendToTarget();
    }

    public void d(boolean z) throws RemoteException {
        this.i = z;
        this.a.obtainMessage(2).sendToTarget();
    }

    public void e(boolean z) throws RemoteException {
        this.f = z;
        this.a.obtainMessage(3).sendToTarget();
    }

    public void f(boolean z) throws RemoteException {
        this.d = z;
    }

    public void g(boolean z) throws RemoteException {
        this.g = z;
    }

    public void h(boolean z) throws RemoteException {
        this.e = z;
    }

    public void i(boolean z) throws RemoteException {
        this.c = z;
    }

    public void j(boolean z) throws RemoteException {
        i(z);
        h(z);
        g(z);
        f(z);
    }

    public void a(int i) throws RemoteException {
        this.k = i;
        this.b.d(i);
    }

    public void b(int i) throws RemoteException {
        this.l = i;
        this.b.e(i);
    }

    public boolean b() throws RemoteException {
        return this.j;
    }

    public boolean c() throws RemoteException {
        return this.h;
    }

    public boolean d() throws RemoteException {
        return this.i;
    }

    public boolean e() throws RemoteException {
        return this.f;
    }

    public boolean f() throws RemoteException {
        return this.d;
    }

    public boolean g() throws RemoteException {
        return this.g;
    }

    public boolean h() throws RemoteException {
        return this.e;
    }

    public boolean i() throws RemoteException {
        return this.c;
    }

    public int j() throws RemoteException {
        return this.k;
    }

    public int k() throws RemoteException {
        return this.l;
    }
}
