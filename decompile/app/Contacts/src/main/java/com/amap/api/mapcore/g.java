package com.amap.api.mapcore;

/* compiled from: AMapDelegateImp */
class g implements Runnable {
    final /* synthetic */ AMapDelegateImp a;

    g(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public void run() {
        if (this.a.aj != null) {
            this.a.aT = false;
            this.a.aj.setVisibility(8);
        }
    }
}
