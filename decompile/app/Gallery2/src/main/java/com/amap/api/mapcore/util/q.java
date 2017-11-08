package com.amap.api.mapcore.util;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Text;
import com.amap.api.maps.model.TextOptions;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.interfaces.IMarker;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
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
public class q {
    l a;
    a b = new a();
    private CopyOnWriteArrayList<cu> c = new CopyOnWriteArrayList(new ArrayList(500));
    private CopyOnWriteArrayList<Integer> d = new CopyOnWriteArrayList();
    private IPoint e;
    private cr f;
    private Handler g = new Handler();
    private Runnable h = new Runnable(this) {
        final /* synthetic */ q a;

        {
            this.a = r1;
        }

        public synchronized void run() {
            this.a.i();
        }
    };
    private boolean i = true;

    /* compiled from: MapOverlayImageView */
    static class a implements Serializable, Comparator<Object> {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            cu cuVar = (cu) obj;
            cu cuVar2 = (cu) obj2;
            if (!(cuVar == null || cuVar2 == null)) {
                try {
                    if (cuVar.getZIndex() > cuVar2.getZIndex()) {
                        return 1;
                    }
                    if (cuVar.getZIndex() < cuVar2.getZIndex()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    fo.b(th, "MapOverlayImageView", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    public l a() {
        return this.a;
    }

    public q(Context context, l lVar) {
        this.a = lVar;
    }

    protected synchronized int b() {
        return this.c.size();
    }

    public synchronized void a(String str) {
        Object obj = null;
        synchronized (this) {
            Iterator it;
            cu cuVar;
            if (str != null) {
                if (str.trim().length() != 0) {
                    this.f = null;
                    this.e = null;
                    if (obj != null) {
                        it = this.c.iterator();
                        while (it.hasNext()) {
                            cuVar = (cu) it.next();
                            if (!str.equals(cuVar.getId())) {
                                cuVar.remove();
                            }
                        }
                    } else {
                        it = this.c.iterator();
                        while (it.hasNext()) {
                            ((cu) it.next()).remove();
                        }
                        this.c.clear();
                    }
                }
            }
            obj = 1;
            try {
                this.f = null;
                this.e = null;
                if (obj != null) {
                    it = this.c.iterator();
                    while (it.hasNext()) {
                        ((cu) it.next()).remove();
                    }
                    this.c.clear();
                } else {
                    it = this.c.iterator();
                    while (it.hasNext()) {
                        cuVar = (cu) it.next();
                        if (!str.equals(cuVar.getId())) {
                            cuVar.remove();
                        }
                    }
                }
            } catch (Throwable e) {
                fo.b(e, "MapOverlayImageView", "clear");
                e.printStackTrace();
            }
        }
    }

    public Marker a(MarkerOptions markerOptions) throws RemoteException {
        if (markerOptions == null) {
            return null;
        }
        Marker marker;
        synchronized (this) {
            Object czVar = new cz(markerOptions, this);
            d(czVar);
            marker = new Marker(czVar);
        }
        return marker;
    }

    public ArrayList<Marker> a(ArrayList<MarkerOptions> arrayList, boolean z) throws RemoteException {
        int i = 0;
        if (arrayList == null || arrayList.size() == 0) {
            return null;
        }
        ArrayList<Marker> arrayList2 = new ArrayList();
        try {
            MarkerOptions markerOptions;
            if (arrayList.size() == 1) {
                markerOptions = (MarkerOptions) arrayList.get(0);
                if (markerOptions != null) {
                    arrayList2.add(a(markerOptions));
                    if (z && markerOptions.getPosition() != null) {
                        this.a.a(ag.a(markerOptions.getPosition(), 18.0f));
                    }
                    return arrayList2;
                }
            }
            final Builder builder = LatLngBounds.builder();
            int i2 = 0;
            while (i2 < arrayList.size()) {
                int i3;
                markerOptions = (MarkerOptions) arrayList.get(i2);
                if (arrayList.get(i2) == null) {
                    i3 = i;
                } else {
                    arrayList2.add(a(markerOptions));
                    if (markerOptions.getPosition() == null) {
                        i3 = i;
                    } else {
                        builder.include(markerOptions.getPosition());
                        i3 = i + 1;
                    }
                }
                i2++;
                i = i3;
            }
            if (z && i > 0) {
                this.a.getMainHandler().postDelayed(new Runnable(this) {
                    final /* synthetic */ q b;

                    public void run() {
                        try {
                            this.b.a.a(ag.a(builder.build(), 50));
                        } catch (Throwable th) {
                        }
                    }
                }, 50);
            }
            return arrayList2;
        } catch (Throwable th) {
            fo.b(th, "AMapDelegateImpGLSurfaceView", "addMarkers");
            th.printStackTrace();
            return arrayList2;
        }
    }

    public Text a(TextOptions textOptions) throws RemoteException {
        if (textOptions == null) {
            return null;
        }
        Text text;
        synchronized (this) {
            Object dfVar = new df(textOptions, this);
            d(dfVar);
            text = new Text(dfVar);
        }
        return text;
    }

    private void d(cu cuVar) {
        try {
            this.c.add(cuVar);
            g();
        } catch (Throwable e) {
            fo.b(e, "MapOverlayImageView", "addMarker");
        }
    }

    public synchronized boolean a(cu cuVar) {
        c(cuVar);
        return this.c.remove(cuVar);
    }

    public synchronized void b(cu cuVar) {
        try {
            if (this.c.remove(cuVar)) {
                i();
                this.c.add(cuVar);
            }
        } catch (Throwable th) {
            fo.b(th, "MapOverlayImageView", "set2Top");
        }
    }

    public void a(cr crVar) {
        if (this.e == null) {
            this.e = new IPoint();
        }
        Rect h = crVar.h();
        this.e = new IPoint(h.left + (h.width() / 2), h.top);
        this.f = crVar;
        try {
            this.a.a(this.f);
        } catch (Throwable th) {
            fo.b(th, "MapOverlayImageView", "showInfoWindow");
            th.printStackTrace();
        }
    }

    public void c(cu cuVar) {
        try {
            if (cuVar.isInfoWindowShown()) {
                this.a.n();
                this.f = null;
            } else if (this.f != null) {
                if (this.f.getId() == cuVar.getId()) {
                    this.f = null;
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    public void c() {
        synchronized (this) {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                cu cuVar = (cu) it.next();
                try {
                    if (cuVar.isVisible()) {
                        cuVar.i();
                    }
                } catch (Throwable th) {
                    fo.b(th, "MapOverlayImageView", "calFPoint");
                    th.printStackTrace();
                }
            }
        }
    }

    private void i() {
        try {
            Collection arrayList = new ArrayList(this.c);
            Collections.sort(arrayList, this.b);
            this.c = new CopyOnWriteArrayList(arrayList);
        } catch (Throwable th) {
            fo.b(th, "MapOverlayImageView", "changeOverlayIndex");
        }
    }

    public void a(GL10 gl10) {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            gl10.glDeleteTextures(1, new int[]{((Integer) it.next()).intValue()}, 0);
        }
        this.d.clear();
        it = this.c.iterator();
        while (it.hasNext()) {
            cu cuVar = (cu) it.next();
            if (e(cuVar)) {
                this.i = cuVar.j();
                if (cuVar.k() || cuVar.isInfoWindowShown()) {
                    cuVar.a(gl10, this.a);
                }
            }
        }
    }

    public void b(GL10 gl10) {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            cu cuVar = (cu) it.next();
            if (cuVar.m()) {
                this.i = cuVar.j();
                if (cuVar.k() || cuVar.isInfoWindowShown()) {
                    cuVar.a(gl10, this.a);
                }
            }
        }
    }

    private boolean e(cu cuVar) {
        boolean z = false;
        switch (this.a.A()) {
            case -1:
            case 1:
                return true;
            case 0:
                if (!cuVar.m()) {
                    z = true;
                }
                return z;
            default:
                return true;
        }
    }

    public cr d() {
        return this.f;
    }

    public cr a(MotionEvent motionEvent) {
        for (int size = this.c.size() - 1; size >= 0; size--) {
            cu cuVar = (cu) this.c.get(size);
            if ((cuVar instanceof cz) && eh.a(cuVar.h(), (int) motionEvent.getX(), (int) motionEvent.getY())) {
                this.f = (cr) cuVar;
                return this.f;
            }
        }
        return null;
    }

    public void a(Integer num) {
        if (num.intValue() != 0) {
            this.d.add(num);
        }
    }

    public synchronized void e() {
        try {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                cu cuVar = (cu) it.next();
                if (cuVar != null) {
                    cuVar.destroy();
                }
            }
            a(null);
        } catch (Throwable th) {
            fo.b(th, "MapOverlayImageView", "destroy");
            th.printStackTrace();
            Log.d("amapApi", "MapOverlayImageView clear erro" + th.getMessage());
        }
    }

    public boolean b(MotionEvent motionEvent) throws RemoteException {
        for (int size = this.c.size() - 1; size >= 0; size--) {
            cu cuVar = (cu) this.c.get(size);
            if ((cuVar instanceof cz) && cuVar.isVisible() && ((cz) cuVar).isClickable()) {
                Rect h = cuVar.h();
                boolean a = eh.a(h, (int) motionEvent.getX(), (int) motionEvent.getY());
                if (a) {
                    this.e = new IPoint(h.left + (h.width() / 2), h.top);
                    this.f = (cr) cuVar;
                    return a;
                }
            }
        }
        return false;
    }

    public synchronized List<Marker> f() {
        List<Marker> arrayList;
        arrayList = new ArrayList();
        try {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                cu cuVar = (cu) it.next();
                if ((cuVar instanceof cz) && cuVar.k()) {
                    arrayList.add(new Marker((IMarker) cuVar));
                }
            }
        } catch (Throwable th) {
            fo.b(th, "MapOverlayImageView", "getMapScreenMarkers");
            th.printStackTrace();
        }
        return arrayList;
    }

    public synchronized void g() {
        this.g.removeCallbacks(this.h);
        this.g.postDelayed(this.h, 10);
    }

    public int a(IMarkerAction iMarkerAction, Rect rect) {
        int displayLevel = iMarkerAction.getDisplayLevel();
        if (displayLevel == 0) {
            return 0;
        }
        Iterator it = this.c.iterator();
        int i = 0;
        while (it.hasNext()) {
            int i2;
            cu cuVar = (cu) it.next();
            IMarkerAction iMarkerAction2 = cuVar.getIMarkerAction();
            if (iMarkerAction2 == null) {
                i2 = i;
            } else {
                int displayLevel2 = iMarkerAction2.getDisplayLevel();
                if (displayLevel2 != 0 && displayLevel > displayLevel2) {
                    Rect h = cuVar.h();
                    if (h == null || !Rect.intersects(rect, h)) {
                        i2 = i;
                    } else if (i != 0) {
                        if (i > displayLevel2) {
                            i = displayLevel2;
                        }
                        i2 = i;
                    } else {
                        i2 = displayLevel2;
                    }
                } else {
                    i2 = i;
                }
            }
            i = i2;
        }
        return i;
    }

    public void h() {
        Iterator it = this.c.iterator();
        while (it.hasNext()) {
            cu cuVar = (cu) it.next();
            if (cuVar != null) {
                cuVar.l();
            }
        }
    }
}
