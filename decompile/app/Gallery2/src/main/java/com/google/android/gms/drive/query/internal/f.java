package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import java.util.List;

/* compiled from: Unknown */
public class f implements Creator<LogicalFilter> {
    static void a(LogicalFilter logicalFilter, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1000, logicalFilter.wj);
        b.a(parcel, 1, logicalFilter.EO, i, false);
        b.b(parcel, 2, logicalFilter.EY, false);
        b.D(parcel, p);
    }

    public LogicalFilter[] aL(int i) {
        return new LogicalFilter[i];
    }

    public LogicalFilter ag(Parcel parcel) {
        Operator operator = null;
        int o = a.o(parcel);
        int i = 0;
        List list = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    operator = (Operator) a.a(parcel, n, Operator.CREATOR);
                    break;
                case 2:
                    list = a.c(parcel, n, FilterHolder.CREATOR);
                    break;
                case 1000:
                    i = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
            List list2 = list;
            operator = operator;
            list = list2;
        }
        if (parcel.dataPosition() == o) {
            return new LogicalFilter(i, operator, list);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return ag(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aL(x0);
    }
}
