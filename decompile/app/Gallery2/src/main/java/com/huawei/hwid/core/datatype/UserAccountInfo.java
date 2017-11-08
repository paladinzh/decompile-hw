package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UserAccountInfo implements Parcelable {
    public static final Creator<UserAccountInfo> CREATOR = new j();
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

    private void a(String str) {
        this.d = str;
    }

    private void b(String str) {
        this.e = str;
    }

    private void c(String str) {
        this.f = str;
    }

    private void d(String str) {
        this.g = str;
    }

    private void e(String str) {
        this.h = str;
    }

    private void f(String str) {
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

    public static void getUserAccInfoInTag(XmlPullParser xmlPullParser, UserAccountInfo userAccountInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && userAccountInfo != null && str != null) {
            if ("accountState".equals(str)) {
                userAccountInfo.setAccountState(xmlPullParser.nextText());
            } else if ("accountType".equals(str)) {
                userAccountInfo.setAccountType(xmlPullParser.nextText());
            } else if ("accountValidStatus".equals(str)) {
                userAccountInfo.a(xmlPullParser.nextText());
            } else if ("updateTime".equals(str)) {
                userAccountInfo.b(xmlPullParser.nextText());
            } else if ("userAccount".equals(str)) {
                userAccountInfo.setUserAccount(xmlPullParser.nextText());
            } else if ("userEMail".equals(str)) {
                userAccountInfo.c(xmlPullParser.nextText());
            } else if ("mobilePhone".equals(str)) {
                userAccountInfo.d(xmlPullParser.nextText());
            } else if ("emailState".equals(str)) {
                userAccountInfo.e(xmlPullParser.nextText());
            } else if ("mobilePhoneState".equals(str)) {
                userAccountInfo.f(xmlPullParser.nextText());
            }
        }
    }
}
