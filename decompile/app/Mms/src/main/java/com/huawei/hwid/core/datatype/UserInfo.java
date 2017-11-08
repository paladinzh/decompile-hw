package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class UserInfo implements Parcelable {
    public static final Creator CREATOR = new l();
    private String A;
    private String B;
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;
    private String i;
    private String j;
    private String k;
    private String l;
    private String m;
    private String n;
    private String o;
    private String p;
    private String q;
    private String r;
    private String s;
    private String t;
    private String u;
    private String v;
    private String w;
    private String x;
    private String y;
    private String z;

    public void setUserValidStatus(String str) {
        this.r = str;
    }

    public void setInviterUserID(String str) {
        this.s = str;
    }

    public void setInviter(String str) {
        this.t = str;
    }

    public void setUpdateTime(String str) {
        this.u = str;
    }

    public void setNickName(String str) {
        this.a = str;
    }

    public void setLanguageCode(String str) {
        this.b = str;
    }

    public void setFirstName(String str) {
        this.c = str;
    }

    public void setLastName(String str) {
        this.d = str;
    }

    public void setUserState(String str) {
        this.e = str;
    }

    public void setGender(String str) {
        this.f = str;
    }

    public void setBirthDate(String str) {
        this.g = str;
    }

    public void setAddress(String str) {
        this.h = str;
    }

    public void setOccupation(String str) {
        this.i = str;
    }

    public String getHeadPictureURL() {
        return this.j;
    }

    public void setHeadPictureURL(String str) {
        this.j = str;
    }

    public void setNationalCode(String str) {
        this.k = str;
    }

    public void setProvince(String str) {
        this.l = str;
    }

    public void setCity(String str) {
        this.m = str;
    }

    public void setPasswordPrompt(String str) {
        this.n = str;
    }

    public void setPasswordAnwser(String str) {
        this.o = str;
    }

    public void setCloudAccount(String str) {
        this.p = str;
    }

    public void setServiceFlag(String str) {
        this.q = str;
    }

    public void setLoginUserName(String str) {
        this.v = str;
    }

    public void setLoginUserNameFlag(String str) {
        this.w = str;
    }

    public void setuserStatusFlags(String str) {
        this.x = str;
    }

    public void settwoStepVerify(String str) {
        this.y = str;
    }

    public void settwoStepTime(String str) {
        this.z = str;
    }

    public void setResetPasswdMode(String str) {
        this.A = str;
    }

    public int describeContents() {
        return 0;
    }

    public void setUserSign(String str) {
        this.B = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.h);
        parcel.writeString(this.g);
        parcel.writeString(this.m);
        parcel.writeString(this.p);
        parcel.writeString(this.c);
        parcel.writeString(this.f);
        parcel.writeString(this.j);
        parcel.writeString(this.b);
        parcel.writeString(this.d);
        parcel.writeString(this.k);
        parcel.writeString(this.a);
        parcel.writeString(this.i);
        parcel.writeString(this.o);
        parcel.writeString(this.n);
        parcel.writeString(this.l);
        parcel.writeString(this.q);
        parcel.writeString(this.e);
        parcel.writeString(this.r);
        parcel.writeString(this.s);
        parcel.writeString(this.t);
        parcel.writeString(this.u);
        parcel.writeString(this.v);
        parcel.writeString(this.w);
        parcel.writeString(this.x);
        parcel.writeString(this.y);
        parcel.writeString(this.z);
        parcel.writeString(this.A);
        parcel.writeString(this.B);
    }
}
