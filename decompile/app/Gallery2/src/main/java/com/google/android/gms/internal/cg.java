package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import java.util.List;

/* compiled from: Unknown */
public class cg implements Creator<cf> {
    static void a(cf cfVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, cfVar.versionCode);
        b.a(parcel, 2, cfVar.nw, false);
        b.a(parcel, 3, cfVar.oi, false);
        b.a(parcel, 4, cfVar.mt, false);
        b.c(parcel, 5, cfVar.errorCode);
        b.a(parcel, 6, cfVar.mu, false);
        b.a(parcel, 7, cfVar.oj);
        b.a(parcel, 8, cfVar.ok);
        b.a(parcel, 9, cfVar.ol);
        b.a(parcel, 10, cfVar.om, false);
        b.a(parcel, 11, cfVar.mx);
        b.c(parcel, 12, cfVar.orientation);
        b.a(parcel, 13, cfVar.on, false);
        b.a(parcel, 14, cfVar.oo);
        b.D(parcel, p);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return g(x0);
    }

    public cf g(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        String str = null;
        String str2 = null;
        List list = null;
        int i2 = 0;
        List list2 = null;
        long j = 0;
        boolean z = false;
        long j2 = 0;
        List list3 = null;
        long j3 = 0;
        int i3 = 0;
        String str3 = null;
        long j4 = 0;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    str = a.m(parcel, n);
                    break;
                case 3:
                    str2 = a.m(parcel, n);
                    break;
                case 4:
                    list = a.y(parcel, n);
                    break;
                case 5:
                    i2 = a.g(parcel, n);
                    break;
                case 6:
                    list2 = a.y(parcel, n);
                    break;
                case 7:
                    j = a.h(parcel, n);
                    break;
                case 8:
                    z = a.c(parcel, n);
                    break;
                case 9:
                    j2 = a.h(parcel, n);
                    break;
                case 10:
                    list3 = a.y(parcel, n);
                    break;
                case 11:
                    j3 = a.h(parcel, n);
                    break;
                case 12:
                    i3 = a.g(parcel, n);
                    break;
                case 13:
                    str3 = a.m(parcel, n);
                    break;
                case 14:
                    j4 = a.h(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new cf(i, str, str2, list, i2, list2, j, z, j2, list3, j3, i3, str3, j4);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public cf[] l(int i) {
        return new cf[i];
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return l(x0);
    }
}
