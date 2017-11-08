package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.games.request.GameRequestEntity;
import java.util.ArrayList;

/* compiled from: Unknown */
public class ha implements Creator<gz> {
    static void a(gz gzVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.b(parcel, 1, gzVar.fT(), false);
        b.c(parcel, 1000, gzVar.getVersionCode());
        b.D(parcel, p);
    }

    public gz[] aY(int i) {
        return new gz[i];
    }

    public gz am(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        ArrayList arrayList = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    arrayList = a.c(parcel, n, GameRequestEntity.CREATOR);
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
            return new gz(i, arrayList);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return am(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aY(x0);
    }
}
