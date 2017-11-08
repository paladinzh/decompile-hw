package com.huawei.hwid.core.datatype;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.huawei.hwid.core.c.d;
import com.huawei.hwid.core.c.k;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhoneNumInfo implements Parcelable {
    public static final Creator CREATOR = new g();
    private String a;
    private String b;
    private String c;
    private Context d;

    public PhoneNumInfo(Context context, String str, String str2) {
        this.d = context;
        this.a = str;
        this.c = str2;
        c();
    }

    private void c() {
        String a = a(this.a);
        if (!b(a)) {
            this.b = a;
        }
    }

    public String a(String str) {
        List<SMSCountryInfo> b = k.b(this.d);
        Set<String> hashSet = new HashSet();
        for (SMSCountryInfo a : b) {
            hashSet.add(d.d(a.a()));
        }
        String str2 = "";
        List<String> arrayList = new ArrayList();
        String d = d.d(str);
        if (!b(d)) {
            String str3;
            for (String str32 : hashSet) {
                if (d.startsWith(str32)) {
                    arrayList.add(str32);
                }
            }
            for (String str322 : arrayList) {
                if (str322.length() <= str2.length()) {
                    str322 = str2;
                }
                str2 = str322;
            }
        }
        return str2;
    }

    private static boolean b(String str) {
        if (str != null && str.trim().length() >= 1) {
            return false;
        }
        return true;
    }

    public String a() {
        return this.b;
    }

    public String b() {
        return this.a;
    }

    public String toString() {
        return "[" + this.b + ", " + this.a + ", " + this.c + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.b);
        parcel.writeString(this.a);
        parcel.writeString(this.c);
    }
}
