package com.google.android.gms.location;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class DetectedActivity implements SafeParcelable {
    public static final DetectedActivityCreator CREATOR = new DetectedActivityCreator();
    int KS;
    int KT;
    private final int wj;

    public DetectedActivity(int versionCode, int activityType, int confidence) {
        this.wj = versionCode;
        this.KS = activityType;
        this.KT = confidence;
    }

    private int bh(int i) {
        return i <= 6 ? i : 4;
    }

    public int describeContents() {
        return 0;
    }

    public int getType() {
        return bh(this.KS);
    }

    public int getVersionCode() {
        return this.wj;
    }

    public String toString() {
        return "DetectedActivity [type=" + getType() + ", confidence=" + this.KT + "]";
    }

    public void writeToParcel(Parcel out, int flags) {
        DetectedActivityCreator.a(this, out, flags);
    }
}
