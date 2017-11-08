package com.google.android.gms.wearable;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;

/* compiled from: Unknown */
public class ConnectionConfiguration implements SafeParcelable {
    public static final Creator<ConnectionConfiguration> CREATOR = new zzg();
    private final String mName;
    final int mVersionCode;
    private boolean zzOq;
    private final int zzUS;
    private final boolean zzaYQ;
    private String zzaYR;
    private boolean zzaYS;
    private String zzaYT;
    private final int zzahe;
    private final String zzanw;

    ConnectionConfiguration(int versionCode, String name, String address, int type, int role, boolean connectionEnabled, boolean isConnected, String peerNodeId, boolean btlePriority, String nodeId) {
        this.mVersionCode = versionCode;
        this.mName = name;
        this.zzanw = address;
        this.zzUS = type;
        this.zzahe = role;
        this.zzaYQ = connectionEnabled;
        this.zzOq = isConnected;
        this.zzaYR = peerNodeId;
        this.zzaYS = btlePriority;
        this.zzaYT = nodeId;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (!(o instanceof ConnectionConfiguration)) {
            return false;
        }
        ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration) o;
        if (zzw.equal(Integer.valueOf(this.mVersionCode), Integer.valueOf(connectionConfiguration.mVersionCode)) && zzw.equal(this.mName, connectionConfiguration.mName) && zzw.equal(this.zzanw, connectionConfiguration.zzanw) && zzw.equal(Integer.valueOf(this.zzUS), Integer.valueOf(connectionConfiguration.zzUS)) && zzw.equal(Integer.valueOf(this.zzahe), Integer.valueOf(connectionConfiguration.zzahe)) && zzw.equal(Boolean.valueOf(this.zzaYQ), Boolean.valueOf(connectionConfiguration.zzaYQ)) && zzw.equal(Boolean.valueOf(this.zzaYS), Boolean.valueOf(connectionConfiguration.zzaYS))) {
            z = true;
        }
        return z;
    }

    public String getAddress() {
        return this.zzanw;
    }

    public String getName() {
        return this.mName;
    }

    public String getNodeId() {
        return this.zzaYT;
    }

    public int getRole() {
        return this.zzahe;
    }

    public int getType() {
        return this.zzUS;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.mVersionCode), this.mName, this.zzanw, Integer.valueOf(this.zzUS), Integer.valueOf(this.zzahe), Boolean.valueOf(this.zzaYQ), Boolean.valueOf(this.zzaYS));
    }

    public boolean isConnected() {
        return this.zzOq;
    }

    public boolean isEnabled() {
        return this.zzaYQ;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("ConnectionConfiguration[ ");
        stringBuilder.append("mName=" + this.mName);
        stringBuilder.append(", mAddress=" + this.zzanw);
        stringBuilder.append(", mType=" + this.zzUS);
        stringBuilder.append(", mRole=" + this.zzahe);
        stringBuilder.append(", mEnabled=" + this.zzaYQ);
        stringBuilder.append(", mIsConnected=" + this.zzOq);
        stringBuilder.append(", mPeerNodeId=" + this.zzaYR);
        stringBuilder.append(", mBtlePriority=" + this.zzaYS);
        stringBuilder.append(", mNodeId=" + this.zzaYT);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzg.zza(this, dest, flags);
    }

    public String zzCr() {
        return this.zzaYR;
    }

    public boolean zzCs() {
        return this.zzaYS;
    }
}
