package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class g implements Creator<FullWalletRequest> {
    static void a(FullWalletRequest fullWalletRequest, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, fullWalletRequest.getVersionCode());
        b.a(parcel, 2, fullWalletRequest.Yk, false);
        b.a(parcel, 3, fullWalletRequest.Yl, false);
        b.a(parcel, 4, fullWalletRequest.Yu, i, false);
        b.D(parcel, p);
    }

    public FullWalletRequest aX(Parcel parcel) {
        Cart cart = null;
        int o = a.o(parcel);
        String str = null;
        int i = 0;
        String str2 = null;
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
                    cart = (Cart) a.a(parcel, n, Cart.CREATOR);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new FullWalletRequest(i, str, str2, cart);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public FullWalletRequest[] cd(int i) {
        return new FullWalletRequest[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return aX(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return cd(x0);
    }
}
