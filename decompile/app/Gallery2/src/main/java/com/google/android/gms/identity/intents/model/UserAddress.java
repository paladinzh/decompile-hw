package com.google.android.gms.identity.intents.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public final class UserAddress implements SafeParcelable {
    public static final Creator<UserAddress> CREATOR = new b();
    String KB;
    String KC;
    String KD;
    String KE;
    String KF;
    String KG;
    String KH;
    String KI;
    String KJ;
    String KK;
    boolean KL;
    String KM;
    String KN;
    String name;
    String oQ;
    private final int wj;

    UserAddress() {
        this.wj = 1;
    }

    UserAddress(int versionCode, String name, String address1, String address2, String address3, String address4, String address5, String administrativeArea, String locality, String countryCode, String postalCode, String sortingCode, String phoneNumber, boolean isPostBox, String companyName, String emailAddress) {
        this.wj = versionCode;
        this.name = name;
        this.KB = address1;
        this.KC = address2;
        this.KD = address3;
        this.KE = address4;
        this.KF = address5;
        this.KG = administrativeArea;
        this.KH = locality;
        this.oQ = countryCode;
        this.KI = postalCode;
        this.KJ = sortingCode;
        this.KK = phoneNumber;
        this.KL = isPostBox;
        this.KM = companyName;
        this.KN = emailAddress;
    }

    public int describeContents() {
        return 0;
    }

    public int getVersionCode() {
        return this.wj;
    }

    public void writeToParcel(Parcel out, int flags) {
        b.a(this, out, flags);
    }
}
