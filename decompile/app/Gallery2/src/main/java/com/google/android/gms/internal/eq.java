package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import java.util.List;

/* compiled from: Unknown */
public class eq implements Creator<ee$a> {
    static void a(ee$a ee_a, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, ee_a.getAccountName(), false);
        b.c(parcel, 1000, ee_a.getVersionCode());
        b.a(parcel, 2, ee_a.dT(), false);
        b.c(parcel, 3, ee_a.dS());
        b.a(parcel, 4, ee_a.dV(), false);
        b.D(parcel, p);
    }

    public ee$a[] R(int i) {
        return new ee$a[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return m(x0);
    }

    public ee$a m(Parcel parcel) {
        int i = 0;
        String str = null;
        int o = a.o(parcel);
        List list = null;
        String str2 = null;
        int i2 = 0;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    str2 = a.m(parcel, n);
                    break;
                case 2:
                    list = a.y(parcel, n);
                    break;
                case 3:
                    i = a.g(parcel, n);
                    break;
                case 4:
                    str = a.m(parcel, n);
                    break;
                case 1000:
                    i2 = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new ee$a(i2, str2, list, i, str);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return R(x0);
    }
}
