package com.huawei.rcs.commonInterface.metadata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class PeerInformation implements Parcelable {
    public static final Creator<PeerInformation> CREATOR = new Creator<PeerInformation>() {
        public PeerInformation createFromParcel(Parcel source) {
            PeerInformation PeerInformation = new PeerInformation();
            PeerInformation.name = source.readString();
            PeerInformation.number = source.readString();
            PeerInformation.uri = source.readString();
            return PeerInformation;
        }

        public PeerInformation[] newArray(int size) {
            return new PeerInformation[size];
        }
    };
    private String name;
    private String number;
    private String uri;

    public PeerInformation(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel arg0, int arg1) {
        arg0.writeString(this.name);
        arg0.writeString(this.number);
        arg0.writeString(this.uri);
    }
}
