package com.google.android.gms.location.places;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzmr;
import java.util.Set;

/* compiled from: Unknown */
public final class UserDataType implements SafeParcelable {
    public static final zzm CREATOR = new zzm();
    public static final UserDataType zzaPX = zzy("test_type", 1);
    public static final UserDataType zzaPY = zzy("labeled_place", 6);
    public static final UserDataType zzaPZ = zzy("here_content", 7);
    public static final Set<UserDataType> zzaQa = zzmr.zza(zzaPX, zzaPY, zzaPZ);
    final int mVersionCode;
    final String zzJN;
    final int zzaQb;

    UserDataType(int versionCode, String type, int enumValue) {
        zzx.zzcM(type);
        this.mVersionCode = versionCode;
        this.zzJN = type;
        this.zzaQb = enumValue;
    }

    private static UserDataType zzy(String str, int i) {
        return new UserDataType(0, str, i);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof UserDataType)) {
            return false;
        }
        UserDataType userDataType = (UserDataType) object;
        if (this.zzJN.equals(userDataType.zzJN)) {
            if (this.zzaQb != userDataType.zzaQb) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return this.zzJN.hashCode();
    }

    public String toString() {
        return this.zzJN;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzm.zza(this, parcel, flags);
    }
}
