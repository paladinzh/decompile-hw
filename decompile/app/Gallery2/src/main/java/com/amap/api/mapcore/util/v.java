package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.RemoteException;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.TileOverlay;
import com.amap.api.maps.model.TileOverlayOptions;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: TileOverlayView */
public class v {
    CopyOnWriteArrayList<cy> a = new CopyOnWriteArrayList();
    a b = new a();
    CopyOnWriteArrayList<Integer> c = new CopyOnWriteArrayList();
    dg d = null;
    private l e;
    private Context f;

    /* compiled from: TileOverlayView */
    static class a implements Serializable, Comparator<Object> {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            cy cyVar = (cy) obj;
            cy cyVar2 = (cy) obj2;
            if (!(cyVar == null || cyVar2 == null)) {
                try {
                    if (cyVar.getZIndex() > cyVar2.getZIndex()) {
                        return 1;
                    }
                    if (cyVar.getZIndex() < cyVar2.getZIndex()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    fo.b(th, "TileOverlayView", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    public v(Context context, l lVar) {
        this.e = lVar;
        this.f = context;
        this.d = new dg(new TileOverlayOptions().tileProvider(new dr(this, 256, 256) {
            final /* synthetic */ v a;

            public String a(int i, int i2, int i3) {
                try {
                    return String.format("http://grid.amap.com/grid/%d/%d/%d?dpiType=webrd&lang=zh_cn&pack=%s&ds=0", new Object[]{Integer.valueOf(i3), Integer.valueOf(i), Integer.valueOf(i2), g.b});
                } catch (Throwable th) {
                    return null;
                }
            }
        }), this, true);
    }

    public l a() {
        return this.e;
    }

    public void a(GL10 gl10) {
        try {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                eh.a(gl10, ((Integer) it.next()).intValue());
            }
            this.c.clear();
            if (g.c == 0 && this.d != null) {
                this.d.a(gl10);
            }
            it = this.a.iterator();
            while (it.hasNext()) {
                cy cyVar = (cy) it.next();
                if (cyVar.isVisible()) {
                    cyVar.a(gl10);
                }
            }
        } catch (Throwable th) {
        }
    }

    public void b() {
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            cy cyVar = (cy) it.next();
            if (cyVar != null) {
                cyVar.remove();
            }
        }
        this.a.clear();
    }

    public void c() {
        Object[] toArray = this.a.toArray();
        Arrays.sort(toArray, this.b);
        this.a.clear();
        for (Object obj : toArray) {
            this.a.add((cy) obj);
        }
    }

    public TileOverlay a(TileOverlayOptions tileOverlayOptions) throws RemoteException {
        if (tileOverlayOptions == null || tileOverlayOptions.getTileProvider() == null) {
            return null;
        }
        cy dgVar = new dg(tileOverlayOptions, this);
        a(dgVar);
        this.e.setRunLowFrame(false);
        return new TileOverlay(dgVar);
    }

    public void a(cy cyVar) {
        b(cyVar);
        this.a.add(cyVar);
        c();
    }

    public boolean b(cy cyVar) {
        return this.a.remove(cyVar);
    }

    public void a(boolean z) {
        try {
            if (g.c == 0) {
                CameraPosition cameraPosition = this.e.getCameraPosition();
                if (cameraPosition != null) {
                    if (cameraPosition.zoom > 10.0f && cameraPosition.isAbroad && this.e.getMapType() == 1) {
                        if (this.d != null) {
                            this.d.a(z);
                        }
                    }
                }
                if (this.d != null) {
                    this.d.c();
                }
            }
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                cy cyVar = (cy) it.next();
                if (cyVar != null && cyVar.isVisible()) {
                    cyVar.a(z);
                }
            }
        } catch (Throwable th) {
            fo.b(th, "TileOverlayView", "refresh");
        }
    }

    public void d() {
        if (this.d != null) {
            this.d.a();
        }
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            cy cyVar = (cy) it.next();
            if (cyVar != null) {
                cyVar.a();
            }
        }
    }

    public void b(boolean z) {
        if (this.d != null) {
            this.d.b(z);
        }
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            cy cyVar = (cy) it.next();
            if (cyVar != null) {
                cyVar.b(z);
            }
        }
    }

    public Context e() {
        return this.f;
    }

    public void a(int i) {
        this.c.add(Integer.valueOf(i));
    }

    public void f() {
        b();
        if (this.d != null) {
            this.d.remove();
        }
        this.d = null;
    }

    public void g() {
        if (this.d != null) {
            this.d.b();
        }
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            cy cyVar = (cy) it.next();
            if (cyVar != null) {
                cyVar.b();
            }
        }
    }
}
