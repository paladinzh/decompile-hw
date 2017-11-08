package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: ScaleGestureMapMessage */
public class av extends ar {
    public float a = 0.0f;
    public int b = 0;
    public int c = 0;

    public av(int i, float f, int i2, int i3) {
        super(i);
        this.a = f;
        this.b = i2;
        this.c = i3;
    }

    public void runCameraUpdate(MapProjection mapProjection) {
        IPoint iPoint;
        IPoint iPoint2 = null;
        int i = this.b;
        int i2 = this.c;
        if (i <= 0 && i2 <= 0) {
            iPoint = null;
        } else {
            iPoint = new IPoint();
            iPoint2 = new IPoint();
            win2geo(mapProjection, i, i2, iPoint);
            mapProjection.setGeoCenter(iPoint.x, iPoint.y);
        }
        mapProjection.setMapZoomer(this.a + mapProjection.getMapZoomer());
        mapProjection.recalculate();
        if (i > 0 || i2 > 0) {
            win2geo(mapProjection, i, i2, iPoint2);
            mapProjection.setGeoCenter((iPoint.x * 2) - iPoint2.x, (iPoint.y * 2) - iPoint2.y);
            mapProjection.recalculate();
        }
    }
}
