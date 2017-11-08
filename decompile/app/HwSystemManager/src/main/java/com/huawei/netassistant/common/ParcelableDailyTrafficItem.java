package com.huawei.netassistant.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ParcelableDailyTrafficItem implements Parcelable {
    public static final Creator<ParcelableDailyTrafficItem> CREATOR = new Creator<ParcelableDailyTrafficItem>() {
        public ParcelableDailyTrafficItem createFromParcel(Parcel in) {
            return new ParcelableDailyTrafficItem(in.readString(), in.readLong());
        }

        public ParcelableDailyTrafficItem[] newArray(int size) {
            return new ParcelableDailyTrafficItem[size];
        }
    };
    public long mDailyTraffic;
    public String mDate;

    public ParcelableDailyTrafficItem(String date, long dailyTraffic) {
        this.mDate = date;
        this.mDailyTraffic = dailyTraffic;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDate);
        dest.writeLong(this.mDailyTraffic);
    }
}
