package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.hwid.core.d.b;
import com.huawei.hwid.core.d.b.e;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TmemberRight implements Parcelable {
    public static final Creator<TmemberRight> CREATOR = new i();
    private long a;
    private int b;
    private String c;
    private String d;
    private String e;
    private int f;
    private String g;
    private String h;

    public void a(long j) {
        this.a = j;
    }

    public void a(int i) {
        this.b = i;
    }

    public void a(String str) {
        this.c = str;
    }

    public void b(String str) {
        this.d = this.c;
    }

    public void c(String str) {
        this.e = str;
    }

    public void d(String str) {
        try {
            this.f = Integer.parseInt(str);
        } catch (Exception e) {
            e.b("TmemberRight", e.getMessage());
        }
    }

    public void e(String str) {
        this.g = str;
    }

    public void f(String str) {
        String a;
        String str2 = "";
        try {
            a = b.a(str, "yyyy-MM-dd", "yyyyMMdd");
        } catch (Exception e) {
            e.c("TmemberRight", e.getMessage());
            a = str2;
        }
        this.h = a;
    }

    public static void a(XmlPullParser xmlPullParser, TmemberRight tmemberRight, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && tmemberRight != null && str != null) {
            if ("userID".equals(str)) {
                tmemberRight.a(Long.parseLong(xmlPullParser.nextText()));
            } else if ("deviceType".equals(str)) {
                try {
                    tmemberRight.a(Integer.parseInt(xmlPullParser.nextText()));
                } catch (Exception e) {
                    e.b("TmemberRight", e.getMessage());
                }
            } else if ("deviceId".equals(str)) {
                tmemberRight.a(xmlPullParser.nextText());
            } else if ("deviceID2".equals(str)) {
                tmemberRight.b(xmlPullParser.nextText());
            } else if ("terminalType".equals(str)) {
                tmemberRight.c(xmlPullParser.nextText());
            } else if ("rightsID".equals(str)) {
                tmemberRight.d(xmlPullParser.nextText());
            } else if ("memberBindTime".equals(str)) {
                tmemberRight.e(xmlPullParser.nextText());
            } else if ("expiredDate".equals(str)) {
                tmemberRight.f(xmlPullParser.nextText());
            } else {
                e.b("TmemberRight", "in getTmemberRightTag nodeName:" + str + " is unknow");
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(this.a);
        parcel.writeInt(this.b);
        parcel.writeString(this.c);
        parcel.writeString(this.d);
        parcel.writeString(this.e);
        parcel.writeInt(this.f);
        parcel.writeString(this.g);
        parcel.writeString(this.h);
    }
}
