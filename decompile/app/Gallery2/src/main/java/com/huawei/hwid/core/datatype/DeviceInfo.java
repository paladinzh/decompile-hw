package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class DeviceInfo implements Parcelable {
    public static final Creator<DeviceInfo> CREATOR = new c();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g = "";

    public void setDeviceIdInDeviceInfo(String str) {
        this.b = str;
    }

    public void setDeviceType(String str) {
        this.a = str;
    }

    public void setTerminalType(String str) {
        this.c = str;
    }

    public void setDeviceAliasName(String str) {
        this.d = str;
    }

    public int describeContents() {
        return 0;
    }

    public void setmLoginTime(String str) {
        this.e = str;
    }

    public void setmLogoutTime(String str) {
        this.f = str;
    }

    public void setmFrequentlyUsed(String str) {
        this.g = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.b);
        parcel.writeString(this.d);
        parcel.writeString(this.a);
        parcel.writeString(this.c);
        parcel.writeString(this.e);
        parcel.writeString(this.f);
        parcel.writeString(this.g);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{'mDeviceAliasName':");
        stringBuilder.append(this.d);
        stringBuilder.append(",'mDeviceID':");
        stringBuilder.append(this.b);
        stringBuilder.append(",'mTerminalType':");
        stringBuilder.append(this.c);
        stringBuilder.append(",'mDeviceType':");
        stringBuilder.append(this.a);
        stringBuilder.append(",'mLoginTime':");
        stringBuilder.append(this.e);
        stringBuilder.append(",'mLogoutTime':");
        stringBuilder.append(this.f);
        stringBuilder.append(",'mFrequentlyUsed':");
        stringBuilder.append(this.g);
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    public static void getDeviceInfoInTag(XmlPullParser xmlPullParser, DeviceInfo deviceInfo, String str) throws XmlPullParserException, IOException {
        if (xmlPullParser != null && deviceInfo != null && str != null) {
            if ("deviceID".equals(str)) {
                deviceInfo.setDeviceIdInDeviceInfo(xmlPullParser.nextText());
            } else if ("deviceType".equals(str)) {
                deviceInfo.setDeviceType(xmlPullParser.nextText());
            } else if ("terminalType".equals(str)) {
                deviceInfo.setTerminalType(xmlPullParser.nextText());
            } else if ("deviceAliasName".equals(str)) {
                deviceInfo.setDeviceAliasName(xmlPullParser.nextText());
            } else if ("loginTime".equals(str)) {
                deviceInfo.setmLoginTime(xmlPullParser.nextText());
            } else if ("logoutTime".equals(str)) {
                deviceInfo.setmLogoutTime(xmlPullParser.nextText());
            } else if ("frequentlyUsed".equals(str)) {
                deviceInfo.setmFrequentlyUsed(xmlPullParser.nextText());
            }
        }
    }
}
