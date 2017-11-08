package com.amap.api.mapcore;

import android.graphics.Bitmap;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.mapcore.util.av;
import com.amap.api.mapcore.util.av.d;
import com.amap.api.mapcore.util.bb;
import com.amap.api.mapcore.util.bd;
import com.amap.api.mapcore.util.bg;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: TileOverlayDelegateImp */
public class bn implements ap {
    private static int g = 0;
    private bo a;
    private TileProvider b;
    private Float c;
    private boolean d;
    private boolean e;
    private ab f;
    private int h;
    private int i;
    private int j;
    private bb k;
    private CopyOnWriteArrayList<a> l;
    private boolean m;
    private b n;
    private String o;
    private FloatBuffer p;

    /* compiled from: TileOverlayDelegateImp */
    public class a implements Cloneable {
        public int a;
        public int b;
        public int c;
        public int d;
        public IPoint e;
        public int f = 0;
        public boolean g = false;
        public FloatBuffer h = null;
        public Bitmap i = null;
        public com.amap.api.mapcore.util.bd.a j = null;
        public int k = 0;
        final /* synthetic */ bn l;

        public /* synthetic */ Object clone() throws CloneNotSupportedException {
            return a();
        }

        public a(bn bnVar, int i, int i2, int i3, int i4) {
            this.l = bnVar;
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
        }

        public a(bn bnVar, a aVar) {
            this.l = bnVar;
            this.a = aVar.a;
            this.b = aVar.b;
            this.c = aVar.c;
            this.d = aVar.d;
            this.e = aVar.e;
            this.h = aVar.h;
        }

        public a a() {
            try {
                a aVar = (a) super.clone();
                aVar.a = this.a;
                aVar.b = this.b;
                aVar.c = this.c;
                aVar.d = this.d;
                aVar.e = (IPoint) this.e.clone();
                aVar.h = this.h.asReadOnlyBuffer();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return new a(this.l, this);
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof a)) {
                return false;
            }
            a aVar = (a) obj;
            if (this.a == aVar.a && this.b == aVar.b && this.c == aVar.c) {
                if (this.d != aVar.d) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int hashCode() {
            return (((this.a * 7) + (this.b * 11)) + (this.c * 13)) + this.d;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.a);
            stringBuilder.append("-");
            stringBuilder.append(this.b);
            stringBuilder.append("-");
            stringBuilder.append(this.c);
            stringBuilder.append("-");
            stringBuilder.append(this.d);
            return stringBuilder.toString();
        }

        public void a(Bitmap bitmap) {
            if (bitmap != null && !bitmap.isRecycled()) {
                try {
                    this.j = null;
                    this.i = bj.a(bitmap, bj.a(bitmap.getWidth()), bj.a(bitmap.getHeight()));
                    this.l.f.f(false);
                } catch (Throwable th) {
                    ce.a(th, "TileOverlayDelegateImp", "setBitmap");
                    th.printStackTrace();
                    if (this.k < 3) {
                        this.l.k.a(true, this);
                        this.k++;
                    }
                }
            } else if (this.k < 3) {
                this.l.k.a(true, this);
                this.k++;
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        public void b() {
            bd.a(this);
            if (this.g) {
                this.l.a.c.add(Integer.valueOf(this.f));
            }
            this.g = false;
            this.f = 0;
            if (!(this.i == null || this.i.isRecycled())) {
                this.i.recycle();
            }
            this.i = null;
            if (this.h != null) {
                this.h.clear();
            }
            this.h = null;
            this.j = null;
        }
    }

    /* compiled from: TileOverlayDelegateImp */
    private class b extends av<ab, Void, List<a>> {
        final /* synthetic */ bn a;
        private int e;
        private boolean f;

        public b(bn bnVar, boolean z) {
            this.a = bnVar;
            this.f = z;
        }

        protected List<a> a(ab... abVarArr) {
            int m;
            int i = 0;
            try {
                int l = abVarArr[0].l();
                m = abVarArr[0].m();
                this.e = (int) abVarArr[0].F();
                i = l;
            } catch (Throwable th) {
                m = 0;
            }
            if (i > 0 && m > 0) {
                return this.a.a(this.e, i, m);
            }
            return null;
        }

        protected void a(List<a> list) {
            if (list != null && list.size() > 0) {
                this.a.a((List) list, this.e, this.f);
                list.clear();
            }
        }
    }

    private static String a(String str) {
        g++;
        return str + g;
    }

    public bn(TileOverlayOptions tileOverlayOptions, bo boVar) {
        this.e = false;
        this.h = 256;
        this.i = 256;
        this.j = -1;
        this.l = new CopyOnWriteArrayList();
        this.m = false;
        this.n = null;
        this.o = null;
        this.p = null;
        this.a = boVar;
        this.b = tileOverlayOptions.getTileProvider();
        this.h = this.b.getTileWidth();
        this.i = this.b.getTileHeight();
        int a = bj.a(this.h);
        float f = ((float) this.h) / ((float) a);
        float a2 = ((float) this.i) / ((float) bj.a(this.i));
        this.p = bj.a(new float[]{0.0f, a2, f, a2, f, 0.0f, 0.0f, 0.0f});
        this.c = Float.valueOf(tileOverlayOptions.getZIndex());
        this.d = tileOverlayOptions.isVisible();
        this.o = c();
        this.f = this.a.a();
        this.j = Integer.valueOf(this.o.substring("TileOverlay".length())).intValue();
        com.amap.api.mapcore.util.ba.a aVar = new com.amap.api.mapcore.util.ba.a(this.a.getContext(), this.o);
        aVar.a(tileOverlayOptions.getMemoryCacheEnabled());
        aVar.b(tileOverlayOptions.getDiskCacheEnabled());
        aVar.a(tileOverlayOptions.getMemCacheSize());
        aVar.b(tileOverlayOptions.getDiskCacheSize());
        String diskCacheDir = tileOverlayOptions.getDiskCacheDir();
        if (!(diskCacheDir == null || diskCacheDir.equals(""))) {
            aVar.a(diskCacheDir);
        }
        this.k = new bb(this.a.getContext(), this.h, this.i);
        this.k.a(this.b);
        this.k.a(aVar);
        b(true);
    }

    public bn(TileOverlayOptions tileOverlayOptions, bo boVar, boolean z) {
        this(tileOverlayOptions, boVar);
        this.e = z;
    }

    public void a() {
        if (this.n != null && this.n.a() == d.RUNNING) {
            this.n.a(true);
        }
        Iterator it = this.l.iterator();
        while (it.hasNext()) {
            ((a) it.next()).b();
        }
        this.l.clear();
        this.k.g();
        this.a.b((ap) this);
        this.f.f(false);
    }

    public void b() {
        this.k.f();
    }

    public String c() {
        if (this.o == null) {
            this.o = a("TileOverlay");
        }
        return this.o;
    }

    public void a(float f) {
        this.c = Float.valueOf(f);
        this.a.c();
    }

    public float d() {
        return this.c.floatValue();
    }

    public void a(boolean z) {
        this.d = z;
        this.f.f(false);
        if (z) {
            b(true);
        }
    }

    public boolean e() {
        return this.d;
    }

    public boolean a(ap apVar) {
        if (equals(apVar) || apVar.c().equals(c())) {
            return true;
        }
        return false;
    }

    public int f() {
        return super.hashCode();
    }

    private boolean a(a aVar) {
        MapProjection c = this.f.c();
        float f = (float) aVar.c;
        int i = this.h;
        int i2 = this.i;
        int i3 = aVar.e.x;
        int i4 = aVar.e.y + ((1 << (20 - ((int) f))) * i2);
        r6 = new float[12];
        FPoint fPoint = new FPoint();
        c.geo2Map(i3, i4, fPoint);
        FPoint fPoint2 = new FPoint();
        c.geo2Map(((1 << (20 - ((int) f))) * i) + i3, i4, fPoint2);
        FPoint fPoint3 = new FPoint();
        c.geo2Map((i * (1 << (20 - ((int) f)))) + i3, i4 - ((1 << (20 - ((int) f))) * i2), fPoint3);
        FPoint fPoint4 = new FPoint();
        c.geo2Map(i3, i4 - ((1 << (20 - ((int) f))) * i2), fPoint4);
        r6[0] = fPoint.x;
        r6[1] = fPoint.y;
        r6[2] = 0.0f;
        r6[3] = fPoint2.x;
        r6[4] = fPoint2.y;
        r6[5] = 0.0f;
        r6[6] = fPoint3.x;
        r6[7] = fPoint3.y;
        r6[8] = 0.0f;
        r6[9] = fPoint4.x;
        r6[10] = fPoint4.y;
        r6[11] = 0.0f;
        if (aVar.h != null) {
            aVar.h = bj.a(r6, aVar.h);
        } else {
            aVar.h = bj.a(r6);
        }
        return true;
    }

    private void a(GL10 gl10, int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (floatBuffer != null && floatBuffer2 != null) {
            gl10.glEnable(3042);
            gl10.glTexEnvf(8960, 8704, 8448.0f);
            gl10.glBlendFunc(770, 771);
            gl10.glColor4f(ContentUtil.FONT_SIZE_NORMAL, ContentUtil.FONT_SIZE_NORMAL, ContentUtil.FONT_SIZE_NORMAL, ContentUtil.FONT_SIZE_NORMAL);
            gl10.glEnable(3553);
            gl10.glEnableClientState(32884);
            gl10.glEnableClientState(32888);
            gl10.glBindTexture(3553, i);
            gl10.glVertexPointer(3, 5126, 0, floatBuffer);
            gl10.glTexCoordPointer(2, 5126, 0, floatBuffer2);
            gl10.glDrawArrays(6, 0, 4);
            gl10.glDisableClientState(32884);
            gl10.glDisableClientState(32888);
            gl10.glDisable(3553);
            gl10.glDisable(3042);
        }
    }

    public void a(GL10 gl10) {
        if (this.l != null && this.l.size() != 0) {
            Iterator it = this.l.iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                if (!aVar.g) {
                    try {
                        IPoint iPoint = aVar.e;
                        if (!(aVar.i == null || aVar.i.isRecycled() || iPoint == null)) {
                            aVar.f = bj.a(gl10, aVar.i);
                            if (aVar.f != 0) {
                                aVar.g = true;
                            }
                            aVar.i = null;
                        }
                    } catch (Throwable th) {
                        ce.a(th, "TileOverlayDelegateImp", "drawTiles");
                    }
                }
                if (aVar.g) {
                    a(aVar);
                    a(gl10, aVar.f, aVar.h, this.p);
                }
            }
        }
    }

    private ArrayList<a> a(int i, int i2, int i3) {
        MapProjection c = this.f.c();
        FPoint fPoint = new FPoint();
        IPoint iPoint = new IPoint();
        IPoint iPoint2 = new IPoint();
        c.win2Map(0, 0, fPoint);
        c.map2Geo(fPoint.x, fPoint.y, iPoint);
        int min = Math.min(Integer.MAX_VALUE, iPoint.x);
        int max = Math.max(0, iPoint.x);
        int min2 = Math.min(Integer.MAX_VALUE, iPoint.y);
        int max2 = Math.max(0, iPoint.y);
        c.win2Map(i2, 0, fPoint);
        c.map2Geo(fPoint.x, fPoint.y, iPoint);
        min = Math.min(min, iPoint.x);
        max = Math.max(max, iPoint.x);
        min2 = Math.min(min2, iPoint.y);
        max2 = Math.max(max2, iPoint.y);
        c.win2Map(0, i3, fPoint);
        c.map2Geo(fPoint.x, fPoint.y, iPoint);
        min = Math.min(min, iPoint.x);
        max = Math.max(max, iPoint.x);
        min2 = Math.min(min2, iPoint.y);
        max2 = Math.max(max2, iPoint.y);
        c.win2Map(i2, i3, fPoint);
        c.map2Geo(fPoint.x, fPoint.y, iPoint);
        int min3 = Math.min(min, iPoint.x);
        int max3 = Math.max(max, iPoint.x);
        min = Math.min(min2, iPoint.y);
        int max4 = Math.max(max2, iPoint.y);
        int i4 = min3 - ((1 << (20 - i)) * this.h);
        int i5 = min - ((1 << (20 - i)) * this.i);
        c.getGeoCenter(iPoint2);
        int i6 = (iPoint2.x >> (20 - i)) / this.h;
        int i7 = (iPoint2.y >> (20 - i)) / this.i;
        min2 = (i6 << (20 - i)) * this.h;
        max2 = (i7 << (20 - i)) * this.i;
        a aVar = new a(this, i6, i7, i, this.j);
        aVar.e = new IPoint(min2, max2);
        ArrayList<a> arrayList = new ArrayList();
        arrayList.add(aVar);
        int i8 = 1;
        while (true) {
            Object obj = null;
            for (min2 = i6 - i8; min2 <= i6 + i8; min2++) {
                max2 = i7 + i8;
                IPoint iPoint3 = new IPoint((min2 << (20 - i)) * this.h, (max2 << (20 - i)) * this.i);
                if (iPoint3.x < max3 && iPoint3.x > i4 && iPoint3.y < max4 && iPoint3.y > i5) {
                    if (obj == null) {
                        obj = 1;
                    }
                    a aVar2 = new a(this, min2, max2, i, this.j);
                    aVar2.e = iPoint3;
                    arrayList.add(aVar2);
                }
                max2 = i7 - i8;
                iPoint3 = new IPoint((min2 << (20 - i)) * this.h, (max2 << (20 - i)) * this.i);
                if (iPoint3.x < max3 && iPoint3.x > i4 && iPoint3.y < max4 && iPoint3.y > i5) {
                    if (obj == null) {
                        obj = 1;
                    }
                    aVar2 = new a(this, min2, max2, i, this.j);
                    aVar2.e = iPoint3;
                    arrayList.add(aVar2);
                }
            }
            for (max2 = (i7 + i8) - 1; max2 > i7 - i8; max2--) {
                min2 = i6 + i8;
                iPoint3 = new IPoint((min2 << (20 - i)) * this.h, (max2 << (20 - i)) * this.i);
                if (iPoint3.x < max3 && iPoint3.x > i4 && iPoint3.y < max4 && iPoint3.y > i5) {
                    if (obj == null) {
                        obj = 1;
                    }
                    aVar2 = new a(this, min2, max2, i, this.j);
                    aVar2.e = iPoint3;
                    arrayList.add(aVar2);
                }
                min2 = i6 - i8;
                iPoint3 = new IPoint((min2 << (20 - i)) * this.h, (max2 << (20 - i)) * this.i);
                if (iPoint3.x < max3 && iPoint3.x > i4 && iPoint3.y < max4 && iPoint3.y > i5) {
                    if (obj == null) {
                        obj = 1;
                    }
                    aVar2 = new a(this, min2, max2, i, this.j);
                    aVar2.e = iPoint3;
                    arrayList.add(aVar2);
                }
            }
            if (obj == null) {
                return arrayList;
            }
            i8++;
        }
    }

    private boolean a(List<a> list, int i, boolean z) {
        if (list == null || this.l == null) {
            return false;
        }
        Iterator it = this.l.iterator();
        while (it.hasNext()) {
            boolean z2;
            a aVar = (a) it.next();
            for (a aVar2 : list) {
                if (aVar.equals(aVar2) && aVar.g) {
                    aVar2.g = aVar.g;
                    aVar2.f = aVar.f;
                    z2 = true;
                    break;
                }
            }
            z2 = false;
            if (!z2) {
                aVar.b();
            }
        }
        this.l.clear();
        if (i > ((int) this.f.s()) || i < ((int) this.f.t())) {
            return false;
        }
        int size = list.size();
        if (size <= 0) {
            return false;
        }
        for (int i2 = 0; i2 < size; i2++) {
            aVar = (a) list.get(i2);
            if (aVar != null) {
                if (this.e) {
                    if (aVar.c >= 10 && !bg.a(aVar.a, aVar.b, aVar.c)) {
                    }
                }
                this.l.add(aVar);
                if (!aVar.g) {
                    this.k.a(z, aVar);
                }
            }
        }
        return true;
    }

    public void b(boolean z) {
        if (!this.m) {
            if (this.n != null && this.n.a() == d.RUNNING) {
                this.n.a(true);
            }
            this.n = new b(this, z);
            this.n.c((Object[]) new ab[]{this.f});
        }
    }

    public void g() {
        this.k.a(false);
    }

    public void c(boolean z) {
        if (this.m != z) {
            this.m = z;
            this.k.b(z);
        }
    }

    public void h() {
        if (this.l != null && this.l.size() != 0) {
            Iterator it = this.l.iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                aVar.g = false;
                aVar.f = 0;
            }
        }
    }
}
