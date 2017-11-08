package com.amap.api.mapcore.util;

import com.amap.api.maps.model.animation.Animation;
import java.util.ArrayList;
import java.util.List;

/* compiled from: GLAnimationSet */
public class dj extends di {
    private int a = 0;
    private boolean b;
    private ArrayList<di> c = new ArrayList();
    private dn w = new dn();
    private long x;

    public /* synthetic */ di a() throws CloneNotSupportedException {
        return m();
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return m();
    }

    public dj(boolean z) {
        a(16, z);
        p();
    }

    public dj m() throws CloneNotSupportedException {
        dj djVar = (dj) super.a();
        djVar.w = new dn();
        djVar.c = new ArrayList();
        int size = this.c.size();
        ArrayList arrayList = this.c;
        for (int i = 0; i < size; i++) {
            djVar.c.add(((di) arrayList.get(i)).a());
        }
        return djVar;
    }

    private void a(int i, boolean z) {
        if (z) {
            this.a |= i;
        } else {
            this.a &= i ^ -1;
        }
    }

    private void p() {
        this.k = 0;
    }

    public void a(long j) {
        this.a |= 32;
        super.a(j);
        this.x = this.l + this.m;
    }

    public void a(Animation animation) {
        boolean z = false;
        this.c.add(animation.glAnimation);
        if (((this.a & 64) == 0) && animation.glAnimation.h()) {
            this.a |= 64;
        }
        if ((this.a & 128) == 0) {
            z = true;
        }
        if (z && animation.glAnimation.i()) {
            this.a |= 128;
        }
        if ((this.a & 32) == 32) {
            this.x = this.l + this.m;
        } else if (this.c.size() != 1) {
            this.x = Math.max(this.x, animation.glAnimation.g() + animation.glAnimation.f());
            this.m = this.x - this.l;
        } else {
            this.m = animation.glAnimation.g() + animation.glAnimation.f();
            this.x = this.l + this.m;
        }
        this.b = true;
    }

    public void b(long j) {
        super.b(j);
        int size = this.c.size();
        ArrayList arrayList = this.c;
        for (int i = 0; i < size; i++) {
            ((di) arrayList.get(i)).b(j);
        }
    }

    public long f() {
        int i = 0;
        ArrayList arrayList = this.c;
        int size = arrayList.size();
        long j = 0;
        if (((this.a & 32) != 32 ? 0 : 1) != 0) {
            return this.m;
        }
        while (i < size) {
            j = Math.max(j, ((di) arrayList.get(i)).f());
            i++;
        }
        return j;
    }

    public boolean a(long j, dn dnVar) {
        int size = this.c.size();
        ArrayList arrayList = this.c;
        dn dnVar2 = this.w;
        dnVar.a();
        int i = size - 1;
        boolean z = true;
        boolean z2 = false;
        boolean z3 = false;
        while (i >= 0) {
            boolean z4;
            di diVar = (di) arrayList.get(i);
            dnVar2.a();
            z3 = diVar.a(j, dnVar, e()) || z3;
            z2 = z2 || diVar.k();
            if (diVar.l() && z) {
                z4 = true;
            } else {
                z4 = false;
            }
            i--;
            z = z4;
        }
        if (z2 && !this.e) {
            if (this.r != null) {
                this.r.onAnimationStart();
            }
            this.e = true;
        }
        if (z != this.d) {
            if (this.r != null) {
                this.r.onAnimationEnd();
            }
            this.d = z;
        }
        return z3;
    }

    public List<di> n() {
        return this.c;
    }

    public boolean h() {
        return (this.a & 64) == 64;
    }

    public boolean i() {
        return (this.a & 128) == 128;
    }

    public void o() {
        this.c.clear();
    }
}
