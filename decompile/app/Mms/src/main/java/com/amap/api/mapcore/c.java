package com.amap.api.mapcore;

import com.amap.api.mapcore.util.bm;
import com.amap.api.mapcore.util.bp;
import com.amap.api.mapcore.util.bu;
import com.amap.api.mapcore.util.bv;
import com.amap.api.mapcore.util.bv.a;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.MapsInitializer;

/* compiled from: AMapDelegateImp */
class c extends Thread {
    final /* synthetic */ AMapDelegateImp a;

    c(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public void run() {
        while (!MapsInitializer.getNetWorkEnable()) {
            try {
                Thread.sleep(5000);
            } catch (Throwable th) {
                interrupt();
                ce.a(th, "AMapDelegateImpGLSurfaceView", "mVerfy");
                th.printStackTrace();
                return;
            }
        }
        bv a = new a(s.b, "3.3.0", s.d).a(new String[]{"com.amap.api.maps", "com.amap.api.mapcore", "com.autonavi.amap.mapcore"}).a();
        bm.b(this.a.H, a);
        if (bm.a == 0) {
            this.a.l.sendEmptyMessage(2);
        }
        bp.a a2 = bp.a(this.a.H, a, "common;exception;sdkcoordinate");
        if (a2 != null) {
            if (a2.g != null) {
                a.a(a2.g.a);
            }
            if (a2.i != null) {
                new bu(this.a.H, s.b, a2.i.a, a2.i.b).a();
            }
        }
        s.h = a;
        ce.a(this.a.H, a);
        interrupt();
        this.a.f(false);
    }
}
