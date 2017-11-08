package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: CameraZoomMessage */
public class ah extends af {
    public void runCameraUpdate(MapProjection mapProjection) {
        this.zoom = mapProjection.getMapZoomer() + this.d;
        this.zoom = eh.a(this.mapConfig, this.zoom);
        a(mapProjection);
    }

    public void mergeCameraUpdateDelegate(MapMessage mapMessage) {
        mapMessage.zoom += this.d;
    }
}
