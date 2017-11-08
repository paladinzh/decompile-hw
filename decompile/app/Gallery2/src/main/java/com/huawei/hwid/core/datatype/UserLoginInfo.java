package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class UserLoginInfo implements Parcelable {
    public static final Creator<UserLoginInfo> CREATOR = new l();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;
    private String h;

    private void a(String str) {
        this.a = str;
    }

    private void b(String str) {
        this.b = str;
    }

    private void c(String str) {
        this.c = str;
    }

    private void d(String str) {
        this.d = str;
    }

    private void e(String str) {
        this.e = str;
    }

    private void f(String str) {
        this.f = str;
    }

    private void g(String str) {
        this.g = str;
    }

    private void h(String str) {
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

    public static void getUserLoginInfoInTag(XmlPullParser xmlPullParser, UserLoginInfo userLoginInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && userLoginInfo != null && str != null) {
            if ("userID".equals(str)) {
                userLoginInfo.a(xmlPullParser.nextText());
            } else if ("registerTime".equals(str)) {
                userLoginInfo.b(xmlPullParser.nextText());
            } else if ("unRegisterTime".equals(str)) {
                userLoginInfo.c(xmlPullParser.nextText());
            } else if ("lastLoginTime".equals(str)) {
                userLoginInfo.d(xmlPullParser.nextText());
            } else if ("registerClientType".equals(str)) {
                userLoginInfo.e(xmlPullParser.nextText());
            } else if ("lastLoginIP".equals(str)) {
                userLoginInfo.h(xmlPullParser.nextText());
            } else if ("registerClientIP".equals(str)) {
                userLoginInfo.f(xmlPullParser.nextText());
            } else if ("registerFrom".equals(str)) {
                userLoginInfo.g(xmlPullParser.nextText());
            }
        }
    }
}
