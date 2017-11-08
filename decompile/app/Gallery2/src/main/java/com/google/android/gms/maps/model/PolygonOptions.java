package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.r;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public final class PolygonOptions implements SafeParcelable {
    public static final PolygonOptionsCreator CREATOR = new PolygonOptionsCreator();
    private float PM;
    private int PN;
    private int PO;
    private float PP;
    private boolean PQ;
    private final List<LatLng> Qo;
    private final List<List<LatLng>> Qp;
    private boolean Qq;
    private final int wj;

    public PolygonOptions() {
        this.PM = 10.0f;
        this.PN = -16777216;
        this.PO = 0;
        this.PP = 0.0f;
        this.PQ = true;
        this.Qq = false;
        this.wj = 1;
        this.Qo = new ArrayList();
        this.Qp = new ArrayList();
    }

    PolygonOptions(int versionCode, List<LatLng> points, List holes, float strokeWidth, int strokeColor, int fillColor, float zIndex, boolean visible, boolean geodesic) {
        this.PM = 10.0f;
        this.PN = -16777216;
        this.PO = 0;
        this.PP = 0.0f;
        this.PQ = true;
        this.Qq = false;
        this.wj = versionCode;
        this.Qo = points;
        this.Qp = holes;
        this.PM = strokeWidth;
        this.PN = strokeColor;
        this.PO = fillColor;
        this.PP = zIndex;
        this.PQ = visible;
        this.Qq = geodesic;
    }

    public int describeContents() {
        return 0;
    }

    public int getFillColor() {
        return this.PO;
    }

    public List<LatLng> getPoints() {
        return this.Qo;
    }

    public int getStrokeColor() {
        return this.PN;
    }

    public float getStrokeWidth() {
        return this.PM;
    }

    int getVersionCode() {
        return this.wj;
    }

    public float getZIndex() {
        return this.PP;
    }

    List hg() {
        return this.Qp;
    }

    public boolean isGeodesic() {
        return this.Qq;
    }

    public boolean isVisible() {
        return this.PQ;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            g.a(this, out, flags);
        } else {
            PolygonOptionsCreator.a(this, out, flags);
        }
    }
}
