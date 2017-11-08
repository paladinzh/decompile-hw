package com.huawei.android.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public enum EHwTransportState implements Parcelable {
    INVALID(-1),
    STOPPED(1),
    PLAYING(0),
    PAUSED_PLAYBACK(2),
    PAUSED_RECODING(5),
    NO_MEDIA_PRESENT(4);
    
    public static final Creator<EHwTransportState> CREATOR = null;
    private int value;

    static {
        CREATOR = new Creator<EHwTransportState>() {
            public EHwTransportState createFromParcel(Parcel source) {
                return EHwTransportState.valueOf(source.readString());
            }

            public EHwTransportState[] newArray(int size) {
                return new EHwTransportState[size];
            }
        };
    }

    private EHwTransportState(int value) {
        this.value = value;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name());
    }
}
