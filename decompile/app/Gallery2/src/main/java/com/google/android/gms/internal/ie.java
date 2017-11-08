package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class ie implements Creator<id> {
    static void a(id idVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, idVar.OG, false);
        b.c(parcel, 1000, idVar.versionCode);
        b.a(parcel, 2, idVar.OH, false);
        b.D(parcel, p);
    }

    public id aD(Parcel parcel) {
        String str = null;
        int o = a.o(parcel);
        int i = 0;
        String str2 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    str2 = a.m(parcel, n);
                    break;
                case 2:
                    str = a.m(parcel, n);
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
            return new id(i, str2, str);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public id[] by(int i) {
        return new id[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return aD(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return by(x0);
    }
}
