package com.huawei.mms.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class NameMatchResult implements Parcelable {
    public static final Creator<NameMatchResult> CREATOR = new Creator<NameMatchResult>() {
        public NameMatchResult createFromParcel(Parcel source) {
            NameMatchResult result = new NameMatchResult();
            result.contactId = source.readLong();
            result.contactName = source.readString();
            return result;
        }

        public NameMatchResult[] newArray(int size) {
            return new NameMatchResult[size];
        }
    };
    public long contactId;
    public String contactName;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int arg1) {
        parcel.writeLong(this.contactId);
        parcel.writeString(this.contactName);
    }
}
