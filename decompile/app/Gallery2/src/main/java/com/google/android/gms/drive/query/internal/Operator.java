package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class Operator implements SafeParcelable {
    public static final Creator<Operator> CREATOR = new h();
    public static final Operator Fa = new Operator("=");
    public static final Operator Fb = new Operator("<");
    public static final Operator Fc = new Operator("<=");
    public static final Operator Fd = new Operator(">");
    public static final Operator Fe = new Operator(">=");
    public static final Operator Ff = new Operator("and");
    public static final Operator Fg = new Operator("or");
    public static final Operator Fh = new Operator("not");
    public static final Operator Fi = new Operator("contains");
    final String mTag;
    final int wj;

    Operator(int versionCode, String tag) {
        this.wj = versionCode;
        this.mTag = tag;
    }

    private Operator(String tag) {
        this(1, tag);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Operator obj2 = (Operator) obj;
        if (this.mTag != null) {
            return this.mTag.equals(obj2.mTag);
        } else {
            if (obj2.mTag != null) {
                return false;
            }
        }
    }

    public int hashCode() {
        return (this.mTag != null ? this.mTag.hashCode() : 0) + 31;
    }

    public void writeToParcel(Parcel out, int flags) {
        h.a(this, out, flags);
    }
}
