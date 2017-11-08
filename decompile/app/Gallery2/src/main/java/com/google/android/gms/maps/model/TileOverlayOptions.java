package com.google.android.gms.maps.model;

import android.os.IBinder;
import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.maps.internal.r;
import com.google.android.gms.maps.model.internal.g;
import com.google.android.gms.maps.model.internal.g.a;

/* compiled from: Unknown */
public final class TileOverlayOptions implements SafeParcelable {
    public static final TileOverlayOptionsCreator CREATOR = new TileOverlayOptionsCreator();
    private float PP;
    private boolean PQ;
    private g Qt;
    private TileProvider Qu;
    private boolean Qv;
    private final int wj;

    public TileOverlayOptions() {
        this.PQ = true;
        this.Qv = true;
        this.wj = 1;
    }

    TileOverlayOptions(int versionCode, IBinder delegate, boolean visible, float zIndex, boolean fadeIn) {
        TileProvider tileProvider = null;
        this.PQ = true;
        this.Qv = true;
        this.wj = versionCode;
        this.Qt = a.au(delegate);
        if (this.Qt != null) {
            tileProvider = new TileProvider(this) {
                private final g Qw = this.Qx.Qt;
                final /* synthetic */ TileOverlayOptions Qx;

                {
                    this.Qx = r2;
                }
            };
        }
        this.Qu = tileProvider;
        this.PQ = visible;
        this.PP = zIndex;
        this.Qv = fadeIn;
    }

    public int describeContents() {
        return 0;
    }

    public boolean getFadeIn() {
        return this.Qv;
    }

    int getVersionCode() {
        return this.wj;
    }

    public float getZIndex() {
        return this.PP;
    }

    IBinder hh() {
        return this.Qt.asBinder();
    }

    public boolean isVisible() {
        return this.PQ;
    }

    public void writeToParcel(Parcel out, int flags) {
        if (r.hc()) {
            j.a(this, out, flags);
        } else {
            TileOverlayOptionsCreator.a(this, out, flags);
        }
    }
}
