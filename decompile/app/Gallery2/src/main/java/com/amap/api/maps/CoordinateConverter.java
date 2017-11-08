package com.amap.api.maps;

import android.content.Context;
import com.amap.api.mapcore.util.ai;
import com.amap.api.mapcore.util.fo;
import com.amap.api.maps.model.LatLng;

public class CoordinateConverter {
    private Context a;
    private CoordType b = null;
    private LatLng c = null;

    /* renamed from: com.amap.api.maps.CoordinateConverter$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = new int[CoordType.values().length];

        static {
            try {
                a[CoordType.BAIDU.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                a[CoordType.MAPBAR.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                a[CoordType.MAPABC.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                a[CoordType.SOSOMAP.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                a[CoordType.ALIYUN.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                a[CoordType.GOOGLE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                a[CoordType.GPS.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
        }
    }

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
            switch (AnonymousClass1.a[this.b.ordinal()]) {
                case 1:
                    latLng = ai.a(this.c);
                    break;
                case 2:
                    latLng = ai.b(this.a, this.c);
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                    latLng = this.c;
                    break;
                case 7:
                    latLng = ai.a(this.a, this.c);
                    break;
            }
        } catch (Throwable th) {
            th.printStackTrace();
            fo.b(th, "CoordinateConverter", "convert");
            latLng = this.c;
        }
        return latLng;
    }
}
