package com.amap.api.mapcore.util;

import android.util.Pair;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapMessage;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: CameraBoundsMessage */
public class ac extends af {
    public void runCameraUpdate(MapProjection mapProjection) {
        Pair a = eh.a((af) this, mapProjection, this.mapConfig);
        mapProjection.setMapZoomer(((Float) a.first).floatValue());
        mapProjection.setGeoCenter(((IPoint) a.second).x, ((IPoint) a.second).y);
        mapProjection.setCameraHeaderAngle(0.0f);
        mapProjection.setMapAngle(0.0f);
    }

    public void mergeCameraUpdateDelegate(MapMessage mapMessage) {
    }
}
