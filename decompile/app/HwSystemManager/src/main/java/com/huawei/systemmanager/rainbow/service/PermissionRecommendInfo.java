package com.huawei.systemmanager.rainbow.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class PermissionRecommendInfo implements Parcelable {
    public static final Creator<PermissionRecommendInfo> CREATOR = new Creator<PermissionRecommendInfo>() {
        public PermissionRecommendInfo createFromParcel(Parcel source) {
            return new PermissionRecommendInfo(source);
        }

        public PermissionRecommendInfo[] newArray(int size) {
            return new PermissionRecommendInfo[size];
        }
    };
    public boolean mInitRecommendStatus;
    public List<String> mRecommendInfoList;

    public PermissionRecommendInfo() {
        this.mInitRecommendStatus = false;
        this.mRecommendInfoList = new ArrayList();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        dest.writeByte(this.mInitRecommendStatus ? (byte) 1 : (byte) 0);
        dest.writeStringList(this.mRecommendInfoList);
    }

    private PermissionRecommendInfo(Parcel source) {
        boolean z = true;
        this.mInitRecommendStatus = false;
        this.mRecommendInfoList = new ArrayList();
        if (source.readByte() != (byte) 1) {
            z = false;
        }
        this.mInitRecommendStatus = z;
        source.readStringList(this.mRecommendInfoList);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" mInitRecommendStatus: " + this.mInitRecommendStatus);
        sb.append(" mRecommendInfoList: " + (this.mRecommendInfoList == null ? this.mRecommendInfoList : this.mRecommendInfoList.toString()));
        return sb.toString();
    }
}
