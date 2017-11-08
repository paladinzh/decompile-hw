package com.google.android.gms.wearable;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public class Asset implements SafeParcelable {
    public static final Creator<Asset> CREATOR = new zze();
    final int mVersionCode;
    public Uri uri;
    private String zzaYM;
    public ParcelFileDescriptor zzaYN;
    private byte[] zzayI;

    Asset(int versionCode, byte[] data, String digest, ParcelFileDescriptor fd, Uri uri) {
        this.mVersionCode = versionCode;
        this.zzayI = data;
        this.zzaYM = digest;
        this.zzaYN = fd;
        this.uri = uri;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (this == o) {
            return true;
        }
        if (!(o instanceof Asset)) {
            return false;
        }
        Asset asset = (Asset) o;
        if (zzw.equal(this.zzayI, asset.zzayI) && zzw.equal(this.zzaYM, asset.zzaYM) && zzw.equal(this.zzaYN, asset.zzaYN)) {
            if (!zzw.equal(this.uri, asset.uri)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public byte[] getData() {
        return this.zzayI;
    }

    public String getDigest() {
        return this.zzaYM;
    }

    public int hashCode() {
        return zzw.hashCode(this.zzayI, this.zzaYM, this.zzaYN, this.uri);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Asset[@");
        stringBuilder.append(Integer.toHexString(hashCode()));
        if (this.zzaYM != null) {
            stringBuilder.append(", ");
            stringBuilder.append(this.zzaYM);
        } else {
            stringBuilder.append(", nodigest");
        }
        if (this.zzayI != null) {
            stringBuilder.append(", size=");
            stringBuilder.append(this.zzayI.length);
        }
        if (this.zzaYN != null) {
            stringBuilder.append(", fd=");
            stringBuilder.append(this.zzaYN);
        }
        if (this.uri != null) {
            stringBuilder.append(", uri=");
            stringBuilder.append(this.uri);
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        zze.zza(this, dest, flags | 1);
    }
}
