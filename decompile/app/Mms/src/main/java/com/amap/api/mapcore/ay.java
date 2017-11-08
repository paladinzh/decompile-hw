package com.amap.api.mapcore;

import com.amap.api.mapcore.util.ce;

/* compiled from: MapOverlayImageView */
class ay implements Runnable {
    final /* synthetic */ aw a;

    ay(aw awVar) {
        this.a = awVar;
    }

    public void run() {
        try {
            this.a.a.q();
        } catch (Throwable th) {
            ce.a(th, "MapOverlayImageView", "redrawInfoWindow post");
            th.printStackTrace();
        }
    }
}
