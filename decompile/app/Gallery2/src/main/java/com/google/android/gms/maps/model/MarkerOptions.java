package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.dynamic.b.a;
import com.google.android.gms.maps.internal.r;
import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public final class MarkerOptions implements SafeParcelable {
    public static final MarkerOptionsCreator CREATOR = new MarkerOptionsCreator();
    private String CX;
    private boolean PQ;
    private float PY;
    private float PZ;
    private LatLng Qf;
    private String Qg;
    private BitmapDescriptor Qh;
    private boolean Qi;
    private boolean Qj;
    private float Qk;
    private float Ql;
    private float Qm;
    private float mAlpha;
    private final int wj;

    public MarkerOptions() {
        this.PY = 0.5f;
        this.PZ = WMElement.CAMERASIZEVALUE1B1;
        this.PQ = true;
        this.Qj = false;
        this.Qk = 0.0f;
        this.Ql = 0.5f;
        this.Qm = 0.0f;
        this.mAlpha = WMElement.CAMERASIZEVALUE1B1;
        this.wj = 1;
    }

    MarkerOptions(int versionCode, LatLng position, String title, String snippet, IBinder wrappedIcon, float anchorU, float anchorV, boolean draggable, boolean visible, boolean flat, float rotation, float infoWindowAnchorU, float infoWindowAnchorV, float alpha) {
        this.PY = 0.5f;
        this.PZ = WMElement.CAMERASIZEVALUE1B1;
        this.PQ = true;
        this.Qj = false;
        this.Qk = 0.0f;
        this.Ql = 0.5f;
        this.Qm = 0.0f;
        this.mAlpha = WMElement.CAMERASIZEVALUE1B1;
        this.wj = versionCode;
        this.Qf = position;
        this.CX = title;
        this.Qg = snippet;
        this.Qh = wrappedIcon != null ? new BitmapDescriptor(a.G(wrappedIcon)) : null;
        this.PY = anchorU;
        this.PZ = anchorV;
        this.Qi = draggable;
        this.PQ = visible;
        this.Qj = flat;
        this.Qk = rotation;
        this.Ql = infoWindowAnchorU;
        this.Qm = infoWindowAnchorV;
        this.mAlpha = alpha;
    }

    public int describeContents() {
        return 0;
    }

    public float getAlpha() {
        return this.mAlpha;
    }

    public float getAnchorU() {
        return this.PY;
    }

    public float getAnchorV() {
        return this.PZ;
    }

    public float getInfoWindowAnchorU() {
        return this.Ql;
    }

    public float getInfoWindowAnchorV() {
        return this.Qm;
    }

    public LatLng getPosition() {
        return this.Qf;
    }

    public float getRotation() {
        return this.Qk;
    }

    public String getSnippet() {
        return this.Qg;
    }

    public String getTitle() {
        return this.CX;
    }

    int getVersionCode() {
        return this.wj;
    }

    IBinder hf() {
        return this.Qh != null ? this.Qh.gK().asBinder() : null;
    }

    public MarkerOptions icon(BitmapDescriptor icon) {
        this.Qh = icon;
        return this;
    }

    public boolean isDraggable() {
        return this.Qi;
    }

    public boolean isFlat() {
        return this.Qj;
    }

    public boolean isVisible() {
        return this.PQ;
    }

    public MarkerOptions position(LatLng position) {
        this.Qf = position;
        return this;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            f.a(this, out, flags);
        } else {
            MarkerOptionsCreator.a(this, out, flags);
        }
    }
}
