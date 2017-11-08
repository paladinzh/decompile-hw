package com.google.android.gms.drive.query.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class g implements Creator<NotFilter> {
    static void a(NotFilter notFilter, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1000, notFilter.wj);
        b.a(parcel, 1, notFilter.EZ, i, false);
        b.D(parcel, p);
    }

    public NotFilter[] aM(int i) {
        return new NotFilter[i];
    }

    public NotFilter ah(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        FilterHolder filterHolder = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    filterHolder = (FilterHolder) a.a(parcel, n, FilterHolder.CREATOR);
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
            return new NotFilter(i, filterHolder);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return ah(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aM(x0);
    }
}
