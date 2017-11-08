package com.amap.api.mapcore;

/* compiled from: AMapDelegateImp */
class i implements Runnable {
    final /* synthetic */ AMapDelegateImp a;

    i(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public synchronized void run() {
        if (this.a.bb) {
            this.a.ba = true;
            this.a.bb = false;
        }
    }
}
