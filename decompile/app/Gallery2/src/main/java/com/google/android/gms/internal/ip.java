package com.google.android.gms.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import java.util.HashSet;
import java.util.Set;

/* compiled from: Unknown */
public class ip implements Creator<io> {
    static void a(io ioVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        Set hB = ioVar.hB();
        if (hB.contains(Integer.valueOf(1))) {
            b.c(parcel, 1, ioVar.getVersionCode());
        }
        if (hB.contains(Integer.valueOf(2))) {
            b.a(parcel, 2, ioVar.getId(), true);
        }
        if (hB.contains(Integer.valueOf(4))) {
            b.a(parcel, 4, ioVar.hS(), i, true);
        }
        if (hB.contains(Integer.valueOf(5))) {
            b.a(parcel, 5, ioVar.getStartDate(), true);
        }
        if (hB.contains(Integer.valueOf(6))) {
            b.a(parcel, 6, ioVar.hT(), i, true);
        }
        if (hB.contains(Integer.valueOf(7))) {
            b.a(parcel, 7, ioVar.getType(), true);
        }
        b.D(parcel, p);
    }

    public io aH(Parcel parcel) {
        String str = null;
        int o = a.o(parcel);
        Set hashSet = new HashSet();
        int i = 0;
        im imVar = null;
        String str2 = null;
        im imVar2 = null;
        String str3 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            im imVar3;
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    hashSet.add(Integer.valueOf(1));
                    break;
                case 2:
                    str3 = a.m(parcel, n);
                    hashSet.add(Integer.valueOf(2));
                    break;
                case 4:
                    imVar3 = (im) a.a(parcel, n, im.CREATOR);
                    hashSet.add(Integer.valueOf(4));
                    imVar2 = imVar3;
                    break;
                case 5:
                    str2 = a.m(parcel, n);
                    hashSet.add(Integer.valueOf(5));
                    break;
                case 6:
                    imVar3 = (im) a.a(parcel, n, im.CREATOR);
                    hashSet.add(Integer.valueOf(6));
                    imVar = imVar3;
                    break;
                case 7:
                    str = a.m(parcel, n);
                    hashSet.add(Integer.valueOf(7));
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new io(hashSet, i, str3, imVar2, str2, imVar, str);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public io[] bE(int i) {
        return new io[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return aH(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return bE(x0);
    }
}
