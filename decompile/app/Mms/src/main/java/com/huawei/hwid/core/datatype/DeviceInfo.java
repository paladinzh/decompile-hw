package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class DeviceInfo implements Parcelable {
    public static final Creator CREATOR = new d();
    private String a;
    private String b;
    private String c;
    private String d;
    private String e;
    private String f;
    private String g;

    public void setDeviceIdInDeviceInfo(String str) {
        this.b = str;
    }

    public void setDeviceType(String str) {
        this.a = str;
    }

    public void setTerminalType(String str) {
        this.c = str;
    }

    public String getDeviceID() {
        return this.b;
    }

    public String getTerminalType() {
        return this.c;
    }

    public String getDeviceType() {
        return this.a;
    }

    public String getDeviceAliasName() {
        return this.d;
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
}
