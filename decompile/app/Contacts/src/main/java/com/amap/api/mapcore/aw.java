package com.amap.api.mapcore;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.Marker;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: MapOverlayImageView */
class aw extends View {
    ab a;
    a b = new a();
    private CopyOnWriteArrayList<ah> c = new CopyOnWriteArrayList(new ArrayList(VTMCDataCache.MAXSIZE));
    private CopyOnWriteArrayList<be> d = new CopyOnWriteArrayList();
    private CopyOnWriteArrayList<Integer> e = new CopyOnWriteArrayList();
    private IPoint f;
    private ah g;
    private Handler h = new Handler();
    private Runnable i = new ax(this);
    private final Handler j = new Handler();
    private final Runnable k = new ay(this);

    /* compiled from: MapOverlayImageView */
    static class a implements Comparator<Object>, Serializable {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            ah ahVar = (ah) obj;
            ah ahVar2 = (ah) obj2;
            if (!(ahVar == null || ahVar2 == null)) {
                try {
                    if (ahVar.G() > ahVar2.G()) {
                        return 1;
                    }
                    if (ahVar.G() < ahVar2.G()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    ce.a(th, "MapOverlayImageView", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    public aw(Context context) {
        super(context);
    }

    public aw(Context context, AttributeSet attributeSet, ab abVar) {
        super(context, attributeSet);
        this.a = abVar;
    }

    protected synchronized int a() {
        return this.c.size();
    }

    public synchronized void a(String str) {
        Object obj = null;
        synchronized (this) {
            Iterator it;
            ah ahVar;
            if (str != null) {
                if (str.trim().length() != 0) {
                    this.g = null;
                    this.f = null;
                    if (obj != null) {
                        it = this.c.iterator();
                        while (it.hasNext()) {
                            ahVar = (ah) it.next();
                            if (!str.equals(ahVar.h())) {
                                ahVar.b();
                            }
                        }
                    } else {
                        it = this.c.iterator();
                        while (it.hasNext()) {
                            ((ah) it.next()).b();
                        }
                        this.c.clear();
                    }
                }
            }
            obj = 1;
            try {
                this.g = null;
                this.f = null;
                if (obj != null) {
                    it = this.c.iterator();
                    while (it.hasNext()) {
                        ((ah) it.next()).b();
                    }
                    this.c.clear();
                } else {
                    it = this.c.iterator();
                    while (it.hasNext()) {
                        ahVar = (ah) it.next();
                        if (!str.equals(ahVar.h())) {
                            ahVar.b();
                        }
                    }
                }
            } catch (Throwable e) {
                ce.a(e, "MapOverlayImageView", "clear");
                e.printStackTrace();
            }
        }
    }

    public synchronized void a(ah ahVar) {
        this.c.add(ahVar);
        h();
    }

    public synchronized boolean b(ah ahVar) {
        e(ahVar);
        return this.c.remove(ahVar);
    }

    public synchronized void c(ah ahVar) {
        try {
            if (this.c.remove(ahVar)) {
                k();
                this.c.add(ahVar);
            }
        } catch (Throwable th) {
            ce.a(th, "MapOverlayImageView", "set2Top");
        }
    }

    public void d(ah ahVar) {
        if (this.f == null) {
            this.f = new IPoint();
        }
        Rect d = ahVar.d();
        this.f = new IPoint(d.left + (d.width() / 2), d.top);
        this.g = ahVar;
        try {
            this.a.a(this.g);
        } catch (Throwable th) {
            ce.a(th, "MapOverlayImageView", "showInfoWindow");
            th.printStackTrace();
        }
    }

    public void e(ah ahVar) {
        try {
            if (ahVar.n()) {
                this.a.E();
                this.g = null;
            } else if (this.g != null) {
                if (this.g.h() == ahVar.h()) {
                    this.g = null;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public synchronized void b() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ah ahVar = (ah) it.next();
            try {
                if (ahVar.o()) {
                    ahVar.r();
                }
            } catch (Throwable th) {
                ce.a(th, "MapOverlayImageView", "calFPoint");
                th.printStackTrace();
            }
        }
    }

    private void k() {
        try {
            Collection arrayList = new ArrayList(this.c);
            Collections.sort(arrayList, this.b);
            this.c = new CopyOnWriteArrayList(arrayList);
        } catch (Throwable th) {
            ce.a(th, "MapOverlayImageView", "changeOverlayIndex");
        }
    }

    public void a(GL10 gl10) {
        Iterator it = this.e.iterator();
        while (it.hasNext()) {
            gl10.glDeleteTextures(1, new int[]{((Integer) it.next()).intValue()}, 0);
            this.a.f(r0.intValue());
        }
        this.e.clear();
        if (!(this.g == null || this.g.F())) {
            j();
        }
        it = this.c.iterator();
        while (it.hasNext()) {
            ah ahVar = (ah) it.next();
            if (ahVar.H() || ahVar.n()) {
                ahVar.a(gl10, this.a);
            }
        }
    }

    public synchronized boolean c() {
        Iterator it = this.c.iterator();
        do {
            if (!it.hasNext()) {
                return true;
            }
        } while (((ah) it.next()).c());
        return false;
    }

    public ah d() {
        return this.g;
    }

    public ah a(MotionEvent motionEvent) {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ah ahVar = (ah) it.next();
            if ((ahVar instanceof ba) && a(ahVar.d(), (int) motionEvent.getX(), (int) motionEvent.getY())) {
                this.g = ahVar;
                return this.g;
            }
        }
        return null;
    }

    public synchronized void a(be beVar) {
        if (beVar == null) {
            return;
        }
        if (beVar.b() != 0) {
            this.d.add(beVar);
        }
    }

    public synchronized void a(int i) {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            be beVar = (be) it.next();
            if (beVar.b() == i) {
                this.d.remove(beVar);
            }
        }
    }

    public void a(Integer num) {
        if (num.intValue() != 0) {
            this.e.add(num);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized int a(BitmapDescriptor bitmapDescriptor) {
        if (bitmapDescriptor != null) {
            if (!(bitmapDescriptor.getBitmap() == null || bitmapDescriptor.getBitmap().isRecycled())) {
                for (int i = 0; i < this.d.size(); i++) {
                    be beVar = (be) this.d.get(i);
                    if (beVar.a().equals(bitmapDescriptor)) {
                        return beVar.b();
                    }
                }
                return 0;
            }
        }
    }

    public synchronized void e() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            try {
                ah ahVar = (ah) it.next();
                if (ahVar != null) {
                    ahVar.p();
                }
            } catch (Throwable th) {
                ce.a(th, "MapOverlayImageView", "destroy");
                th.printStackTrace();
                Log.d("amapApi", "MapOverlayImageView clear erro" + th.getMessage());
            }
        }
        a(null);
        it = this.d.iterator();
        while (it.hasNext()) {
            ((be) it.next()).a().recycle();
        }
        this.d.clear();
    }

    public boolean b(MotionEvent motionEvent) throws RemoteException {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ah ahVar = (ah) it.next();
            if ((ahVar instanceof ba) && ahVar.o()) {
                Rect d = ahVar.d();
                boolean a = a(d, (int) motionEvent.getX(), (int) motionEvent.getY());
                if (a) {
                    this.f = new IPoint(d.left + (d.width() / 2), d.top);
                    this.g = ahVar;
                    return a;
                }
            }
        }
        return false;
    }

    public boolean a(Rect rect, int i, int i2) {
        return rect.contains(i, i2);
    }

    public synchronized List<Marker> f() {
        List<Marker> arrayList;
        arrayList = new ArrayList();
        try {
            Rect rect = new Rect(0, 0, this.a.l(), this.a.m());
            IPoint iPoint = new IPoint();
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                ah ahVar = (ah) it.next();
                if (!(ahVar instanceof bl)) {
                    FPoint f = ahVar.f();
                    if (f != null) {
                        this.a.c().map2Win(f.x, f.y, iPoint);
                        if (a(rect, iPoint.x, iPoint.y)) {
                            arrayList.add(new Marker(ahVar));
                        }
                    }
                }
            }
        } catch (Throwable th) {
            ce.a(th, "MapOverlayImageView", "getMapScreenMarkers");
            th.printStackTrace();
        }
        return arrayList;
    }

    public synchronized void g() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ah ahVar = (ah) it.next();
            if (ahVar.x()) {
                ahVar.y();
            }
        }
    }

    protected synchronized void h() {
        this.h.removeCallbacks(this.i);
        this.h.postDelayed(this.i, 10);
    }

    public void i() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            ah ahVar = (ah) it.next();
            if (ahVar != null) {
                ahVar.J();
            }
        }
        if (this.d != null) {
            this.d.clear();
        }
    }

    public void j() {
        this.j.post(this.k);
    }
}
