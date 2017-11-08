package com.amap.api.mapcore.util;

import com.amap.api.maps.model.LatLng;

/* compiled from: GLTranslateAnimation */
public class do extends di {
    public double a = 0.0d;
    public double b = 0.0d;
    public double c = 0.0d;
    public double w = 0.0d;
    public double x = 0.0d;
    public double y = 0.0d;

    public do(LatLng latLng) {
        this.c = latLng.longitude;
        this.w = latLng.latitude;
    }

    protected void a(float f, dn dnVar) {
        this.x = this.a;
        this.y = this.b;
        if (this.a != this.c) {
            this.x = this.a + ((this.c - this.a) * ((double) f));
        }
        if (this.b != this.w) {
            this.y = this.b + ((this.w - this.b) * ((double) f));
        }
        dnVar.a = this.x;
        dnVar.b = this.y;
    }
}
