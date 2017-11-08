package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.wearable.DataItemAsset;

/* compiled from: Unknown */
public class DataItemAssetParcelable implements SafeParcelable, DataItemAsset {
    public static final Creator<DataItemAssetParcelable> CREATOR = new zzaa();
    final int mVersionCode;
    private final String zztP;
    private final String zzwj;

    DataItemAssetParcelable(int versionCode, String id, String key) {
        this.mVersionCode = versionCode;
        this.zzwj = id;
        this.zztP = key;
    }

    public DataItemAssetParcelable(DataItemAsset value) {
        this.mVersionCode = 1;
        this.zzwj = (String) zzx.zzv(value.getId());
        this.zztP = (String) zzx.zzv(value.getDataItemKey());
    }

    public int describeContents() {
        return 0;
    }

    public String getDataItemKey() {
        return this.zztP;
    }

    public String getId() {
        return this.zzwj;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DataItemAssetParcelable[");
        stringBuilder.append("@");
        stringBuilder.append(Integer.toHexString(hashCode()));
        if (this.zzwj != null) {
            stringBuilder.append(",");
            stringBuilder.append(this.zzwj);
        } else {
            stringBuilder.append(",noid");
        }
        stringBuilder.append(", key=");
        stringBuilder.append(this.zztP);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzaa.zza(this, dest, flags);
    }
}
