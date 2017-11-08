package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AccountInfo implements Parcelable {
    public static final Creator<AccountInfo> CREATOR = new Creator<AccountInfo>() {
        public AccountInfo createFromParcel(Parcel source) {
            return new AccountInfo(source);
        }

        public AccountInfo[] newArray(int size) {
            return new AccountInfo[size];
        }
    };
    private String accountName;
    private String authType;
    private String deviceId;
    private String deviceIdType;
    private String deviceType;
    private String nickName;
    private String serviceToken;
    private String siteId;
    private String userId;

    public String getAccountName() {
        return this.accountName;
    }

    public String getUserId() {
        return this.userId;
    }

    public String toString() {
        StringBuilder retStr = new StringBuilder(128);
        retStr.append("AccountInfo [accountName=").append(this.accountName).append(", nickName=").append(this.nickName).append(", serviceToken=").append(this.serviceToken).append(", deviceId=").append(this.deviceId).append(", deviceIdType=").append(this.deviceIdType).append(", deviceType=").append(this.deviceType).append(", userId=").append(this.userId).append(", siteId=").append(this.siteId).append("AuthType=").append(this.authType).append("]");
        return retStr.toString();
    }

    private AccountInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.userId = in.readString();
        this.accountName = in.readString();
        this.nickName = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.accountName);
        dest.writeString(this.nickName);
    }
}
