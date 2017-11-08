package com.huawei.android.totemweather.aidl;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RequestData implements Parcelable {
    public static final Creator<RequestData> CREATOR = new Creator<RequestData>() {
        public RequestData createFromParcel(Parcel p) {
            return new RequestData(p);
        }

        public RequestData[] newArray(int size) {
            return new RequestData[size];
        }
    };
    private boolean mAllDay = true;
    private String mCityId;
    private int mCityType = 2;
    private double mLatitude;
    private double mLongitude;
    private String mPackageName;
    private String mRequesetFlag;

    public RequestData(Parcel in) {
        readFromParcel(in);
    }

    public RequestData(Context context, double latitude, double longitude) {
        this.mPackageName = context.getPackageName();
        this.mRequesetFlag = latitude + "," + longitude;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
    }

    public String getmRequesetFlag() {
        return this.mRequesetFlag;
    }

    public void setmRequesetFlag(String mRequesetFlag) {
        this.mRequesetFlag = mRequesetFlag;
    }

    public void setmAllDay(boolean mAllDay) {
        this.mAllDay = mAllDay;
    }

    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel in) {
        boolean z = true;
        this.mPackageName = in.readString();
        this.mRequesetFlag = in.readString();
        this.mCityId = in.readString();
        this.mLatitude = in.readDouble();
        this.mLongitude = in.readDouble();
        this.mCityType = in.readInt();
        if (in.readInt() != 1) {
            z = false;
        }
        this.mAllDay = z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        int i;
        dest.writeString(this.mPackageName);
        dest.writeString(this.mRequesetFlag);
        dest.writeString(this.mCityId);
        dest.writeDouble(this.mLatitude);
        dest.writeDouble(this.mLongitude);
        dest.writeInt(this.mCityType);
        if (this.mAllDay) {
            i = 1;
        } else {
            i = 0;
        }
        dest.writeInt(i);
    }

    public String toString() {
        return "RequestData [mPackageName=" + this.mPackageName + ", mRequesetFlag=" + this.mRequesetFlag + ", mCityId=" + this.mCityId + ", mCityType=" + this.mCityType + ", mAllDay=" + this.mAllDay + "]";
    }
}
