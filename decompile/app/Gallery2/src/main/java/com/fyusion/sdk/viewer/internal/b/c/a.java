package com.fyusion.sdk.viewer.internal.b.c;

import android.support.annotation.Nullable;
import android.util.Log;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.i;
import com.fyusion.sdk.common.m;
import com.fyusion.sdk.common.n;
import com.fyusion.sdk.viewer.internal.b.e;
import com.fyusion.sdk.viewer.internal.f.d;
import java.io.File;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: Unknown */
public class a implements e {
    protected int b;
    protected n c;
    protected h d;
    protected a e;
    protected CopyOnWriteArrayList<b> f;
    protected int g;
    protected int h;
    protected String i;
    protected i j;
    protected boolean k;
    private String l;
    private a m;
    private File n;
    private m o;
    private Map<Integer, com.fyusion.sdk.core.a.h> p;
    private boolean q;
    private c r;

    /* compiled from: Unknown */
    public static class a implements e {
        private final e b;
        private String c;

        public a(e eVar, String str) {
            this.b = eVar;
            this.c = eVar.d() + "_" + str;
        }

        public e a() {
            return this.b;
        }

        public void a(MessageDigest messageDigest) {
            throw new UnsupportedOperationException();
        }

        public String d() {
            return this.c;
        }
    }

    /* compiled from: Unknown */
    public interface b {
        void a();

        void a(a aVar);
    }

    /* compiled from: Unknown */
    public interface c {
        void a(e eVar);
    }

    protected a(h hVar) {
        this.q = false;
        this.g = 0;
        this.h = 0;
        this.j = null;
        this.k = false;
        this.l = hVar.getId();
        this.d = hVar;
        this.c = hVar.getMagic();
        b(this.c.getSliceStartFrame(this.c.getThumbSlice()));
        this.f = new CopyOnWriteArrayList();
        this.p = new ConcurrentHashMap();
    }

    a(h hVar, boolean z, c cVar) {
        this(hVar);
        this.b = hVar.getHeight(z);
        this.r = cVar;
    }

    public static a a(a aVar, boolean z, c cVar) {
        if (aVar.i() != null) {
            aVar.e = null;
        }
        a aVar2 = new a(aVar.n(), z, cVar);
        aVar2.g = aVar.k();
        aVar2.h = aVar.l();
        aVar2.e = aVar;
        return aVar2;
    }

    private void b(int i) {
        this.g = i;
        this.h = i;
        this.q = true;
    }

    public com.fyusion.sdk.core.a.h a(int i) {
        return !this.p.containsKey(Integer.valueOf(i)) ? this.e == null ? null : this.e.a(i) : (com.fyusion.sdk.core.a.h) this.p.get(Integer.valueOf(i));
    }

    public synchronized void a(int i, com.fyusion.sdk.core.a.h hVar) {
        d.a(this.q, "Pivot has not been set");
        this.p.put(Integer.valueOf(i), hVar);
        int i2 = this.g;
        while (true) {
            i2--;
            if (i2 >= 0) {
                if (this.p.get(Integer.valueOf(i2)) == null) {
                    break;
                }
                this.g = i2;
            } else {
                break;
            }
        }
        i2 = 1000;
        if (this.c != null) {
            if (this.c.getEndFrame() > 0) {
                i2 = this.c.getNumProcessedFrames();
            }
        }
        int i3 = i2;
        i2 = this.h;
        while (true) {
            i2++;
            if (i2 < i3 && this.p.get(Integer.valueOf(i2)) != null) {
                this.h = i2;
            }
        }
        if (this.j != null) {
            i2 = 75;
            if (this.b > this.d.getPreviewHeight()) {
                i2 = this.c.getNumProcessedFrames();
            }
            if (this.j.c() >= i2) {
                this.j.e();
            }
        }
        if (this.h - this.g == 3) {
            Iterator it = this.f.iterator();
            while (it.hasNext()) {
                ((b) it.next()).a();
            }
        }
    }

    public void a(h hVar) {
    }

    public void a(m mVar) {
        this.o = mVar;
    }

    public void a(b bVar) {
        if (bVar != null) {
            if (!this.f.contains(bVar)) {
                this.f.add(bVar);
            }
            if (this.h - this.g >= 3) {
                bVar.a();
            }
        }
    }

    public synchronized void a(File file) {
        this.i = file.getAbsolutePath();
        this.j = new i(file);
        if (!this.j.a(com.fyusion.sdk.common.i.a.READ_WRITE, com.fyusion.sdk.common.i.b.TRUNCATE)) {
            Log.e("FyuseData", "Error creating IO for " + file.getAbsolutePath());
            this.j = null;
        }
    }

    public void a(MessageDigest messageDigest) {
        throw new UnsupportedOperationException();
    }

    public synchronized i b(@Nullable File file) {
        if (this.j == null) {
            if (file == null) {
                return null;
            }
            a(file);
        }
        return this.j;
    }

    public void b(b bVar) {
        this.f.remove(bVar);
    }

    public void c(File file) {
        this.n = file;
    }

    public String d() {
        return this.l;
    }

    public boolean e() {
        return this.k;
    }

    public m f() {
        return this.o;
    }

    public File g() {
        return this.n;
    }

    public void h() {
        Log.d("FyuseData", "clear data: " + d() + "_" + this.b);
        this.p.clear();
        this.f.clear();
        b(this.c.getSliceStartFrame(this.c.getThumbSlice()));
        if (this.j != null) {
            this.j.i();
            this.j = null;
        }
        if (this.r != null) {
            this.r.a(o());
        }
    }

    public a i() {
        return this.e;
    }

    public int j() {
        return this.b;
    }

    public int k() {
        return this.g;
    }

    public int l() {
        return this.h;
    }

    public n m() {
        return this.c;
    }

    public h n() {
        return this.d;
    }

    public e o() {
        if (this.m == null) {
            this.m = new a(this, String.valueOf(this.b));
        }
        return this.m;
    }
}
