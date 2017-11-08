package com.amap.api.location;

import android.content.Context;
import com.loc.e;
import com.loc.j;

public class CoordinateConverter {
    DPoint a = null;
    private Context b;
    private CoordType c = null;
    private DPoint d = null;

    public enum CoordType {
        BAIDU,
        MAPBAR,
        MAPABC,
        SOSOMAP,
        ALIYUN,
        GOOGLE,
        GPS
    }

    public CoordinateConverter(Context context) {
        this.b = context;
    }

    public synchronized DPoint convert() throws Exception {
        DPoint dPoint;
        Object obj = 1;
        synchronized (this) {
            if (this.c == null) {
                throw new IllegalArgumentException("转换坐标类型不能为空");
            } else if (this.d != null) {
                if ((this.d.getLongitude() > 180.0d ? 1 : null) != null || this.d.getLongitude() < -180.0d) {
                    throw new IllegalArgumentException("请传入合理经度");
                }
                if (this.d.getLatitude() <= 90.0d) {
                    obj = null;
                }
                if (obj != null || this.d.getLatitude() < -90.0d) {
                    throw new IllegalArgumentException("请传入合理纬度");
                }
                switch (a.a[this.c.ordinal()]) {
                    case 1:
                        this.a = j.a(this.d);
                        break;
                    case 2:
                        this.a = j.b(this.b, this.d);
                        break;
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                        this.a = this.d;
                        break;
                    case 7:
                        this.a = j.a(this.b, this.d);
                        break;
                }
                dPoint = this.a;
            } else {
                throw new IllegalArgumentException("转换坐标源不能为空");
            }
        }
        return dPoint;
    }

    public synchronized CoordinateConverter coord(DPoint dPoint) throws Exception {
        Object obj = 1;
        synchronized (this) {
            if (dPoint != null) {
                if ((dPoint.getLongitude() > 180.0d ? 1 : null) != null || dPoint.getLongitude() < -180.0d) {
                    throw new IllegalArgumentException("请传入合理经度");
                }
                if (dPoint.getLatitude() <= 90.0d) {
                    obj = null;
                }
                if (obj != null || dPoint.getLatitude() < -90.0d) {
                    throw new IllegalArgumentException("请传入合理纬度");
                }
                this.d = dPoint;
            } else {
                throw new IllegalArgumentException("传入经纬度对象为空");
            }
        }
        return this;
    }

    public synchronized CoordinateConverter from(CoordType coordType) {
        this.c = coordType;
        return this;
    }

    public boolean isAMapDataAvailable(double d, double d2) {
        return e.a(d, d2);
    }
}
