package com.android.contacts.hap.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.android.contacts.util.KeepContactsMethodAnnotation;

public class NumberMarkInfo implements Parcelable {
    public static final Creator<NumberMarkInfo> CREATOR = new Creator<NumberMarkInfo>() {
        public NumberMarkInfo createFromParcel(Parcel source) {
            boolean z;
            boolean z2;
            boolean z3 = true;
            String readString = source.readString();
            String readString2 = source.readString();
            String readString3 = source.readString();
            String readString4 = source.readString();
            if (source.readInt() == 1) {
                z = true;
            } else {
                z = false;
            }
            String readString5 = source.readString();
            if (source.readInt() == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            if (source.readInt() != 1) {
                z3 = false;
            }
            return new NumberMarkInfo(readString, readString2, readString3, readString4, z, readString5, z2, z3, source.readInt());
        }

        public NumberMarkInfo[] newArray(int size) {
            return new NumberMarkInfo[size];
        }
    };
    String attribute;
    String classify;
    String description = "";
    String errorMsg = "";
    boolean isCloudMark;
    boolean isVerified;
    boolean isVip;
    int markedCount;
    String name;
    String number;
    String supplier;
    String vipMsg;

    public NumberMarkInfo(String aNumber, String aName, String aClassify, int aMarkedCount, boolean aIsCloudMark) {
        this.number = aNumber;
        this.name = aName;
        this.classify = aClassify;
        this.markedCount = aMarkedCount;
        this.isCloudMark = aIsCloudMark;
    }

    @KeepContactsMethodAnnotation
    public NumberMarkInfo(String aNumber, String aAttribute, String aName, String aClassify, boolean aIsCloudMark, int aMarkedCount, String aSupplier) {
        this.number = aNumber;
        this.attribute = aAttribute;
        this.name = aName;
        this.classify = aClassify;
        this.isCloudMark = aIsCloudMark;
        this.markedCount = aMarkedCount;
        this.supplier = aSupplier;
    }

    @KeepContactsMethodAnnotation
    public NumberMarkInfo(String aNumber, String aAttribute, String aName, String aClassify, boolean aIsCloudMark, int aMarkedCount, String aSupplier, String aDescription) {
        this.number = aNumber;
        this.attribute = aAttribute;
        this.name = aName;
        this.classify = aClassify;
        this.isCloudMark = aIsCloudMark;
        this.markedCount = aMarkedCount;
        this.supplier = aSupplier;
        this.description = aDescription;
    }

    @KeepContactsMethodAnnotation
    public NumberMarkInfo(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public NumberMarkInfo(String aNumber, String aAttribute, String aName, String aClassify, boolean aIsVip, String aVipMsg, boolean aIsVerified, boolean aIsCloudMark, int aMarkedCount) {
        this.number = aNumber;
        this.attribute = aAttribute;
        this.name = aName;
        this.classify = aClassify;
        this.isVip = aIsVip;
        this.vipMsg = aVipMsg;
        this.isVerified = aIsVerified;
        this.isCloudMark = aIsCloudMark;
        this.markedCount = aMarkedCount;
    }

    public String getNumber() {
        return this.number;
    }

    public String getName() {
        return this.name;
    }

    public String getClassify() {
        return this.classify;
    }

    public int getMarkedCount() {
        return this.markedCount;
    }

    public boolean isCloudMark() {
        return this.isCloudMark;
    }

    public void setMarkedCount(int markedCount) {
        this.markedCount = markedCount;
    }

    public String getSupplier() {
        return this.supplier;
    }

    public String getErrorMsg() {
        return this.errorMsg;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isBrandInfo() {
        if (this.isCloudMark && this.markedCount <= 0 && ("brand".equals(this.classify) || "w3".equals(this.classify))) {
            return true;
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        dest.writeString(this.number);
        dest.writeString(this.attribute);
        dest.writeString(this.name);
        dest.writeString(this.classify);
        if (this.isVip) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        dest.writeString(this.vipMsg);
        if (this.isVerified) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
        if (!this.isCloudMark) {
            i2 = 0;
        }
        dest.writeInt(i2);
        dest.writeInt(this.markedCount);
    }
}
