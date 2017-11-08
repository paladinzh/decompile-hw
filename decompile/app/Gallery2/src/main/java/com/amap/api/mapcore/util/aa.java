package com.amap.api.mapcore.util;

import android.os.SystemClock;
import com.autonavi.amap.mapcore.ADGLAnimation;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: ADGLMapAnimFling */
public class aa extends ADGLAnimation {
    private float a;
    private float b;
    private IPoint c;
    private z d = null;
    private boolean e;
    private boolean f;
    private int g;
    private int h;
    private int i;
    private int j;

    public aa(int i, int i2, int i3) {
        this.g = i2;
        this.h = i3;
        this.i = i2;
        this.j = i3;
        a();
        this._duration = i;
    }

    public void a() {
        if (this.d != null) {
            this.d.a();
        }
        this.a = 0.0f;
        this.b = 0.0f;
        this.f = false;
        this.e = false;
    }

    public void a(float f, float f2) {
        this.d = null;
        this.a = f;
        this.b = f2;
        this.d = new z();
        this.d.a(2, 1.2f);
        this.f = false;
        this.e = false;
    }

    public void a(Object obj) {
        MapProjection mapProjection = (MapProjection) obj;
        if (mapProjection != null) {
            this.e = false;
            this._isOver = true;
            int i = (int) ((this.a * ((float) this._duration)) / 2000.0f);
            int i2 = (int) ((this.b * ((float) this._duration)) / 2000.0f);
            if (!(Math.abs(i) == 0 || Math.abs(i2) == 0)) {
                if (this.c == null) {
                    this.c = new IPoint();
                }
                mapProjection.getGeoCenter(this.c);
                this._isOver = false;
                this.d.a((float) this.g, (float) this.h);
                this.d.b((float) (this.g - i), (float) (this.h - i2));
                this.f = this.d.b();
            }
            if (this.f) {
                this.e = true;
                this._startTime = SystemClock.uptimeMillis();
            } else {
                this.e = true;
                this._startTime = SystemClock.uptimeMillis();
            }
        }
    }

    public void doAnimation(Object obj) {
        MapProjection mapProjection = (MapProjection) obj;
        if (mapProjection != null) {
            if (!this.e) {
                a(obj);
            }
            if (!this._isOver) {
                this._offsetTime = SystemClock.uptimeMillis() - this._startTime;
                float f = ((float) this._offsetTime) / ((float) this._duration);
                if (f > WMElement.CAMERASIZEVALUE1B1) {
                    this._isOver = true;
                    f = WMElement.CAMERASIZEVALUE1B1;
                }
                if (f >= 0.0f && f <= WMElement.CAMERASIZEVALUE1B1 && this.f) {
                    this.d.b(f);
                    int i = (int) this.d.i();
                    int j = (int) this.d.j();
                    FPoint fPoint = new FPoint();
                    mapProjection.win2Map((this.g + i) - this.i, (this.h + j) - this.j, fPoint);
                    mapProjection.setMapCenter(fPoint.x, fPoint.y);
                    this.i = i;
                    this.j = j;
                }
            }
        }
    }
}
