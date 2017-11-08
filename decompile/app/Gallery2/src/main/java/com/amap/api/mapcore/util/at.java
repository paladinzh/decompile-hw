package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: MoveGestureMapMessage */
public class at extends ar {
    public float a = 0.0f;
    public float b = 0.0f;

    public at(int i, float f, float f2) {
        super(i);
        this.a = f;
        this.b = f2;
    }

    public void runCameraUpdate(MapProjection mapProjection) {
        float f = (float) (this.width >> 1);
        int i = (int) (f - ((float) ((int) this.a)));
        int i2 = (int) (((float) (this.height >> 1)) - ((float) ((int) this.b)));
        FPoint fPoint = new FPoint();
        mapProjection.win2Map(i, i2, fPoint);
        mapProjection.setMapCenter(fPoint.x, fPoint.y);
        mapProjection.recalculate();
    }
}
