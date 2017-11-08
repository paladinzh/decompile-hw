package com.amap.api.mapcore.util;

import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: CameraScrollMessage */
public class ae extends af {
    public void runCameraUpdate(MapProjection mapProjection) {
        float f = this.b;
        f += ((float) this.width) / 2.0f;
        float f2 = this.c + (((float) this.height) / 2.0f);
        IPoint iPoint = new IPoint();
        a(mapProjection, (int) f, (int) f2, iPoint);
        mapProjection.setGeoCenter(iPoint.x, iPoint.y);
    }

    public void a(MapProjection mapProjection, int i, int i2, IPoint iPoint) {
        FPoint fPoint = new FPoint();
        mapProjection.win2Map(i, i2, fPoint);
        mapProjection.map2Geo(fPoint.x, fPoint.y, iPoint);
    }

    public void mergeCameraUpdateDelegate(MapMessage mapMessage) {
    }
}
