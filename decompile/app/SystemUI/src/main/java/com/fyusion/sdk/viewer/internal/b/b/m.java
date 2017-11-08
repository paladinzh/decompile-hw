package com.fyusion.sdk.viewer.internal.b.b;

import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.i;
import com.fyusion.sdk.common.l;
import com.fyusion.sdk.common.p;
import com.fyusion.sdk.viewer.d;
import com.fyusion.sdk.viewer.e;
import com.fyusion.sdk.viewer.internal.b;
import com.fyusion.sdk.viewer.internal.b.c.c;
import com.fyusion.sdk.viewer.internal.b.c.g;
import com.fyusion.sdk.viewer.internal.b.c.g.a;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
final class m {
    private b a;
    private Object b;
    private boolean c;
    private f.b d;
    private d e;
    private d f;
    private volatile i g;
    private List<Integer> h;
    private int i = 0;
    private Map<Integer, Integer> j;
    private boolean k;
    private boolean l;
    private boolean m;
    private boolean n = false;
    private c o;

    m() {
    }

    m a(b bVar, Object obj, boolean z, d dVar, d dVar2, f.b bVar2) {
        this.a = bVar;
        this.b = obj;
        this.c = z;
        this.f = dVar;
        this.d = bVar2;
        this.e = dVar2;
        this.j = new HashMap();
        this.k = false;
        this.l = false;
        this.m = false;
        this.o = new c(bVar2);
        return this;
    }

    a<File> a(int i) {
        return this.o.a(this.g, i, this.c, b());
    }

    Object a() {
        return this.b;
    }

    void a(int i, int i2) {
        if (this.g != null) {
            int sliceFrames = this.g.getMagic().getSliceFrames(i);
            if (sliceFrames != i2) {
                this.j.put(Integer.valueOf(i), Integer.valueOf(i2));
                this.k = true;
                h.c("JobHelper", "Number of decoded frames of slice(" + i + "):" + i2 + " is not as expected: " + sliceFrames);
            }
        }
    }

    void a(i iVar) {
        this.g = iVar;
    }

    void a(boolean z) {
        this.n = z;
    }

    int b(int i) {
        Integer num = (Integer) this.j.get(Integer.valueOf(i));
        if (num == null) {
            num = Integer.valueOf(this.g.getMagic().getSliceFrames(i));
            this.j.put(Integer.valueOf(i), num);
        }
        return num.intValue();
    }

    int b(int i, int i2) {
        return this.g.getMagic().getSliceStartFrame(i) + i2;
    }

    boolean b() {
        return (!l.b() || this.n || this.c) ? false : true;
    }

    void c() {
        this.b = null;
        this.e = null;
        this.f = null;
        this.g = null;
        this.i = 0;
        this.l = false;
        this.m = false;
        this.k = false;
        this.h = null;
        this.f = null;
        this.n = false;
        this.j.clear();
    }

    boolean c(int i) {
        if (this.m) {
            return true;
        }
        List g = g();
        if (!g.isEmpty()) {
            this.m = ((Integer) g.get(g.size() - 1)).equals(Integer.valueOf(i));
        }
        return this.m;
    }

    boolean c(int i, int i2) {
        boolean z = true;
        if (this.l) {
            return true;
        }
        int b = b(i);
        if (b > -1) {
            if (c(i)) {
                if (b != i2 + 1) {
                }
                this.l = z;
            }
            z = false;
            this.l = z;
        }
        return this.l;
    }

    a<com.fyusion.sdk.viewer.internal.b.c.a> d() {
        return ((g) this.a.c().a(this.b).get(0)).a(this.b, this.c);
    }

    a<File> e() {
        return this.o.a(this.g);
    }

    i f() {
        return this.g;
    }

    List<Integer> g() {
        if (this.g != null && this.h == null) {
            this.h = !this.c ? Collections.singletonList(Integer.valueOf(this.g.getMagic().getThumbSlice())) : this.g.sliceTraversalIndex();
        }
        return this.h;
    }

    int h() {
        if (this.k) {
            this.k = false;
            this.i = 0;
        }
        if (this.g != null && this.i == 0) {
            p magic = this.g.getMagic();
            Iterator it = !this.c ? Collections.singletonList(Integer.valueOf(magic.getThumbSlice())).iterator() : this.g.sliceTraversalIndex().iterator();
            while (it.hasNext()) {
                int intValue = ((Integer) it.next()).intValue();
                int b = b(intValue);
                if (b <= -1) {
                    this.i = magic.getSliceFrames(intValue) + this.i;
                } else {
                    this.i += b;
                }
            }
        }
        return this.i;
    }

    d i() {
        return this.e;
    }

    int j() {
        return this.g != null ? this.g.getWidth(this.c) : 0;
    }

    int k() {
        return this.g != null ? this.g.getHeight(this.c) : 0;
    }

    e l() {
        return this.a.c().a();
    }
}
