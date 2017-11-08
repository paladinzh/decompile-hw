package com.amap.api.mapcore;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.LatLng;
import com.autonavi.amap.mapcore.VTMCDataCache;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: GLOverlayLayer */
class w {
    private static int c = 0;
    ab a;
    a b = new a();
    private CopyOnWriteArrayList<aj> d = new CopyOnWriteArrayList(new ArrayList(VTMCDataCache.MAXSIZE));
    private CopyOnWriteArrayList<Integer> e = new CopyOnWriteArrayList();
    private Handler f = new Handler();
    private Runnable g = new x(this);

    /* compiled from: GLOverlayLayer */
    static class a implements Comparator<Object>, Serializable {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            aj ajVar = (aj) obj;
            aj ajVar2 = (aj) obj2;
            if (!(ajVar == null || ajVar2 == null)) {
                try {
                    if (ajVar.d() > ajVar2.d()) {
                        return 1;
                    }
                    if (ajVar.d() < ajVar2.d()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    ce.a(th, "GLOverlayLayer", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    static String a(String str) {
        c++;
        return str + c;
    }

    public w(ab abVar) {
        this.a = abVar;
    }

    public synchronized void b(String str) {
        if (str != null) {
            if (str.trim().length() != 0) {
                Iterator it = this.d.iterator();
                while (it.hasNext()) {
                    aj ajVar = (aj) it.next();
                    if (!str.equals(ajVar.c())) {
                        this.d.remove(ajVar);
                    }
                }
            }
        }
        try {
            this.d.clear();
            c = 0;
        } catch (Throwable th) {
            ce.a(th, "GLOverlayLayer", "clear");
            th.printStackTrace();
            Log.d("amapApi", "GLOverlayLayer clear erro" + th.getMessage());
        }
    }

    public synchronized void a() {
        try {
            Iterator it = this.d.iterator();
            while (it.hasNext()) {
                ((aj) it.next()).j();
            }
            b(null);
        } catch (Throwable th) {
            ce.a(th, "GLOverlayLayer", "destory");
            th.printStackTrace();
            Log.d("amapApi", "GLOverlayLayer destory erro" + th.getMessage());
        }
    }

    private synchronized aj d(String str) throws RemoteException {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            aj ajVar = (aj) it.next();
            if (ajVar != null && ajVar.c().equals(str)) {
                return ajVar;
            }
        }
        return null;
    }

    public synchronized void a(aj ajVar) throws RemoteException {
        this.d.add(ajVar);
        b();
    }

    public synchronized boolean c(String str) throws RemoteException {
        aj d = d(str);
        if (d == null) {
            return false;
        }
        return this.d.remove(d);
    }

    protected synchronized void b() {
        this.f.removeCallbacks(this.g);
        this.f.postDelayed(this.g, 10);
    }

    public void a(GL10 gl10, boolean z, int i) {
        Iterator it = this.e.iterator();
        while (it.hasNext()) {
            gl10.glDeleteTextures(1, new int[]{((Integer) it.next()).intValue()}, 0);
            this.a.f(r0.intValue());
        }
        this.e.clear();
        int size = this.d.size();
        Iterator it2 = this.d.iterator();
        while (it2.hasNext()) {
            aj ajVar = (aj) it2.next();
            try {
                if (ajVar.e()) {
                    if (size <= 20) {
                        if (z) {
                            if (ajVar.d() <= ((float) i)) {
                                ajVar.a(gl10);
                            }
                        } else if (ajVar.d() > ((float) i)) {
                            ajVar.a(gl10);
                        }
                    } else if (ajVar.a()) {
                        if (z) {
                            if (ajVar.d() <= ((float) i)) {
                                ajVar.a(gl10);
                            }
                        } else if (ajVar.d() > ((float) i)) {
                            ajVar.a(gl10);
                        }
                    }
                }
            } catch (Throwable e) {
                ce.a(e, "GLOverlayLayer", "draw");
                e.printStackTrace();
            }
        }
    }

    public void a(Integer num) {
        if (num.intValue() != 0) {
            this.e.add(num);
        }
    }

    public synchronized void c() {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            aj ajVar = (aj) it.next();
            if (ajVar != null) {
                try {
                    ajVar.g();
                } catch (Throwable e) {
                    ce.a(e, "GLOverlayLayer", "calMapFPoint");
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean d() {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            aj ajVar = (aj) it.next();
            if (ajVar != null && !ajVar.k()) {
                return false;
            }
        }
        return true;
    }

    public synchronized aj a(LatLng latLng) {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            aj ajVar = (aj) it.next();
            if (ajVar != null && ajVar.k() && (ajVar instanceof al) && ((al) ajVar).b(latLng)) {
                return ajVar;
            }
        }
        return null;
    }

    public void e() {
        Iterator it = this.d.iterator();
        while (it.hasNext()) {
            aj ajVar = (aj) it.next();
            if (ajVar != null) {
                if (ajVar instanceof al) {
                    ((al) ajVar).o();
                } else if (ajVar instanceof af) {
                    ((af) ajVar).p();
                }
            }
        }
    }
}
