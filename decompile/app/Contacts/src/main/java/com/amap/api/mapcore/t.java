package com.amap.api.mapcore;

import com.amap.api.mapcore.util.ce;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: CustomGLOverlayLayer */
class t {
    a a = new a();
    private CopyOnWriteArrayList<v> b = new CopyOnWriteArrayList();

    /* compiled from: CustomGLOverlayLayer */
    static class a implements Comparator<Object>, Serializable {
        a() {
        }

        public int compare(Object obj, Object obj2) {
            v vVar = (v) obj;
            v vVar2 = (v) obj2;
            if (!(vVar == null || vVar2 == null)) {
                try {
                    if (vVar.getZIndex() > vVar2.getZIndex()) {
                        return 1;
                    }
                    if (vVar.getZIndex() < vVar2.getZIndex()) {
                        return -1;
                    }
                } catch (Throwable th) {
                    ce.a(th, "CustomGLOverlayLayer", "compare");
                    th.printStackTrace();
                }
            }
            return 0;
        }
    }

    t() {
    }

    public boolean a(v vVar) {
        if (this.b.contains(vVar)) {
            return this.b.remove(vVar);
        }
        return false;
    }

    public void a(GL10 gl10) {
        Iterator it = this.b.iterator();
        while (it.hasNext()) {
            ((v) it.next()).onDrawFrame(gl10);
        }
    }
}
