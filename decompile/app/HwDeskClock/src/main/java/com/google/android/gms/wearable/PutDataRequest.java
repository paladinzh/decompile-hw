package com.google.android.gms.wearable;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.internal.DataItemAssetParcelable;
import java.security.SecureRandom;
import java.util.Random;

/* compiled from: Unknown */
public class PutDataRequest implements SafeParcelable {
    public static final Creator<PutDataRequest> CREATOR = new zzh();
    private static final Random zzaYX = new SecureRandom();
    private final Uri mUri;
    final int mVersionCode;
    private final Bundle zzaYY;
    private byte[] zzayI;

    PutDataRequest(int versionCode, Uri uri, Bundle assets, byte[] data) {
        this.mVersionCode = versionCode;
        this.mUri = uri;
        this.zzaYY = assets;
        this.zzaYY.setClassLoader(DataItemAssetParcelable.class.getClassLoader());
        this.zzayI = data;
    }

    public int describeContents() {
        return 0;
    }

    public byte[] getData() {
        return this.zzayI;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String toString() {
        return toString(Log.isLoggable("DataMap", 3));
    }

    public String toString(boolean verbose) {
        StringBuilder stringBuilder = new StringBuilder("PutDataRequest[");
        stringBuilder.append("dataSz=" + (this.zzayI != null ? Integer.valueOf(this.zzayI.length) : "null"));
        stringBuilder.append(", numAssets=" + this.zzaYY.size());
        stringBuilder.append(", uri=" + this.mUri);
        if (verbose) {
            stringBuilder.append("]\n  assets: ");
            for (String str : this.zzaYY.keySet()) {
                stringBuilder.append("\n    " + str + ": " + this.zzaYY.getParcelable(str));
            }
            stringBuilder.append("\n  ]");
            return stringBuilder.toString();
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzh.zza(this, dest, flags);
    }

    public Bundle zzCt() {
        return this.zzaYY;
    }
}
