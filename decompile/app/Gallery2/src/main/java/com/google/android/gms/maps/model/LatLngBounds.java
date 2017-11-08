package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.internal.ep;
import com.google.android.gms.internal.er;
import com.google.android.gms.maps.internal.r;

/* compiled from: Unknown */
public final class LatLngBounds implements SafeParcelable {
    public static final LatLngBoundsCreator CREATOR = new LatLngBoundsCreator();
    public final LatLng northeast;
    public final LatLng southwest;
    private final int wj;

    /* compiled from: Unknown */
    public static final class Builder {
        private double Qa = Double.POSITIVE_INFINITY;
        private double Qb = Double.NEGATIVE_INFINITY;
        private double Qc = Double.NaN;
        private double Qd = Double.NaN;

        private boolean d(double d) {
            boolean z = true;
            boolean z2 = false;
            if (this.Qc <= this.Qd) {
                if (this.Qc > d || d > this.Qd) {
                    z = false;
                }
                return z;
            }
            if ((this.Qc <= d) || d <= this.Qd) {
                z2 = true;
            }
            return z2;
        }

        public LatLngBounds build() {
            boolean z = false;
            if (!Double.isNaN(this.Qc)) {
                z = true;
            }
            er.a(z, "no included points");
            return new LatLngBounds(new LatLng(this.Qa, this.Qc), new LatLng(this.Qb, this.Qd));
        }

        public Builder include(LatLng point) {
            this.Qa = Math.min(this.Qa, point.latitude);
            this.Qb = Math.max(this.Qb, point.latitude);
            double d = point.longitude;
            if (Double.isNaN(this.Qc)) {
                this.Qc = d;
                this.Qd = d;
            } else if (!d(d)) {
                if (LatLngBounds.b(this.Qc, d) < LatLngBounds.c(this.Qd, d)) {
                    this.Qc = d;
                }
                this.Qd = d;
            }
            return this;
        }
    }

    LatLngBounds(int versionCode, LatLng southwest, LatLng northeast) {
        er.b((Object) southwest, (Object) "null southwest");
        er.b((Object) northeast, (Object) "null northeast");
        er.a(northeast.latitude >= southwest.latitude, "southern latitude exceeds northern latitude (%s > %s)", Double.valueOf(southwest.latitude), Double.valueOf(northeast.latitude));
        this.wj = versionCode;
        this.southwest = southwest;
        this.northeast = northeast;
    }

    public LatLngBounds(LatLng southwest, LatLng northeast) {
        this(1, southwest, northeast);
    }

    private static double b(double d, double d2) {
        return ((d - d2) + 360.0d) % 360.0d;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static double c(double d, double d2) {
        return ((d2 - d) + 360.0d) % 360.0d;
    }

    private boolean c(double d) {
        return this.southwest.latitude <= d && d <= this.northeast.latitude;
    }

    private boolean d(double d) {
        boolean z = true;
        boolean z2 = false;
        if (this.southwest.longitude <= this.northeast.longitude) {
            if (this.southwest.longitude > d || d > this.northeast.longitude) {
                z = false;
            }
            return z;
        }
        if ((this.southwest.longitude <= d) || d <= this.northeast.longitude) {
            z2 = true;
        }
        return z2;
    }

    public boolean contains(LatLng point) {
        return c(point.latitude) && d(point.longitude);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof LatLngBounds)) {
            return false;
        }
        LatLngBounds latLngBounds = (LatLngBounds) o;
        if (this.southwest.equals(latLngBounds.southwest)) {
            if (!this.northeast.equals(latLngBounds.northeast)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    int getVersionCode() {
        return this.wj;
    }

    public int hashCode() {
        return ep.hashCode(this.southwest, this.northeast);
    }

    public String toString() {
        return ep.e(this).a("southwest", this.southwest).a("northeast", this.northeast).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            d.a(this, out, flags);
        } else {
            LatLngBoundsCreator.a(this, out, flags);
        }
    }
}
