package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class ShareReceiver implements Parcelable {
    public static final Creator<ShareReceiver> CREATOR = new Creator<ShareReceiver>() {
        public ShareReceiver createFromParcel(Parcel source) {
            return new ShareReceiver(source);
        }

        public ShareReceiver[] newArray(int size) {
            return new ShareReceiver[size];
        }
    };
    private String headPictureLocalPath;
    private String headPictureURL;
    private long lastUpdatePicTime;
    private int privilege;
    private String receiverAcc;
    private String receiverId;
    private String receiverName;
    private String shareId;
    private int status;

    public String getReceiverId() {
        return this.receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverAcc() {
        return this.receiverAcc;
    }

    public void setReceiverAcc(String receiverAcc) {
        this.receiverAcc = receiverAcc;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getShareId() {
        return this.shareId;
    }

    public String getReceiverName() {
        return this.receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String toString() {
        return "ShareReceiver [receiverID=" + this.receiverId + ", receiverAcc=" + this.receiverAcc + ", status=" + this.status + ", privilege=" + this.privilege + ", shareID=" + this.shareId + ", receiverName=" + this.receiverName + ", headPictureLocalPath=" + this.headPictureLocalPath + ", headPictureURL=" + this.headPictureURL + ", lastUpdatePicTime=" + this.lastUpdatePicTime + "]";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.receiverId);
        dest.writeString(this.receiverAcc);
        dest.writeInt(this.status);
        dest.writeInt(this.privilege);
        dest.writeString(this.shareId);
    }

    private ShareReceiver(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.receiverId = in.readString();
        this.receiverAcc = in.readString();
        this.status = in.readInt();
        this.privilege = in.readInt();
        this.shareId = in.readString();
    }
}
