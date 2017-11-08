package com.fyusion.sdk.common.ext.filter.a;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import com.fyusion.sdk.common.ext.filter.FilterControl;
import com.fyusion.sdk.common.ext.filter.PerPixelFilter;

/* compiled from: Unknown */
public abstract class a<CTRL extends FilterControl> implements PerPixelFilter {
    boolean a = true;
    boolean b = true;
    private int c;
    private CTRL d;
    private boolean e;

    public a(int i) {
        this.c = i;
        this.e = true;
    }

    public void a() {
    }

    abstract void a(int i);

    @CallSuper
    public void a(@NonNull CTRL ctrl) {
        this.d = ctrl;
    }

    public void a(boolean z) {
        this.e = z;
    }

    public void a(o[] oVarArr, boolean z, s sVar) {
        if (!(oVarArr[1] == null || oVarArr[1].b == oVarArr[0].b)) {
            oVarArr[1].b();
        }
        oVarArr[1] = new o(oVarArr[0]);
    }

    public void b() {
    }

    public FilterControl c() {
        return this.d;
    }

    public String d() {
        return null;
    }

    public String e() {
        return null;
    }

    abstract String f();

    abstract String g();

    public int getLayer() {
        return this.c;
    }

    public String getName() {
        return this.d == null ? null : this.d.getName();
    }

    public float getValue() {
        return this.d == null ? 0.0f : this.d.getValue();
    }

    abstract void h();

    public boolean isEnabled() {
        return this.e;
    }
}
