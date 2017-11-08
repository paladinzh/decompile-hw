package com.amap.api.mapcore;

import android.content.Context;
import android.view.View;
import com.amap.api.mapcore.util.bj;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.TileOverlayOptions;
import com.amap.api.maps.model.UrlTileProvider;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: TileOverlayView */
public class bo extends View {
    CopyOnWriteArrayList<ap> a = new CopyOnWriteArrayList();
    a b = new a();
    CopyOnWriteArrayList<Integer> c = new CopyOnWriteArrayList();
    bn d = null;
    private ab e;

    /* compiled from: TileOverlayView */
    static class a implements Comparator<Object>, Serializable {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            ap apVar = (ap) obj;
            ap apVar2 = (ap) obj2;
            if (!(apVar == null || apVar2 == null)) {
                try {
                    if (apVar.d() > apVar2.d()) {
                        return 1;
                    }
                    if (apVar.d() < apVar2.d()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    ce.a(th, "TileOverlayView", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    public bo(Context context) {
        super(context);
    }

    public bo(Context context, ab abVar) {
        super(context);
        this.e = abVar;
        this.d = new bn(new TileOverlayOptions().tileProvider(new UrlTileProvider(this, 256, 256) {
            final /* synthetic */ bo a;

            public URL getTileUrl(int i, int i2, int i3) {
                try {
                    return new URL(String.format("http://grid.amap.com/grid/%d/%d/%d?dpiType=webrd&lang=zh_cn&pack=%s&version=3.3.0", new Object[]{Integer.valueOf(i3), Integer.valueOf(i), Integer.valueOf(i2), s.c}));
                } catch (Throwable th) {
                    return null;
                }
            }
        }), this, true);
    }

    ab a() {
        return this.e;
    }

    public void a(GL10 gl10) {
        try {
            Iterator it = this.c.iterator();
            while (it.hasNext()) {
                bj.a(gl10, ((Integer) it.next()).intValue());
            }
            this.c.clear();
            this.d.a(gl10);
            it = this.a.iterator();
            while (it.hasNext()) {
                ap apVar = (ap) it.next();
                if (apVar.e()) {
                    apVar.a(gl10);
                }
            }
        } catch (Throwable th) {
        }
    }

    public void b() {
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            ap apVar = (ap) it.next();
            if (apVar != null) {
                apVar.a();
            }
        }
        this.a.clear();
    }

    void c() {
        Object[] toArray = this.a.toArray();
        Arrays.sort(toArray, this.b);
        this.a.clear();
        for (Object obj : toArray) {
            this.a.add((ap) obj);
        }
    }

    public void a(ap apVar) {
        b(apVar);
        this.a.add(apVar);
        c();
    }

    public boolean b(ap apVar) {
        return this.a.remove(apVar);
    }

    public void a(boolean z) {
        this.d.b(z);
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            ap apVar = (ap) it.next();
            if (apVar != null && apVar.e()) {
                apVar.b(z);
            }
        }
    }

    public void d() {
        this.d.g();
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            ap apVar = (ap) it.next();
            if (apVar != null) {
                apVar.g();
            }
        }
    }

    public void b(boolean z) {
        this.d.c(z);
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            ap apVar = (ap) it.next();
            if (apVar != null) {
                apVar.c(z);
            }
        }
    }

    public void e() {
        this.d.a();
        this.d = null;
    }

    public void f() {
        this.d.h();
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            ap apVar = (ap) it.next();
            if (apVar != null) {
                apVar.h();
            }
        }
    }
}
