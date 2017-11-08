package com.amap.api.mapcore.util;

import android.content.Context;
import android.text.TextUtils;
import com.amap.api.maps.model.LatLng;
import com.autonavi.amap.mapcore.CoordUtil;
import com.autonavi.amap.mapcore.DPoint;
import java.io.File;
import java.math.BigDecimal;

/* compiled from: OffsetUtil */
public class a {
    static double a = 3.141592653589793d;
    private static boolean b = false;

    public static LatLng a(Context context, LatLng latLng) {
        if (context == null) {
            return null;
        }
        String a = bu.a(context, "libwgs2gcj.so");
        if (!(TextUtils.isEmpty(a) || !new File(a).exists() || b)) {
            try {
                System.load(a);
                b = true;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        DPoint a2 = a(new DPoint(latLng.longitude, latLng.latitude), b);
        return new LatLng(a2.y, a2.x, false);
    }

    private static DPoint a(DPoint dPoint, boolean z) {
        double[] dArr;
        try {
            dArr = new double[2];
            if (z) {
                if (CoordUtil.convertToGcj(new double[]{dPoint.x, dPoint.y}, dArr) != 0) {
                    dArr = dq.a(dPoint.x, dPoint.y);
                }
            } else {
                dArr = dq.a(dPoint.x, dPoint.y);
            }
        } catch (Throwable th) {
            return dPoint;
        }
        return new DPoint(dArr[0], dArr[1]);
    }

    public static LatLng b(Context context, LatLng latLng) {
        try {
            DPoint c = c(latLng.longitude, latLng.latitude);
            return a(context, new LatLng(c.y, c.x, false));
        } catch (Throwable th) {
            th.printStackTrace();
            return latLng;
        }
    }

    public static double a(double d, double d2) {
        return (Math.cos(d2 / 100000.0d) * (d / 18000.0d)) + (Math.sin(d / 100000.0d) * (d2 / 9000.0d));
    }

    public static double b(double d, double d2) {
        return (Math.sin(d2 / 100000.0d) * (d / 18000.0d)) + (Math.cos(d / 100000.0d) * (d2 / 9000.0d));
    }

    private static DPoint c(double d, double d2) {
        int i;
        double d3 = (double) (((long) (100000.0d * d)) % 36000000);
        double d4 = (double) (((long) (100000.0d * d2)) % 36000000);
        int i2 = (int) ((-b(d3, d4)) + d4);
        int i3 = (int) (((double) (d3 > 0.0d ? 1 : -1)) + ((-a((double) ((int) ((-a(d3, d4)) + d3)), (double) i2)) + d3));
        double d5 = (-b((double) i3, (double) i2)) + d4;
        if (d4 > 0.0d) {
            i = 1;
        } else {
            i = -1;
        }
        return new DPoint(((double) i3) / 100000.0d, ((double) ((int) (((double) i) + d5))) / 100000.0d);
    }

    public static LatLng a(LatLng latLng) {
        if (latLng != null) {
            try {
                DPoint a = a(new DPoint(latLng.longitude, latLng.latitude), 2);
                return new LatLng(a.y, a.x, false);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }
        return latLng;
    }

    private static double a(double d) {
        return Math.sin((3000.0d * d) * (a / 180.0d)) * 2.0E-5d;
    }

    private static double b(double d) {
        return Math.cos((3000.0d * d) * (a / 180.0d)) * 3.0E-6d;
    }

    private static DPoint d(double d, double d2) {
        DPoint dPoint = new DPoint();
        double sin = (Math.sin(b(d) + Math.atan2(d2, d)) * (a(d2) + Math.sqrt((d * d) + (d2 * d2)))) + 0.006d;
        dPoint.x = a((Math.cos(b(d) + Math.atan2(d2, d)) * (a(d2) + Math.sqrt((d * d) + (d2 * d2)))) + 0.0065d, 8);
        dPoint.y = a(sin, 8);
        return dPoint;
    }

    private static double a(double d, int i) {
        return new BigDecimal(d).setScale(i, 4).doubleValue();
    }

    private static DPoint a(DPoint dPoint, int i) {
        double d = 0.006401062d;
        double d2 = 0.0060424805d;
        int i2 = 0;
        DPoint dPoint2 = null;
        while (i2 < i) {
            DPoint a = a(dPoint.x, dPoint.y, d, d2);
            d = dPoint.x - a.x;
            d2 = dPoint.y - a.y;
            i2++;
            dPoint2 = a;
        }
        return dPoint2;
    }

    private static DPoint a(double d, double d2, double d3, double d4) {
        DPoint dPoint = new DPoint();
        double d5 = d - d3;
        double d6 = d2 - d4;
        DPoint d7 = d(d5, d6);
        dPoint.x = a((d5 + d) - d7.x, 8);
        dPoint.y = a((d2 + d6) - d7.y, 8);
        return dPoint;
    }
}
