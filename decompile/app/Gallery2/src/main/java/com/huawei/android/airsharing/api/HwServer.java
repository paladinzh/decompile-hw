package com.huawei.android.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwServer implements Parcelable {
    public static final Creator<HwServer> CREATOR = new Creator<HwServer>() {
        public HwServer[] newArray(int size) {
            return new HwServer[size];
        }

        public HwServer createFromParcel(Parcel source) {
            return new HwServer(source);
        }
    };
    private int mIPoint;
    private String mStrDescription;
    private String mStrIpAddr;
    private String mStrName;

    public String getName() {
        return this.mStrName;
    }

    public HwServer(Parcel pl) {
        this.mStrName = pl.readString();
        this.mStrDescription = pl.readString();
        this.mIPoint = pl.readInt();
        this.mStrIpAddr = pl.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(this.mStrName);
        arg0.writeString(this.mStrDescription);
        arg0.writeInt(this.mIPoint);
        arg0.writeString(this.mStrIpAddr);
    }
}
