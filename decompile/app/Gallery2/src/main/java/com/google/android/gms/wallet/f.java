package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.identity.intents.model.UserAddress;

/* compiled from: Unknown */
public class f implements Creator<FullWallet> {
    static void a(FullWallet fullWallet, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, fullWallet.getVersionCode());
        b.a(parcel, 2, fullWallet.Yk, false);
        b.a(parcel, 3, fullWallet.Yl, false);
        b.a(parcel, 4, fullWallet.Ym, i, false);
        b.a(parcel, 5, fullWallet.Yn, false);
        b.a(parcel, 6, fullWallet.Yo, i, false);
        b.a(parcel, 7, fullWallet.Yp, i, false);
        b.a(parcel, 8, fullWallet.Yq, false);
        b.a(parcel, 9, fullWallet.Yr, i, false);
        b.a(parcel, 10, fullWallet.Ys, i, false);
        b.a(parcel, 11, fullWallet.Yt, i, false);
        b.D(parcel, p);
    }

    public FullWallet aW(Parcel parcel) {
        InstrumentInfo[] instrumentInfoArr = null;
        int o = a.o(parcel);
        int i = 0;
        UserAddress userAddress = null;
        UserAddress userAddress2 = null;
        String[] strArr = null;
        Address address = null;
        Address address2 = null;
        String str = null;
        ProxyCard proxyCard = null;
        String str2 = null;
        String str3 = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    str3 = a.m(parcel, n);
                    break;
                case 3:
                    str2 = a.m(parcel, n);
                    break;
                case 4:
                    proxyCard = (ProxyCard) a.a(parcel, n, ProxyCard.CREATOR);
                    break;
                case 5:
                    str = a.m(parcel, n);
                    break;
                case 6:
                    address2 = (Address) a.a(parcel, n, Address.CREATOR);
                    break;
                case 7:
                    address = (Address) a.a(parcel, n, Address.CREATOR);
                    break;
                case 8:
                    strArr = a.x(parcel, n);
                    break;
                case 9:
                    userAddress2 = (UserAddress) a.a(parcel, n, UserAddress.CREATOR);
                    break;
                case 10:
                    userAddress = (UserAddress) a.a(parcel, n, UserAddress.CREATOR);
                    break;
                case 11:
                    instrumentInfoArr = (InstrumentInfo[]) a.b(parcel, n, InstrumentInfo.CREATOR);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new FullWallet(i, str3, str2, proxyCard, str, address2, address, strArr, userAddress2, userAddress, instrumentInfoArr);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public FullWallet[] cc(int i) {
        return new FullWallet[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return aW(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return cc(x0);
    }
}
