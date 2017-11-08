package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.wearable.Channel;

/* compiled from: Unknown */
public class ChannelImpl implements SafeParcelable, Channel {
    public static final Creator<ChannelImpl> CREATOR = new zzn();
    private final String mPath;
    final int mVersionCode;
    private final String zzaYT;
    private final String zzaZP;

    ChannelImpl(int versionCode, String token, String nodeId, String path) {
        this.mVersionCode = versionCode;
        this.zzaZP = (String) zzx.zzv(token);
        this.zzaYT = (String) zzx.zzv(nodeId);
        this.mPath = (String) zzx.zzv(path);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object other) {
        boolean z = true;
        if (other == this) {
            return true;
        }
        if (!(other instanceof ChannelImpl)) {
            return false;
        }
        ChannelImpl channelImpl = (ChannelImpl) other;
        if (this.zzaZP.equals(channelImpl.zzaZP) && zzw.equal(channelImpl.zzaYT, this.zzaYT) && zzw.equal(channelImpl.mPath, this.mPath)) {
            if (channelImpl.mVersionCode != this.mVersionCode) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public String getNodeId() {
        return this.zzaYT;
    }

    public String getPath() {
        return this.mPath;
    }

    public String getToken() {
        return this.zzaZP;
    }

    public int hashCode() {
        return this.zzaZP.hashCode();
    }

    public String toString() {
        return "ChannelImpl{versionCode=" + this.mVersionCode + ", token='" + this.zzaZP + '\'' + ", nodeId='" + this.zzaYT + '\'' + ", path='" + this.mPath + '\'' + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzn.zza(this, dest, flags);
    }
}
