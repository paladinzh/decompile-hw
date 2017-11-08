package com.amap.api.maps.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import com.amap.api.mapcore.util.eh;

public final class LatLngBounds implements Parcelable {
    public static final LatLngBoundsCreator CREATOR = new LatLngBoundsCreator();
    private final int a;
    private boolean b;
    public final LatLng northeast;
    public final LatLng southwest;

    public static final class Builder {
        private double a = Double.POSITIVE_INFINITY;
        private double b = Double.NEGATIVE_INFINITY;
        private double c = Double.NaN;
        private double d = Double.NaN;

        public Builder include(LatLng latLng) {
            if (latLng == null) {
                return this;
            }
            this.a = Math.min(this.a, latLng.latitude);
            this.b = Math.max(this.b, latLng.latitude);
            double d = latLng.longitude;
            if (Double.isNaN(this.c)) {
                this.c = d;
                this.d = d;
            } else if (!a(d)) {
                if (LatLngBounds.c(this.c, d) < LatLngBounds.d(this.d, d)) {
                    this.c = d;
                } else {
                    this.d = d;
                }
            }
            return this;
        }

        private boolean a(double d) {
            boolean z = true;
            boolean z2 = false;
            if (this.c <= this.d) {
                if (this.c > d || d > this.d) {
                    z = false;
                }
                return z;
            }
            if ((this.c <= d) || d <= this.d) {
                z2 = true;
            }
            return z2;
        }

        public LatLngBounds build() {
            if (!Double.isNaN(this.c)) {
                return new LatLngBounds(new LatLng(this.a, this.c, false), new LatLng(this.b, this.d, false));
            }
            Log.w("LatLngBounds", "no included points");
            return null;
        }
    }

    LatLngBounds(int i, LatLng latLng, LatLng latLng2) {
        this.b = true;
        if (latLng == null) {
            Log.w("LatLngBounds", "null southwest");
            this.b = false;
        }
        if (latLng2 == null) {
            Log.w("LatLngBounds", "null northeast");
            this.b = false;
        }
        if (latLng2.latitude <= latLng.latitude) {
            Log.w("LatLngBounds", "southern latitude exceeds northern latitude (" + latLng.latitude + " > " + latLng2.latitude + ")");
            this.b = false;
        }
        this.a = i;
        this.southwest = latLng;
        this.northeast = latLng2;
    }

    public LatLngBounds(LatLng latLng, LatLng latLng2) {
        this(1, latLng, latLng2);
    }

    int a() {
        return this.a;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean contains(LatLng latLng) {
        boolean z = false;
        if (latLng == null) {
            return false;
        }
        if (this.b) {
            if (a(latLng.latitude) && b(latLng.longitude)) {
                z = true;
            }
            return z;
        }
        Log.w("LatLngBounds", "this LatLngBounds is invalid!");
        return false;
    }

    public boolean contains(LatLngBounds latLngBounds) {
        boolean z = false;
        if (latLngBounds == null) {
            return false;
        }
        if (this.b) {
            if (contains(latLngBounds.southwest) && contains(latLngBounds.northeast)) {
                z = true;
            }
            return z;
        }
        Log.w("LatLngBounds", "this LatLngBounds is invalid!");
        return false;
    }

    public boolean intersects(LatLngBounds latLngBounds) {
        boolean z = false;
        if (latLngBounds == null) {
            return false;
        }
        if (this.b) {
            if (a(latLngBounds) || latLngBounds.a(this)) {
                z = true;
            }
            return z;
        }
        Log.w("LatLngBounds", "this LatLngBounds is invalid!");
        return false;
    }

    private boolean a(LatLngBounds latLngBounds) {
        boolean z = false;
        if (latLngBounds == null || latLngBounds.northeast == null || latLngBounds.southwest == null || this.northeast == null || this.southwest == null) {
            return false;
        }
        if (this.b) {
            double d = ((latLngBounds.northeast.latitude + latLngBounds.southwest.latitude) - this.northeast.latitude) - this.southwest.latitude;
            double d2 = ((this.northeast.latitude - this.southwest.latitude) + latLngBounds.northeast.latitude) - latLngBounds.southwest.latitude;
            if (Math.abs(((latLngBounds.northeast.longitude + latLngBounds.southwest.longitude) - this.northeast.longitude) - this.southwest.longitude) < ((this.northeast.longitude - this.southwest.longitude) + latLngBounds.northeast.longitude) - this.southwest.longitude && Math.abs(d) < d2) {
                z = true;
            }
            return z;
        }
        Log.w("LatLngBounds", "this LatLngBounds is invalid!");
        return false;
    }

    public LatLngBounds including(LatLng latLng) {
        if (latLng == null) {
            return this;
        }
        if (this.b) {
            double d;
            double min = Math.min(this.southwest.latitude, latLng.latitude);
            double max = Math.max(this.northeast.latitude, latLng.latitude);
            double d2 = this.northeast.longitude;
            double d3 = this.southwest.longitude;
            double d4 = latLng.longitude;
            if (b(d4)) {
                d = d2;
            } else if (c(d3, d4) < d(d2, d4)) {
                d3 = d4;
                d = d2;
            } else {
                d = d4;
            }
            return new LatLngBounds(new LatLng(min, d3, false), new LatLng(max, d, false));
        }
        Log.w("LatLngBounds", "this LatLngBounds is invalid!");
        return null;
    }

    private static double c(double d, double d2) {
        return ((d - d2) + 360.0d) % 360.0d;
    }

    private static double d(double d, double d2) {
        return ((d2 - d) + 360.0d) % 360.0d;
    }

    private boolean a(double d) {
        return this.southwest.latitude <= d && d <= this.northeast.latitude;
    }

    private boolean b(double d) {
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

    public int hashCode() {
        return eh.a(new Object[]{this.southwest, this.northeast});
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (!this.b) {
            Log.w("LatLngBounds", "this LatLngBounds is invalid!");
            return false;
        } else if (this == obj) {
            return true;
        } else {
            if (!(obj instanceof LatLngBounds)) {
                return false;
            }
            LatLngBounds latLngBounds = (LatLngBounds) obj;
            if (this.southwest.equals(latLngBounds.southwest)) {
                if (!this.northeast.equals(latLngBounds.northeast)) {
                }
                return z;
            }
            z = false;
            return z;
        }
    }

    public String toString() {
        return eh.a(eh.a("southwest", this.southwest), eh.a("northeast", this.northeast));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        LatLngBoundsCreator.a(this, parcel, i);
    }
}
