package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public final class LatLngBounds implements SafeParcelable {
    public static final zzd CREATOR = new zzd();
    private final int mVersionCode;
    public final LatLng northeast;
    public final LatLng southwest;

    /* compiled from: Unknown */
    public static final class Builder {
        private double zzaTu = Double.POSITIVE_INFINITY;
        private double zzaTv = Double.NEGATIVE_INFINITY;
        private double zzaTw = Double.NaN;
        private double zzaTx = Double.NaN;

        private boolean zzj(double d) {
            boolean z = true;
            boolean z2 = false;
            if (this.zzaTw <= this.zzaTx) {
                if (this.zzaTw > d || d > this.zzaTx) {
                    z = false;
                }
                return z;
            }
            if ((this.zzaTw <= d) || d <= this.zzaTx) {
                z2 = true;
            }
            return z2;
        }

        public LatLngBounds build() {
            boolean z = false;
            if (!Double.isNaN(this.zzaTw)) {
                z = true;
            }
            zzx.zza(z, (Object) "no included points");
            return new LatLngBounds(new LatLng(this.zzaTu, this.zzaTw), new LatLng(this.zzaTv, this.zzaTx));
        }

        public Builder include(LatLng point) {
            this.zzaTu = Math.min(this.zzaTu, point.latitude);
            this.zzaTv = Math.max(this.zzaTv, point.latitude);
            double d = point.longitude;
            if (Double.isNaN(this.zzaTw)) {
                this.zzaTw = d;
                this.zzaTx = d;
            } else if (!zzj(d)) {
                if (LatLngBounds.zzb(this.zzaTw, d) < LatLngBounds.zzc(this.zzaTx, d)) {
                    this.zzaTw = d;
                }
                this.zzaTx = d;
            }
            return this;
        }
    }

    LatLngBounds(int versionCode, LatLng southwest, LatLng northeast) {
        zzx.zzb((Object) southwest, (Object) "null southwest");
        zzx.zzb((Object) northeast, (Object) "null northeast");
        zzx.zzb(northeast.latitude >= southwest.latitude, "southern latitude exceeds northern latitude (%s > %s)", Double.valueOf(southwest.latitude), Double.valueOf(northeast.latitude));
        this.mVersionCode = versionCode;
        this.southwest = southwest;
        this.northeast = northeast;
    }

    public LatLngBounds(LatLng southwest, LatLng northeast) {
        this(1, southwest, northeast);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static double zzb(double d, double d2) {
        return ((d - d2) + 360.0d) % 360.0d;
    }

    private static double zzc(double d, double d2) {
        return ((d2 - d) + 360.0d) % 360.0d;
    }

    private boolean zzi(double d) {
        return this.southwest.latitude <= d && d <= this.northeast.latitude;
    }

    private boolean zzj(double d) {
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
        return zzi(point.latitude) && zzj(point.longitude);
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

    public LatLng getCenter() {
        double d = (this.southwest.latitude + this.northeast.latitude) / 2.0d;
        double d2 = this.northeast.longitude;
        double d3 = this.southwest.longitude;
        return new LatLng(d, d3 <= d2 ? (d2 + d3) / 2.0d : ((d2 + 360.0d) + d3) / 2.0d);
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public int hashCode() {
        return zzw.hashCode(this.southwest, this.northeast);
    }

    public LatLngBounds including(LatLng point) {
        double min = Math.min(this.southwest.latitude, point.latitude);
        double max = Math.max(this.northeast.latitude, point.latitude);
        double d = this.northeast.longitude;
        double d2 = this.southwest.longitude;
        double d3 = point.longitude;
        if (zzj(d3)) {
            d3 = d;
        } else if (zzb(d2, d3) < zzc(d, d3)) {
            d2 = d3;
            d3 = d;
        }
        return new LatLngBounds(new LatLng(min, d2), new LatLng(max, d3));
    }

    public String toString() {
        return zzw.zzy(this).zzg("southwest", this.southwest).zzg("northeast", this.northeast).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzd.zza(this, out, flags);
    }
}
