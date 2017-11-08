package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.Node;

/* compiled from: Unknown */
public class NodeParcelable implements SafeParcelable, Node {
    public static final Creator<NodeParcelable> CREATOR = new zzbb();
    final int mVersionCode;
    private final String zzahj;
    private final int zzbaR;
    private final boolean zzbaS;
    private final String zzwj;

    NodeParcelable(int versionCode, String id, String displayName, int hopCount, boolean isNearby) {
        this.mVersionCode = versionCode;
        this.zzwj = id;
        this.zzahj = displayName;
        this.zzbaR = hopCount;
        this.zzbaS = isNearby;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        return o instanceof NodeParcelable ? ((NodeParcelable) o).zzwj.equals(this.zzwj) : false;
    }

    public String getDisplayName() {
        return this.zzahj;
    }

    public int getHopCount() {
        return this.zzbaR;
    }

    public String getId() {
        return this.zzwj;
    }

    public int hashCode() {
        return this.zzwj.hashCode();
    }

    public boolean isNearby() {
        return this.zzbaS;
    }

    public String toString() {
        return "Node{" + this.zzahj + ", id=" + this.zzwj + ", hops=" + this.zzbaR + ", isNearby=" + this.zzbaS + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzbb.zza(this, dest, flags);
    }
}
