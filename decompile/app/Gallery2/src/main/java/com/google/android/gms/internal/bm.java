package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class bm implements Creator<bn> {
    static void a(bn bnVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, bnVar.versionCode);
        b.a(parcel, 2, bnVar.mY, false);
        b.a(parcel, 3, bnVar.mZ, false);
        b.a(parcel, 4, bnVar.mimeType, false);
        b.a(parcel, 5, bnVar.packageName, false);
        b.a(parcel, 6, bnVar.na, false);
        b.a(parcel, 7, bnVar.nb, false);
        b.a(parcel, 8, bnVar.nc, false);
        b.D(parcel, p);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return d(x0);
    }

    public bn d(Parcel parcel) {
        String str = null;
        int o = a.o(parcel);
        int i = 0;
        String str2 = null;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String str7 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    str7 = a.m(parcel, n);
                    break;
                case 3:
                    str6 = a.m(parcel, n);
                    break;
                case 4:
                    str5 = a.m(parcel, n);
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
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new bn(i, str7, str6, str5, str4, str3, str2, str);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public bn[] i(int i) {
        return new bn[i];
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return i(x0);
    }
}
