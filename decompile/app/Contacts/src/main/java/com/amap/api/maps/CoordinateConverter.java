package com.amap.api.maps;

import android.content.Context;
import com.amap.api.mapcore.util.a;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.LatLng;

public class CoordinateConverter {
    private Context a;
    private CoordType b = null;
    private LatLng c = null;

    public enum CoordType {
        BAIDU,
        MAPBAR,
        GPS,
        MAPABC,
        SOSOMAP,
        ALIYUN,
        GOOGLE
    }

    public CoordinateConverter(Context context) {
        this.a = context;
    }

    public CoordinateConverter from(CoordType coordType) {
        this.b = coordType;
        return this;
    }

    public CoordinateConverter coord(LatLng latLng) {
        this.c = latLng;
        return this;
    }

    public LatLng convert() {
        LatLng latLng = null;
        if (this.b == null || this.c == null) {
            return null;
        }
        try {
            switch (a.a[this.b.ordinal()]) {
                case 1:
                    latLng = a.a(this.c);
                    break;
                case 2:
                    latLng = a.b(this.a, this.c);
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    latLng = this.c;
                    break;
                case 7:
                    latLng = a.a(this.a, this.c);
                    break;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            ce.a(th, "CoordinateConverter", "convert");
            latLng = this.c;
        }
        return latLng;
    }
}
