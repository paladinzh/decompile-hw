package com.huawei.systemmanager.comm.valueprefer;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ValuePair implements Parcelable {
    public static final Creator<ValuePair> CREATOR = new Creator<ValuePair>() {
        public ValuePair createFromParcel(Parcel in) {
            return new ValuePair(in);
        }

        public ValuePair[] newArray(int size) {
            return new ValuePair[size];
        }
    };
    private final String key;
    private final String value;

    public ValuePair(String key, boolean value) {
        this(key, String.valueOf(value));
    }

    public ValuePair(String key, int value) {
        this(key, String.valueOf(value));
    }

    public ValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    private ValuePair(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
    }

    public String getKey() {
        return this.key;
    }

    public String getValue() {
        return this.value;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.value);
    }
}
