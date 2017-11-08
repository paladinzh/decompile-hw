package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UserLoginInfo implements Parcelable {
    public static final Creator CREATOR = new m();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;

    public void setUserIDByUserLoginInfo(String str) {
        this.a = str;
    }

    public void setRegisterTime(String str) {
        this.b = str;
    }

    public void setUnRegisterTime(String str) {
        this.c = str;
    }

    public void setLastLoginTime(String str) {
        this.d = str;
    }

    public void setRegisterClientType(String str) {
        this.e = str;
    }

    public void setRegisterClientIP(String str) {
        this.f = str;
    }

    public void setRegisterFrom(String str) {
        this.g = str;
    }

    public void setLastLoginIP(String str) {
        this.h = str;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.h);
        parcel.writeString(this.d);
        parcel.writeString(this.f);
        parcel.writeString(this.e);
        parcel.writeString(this.g);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.a);
    }
}
