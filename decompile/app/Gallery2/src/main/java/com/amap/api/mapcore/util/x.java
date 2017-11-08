package com.amap.api.mapcore.util;

import com.amap.api.maps.model.WeightedLatLng;
import com.autonavi.amap.mapcore.MapConfig;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: ADGLAnimationParam */
public abstract class x {
    protected int a;
    protected float b;
    protected float c;
    protected float d;
    protected boolean e;
    protected boolean f;
    protected boolean g;
    protected boolean h;

    public abstract void d();

    static float a(float f) {
        return (f * f) * 8.0f;
    }

    public void a() {
        this.e = false;
        this.f = false;
        this.a = 0;
        this.b = WMElement.CAMERASIZEVALUE1B1;
        this.e = false;
        this.f = false;
        this.g = false;
        this.h = false;
    }

    public boolean b() {
        if (!this.e) {
            d();
        }
        if (this.e && this.f) {
            return true;
        }
        return false;
    }

    public float c() {
        return this.d;
    }

    public void b(float f) {
        this.c = f;
        float f2;
        switch (this.a) {
            case 0:
                break;
            case 1:
                f = (float) Math.pow((double) f, (double) (this.b * 2.0f));
                break;
            case 2:
                if (this.b != WMElement.CAMERASIZEVALUE1B1) {
                    f = (float) (WeightedLatLng.DEFAULT_INTENSITY - Math.pow((double) (WMElement.CAMERASIZEVALUE1B1 - f), (double) (this.b * 2.0f)));
                    break;
                } else {
                    f = WMElement.CAMERASIZEVALUE1B1 - ((WMElement.CAMERASIZEVALUE1B1 - f) * (WMElement.CAMERASIZEVALUE1B1 - f));
                    break;
                }
            case 3:
                f = (float) ((Math.cos(((double) (f + WMElement.CAMERASIZEVALUE1B1)) * 3.141592653589793d) / 2.0d) + 0.5d);
                break;
            case 4:
                f2 = 1.1226f * f;
                if (f2 >= 0.3535f) {
                    if (f2 >= 0.7408f) {
                        if (f2 >= 0.9644f) {
                            f = a(f2 - 1.0435f) + 0.95f;
                            break;
                        } else {
                            f = a(f2 - 0.8526f) + 0.9f;
                            break;
                        }
                    }
                    f = a(f2 - 0.54719f) + 0.7f;
                    break;
                }
                f = a(f2);
                break;
            case 5:
                f2 = f - WMElement.CAMERASIZEVALUE1B1;
                f = (((f2 * MapConfig.MIN_ZOOM) + 2.0f) * (f2 * f2)) + WMElement.CAMERASIZEVALUE1B1;
                break;
            case 6:
                if (f >= 0.0f) {
                    if (f >= 0.25f) {
                        if (f >= 0.5f) {
                            if (f >= 0.75f) {
                                if (f > WMElement.CAMERASIZEVALUE1B1) {
                                    f = 0.0f;
                                    break;
                                } else {
                                    f = 4.0f - (4.0f * f);
                                    break;
                                }
                            }
                            f = (4.0f * f) - 2.0f;
                            break;
                        }
                        f = 2.0f - (4.0f * f);
                        break;
                    }
                    f *= 4.0f;
                    break;
                }
                f = 0.0f;
                break;
            default:
                f = 0.0f;
                break;
        }
        this.d = f;
    }

    public void a(int i, float f) {
        this.a = i;
        this.b = f;
    }

    public x() {
        this.e = false;
        this.f = false;
        this.a = 0;
        this.b = WMElement.CAMERASIZEVALUE1B1;
        this.e = false;
        this.f = false;
        this.g = false;
        this.h = false;
    }
}
