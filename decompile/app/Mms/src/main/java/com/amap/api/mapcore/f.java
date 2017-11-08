package com.amap.api.mapcore;

/* compiled from: AMapDelegateImp */
class f implements Runnable {
    final /* synthetic */ AMapDelegateImp a;

    f(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public void run() {
        if (this.a.aj != null) {
            this.a.aT = true;
            if (this.a.al != null) {
                this.a.al.c(false);
            }
        }
    }
}
