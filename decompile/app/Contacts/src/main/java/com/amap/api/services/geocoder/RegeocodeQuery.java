package com.amap.api.services.geocoder;

import com.amap.api.services.core.LatLonPoint;

public class RegeocodeQuery {
    private LatLonPoint a;
    private float b;
    private String c = GeocodeSearch.AMAP;

    public RegeocodeQuery(LatLonPoint latLonPoint, float f, String str) {
        this.a = latLonPoint;
        this.b = f;
        setLatLonType(str);
    }

    public LatLonPoint getPoint() {
        return this.a;
    }

    public void setPoint(LatLonPoint latLonPoint) {
        this.a = latLonPoint;
    }

    public float getRadius() {
        return this.b;
    }

    public void setRadius(float f) {
        this.b = f;
    }

    public String getLatLonType() {
        return this.c;
    }

    public void setLatLonType(String str) {
        if (str != null) {
            if (str.equals(GeocodeSearch.AMAP) || str.equals(GeocodeSearch.GPS)) {
                this.c = str;
            }
        }
    }

    public int hashCode() {
        int hashCode;
        int i = 0;
        if (this.c != null) {
            hashCode = this.c.hashCode();
        } else {
            hashCode = 0;
        }
        hashCode = (hashCode + 31) * 31;
        if (this.a != null) {
            i = this.a.hashCode();
        }
        return ((hashCode + i) * 31) + Float.floatToIntBits(this.b);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        RegeocodeQuery regeocodeQuery = (RegeocodeQuery) obj;
        if (this.c != null) {
            if (!this.c.equals(regeocodeQuery.c)) {
                return false;
            }
        } else if (regeocodeQuery.c != null) {
            return false;
        }
        if (this.a != null) {
            if (!this.a.equals(regeocodeQuery.a)) {
                return false;
            }
        } else if (regeocodeQuery.a != null) {
            return false;
        }
        return Float.floatToIntBits(this.b) == Float.floatToIntBits(regeocodeQuery.b);
    }
}
