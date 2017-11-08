package com.google.android.gms.plus.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class j implements Creator<h> {
    static void a(h hVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, hVar.getAccountName(), false);
        b.c(parcel, 1000, hVar.getVersionCode());
        b.a(parcel, 2, hVar.hq(), false);
        b.a(parcel, 3, hVar.hr(), false);
        b.a(parcel, 4, hVar.hs(), false);
        b.a(parcel, 5, hVar.ht(), false);
        b.a(parcel, 6, hVar.hu(), false);
        b.a(parcel, 7, hVar.hv(), false);
        b.a(parcel, 8, hVar.hw(), false);
        b.a(parcel, 9, hVar.hx(), i, false);
        b.D(parcel, p);
    }

    public h aF(Parcel parcel) {
        PlusCommonExtras plusCommonExtras = null;
        int o = a.o(parcel);
        int i = 0;
        String str = null;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String[] strArr = null;
        String[] strArr2 = null;
        String[] strArr3 = null;
        String str5 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    str5 = a.m(parcel, n);
                    break;
                case 2:
                    strArr3 = a.x(parcel, n);
                    break;
                case 3:
                    strArr2 = a.x(parcel, n);
                    break;
                case 4:
                    strArr = a.x(parcel, n);
                    break;
                case 5:
                    str4 = a.m(parcel, n);
                    break;
                case 6:
                    str3 = a.m(parcel, n);
                    break;
                case 7:
                    str2 = a.m(parcel, n);
                    break;
                case 8:
                    str = a.m(parcel, n);
                    break;
                case 9:
                    plusCommonExtras = (PlusCommonExtras) a.a(parcel, n, PlusCommonExtras.CREATOR);
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
            return new h(i, str5, strArr3, strArr2, strArr, str4, str3, str2, str, plusCommonExtras);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public h[] bC(int i) {
        return new h[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return aF(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return bC(x0);
    }
}
