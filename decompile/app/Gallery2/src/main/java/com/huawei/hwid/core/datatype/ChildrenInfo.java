package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ChildrenInfo implements Parcelable {
    public static final Creator<ChildrenInfo> CREATOR = new b();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;

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
        this.f = str;
    }

    private void f(String str) {
        this.g = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
    }

    public static void a(XmlPullParser xmlPullParser, ChildrenInfo childrenInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && childrenInfo != null && str != null) {
            if ("childrenUserID".equals(str)) {
                childrenInfo.a(xmlPullParser.nextText());
            } else if ("birthDate".equals(str)) {
                childrenInfo.b(xmlPullParser.nextText());
            } else if ("uniquelyNickname".equals(str)) {
                childrenInfo.c(xmlPullParser.nextText());
            } else if ("headPictureURL".equals(str)) {
                childrenInfo.d(xmlPullParser.nextText());
            } else if ("accountname".equals(str)) {
                childrenInfo.d(xmlPullParser.nextText());
            } else if ("nickName".equals(str)) {
                childrenInfo.e(xmlPullParser.nextText());
            } else if ("loginUserName".equals(str)) {
                childrenInfo.f(xmlPullParser.nextText());
            }
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{'childrenUserID':");
        stringBuilder.append(this.a);
        stringBuilder.append(",'birthDate':");
        stringBuilder.append(this.b);
        stringBuilder.append(",'uniquelyNickname':");
        stringBuilder.append(this.c);
        stringBuilder.append(",'headPictureURL':");
        stringBuilder.append(this.e);
        stringBuilder.append(",'accountName':");
        stringBuilder.append(this.d);
        stringBuilder.append(this.f);
        stringBuilder.append(this.g);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public int describeContents() {
        return 0;
    }
}
