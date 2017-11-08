package com.amap.api.mapcore;

import com.amap.api.mapcore.util.ce;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

/* compiled from: GLOverlayLayer */
class x implements Runnable {
    final /* synthetic */ w a;

    x(w wVar) {
        this.a = wVar;
    }

    public synchronized void run() {
        try {
            synchronized (this.a) {
                Collection arrayList = new ArrayList(this.a.d);
                Collections.sort(arrayList, this.a.b);
                this.a.d = new CopyOnWriteArrayList(arrayList);
            }
        } catch (Throwable th) {
            ce.a(th, "MapOverlayImageView", "changeOverlayIndex");
        }
    }
}
