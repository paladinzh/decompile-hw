package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class c implements Creator<FilterHolder> {
    static void a(FilterHolder filterHolder, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, filterHolder.ER, i, false);
        b.c(parcel, 1000, filterHolder.wj);
        b.a(parcel, 2, filterHolder.ES, i, false);
        b.a(parcel, 3, filterHolder.ET, i, false);
        b.a(parcel, 4, filterHolder.EU, i, false);
        b.a(parcel, 5, filterHolder.EV, i, false);
        b.D(parcel, p);
    }

    public FilterHolder[] aJ(int i) {
        return new FilterHolder[i];
    }

    public FilterHolder ae(Parcel parcel) {
        InFilter inFilter = null;
        int o = a.o(parcel);
        int i = 0;
        NotFilter notFilter = null;
        LogicalFilter logicalFilter = null;
        FieldOnlyFilter fieldOnlyFilter = null;
        ComparisonFilter comparisonFilter = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    comparisonFilter = (ComparisonFilter) a.a(parcel, n, ComparisonFilter.CREATOR);
                    break;
                case 2:
                    fieldOnlyFilter = (FieldOnlyFilter) a.a(parcel, n, FieldOnlyFilter.CREATOR);
                    break;
                case 3:
                    logicalFilter = (LogicalFilter) a.a(parcel, n, LogicalFilter.CREATOR);
                    break;
                case 4:
                    notFilter = (NotFilter) a.a(parcel, n, NotFilter.CREATOR);
                    break;
                case 5:
                    inFilter = (InFilter) a.a(parcel, n, InFilter.CREATOR);
                    break;
                case 1000:
                    i = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new FilterHolder(i, comparisonFilter, fieldOnlyFilter, logicalFilter, notFilter, inFilter);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return ae(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aJ(x0);
    }
}
