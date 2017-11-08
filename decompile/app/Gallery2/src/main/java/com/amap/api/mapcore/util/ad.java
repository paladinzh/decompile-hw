package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: CameraPositionMessage */
public class ad extends af {
    public void runCameraUpdate(MapProjection mapProjection) {
        a(mapProjection);
    }

    public void mergeCameraUpdateDelegate(MapMessage mapMessage) {
        mapMessage.geoPoint = this.geoPoint != null ? this.geoPoint : mapMessage.geoPoint;
        mapMessage.zoom = !Float.isNaN(this.zoom) ? this.zoom : mapMessage.zoom;
        mapMessage.bearing = !Float.isNaN(this.bearing) ? this.bearing : mapMessage.bearing;
        mapMessage.tilt = !Float.isNaN(this.tilt) ? this.tilt : mapMessage.tilt;
    }
}
