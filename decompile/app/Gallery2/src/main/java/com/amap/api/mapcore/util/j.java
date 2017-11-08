package com.amap.api.mapcore.util;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.maps.model.ArcOptions;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.NavigateArrowOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.PolylineOptions;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: GLOverlayLayer */
public class j {
    private static int c = 0;
    l a;
    a b = new a();
    private CopyOnWriteArrayList<ct> d = new CopyOnWriteArrayList(new ArrayList(500));
    private CopyOnWriteArrayList<Integer> e = new CopyOnWriteArrayList();
    private Handler f = new Handler();
    private Runnable g = new Runnable(this) {
        final /* synthetic */ j a;

        {
            this.a = r1;
        }

        public synchronized void run() {
            try {
                synchronized (this.a) {
                    Collection arrayList = new ArrayList(this.a.d);
                    Collections.sort(arrayList, this.a.b);
                    this.a.d = new CopyOnWriteArrayList(arrayList);
                }
            } catch (Throwable th) {
                fo.b(th, "MapOverlayImageView", "changeOverlayIndex");
            }
        }
    };

    /* compiled from: GLOverlayLayer */
    static class a implements Serializable, Comparator<Object> {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            ct ctVar = (ct) obj;
            ct ctVar2 = (ct) obj2;
            if (!(ctVar == null || ctVar2 == null)) {
                try {
                    if (ctVar.getZIndex() > ctVar2.getZIndex()) {
                        return 1;
                    }
                    if (ctVar.getZIndex() < ctVar2.getZIndex()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    fo.b(th, "GLOverlayLayer", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    public static String a(String str) {
        c++;
        return str + c;
    }

    public j(l lVar) {
        this.a = lVar;
    }

    public synchronized void b(String str) {
        if (str != null) {
            if (str.trim().length() != 0) {
                Iterator it = this.d.iterator();
                while (it.hasNext()) {
                    ct ctVar = (ct) it.next();
                    if (!str.equals(ctVar.getId())) {
                        this.d.remove(ctVar);
                    }
                }
            }
        }
        try {
            this.d.clear();
            a();
        } catch (Throwable th) {
            fo.b(th, "GLOverlayLayer", "clear");
            th.printStackTrace();
            Log.d("amapApi", "GLOverlayLayer clear erro" + th.getMessage());
        }
    }

    public static void a() {
        c = 0;
    }

    public synchronized void b() {
        try {
            Iterator it = this.d.iterator();
            while (it.hasNext()) {
                ((ct) it.next()).destroy();
            }
            b(null);
        } catch (Throwable th) {
            fo.b(th, "GLOverlayLayer", "destory");
            th.printStackTrace();
            Log.d("amapApi", "GLOverlayLayer destory erro" + th.getMessage());
        }
    }

    synchronized ct c(String str) throws RemoteException {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            ct ctVar = (ct) it.next();
            if (ctVar != null && ctVar.getId().equals(str)) {
                return ctVar;
            }
        }
        return null;
    }

    public synchronized cw a(PolylineOptions polylineOptions) throws RemoteException {
        if (polylineOptions == null) {
            return null;
        }
        ct ddVar = new dd(this, polylineOptions);
        a(ddVar);
        return ddVar;
    }

    public synchronized cs a(NavigateArrowOptions navigateArrowOptions) throws RemoteException {
        if (navigateArrowOptions == null) {
            return null;
        }
        ct dbVar = new db(this.a);
        dbVar.setTopColor(navigateArrowOptions.getTopColor());
        dbVar.setPoints(navigateArrowOptions.getPoints());
        dbVar.setVisible(navigateArrowOptions.isVisible());
        dbVar.setWidth(navigateArrowOptions.getWidth());
        dbVar.setZIndex(navigateArrowOptions.getZIndex());
        a(dbVar);
        return dbVar;
    }

    public synchronized cv a(PolygonOptions polygonOptions) throws RemoteException {
        if (polygonOptions == null) {
            return null;
        }
        ct dcVar = new dc(this.a);
        dcVar.setFillColor(polygonOptions.getFillColor());
        dcVar.setPoints(polygonOptions.getPoints());
        dcVar.setVisible(polygonOptions.isVisible());
        dcVar.setStrokeWidth(polygonOptions.getStrokeWidth());
        dcVar.setZIndex(polygonOptions.getZIndex());
        dcVar.setStrokeColor(polygonOptions.getStrokeColor());
        a(dcVar);
        return dcVar;
    }

    public synchronized co a(CircleOptions circleOptions) throws RemoteException {
        if (circleOptions == null) {
            return null;
        }
        ct ckVar = new ck(this.a);
        ckVar.setFillColor(circleOptions.getFillColor());
        ckVar.setCenter(circleOptions.getCenter());
        ckVar.setVisible(circleOptions.isVisible());
        ckVar.setStrokeWidth(circleOptions.getStrokeWidth());
        ckVar.setZIndex(circleOptions.getZIndex());
        ckVar.setStrokeColor(circleOptions.getStrokeColor());
        ckVar.setRadius(circleOptions.getRadius());
        a(ckVar);
        return ckVar;
    }

    public synchronized cn a(ArcOptions arcOptions) throws RemoteException {
        if (arcOptions == null) {
            return null;
        }
        ct cjVar = new cj(this.a);
        cjVar.setStrokeColor(arcOptions.getStrokeColor());
        cjVar.a(arcOptions.getStart());
        cjVar.b(arcOptions.getPassed());
        cjVar.c(arcOptions.getEnd());
        cjVar.setVisible(arcOptions.isVisible());
        cjVar.setStrokeWidth(arcOptions.getStrokeWidth());
        cjVar.setZIndex(arcOptions.getZIndex());
        a(cjVar);
        return cjVar;
    }

    public synchronized cp a(GroundOverlayOptions groundOverlayOptions) throws RemoteException {
        if (groundOverlayOptions == null) {
            return null;
        }
        ct cmVar = new cm(this.a);
        cmVar.a(groundOverlayOptions.getAnchorU(), groundOverlayOptions.getAnchorV());
        cmVar.setDimensions(groundOverlayOptions.getWidth(), groundOverlayOptions.getHeight());
        cmVar.setImage(groundOverlayOptions.getImage());
        cmVar.setPosition(groundOverlayOptions.getLocation());
        cmVar.setPositionFromBounds(groundOverlayOptions.getBounds());
        cmVar.setBearing(groundOverlayOptions.getBearing());
        cmVar.setTransparency(groundOverlayOptions.getTransparency());
        cmVar.setVisible(groundOverlayOptions.isVisible());
        cmVar.setZIndex(groundOverlayOptions.getZIndex());
        a(cmVar);
        return cmVar;
    }

    private void a(ct ctVar) throws RemoteException {
        this.d.add(ctVar);
        c();
    }

    public synchronized boolean d(String str) throws RemoteException {
        ct c = c(str);
        if (c == null) {
            return false;
        }
        return this.d.remove(c);
    }

    public synchronized void c() {
        this.f.removeCallbacks(this.g);
        this.f.postDelayed(this.g, 10);
    }

    public void a(GL10 gl10, boolean z, int i) {
        Iterator it = this.e.iterator();
        while (it.hasNext()) {
            gl10.glDeleteTextures(1, new int[]{((Integer) it.next()).intValue()}, 0);
        }
        this.e.clear();
        int size = this.d.size();
        Iterator it2 = this.d.iterator();
        while (it2.hasNext()) {
            ct ctVar = (ct) it2.next();
            try {
                if (ctVar.isVisible()) {
                    if (size <= 20) {
                        if (z) {
                            if (ctVar.getZIndex() <= ((float) i)) {
                                ctVar.a(gl10);
                            }
                        } else if (ctVar.getZIndex() > ((float) i)) {
                            ctVar.a(gl10);
                        }
                    } else if (ctVar.a()) {
                        if (z) {
                            if (ctVar.getZIndex() <= ((float) i)) {
                                ctVar.a(gl10);
                            }
                        } else if (ctVar.getZIndex() > ((float) i)) {
                            ctVar.a(gl10);
                        }
                    }
                }
            } catch (Throwable e) {
                fo.b(e, "GLOverlayLayer", "draw");
                e.printStackTrace();
            }
        }
    }

    public void a(GL10 gl10) {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            ct ctVar = (ct) it.next();
            try {
                if (ctVar.isVisible() && ctVar.isAboveMaskLayer() && ctVar.a()) {
                    ctVar.a(gl10);
                }
            } catch (Throwable th) {
                fo.b(th, "GLOverlayLayer", "draw");
                th.printStackTrace();
            }
        }
    }

    public void a(Integer num) {
        if (num.intValue() != 0) {
            this.e.add(num);
        }
    }

    public synchronized void d() {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            ct ctVar = (ct) it.next();
            if (ctVar != null) {
                try {
                    ctVar.b();
                } catch (Throwable e) {
                    fo.b(e, "GLOverlayLayer", "calMapFPoint");
                    e.printStackTrace();
                }
            }
        }
    }

    public synchronized ct a(LatLng latLng) {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            ct ctVar = (ct) it.next();
            if (ctVar != null && ctVar.c() && (ctVar instanceof cw) && ((cw) ctVar).a(latLng)) {
                return ctVar;
            }
        }
        return null;
    }

    public l e() {
        return this.a;
    }

    public void f() {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            ct ctVar = (ct) it.next();
            if (ctVar != null) {
                if (ctVar instanceof cw) {
                    ((cw) ctVar).d();
                } else if (ctVar instanceof cp) {
                    ((cp) ctVar).d();
                }
            }
        }
    }
}
