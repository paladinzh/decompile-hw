package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: RotateGestureMapMessage */
public class au extends ar {
    public int a = 0;
    public int b = 0;
    public float c = 0.0f;

    public au(int i, float f, int i2, int i3) {
        super(i);
        this.c = f;
        this.a = i2;
        this.b = i3;
    }

    public void runCameraUpdate(MapProjection mapProjection) {
        IPoint iPoint;
        IPoint iPoint2 = null;
        int i = this.a;
        int i2 = this.b;
        if (i <= 0 && i2 <= 0) {
            iPoint = null;
        } else {
            iPoint = new IPoint();
            iPoint2 = new IPoint();
            win2geo(mapProjection, i, i2, iPoint);
            mapProjection.setGeoCenter(iPoint.x, iPoint.y);
        }
        mapProjection.setMapAngle(this.c + mapProjection.getMapAngle());
        mapProjection.recalculate();
        if (i > 0 || i2 > 0) {
            win2geo(mapProjection, i, i2, iPoint2);
            mapProjection.setGeoCenter((iPoint.x * 2) - iPoint2.x, (iPoint.y * 2) - iPoint2.y);
            mapProjection.recalculate();
        }
    }
}
