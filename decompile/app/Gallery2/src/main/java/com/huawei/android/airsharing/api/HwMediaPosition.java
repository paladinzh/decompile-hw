package com.huawei.android.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwMediaPosition implements Parcelable {
    public static final Creator<HwMediaPosition> CREATOR = new Creator<HwMediaPosition>() {
        public HwMediaPosition[] newArray(int size) {
            return new HwMediaPosition[size];
        }

        public HwMediaPosition createFromParcel(Parcel source) {
            HwMediaPosition position = new HwMediaPosition();
            position.setTrackDur(source.readString());
            position.setTrackMetaData(source.readString());
            position.setTrackURI(source.readString());
            position.setRelTime(source.readString());
            return position;
        }
    };
    private String relTime;
    private String trackDur;
    private String trackMetaData;
    private String trackURI;

    public String getTrackDur() {
        return this.trackDur;
    }

    public void setTrackDur(String trackDur) {
        this.trackDur = trackDur;
    }

    public void setTrackMetaData(String trackMetaData) {
        this.trackMetaData = trackMetaData;
    }

    public void setTrackURI(String trackURI) {
        this.trackURI = trackURI;
    }

    public String getRelTime() {
        return this.relTime;
    }

    public void setRelTime(String relTime) {
        this.relTime = relTime;
    }

    public String toString() {
        return "PositionInfo [trackDur=" + this.trackDur + ", trackMetaData=" + this.trackMetaData + ", trackURI=" + this.trackURI + ", relTime=" + this.relTime + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.trackDur);
        dest.writeString(this.trackMetaData);
        dest.writeString(this.trackURI);
        dest.writeString(this.relTime);
    }
}
