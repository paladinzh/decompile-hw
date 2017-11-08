package com.google.android.gms.maps.model;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class Tile implements SafeParcelable {
    public static final zzn CREATOR = new zzn();
    public final byte[] data;
    public final int height;
    private final int mVersionCode;
    public final int width;

    Tile(int versionCode, int width, int height, byte[] data) {
        this.mVersionCode = versionCode;
        this.width = width;
        this.height = height;
        this.data = data;
    }

    public Tile(int width, int height, byte[] data) {
        this(1, width, height, data);
    }

    public int describeContents() {
        return 0;
    }

    int getVersionCode() {
        return this.mVersionCode;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzn.zza(this, out, flags);
    }
}
