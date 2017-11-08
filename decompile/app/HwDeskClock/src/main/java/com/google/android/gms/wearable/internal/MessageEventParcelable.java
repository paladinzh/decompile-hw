package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.MessageEvent;

/* compiled from: Unknown */
public class MessageEventParcelable implements SafeParcelable, MessageEvent {
    public static final Creator<MessageEventParcelable> CREATOR = new zzaz();
    private final String mPath;
    final int mVersionCode;
    private final String zzaDK;
    private final int zzags;
    private final byte[] zzayI;

    MessageEventParcelable(int versionCode, int requestId, String path, byte[] data, String source) {
        this.mVersionCode = versionCode;
        this.zzags = requestId;
        this.mPath = path;
        this.zzayI = data;
        this.zzaDK = source;
    }

    public int describeContents() {
        return 0;
    }

    public byte[] getData() {
        return this.zzayI;
    }

    public String getPath() {
        return this.mPath;
    }

    public int getRequestId() {
        return this.zzags;
    }

    public String getSourceNodeId() {
        return this.zzaDK;
    }

    public String toString() {
        return "MessageEventParcelable[" + this.zzags + "," + this.mPath + ", size=" + (this.zzayI != null ? Integer.valueOf(this.zzayI.length) : "null") + "]";
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzaz.zza(this, dest, flags);
    }
}
