package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class hk implements Creator<hj> {
    static void a(hj hjVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, hjVar.getRequestId(), false);
        b.c(parcel, 1000, hjVar.getVersionCode());
        b.a(parcel, 2, hjVar.getExpirationTime());
        b.a(parcel, 3, hjVar.gn());
        b.a(parcel, 4, hjVar.getLatitude());
        b.a(parcel, 5, hjVar.getLongitude());
        b.a(parcel, 6, hjVar.go());
        b.c(parcel, 7, hjVar.gp());
        b.c(parcel, 8, hjVar.getNotificationResponsiveness());
        b.c(parcel, 9, hjVar.gq());
        b.D(parcel, p);
    }

    public hj av(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        String str = null;
        int i2 = 0;
        short s = (short) 0;
        double d = 0.0d;
        double d2 = 0.0d;
        float f = 0.0f;
        long j = 0;
        int i3 = 0;
        int i4 = -1;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    str = a.m(parcel, n);
                    break;
                case 2:
                    j = a.h(parcel, n);
                    break;
                case 3:
                    s = a.f(parcel, n);
                    break;
                case 4:
                    d = a.k(parcel, n);
                    break;
                case 5:
                    d2 = a.k(parcel, n);
                    break;
                case 6:
                    f = a.j(parcel, n);
                    break;
                case 7:
                    i2 = a.g(parcel, n);
                    break;
                case 8:
                    i3 = a.g(parcel, n);
                    break;
                case 9:
                    i4 = a.g(parcel, n);
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
            return new hj(i, str, i2, s, d, d2, f, j, i3, i4);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public hj[] bp(int i) {
        return new hj[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return av(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return bp(x0);
    }
}
