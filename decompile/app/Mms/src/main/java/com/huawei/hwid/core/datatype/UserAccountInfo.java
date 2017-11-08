package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UserAccountInfo implements Parcelable {
    public static final Creator CREATOR = new k();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;

    public String getAccountType() {
        return this.a;
    }

    public void setAccountType(String str) {
        this.a = str;
    }

    public String getUserAccount() {
        return this.b;
    }

    public void setUserAccount(String str) {
        this.b = str;
    }

    public String getAccountState() {
        return this.c;
    }

    public void setAccountState(String str) {
        this.c = str;
    }

    public void setAccountValidStatus(String str) {
        this.d = str;
    }

    public void setUpdateTime(String str) {
        this.e = str;
    }

    public void setUserEMail(String str) {
        this.f = str;
    }

    public void setMobilePhone(String str) {
        this.g = str;
    }

    public void setUserEmailState(String str) {
        this.h = str;
    }

    public void setMobilePhoneState(String str) {
        this.i = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
        parcel.writeString(this.i);
    }
}
