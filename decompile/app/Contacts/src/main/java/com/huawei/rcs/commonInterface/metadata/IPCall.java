package com.huawei.rcs.commonInterface.metadata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class IPCall implements Parcelable {
    public static final Creator<IPCall> CREATOR = new Creator<IPCall>() {
        public IPCall createFromParcel(Parcel source) {
            IPCall ipCall = new IPCall(source.readInt());
            ipCall.setName(source.readString());
            ipCall.setNumber(source.readString());
            ipCall.setNamePresentation(source.readInt());
            ipCall.setNumberPresentation(source.readInt());
            ipCall.setUri(source.readString());
            ipCall.setType(source.readInt());
            ipCall.setStatus(source.readInt());
            ipCall.setIsMt(source.readInt());
            ipCall.setToa(source.readInt());
            ipCall.setSessionID(source.readLong());
            return ipCall;
        }

        public IPCall[] newArray(int size) {
            return new IPCall[size];
        }
    };
    private int index;
    private int isMt;
    private String name;
    private int namePresentation;
    private String number;
    private int numberPresentation;
    private long sessionID;
    private int status;
    private int toa;
    private int type;
    private String uri;

    public IPCall(int index) {
        this.index = index;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setNamePresentation(int namePresentation) {
        this.namePresentation = namePresentation;
    }

    public void setNumberPresentation(int numberPresentation) {
        this.numberPresentation = numberPresentation;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setIsMt(int isMt) {
        this.isMt = isMt;
    }

    public void setToa(int toa) {
        this.toa = toa;
    }

    public void setSessionID(long sessionID) {
        this.sessionID = sessionID;
    }

    public String toString() {
        return "IPCall [index=" + this.index + ", name=" + this.name + ", number=" + this.number + ", namePresentation=" + this.namePresentation + ", numberPresentation=" + this.numberPresentation + ", uri=" + this.uri + ", type=" + this.type + ", status=" + this.status + ", isMt=" + this.isMt + ", toa=" + this.toa + ", sessionID=" + this.sessionID + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.index);
        dest.writeString(this.name);
        dest.writeString(this.number);
        dest.writeInt(this.namePresentation);
        dest.writeInt(this.numberPresentation);
        dest.writeString(this.uri);
        dest.writeInt(this.type);
        dest.writeInt(this.status);
        dest.writeInt(this.isMt);
        dest.writeInt(this.toa);
        dest.writeLong(this.sessionID);
    }
}
