package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.identity.intents.model.UserAddress;

/* compiled from: Unknown */
public class k implements Creator<MaskedWallet> {
    static void a(MaskedWallet maskedWallet, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, maskedWallet.getVersionCode());
        b.a(parcel, 2, maskedWallet.Yk, false);
        b.a(parcel, 3, maskedWallet.Yl, false);
        b.a(parcel, 4, maskedWallet.Yq, false);
        b.a(parcel, 5, maskedWallet.Yn, false);
        b.a(parcel, 6, maskedWallet.Yo, i, false);
        b.a(parcel, 7, maskedWallet.Yp, i, false);
        b.a(parcel, 8, maskedWallet.YW, i, false);
        b.a(parcel, 9, maskedWallet.YX, i, false);
        b.a(parcel, 10, maskedWallet.Yr, i, false);
        b.a(parcel, 11, maskedWallet.Ys, i, false);
        b.a(parcel, 12, maskedWallet.Yt, i, false);
        b.D(parcel, p);
    }

    public MaskedWallet bb(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        String str = null;
        String str2 = null;
        String[] strArr = null;
        String str3 = null;
        Address address = null;
        Address address2 = null;
        LoyaltyWalletObject[] loyaltyWalletObjectArr = null;
        OfferWalletObject[] offerWalletObjectArr = null;
        UserAddress userAddress = null;
        UserAddress userAddress2 = null;
        InstrumentInfo[] instrumentInfoArr = null;
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
                    strArr = a.x(parcel, n);
                    break;
                case 5:
                    str3 = a.m(parcel, n);
                    break;
                case 6:
                    address = (Address) a.a(parcel, n, Address.CREATOR);
                    break;
                case 7:
                    address2 = (Address) a.a(parcel, n, Address.CREATOR);
                    break;
                case 8:
                    loyaltyWalletObjectArr = (LoyaltyWalletObject[]) a.b(parcel, n, LoyaltyWalletObject.CREATOR);
                    break;
                case 9:
                    offerWalletObjectArr = (OfferWalletObject[]) a.b(parcel, n, OfferWalletObject.CREATOR);
                    break;
                case 10:
                    userAddress = (UserAddress) a.a(parcel, n, UserAddress.CREATOR);
                    break;
                case 11:
                    userAddress2 = (UserAddress) a.a(parcel, n, UserAddress.CREATOR);
                    break;
                case 12:
                    instrumentInfoArr = (InstrumentInfo[]) a.b(parcel, n, InstrumentInfo.CREATOR);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new MaskedWallet(i, str, str2, strArr, str3, address, address2, loyaltyWalletObjectArr, offerWalletObjectArr, userAddress, userAddress2, instrumentInfoArr);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public MaskedWallet[] ch(int i) {
        return new MaskedWallet[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return bb(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ch(x0);
    }
}
