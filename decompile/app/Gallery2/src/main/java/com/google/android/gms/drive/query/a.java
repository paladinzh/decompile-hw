package com.google.android.gms.drive.query;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.drive.query.internal.LogicalFilter;

/* compiled from: Unknown */
public class a implements Creator<Query> {
    static void a(Query query, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1000, query.wj);
        b.a(parcel, 1, query.EL, i, false);
        b.a(parcel, 3, query.EM, false);
        b.D(parcel, p);
    }

    public Query[] aG(int i) {
        return new Query[i];
    }

    public Query ab(Parcel parcel) {
        LogicalFilter logicalFilter = null;
        int o = com.google.android.gms.common.internal.safeparcel.a.o(parcel);
        int i = 0;
        String str = null;
        while (parcel.dataPosition() < o) {
            int n = com.google.android.gms.common.internal.safeparcel.a.n(parcel);
            switch (com.google.android.gms.common.internal.safeparcel.a.S(n)) {
                case 1:
                    logicalFilter = (LogicalFilter) com.google.android.gms.common.internal.safeparcel.a.a(parcel, n, LogicalFilter.CREATOR);
                    break;
                case 3:
                    str = com.google.android.gms.common.internal.safeparcel.a.m(parcel, n);
                    break;
                case 1000:
                    i = com.google.android.gms.common.internal.safeparcel.a.g(parcel, n);
                    break;
                default:
                    com.google.android.gms.common.internal.safeparcel.a.b(parcel, n);
                    break;
            }
            String str2 = str;
            logicalFilter = logicalFilter;
            str = str2;
        }
        if (parcel.dataPosition() == o) {
            return new Query(i, logicalFilter, str);
        }
        throw new com.google.android.gms.common.internal.safeparcel.a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return ab(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aG(x0);
    }
}
