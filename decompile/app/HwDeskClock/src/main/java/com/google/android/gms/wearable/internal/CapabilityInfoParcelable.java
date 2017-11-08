package com.google.android.gms.wearable.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.Node;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public class CapabilityInfoParcelable implements SafeParcelable, CapabilityInfo {
    public static final Creator<CapabilityInfoParcelable> CREATOR = new zzj();
    private final String mName;
    final int mVersionCode;
    private Set<Node> zzaZJ;
    private final List<NodeParcelable> zzaZM;
    private final Object zzpc = new Object();

    CapabilityInfoParcelable(int versionCode, String name, List<NodeParcelable> nodeList) {
        this.mVersionCode = versionCode;
        this.mName = name;
        this.zzaZM = nodeList;
        this.zzaZJ = null;
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
        CapabilityInfoParcelable capabilityInfoParcelable = (CapabilityInfoParcelable) o;
        return this.mVersionCode == capabilityInfoParcelable.mVersionCode ? (this.mName != null ? this.mName.equals(capabilityInfoParcelable.mName) : capabilityInfoParcelable.mName == null) ? this.zzaZM != null ? this.zzaZM.equals(capabilityInfoParcelable.zzaZM) : capabilityInfoParcelable.zzaZM == null : false : false;
    }

    public String getName() {
        return this.mName;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = ((this.mName == null ? 0 : this.mName.hashCode()) + (this.mVersionCode * 31)) * 31;
        if (this.zzaZM != null) {
            i = this.zzaZM.hashCode();
        }
        return hashCode + i;
    }

    public String toString() {
        return "CapabilityInfo{" + this.mName + ", " + this.zzaZM + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzj.zza(this, dest, flags);
    }

    public List<NodeParcelable> zzCE() {
        return this.zzaZM;
    }
}
