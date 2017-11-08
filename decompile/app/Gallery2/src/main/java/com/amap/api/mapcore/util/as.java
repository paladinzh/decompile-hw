package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: HoverGestureMapMessage */
public class as extends ar {
    public float a = 0.0f;

    public as(int i, float f) {
        super(i);
        this.a = f;
    }

    public void runCameraUpdate(MapProjection mapProjection) {
        float f = 0.0f;
        float cameraHeaderAngle = mapProjection.getCameraHeaderAngle() + this.a;
        if (cameraHeaderAngle >= 0.0f) {
            if (cameraHeaderAngle > 65.0f) {
                f = 65.0f;
            } else if (mapProjection.getCameraHeaderAngle() <= 40.0f || cameraHeaderAngle <= 40.0f) {
                f = cameraHeaderAngle;
            } else if (mapProjection.getCameraHeaderAngle() > cameraHeaderAngle) {
                f = 40.0f;
            } else {
                f = cameraHeaderAngle;
            }
        }
        mapProjection.setCameraHeaderAngle(f);
        mapProjection.recalculate();
    }
}
