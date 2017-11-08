package com.google.android.gms.wallet;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.identity.intents.model.CountrySpecification;
import java.util.ArrayList;

/* compiled from: Unknown */
public final class MaskedWalletRequest implements SafeParcelable {
    public static final Creator<MaskedWalletRequest> CREATOR = new l();
    boolean YY;
    boolean YZ;
    String Yg;
    String Yl;
    Cart Yu;
    boolean Za;
    String Zb;
    String Zc;
    boolean Zd;
    boolean Ze;
    CountrySpecification[] Zf;
    boolean Zg;
    boolean Zh;
    ArrayList<CountrySpecification> Zi;
    private final int wj;

    MaskedWalletRequest() {
        this.wj = 3;
        this.Zg = true;
        this.Zh = true;
    }

    MaskedWalletRequest(int versionCode, String merchantTransactionId, boolean phoneNumberRequired, boolean shippingAddressRequired, boolean useMinimalBillingAddress, String estimatedTotalPrice, String currencyCode, String merchantName, Cart cart, boolean shouldRetrieveWalletObjects, boolean isBillingAgreement, CountrySpecification[] allowedShippingCountrySpecifications, boolean allowPrepaidCard, boolean allowDebitCard, ArrayList<CountrySpecification> allowedCountrySpecificationsForShipping) {
        this.wj = versionCode;
        this.Yl = merchantTransactionId;
        this.YY = phoneNumberRequired;
        this.YZ = shippingAddressRequired;
        this.Za = useMinimalBillingAddress;
        this.Zb = estimatedTotalPrice;
        this.Yg = currencyCode;
        this.Zc = merchantName;
        this.Yu = cart;
        this.Zd = shouldRetrieveWalletObjects;
        this.Ze = isBillingAgreement;
        this.Zf = allowedShippingCountrySpecifications;
        this.Zg = allowPrepaidCard;
        this.Zh = allowDebitCard;
        this.Zi = allowedCountrySpecificationsForShipping;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel dest, int flags) {
        l.a(this, dest, flags);
    }
}
