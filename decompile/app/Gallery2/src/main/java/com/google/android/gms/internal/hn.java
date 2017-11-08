package com.google.android.gms.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* compiled from: Unknown */
public final class hn implements SafeParcelable {
    public static final ho CREATOR = new ho();
    final List<ht> LA;
    private final String LB;
    private final boolean LC;
    private final Set<ht> LD;
    final int wj;

    hn(int i, List<ht> list, String str, boolean z) {
        this.wj = i;
        this.LA = list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
        if (str == null) {
            str = "";
        }
        this.LB = str;
        this.LC = z;
        this.LD = !this.LA.isEmpty() ? Collections.unmodifiableSet(new HashSet(this.LA)) : Collections.emptySet();
    }

    public int describeContents() {
        ho hoVar = CREATOR;
        return 0;
    }

    public boolean equals(Object object) {
        boolean z = true;
        if (this == object) {
            return true;
        }
        if (!(object instanceof hn)) {
            return false;
        }
        hn hnVar = (hn) object;
        if (this.LD.equals(hnVar.LD) && this.LB == hnVar.LB) {
            if (this.LC != hnVar.LC) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public String gr() {
        return this.LB;
    }

    public boolean gs() {
        return this.LC;
    }

    public int hashCode() {
        return ep.hashCode(this.LD, this.LB, Boolean.valueOf(this.LC));
    }

    public String toString() {
        return ep.e(this).a("types", this.LD).a("textQuery", this.LB).a("isOpenNowRequired", Boolean.valueOf(this.LC)).toString();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        ho hoVar = CREATOR;
        ho.a(this, parcel, flags);
    }
}
