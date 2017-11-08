package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.zzb;

/* compiled from: Unknown */
public class AmsEntityUpdateParcelable implements SafeParcelable, zzb {
    public static final Creator<AmsEntityUpdateParcelable> CREATOR = new zze();
    private final String mValue;
    final int mVersionCode;
    private byte zzaZx;
    private final byte zzaZy;

    AmsEntityUpdateParcelable(int versionCode, byte entityId, byte attributeId, String value) {
        this.zzaZx = (byte) entityId;
        this.mVersionCode = versionCode;
        this.zzaZy = (byte) attributeId;
        this.mValue = value;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AmsEntityUpdateParcelable amsEntityUpdateParcelable = (AmsEntityUpdateParcelable) o;
        return this.zzaZx == amsEntityUpdateParcelable.zzaZx && this.mVersionCode == amsEntityUpdateParcelable.mVersionCode && this.zzaZy == amsEntityUpdateParcelable.zzaZy && this.mValue.equals(amsEntityUpdateParcelable.mValue);
    }

    public String getValue() {
        return this.mValue;
    }

    public int hashCode() {
        return (((((this.mVersionCode * 31) + this.zzaZx) * 31) + this.zzaZy) * 31) + this.mValue.hashCode();
    }

    public String toString() {
        return "AmsEntityUpdateParcelable{mVersionCode=" + this.mVersionCode + ", mEntityId=" + this.zzaZx + ", mAttributeId=" + this.zzaZy + ", mValue='" + this.mValue + '\'' + '}';
    }

    public void writeToParcel(Parcel dest, int flags) {
        zze.zza(this, dest, flags);
    }

    public byte zzCw() {
        return this.zzaZx;
    }

    public byte zzCx() {
        return this.zzaZy;
    }
}
