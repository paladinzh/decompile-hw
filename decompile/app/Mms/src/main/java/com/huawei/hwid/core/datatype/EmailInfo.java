package com.huawei.hwid.core.datatype;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class EmailInfo implements Parcelable {
    public static final Creator CREATOR = new e();
    private String a;
    private String b;

    public EmailInfo(String str, String str2) {
        this.a = str;
        this.b = str2;
    }

    private EmailInfo() {
    }

    public String a() {
        return this.a;
    }

    public String toString() {
        return "[" + this.a + "," + this.b + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.a);
        parcel.writeString(this.b);
    }
}
