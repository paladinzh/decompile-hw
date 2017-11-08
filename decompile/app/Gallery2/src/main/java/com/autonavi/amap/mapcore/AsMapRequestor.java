package com.autonavi.amap.mapcore;

/* compiled from: ConnectionManager */
class AsMapRequestor implements Runnable {
    public BaseMapLoader mMapLoader = null;

    public AsMapRequestor(BaseMapLoader baseMapLoader) {
        this.mMapLoader = baseMapLoader;
    }

    public void run() {
        try {
            this.mMapLoader.doRequest();
        } catch (Throwable th) {
        }
    }
}
