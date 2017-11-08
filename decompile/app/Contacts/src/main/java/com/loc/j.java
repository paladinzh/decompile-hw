package com.loc;

import android.content.Context;
import android.text.TextUtils;
import com.amap.api.location.CoordUtil;
import com.amap.api.location.DPoint;
import java.io.File;
import java.math.BigDecimal;

/* compiled from: OffsetUtil */
public class j {
    static double a = 3.141592653589793d;
    private static boolean b = false;

    private static double a(double d) {
        return Math.sin((3000.0d * d) * (a / 180.0d)) * 2.0E-5d;
    }

    public static double a(double d, double d2) {
        return (Math.cos(d2 / 100000.0d) * (d / 18000.0d)) + (Math.sin(d / 100000.0d) * (d2 / 9000.0d));
    }

    private static double a(double d, int i) {
        return new BigDecimal(d).setScale(i, 4).doubleValue();
    }

    private static DPoint a(double d, double d2, double d3, double d4) {
        DPoint dPoint = new DPoint();
        double d5 = d - d3;
        double d6 = d2 - d4;
        DPoint d7 = d(d5, d6);
        dPoint.setLongitude(a((d5 + d) - d7.getLongitude(), 8));
        dPoint.setLatitude(a((d2 + d6) - d7.getLatitude(), 8));
        return dPoint;
    }

    public static DPoint a(Context context, double d, double d2) {
        return context != null ? a(context, new DPoint(d2, d)) : null;
    }

    public static DPoint a(Context context, DPoint dPoint) {
        if (context == null) {
            return null;
        }
        String a = u.a(context, "libwgs2gcj.so");
        if (!(TextUtils.isEmpty(a) || !new File(a).exists() || b)) {
            try {
                System.load(a);
                b = true;
            } catch (Throwable th) {
                e.a(th, "OffsetUtil", "offset");
            }
        }
        return a(dPoint, b);
    }

    public static DPoint a(DPoint dPoint) {
        if (dPoint != null) {
            try {
                return a(dPoint, 2);
            } catch (Throwable th) {
                e.a(th, "OffsetUtil", "B2G");
            }
        }
        return dPoint;
    }

    private static DPoint a(DPoint dPoint, int i) {
        double d = 0.006401062d;
        double d2 = 0.0060424805d;
        int i2 = 0;
        DPoint dPoint2 = null;
        while (i2 < i) {
            DPoint a = a(dPoint.getLongitude(), dPoint.getLatitude(), d, d2);
            d = dPoint.getLongitude() - a.getLongitude();
            d2 = dPoint.getLatitude() - a.getLatitude();
            i2++;
            dPoint2 = a;
        }
        return dPoint2;
    }

    private static DPoint a(DPoint dPoint, boolean z) {
        double[] dArr;
        try {
            dArr = new double[2];
            if (z) {
                if (CoordUtil.convertToGcj(new double[]{dPoint.getLongitude(), dPoint.getLatitude()}, dArr) != 0) {
                    dArr = cx.a(dPoint.getLongitude(), dPoint.getLatitude());
                }
            } else {
                dArr = cx.a(dPoint.getLongitude(), dPoint.getLatitude());
            }
        } catch (Throwable th) {
            e.a(th, "OffsetUtil", "cover part2");
            return dPoint;
        }
        return new DPoint(dArr[1], dArr[0]);
    }

    private static double b(double d) {
        return Math.cos((3000.0d * d) * (a / 180.0d)) * 3.0E-6d;
    }

    public static double b(double d, double d2) {
        return (Math.sin(d2 / 100000.0d) * (d / 18000.0d)) + (Math.cos(d / 100000.0d) * (d2 / 9000.0d));
    }

    public static DPoint b(Context context, DPoint dPoint) {
        try {
            return a(context, c(dPoint.getLongitude(), dPoint.getLatitude()));
        } catch (Throwable th) {
            e.a(th, "OffsetUtil", "marbar2G");
            return dPoint;
        }
    }

    private static DPoint c(double d, double d2) {
        double d3 = (double) (((long) (100000.0d * d)) % 36000000);
        double d4 = (double) (((long) (100000.0d * d2)) % 36000000);
        int i = (int) ((-b(d3, d4)) + d4);
        int i2 = (int) (((double) (d3 > 0.0d ? 1 : -1)) + ((-a((double) ((int) ((-a(d3, d4)) + d3)), (double) i)) + d3));
        return new DPoint(((double) ((int) (((double) (d4 > 0.0d ? 1 : -1)) + ((-b((double) i2, (double) i)) + d4)))) / 100000.0d, ((double) i2) / 100000.0d);
    }

    private static DPoint d(double d, double d2) {
        DPoint dPoint = new DPoint();
        double sin = (Math.sin(b(d) + Math.atan2(d2, d)) * (a(d2) + Math.sqrt((d * d) + (d2 * d2)))) + 0.006d;
        dPoint.setLongitude(a((Math.cos(b(d) + Math.atan2(d2, d)) * (a(d2) + Math.sqrt((d * d) + (d2 * d2)))) + 0.0065d, 8));
        dPoint.setLatitude(a(sin, 8));
        return dPoint;
    }
}
