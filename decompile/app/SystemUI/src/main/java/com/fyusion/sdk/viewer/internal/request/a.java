package com.fyusion.sdk.viewer.internal.request;

import android.graphics.drawable.Drawable;
import com.fyusion.sdk.viewer.internal.b.b.d;

/* compiled from: Unknown */
public abstract class a<CHILD extends a<CHILD>> implements Cloneable {
    private int a;
    private d b = d.c;
    private com.fyusion.sdk.viewer.d c = com.fyusion.sdk.viewer.d.NORMAL;
    private Drawable d;
    private Drawable e;
    private int f = -1;
    private int g = -1;
    private boolean h = false;
    private boolean i;
    private boolean j;

    private static boolean a(int i, int i2) {
        return (i & i2) != 0;
    }

    private CHILD j() {
        if (!this.i) {
            return this;
        }
        throw new IllegalStateException("You cannot modify locked RequestOptions, consider clone()");
    }

    public final CHILD a() {
        try {
            a aVar = (a) super.clone();
            aVar.i = false;
            aVar.j = false;
            return aVar;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public final CHILD a(a<?> aVar) {
        if (this.j) {
            return a().a((a) aVar);
        }
        if (a(aVar.a, 2)) {
            this.h = aVar.h;
        }
        if (a(aVar.a, 4)) {
            this.b = aVar.b;
        }
        if (a(aVar.a, 8)) {
            this.c = aVar.c;
        }
        if (a(aVar.a, 16)) {
            this.d = aVar.d;
        }
        if (a(aVar.a, 64)) {
            this.e = aVar.e;
        }
        return j();
    }

    public final CHILD a(boolean z) {
        if (this.j) {
            return a().a(z);
        }
        this.h = z;
        this.a |= 2;
        return j();
    }

    public final CHILD b() {
        this.i = true;
        return this;
    }

    public final d c() {
        return this.b;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return a();
    }

    public final Drawable d() {
        return this.d;
    }

    public final Drawable e() {
        return this.e;
    }

    public final com.fyusion.sdk.viewer.d f() {
        return this.c;
    }

    public final int g() {
        return this.g;
    }

    public final int h() {
        return this.f;
    }

    public final boolean i() {
        return this.h;
    }
}
