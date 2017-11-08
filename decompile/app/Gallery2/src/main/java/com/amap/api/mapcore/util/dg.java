package com.amap.api.mapcore.util;

import android.graphics.Bitmap;
import com.amap.api.mapcore.util.dq.d;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.TileProvider;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import com.autonavi.amap.mapcore.interfaces.ITileOverlay;
import com.huawei.watermark.manager.parse.WMElement;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: TileOverlayDelegateImp */
public class dg implements cy {
    private static int g = 0;
    private v a;
    private TileProvider b;
    private Float c;
    private boolean d;
    private boolean e;
    private l f;
    private int h;
    private int i;
    private int j;
    private dx k;
    private CopyOnWriteArrayList<a> l;
    private boolean m;
    private b n;
    private final String o;
    private String p;
    private FloatBuffer q;

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
        public com.amap.api.mapcore.util.dz.a j = null;
        public int k = 0;
        final /* synthetic */ dg l;

        public /* synthetic */ Object clone() throws CloneNotSupportedException {
            return a();
        }

        public a(dg dgVar, int i, int i2, int i3, int i4) {
            this.l = dgVar;
            this.a = i;
            this.b = i2;
            this.c = i3;
            this.d = i4;
        }

        public a(dg dgVar, a aVar) {
            this.l = dgVar;
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
                    this.i = eh.a(bitmap, eh.a(bitmap.getWidth()), eh.a(bitmap.getHeight()));
                    this.l.f.setRunLowFrame(false);
                } catch (Throwable th) {
                    fo.b(th, "TileOverlayDelegateImp", "setBitmap");
                    th.printStackTrace();
                    if (this.k < 3) {
                        if (this.l.k != null) {
                            this.l.k.a(true, this);
                        }
                        this.k++;
                    }
                }
            } else if (this.k < 3) {
                if (this.l.k != null) {
                    this.l.k.a(true, this);
                }
                this.k++;
            }
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }

        public void b() {
            try {
                dz.a(this);
                if (this.g) {
                    this.l.a.a(this.f);
                }
                this.g = false;
                this.f = 0;
                if (this.i != null) {
                    if (!this.i.isRecycled()) {
                        this.i.recycle();
                    }
                }
                this.i = null;
                if (this.h != null) {
                    this.h.clear();
                }
                this.h = null;
                this.j = null;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
    }

    /* compiled from: TileOverlayDelegateImp */
    private class b extends dq<l, Void, List<a>> {
        final /* synthetic */ dg a;
        private int e;
        private boolean f;

        public b(dg dgVar, boolean z) {
            this.a = dgVar;
            this.f = z;
        }

        protected List<a> a(l... lVarArr) {
            try {
                int mapWidth = lVarArr[0].getMapWidth();
                int mapHeight = lVarArr[0].getMapHeight();
                this.e = (int) lVarArr[0].o();
                if (mapWidth > 0 && mapHeight > 0) {
                    return this.a.a(this.e, mapWidth, mapHeight);
                }
                return null;
            } catch (Throwable th) {
                th.printStackTrace();
                return null;
            }
        }

        protected void a(List<a> list) {
            if (list != null) {
                try {
                    if (list.size() > 0) {
                        this.a.a((List) list, this.e, this.f);
                        list.clear();
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
    }

    private static String a(String str) {
        g++;
        return str + g;
    }

    public dg(TileOverlayOptions tileOverlayOptions, v vVar) {
        this.e = false;
        this.h = 256;
        this.i = 256;
        this.j = -1;
        this.l = new CopyOnWriteArrayList();
        this.m = false;
        this.n = null;
        this.o = "TileOverlay";
        this.p = null;
        this.q = null;
        this.a = vVar;
        this.b = tileOverlayOptions.getTileProvider();
        this.h = this.b.getTileWidth();
        this.i = this.b.getTileHeight();
        int a = eh.a(this.h);
        float f = ((float) this.h) / ((float) a);
        float a2 = ((float) this.i) / ((float) eh.a(this.i));
        this.q = eh.a(new float[]{0.0f, a2, f, a2, f, 0.0f, 0.0f, 0.0f});
        this.c = Float.valueOf(tileOverlayOptions.getZIndex());
        this.d = tileOverlayOptions.isVisible();
        this.p = getId();
        this.f = this.a.a();
        this.j = Integer.valueOf(this.p.substring("TileOverlay".length())).intValue();
        try {
            com.amap.api.mapcore.util.dw.a aVar = new com.amap.api.mapcore.util.dw.a(this.a.e(), this.p);
            aVar.a(tileOverlayOptions.getMemoryCacheEnabled());
            aVar.b(tileOverlayOptions.getDiskCacheEnabled());
            aVar.a(tileOverlayOptions.getMemCacheSize());
            aVar.b(tileOverlayOptions.getDiskCacheSize());
            String diskCacheDir = tileOverlayOptions.getDiskCacheDir();
            if (diskCacheDir != null) {
                if (!diskCacheDir.equals("")) {
                    aVar.a(diskCacheDir);
                }
            }
            this.k = new dx(this.a.e(), this.h, this.i);
            this.k.a(this.b);
            this.k.a(aVar);
        } catch (Throwable th) {
            th.printStackTrace();
        }
        a(true);
    }

    public dg(TileOverlayOptions tileOverlayOptions, v vVar, boolean z) {
        this(tileOverlayOptions, vVar);
        this.e = z;
    }

    public void remove() {
        if (this.n != null && this.n.a() == d.RUNNING) {
            this.n.a(true);
        }
        Iterator it = this.l.iterator();
        while (it.hasNext()) {
            ((a) it.next()).b();
        }
        this.l.clear();
        if (this.k != null) {
            this.k.g();
        }
        this.a.b((cy) this);
        this.f.setRunLowFrame(false);
    }

    public void clearTileCache() {
        if (this.k != null) {
            this.k.f();
        }
    }

    public String getId() {
        if (this.p == null) {
            this.p = a("TileOverlay");
        }
        return this.p;
    }

    public void setZIndex(float f) {
        this.c = Float.valueOf(f);
        this.a.c();
    }

    public float getZIndex() {
        return this.c.floatValue();
    }

    public void setVisible(boolean z) {
        this.d = z;
        this.f.setRunLowFrame(false);
        if (z) {
            a(true);
        }
    }

    public boolean isVisible() {
        return this.d;
    }

    public boolean equalsRemote(ITileOverlay iTileOverlay) {
        if (equals(iTileOverlay) || iTileOverlay.getId().equals(getId())) {
            return true;
        }
        return false;
    }

    public int hashCodeRemote() {
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
            aVar.h = eh.a(r6, aVar.h);
        } else {
            aVar.h = eh.a(r6);
        }
        return true;
    }

    private void a(GL10 gl10, int i, FloatBuffer floatBuffer, FloatBuffer floatBuffer2) {
        if (floatBuffer != null && floatBuffer2 != null) {
            gl10.glEnable(3042);
            gl10.glTexEnvf(8960, 8704, 8448.0f);
            gl10.glBlendFunc(1, 771);
            gl10.glColor4f(WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
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
                            aVar.f = eh.a(gl10, aVar.i);
                            if (aVar.f != 0) {
                                aVar.g = true;
                            }
                            aVar.i = null;
                        }
                    } catch (Throwable th) {
                        fo.b(th, "TileOverlayDelegateImp", "drawTiles");
                    }
                }
                if (aVar.g) {
                    a(aVar);
                    a(gl10, aVar.f, aVar.h, this.q);
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

    public void c() {
        if (this.l != null) {
            this.l.clear();
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
        if (i > ((int) this.f.getMaxZoomLevel()) || i < ((int) this.f.getMinZoomLevel())) {
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
                    if (aVar.c >= 10 && !ee.a(aVar.a, aVar.b, aVar.c)) {
                    }
                }
                this.l.add(aVar);
                if (!(aVar.g || this.k == null)) {
                    this.k.a(z, aVar);
                }
            }
        }
        return true;
    }

    public void a(boolean z) {
        if (!this.m) {
            if (this.n != null && this.n.a() == d.RUNNING) {
                this.n.a(true);
            }
            this.n = new b(this, z);
            this.n.c((Object[]) new l[]{this.f});
        }
    }

    public void a() {
        if (this.k != null) {
            this.k.a(false);
        }
    }

    public void b(boolean z) {
        if (this.m != z) {
            this.m = z;
            if (this.k != null) {
                this.k.b(z);
            }
        }
    }

    public void b() {
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
