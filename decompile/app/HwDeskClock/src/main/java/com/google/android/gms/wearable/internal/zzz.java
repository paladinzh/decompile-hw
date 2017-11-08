package com.google.android.gms.wearable.internal;

import com.google.android.gms.wearable.DataItemAsset;

/* compiled from: Unknown */
public class zzz implements DataItemAsset {
    private final String zztP;
    private final String zzwj;

    public String getDataItemKey() {
        return this.zztP;
    }

    public String getId() {
        return this.zzwj;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DataItemAssetEntity[");
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
}
