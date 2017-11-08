package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.drive.query.Filter;
import java.util.List;

/* compiled from: Unknown */
public class LogicalFilter implements SafeParcelable, Filter {
    public static final Creator<LogicalFilter> CREATOR = new f();
    final Operator EO;
    final List<FilterHolder> EY;
    final int wj;

    LogicalFilter(int versionCode, Operator operator, List<FilterHolder> filterHolders) {
        this.wj = versionCode;
        this.EO = operator;
        this.EY = filterHolders;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        f.a(this, out, flags);
    }
}
