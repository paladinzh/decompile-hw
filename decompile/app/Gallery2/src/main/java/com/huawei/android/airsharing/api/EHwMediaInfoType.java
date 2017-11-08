package com.huawei.android.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum EHwMediaInfoType implements Parcelable {
    IMAGE,
    AUDIO,
    VIDEO,
    FOLDER,
    IMAGE_VIDEO;
    
    public static final Creator<EHwMediaInfoType> CREATOR = null;

    static {
        CREATOR = new Creator<EHwMediaInfoType>() {
            public EHwMediaInfoType createFromParcel(Parcel source) {
                return EHwMediaInfoType.valueOf(source.readString());
            }

            public EHwMediaInfoType[] newArray(int size) {
                return new EHwMediaInfoType[size];
            }
        };
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
