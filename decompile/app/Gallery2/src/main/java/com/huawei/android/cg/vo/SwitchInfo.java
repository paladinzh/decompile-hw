package com.huawei.android.cg.vo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SwitchInfo implements Parcelable {
    public static final Creator<SwitchInfo> CREATOR = new Creator<SwitchInfo>() {
        public SwitchInfo createFromParcel(Parcel source) {
            return new SwitchInfo(source);
        }

        public SwitchInfo[] newArray(int size) {
            return new SwitchInfo[size];
        }
    };
    private Bundle customAlbumSwitches;
    private boolean generalAlbumSwitch;
    private boolean shareAlbumSwitch;
    private boolean smartAlbumSwitch;

    public SwitchInfo() {
        this.customAlbumSwitches = new Bundle();
    }

    private SwitchInfo(Parcel in) {
        this.customAlbumSwitches = new Bundle();
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        boolean z;
        boolean z2 = true;
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.generalAlbumSwitch = z;
        if (in.readByte() != (byte) 0) {
            z = true;
        } else {
            z = false;
        }
        this.shareAlbumSwitch = z;
        if (in.readByte() == (byte) 0) {
            z2 = false;
        }
        this.smartAlbumSwitch = z2;
        this.customAlbumSwitches = in.readBundle();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        int i2 = 1;
        if (this.generalAlbumSwitch) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
        if (this.shareAlbumSwitch) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeByte((byte) i);
        if (!this.smartAlbumSwitch) {
            i2 = 0;
        }
        dest.writeByte((byte) i2);
        dest.writeBundle(this.customAlbumSwitches);
    }
}
