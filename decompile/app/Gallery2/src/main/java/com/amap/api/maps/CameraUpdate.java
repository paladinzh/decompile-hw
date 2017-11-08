package com.amap.api.maps;

import com.autonavi.amap.mapcore.MapMessage;

public final class CameraUpdate {
    MapMessage a;

    CameraUpdate(MapMessage mapMessage) {
        this.a = mapMessage;
    }

    public MapMessage getCameraUpdateFactoryDelegate() {
        return this.a;
    }
}
