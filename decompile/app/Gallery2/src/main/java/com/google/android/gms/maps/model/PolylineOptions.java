package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.r;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class PolylineOptions implements SafeParcelable {
    public static final PolylineOptionsCreator CREATOR = new PolylineOptionsCreator();
    private float PP;
    private boolean PQ;
    private float PU;
    private final List<LatLng> Qo;
    private boolean Qq;
    private final int wj;
    private int yX;

    public PolylineOptions() {
        this.PU = 10.0f;
        this.yX = -16777216;
        this.PP = 0.0f;
        this.PQ = true;
        this.Qq = false;
        this.wj = 1;
        this.Qo = new ArrayList();
    }

    PolylineOptions(int versionCode, List points, float width, int color, float zIndex, boolean visible, boolean geodesic) {
        this.PU = 10.0f;
        this.yX = -16777216;
        this.PP = 0.0f;
        this.PQ = true;
        this.Qq = false;
        this.wj = versionCode;
        this.Qo = points;
        this.PU = width;
        this.yX = color;
        this.PP = zIndex;
        this.PQ = visible;
        this.Qq = geodesic;
    }

    public int describeContents() {
        return 0;
    }

    public int getColor() {
        return this.yX;
    }

    public List<LatLng> getPoints() {
        return this.Qo;
    }

    int getVersionCode() {
        return this.wj;
    }

    public float getWidth() {
        return this.PU;
    }

    public float getZIndex() {
        return this.PP;
    }

    public boolean isGeodesic() {
        return this.Qq;
    }

    public boolean isVisible() {
        return this.PQ;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            h.a(this, out, flags);
        } else {
            PolylineOptionsCreator.a(this, out, flags);
        }
    }
}
