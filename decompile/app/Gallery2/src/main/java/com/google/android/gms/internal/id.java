package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class id implements SafeParcelable {
    public static final ie CREATOR = new ie();
    public final String OG;
    public final String OH;
    public final int versionCode;

    public id(int i, String str, String str2) {
        this.versionCode = i;
        this.OG = str;
        this.OH = str2;
    }

    public int describeContents() {
        ie ieVar = CREATOR;
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (object == null || !(object instanceof id)) {
            return false;
        }
        id idVar = (id) object;
        if (this.OH.equals(idVar.OH)) {
            if (!this.OG.equals(idVar.OG)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return ep.hashCode(this.OG, this.OH);
    }

    public String toString() {
        return ep.e(this).a("clientPackageName", this.OG).a("locale", this.OH).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        ie ieVar = CREATOR;
        ie.a(this, out, flags);
    }
}
