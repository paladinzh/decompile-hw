package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import java.util.ArrayList;

/* compiled from: Unknown */
public class jk implements Creator<jj> {
    static void a(jj jjVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, jjVar.getVersionCode());
        b.a(parcel, 2, jjVar.ZA, false);
        b.a(parcel, 3, jjVar.ZB, false);
        b.b(parcel, 4, jjVar.ZC, false);
        b.D(parcel, p);
    }

    public jj bh(Parcel parcel) {
        String str = null;
        int o = a.o(parcel);
        ArrayList eH = fj.eH();
        int i = 0;
        String str2 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    str2 = a.m(parcel, n);
                    break;
                case 3:
                    str = a.m(parcel, n);
                    break;
                case 4:
                    eH = a.c(parcel, n, jh.CREATOR);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new jj(i, str2, str, eH);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public jj[] cn(int i) {
        return new jj[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return bh(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return cn(x0);
    }
}
