package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public class ShareInfo implements Parcelable {
    public static final Creator<ShareInfo> CREATOR = new Creator<ShareInfo>() {
        public ShareInfo createFromParcel(Parcel source) {
            return new ShareInfo(source);
        }

        public ShareInfo[] newArray(int size) {
            return new ShareInfo[size];
        }
    };
    protected int countNum;
    protected long createTime;
    protected long flversion;
    protected int iversion;
    protected List<String> localThumbPath;
    protected String ownerAcc;
    protected String ownerId;
    protected String privilege;
    protected List<ShareReceiver> receiverList;
    protected String resource;
    protected String shareId;
    protected String shareName;
    protected String source;
    protected long totalSize;
    protected int type;

    public String getShareId() {
        return this.shareId;
    }

    public String getOwnerId() {
        return this.ownerId;
    }

    public String getShareName() {
        return this.shareName;
    }

    public void setShareName(String shareName) {
        this.shareName = shareName;
    }

    public String getOwnerAcc() {
        return this.ownerAcc;
    }

    public List<ShareReceiver> getReceiverList() {
        return this.receiverList;
    }

    public int getType() {
        return this.type;
    }

    public List<String> getLocalThumbPath() {
        return this.localThumbPath;
    }

    public String toString() {
        return "ShareInfo [shareID=" + this.shareId + ", ownerID=" + this.ownerId + ", shareName=" + this.shareName + ", ownerAcc=" + this.ownerAcc + ", receiverList=" + this.receiverList + ", type=" + this.type + ", countNum=" + this.countNum + ", resource=" + this.resource + ", localThumbPath=" + this.localThumbPath + ", createTime=" + this.createTime + ", source=" + this.source + ", flversion=" + this.flversion + ", iversion=" + this.iversion + ", privilege=" + this.privilege + ", totalSize=" + this.totalSize + "]";
    }

    private ShareInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.shareId = in.readString();
        this.ownerId = in.readString();
        this.shareName = in.readString();
        this.ownerAcc = in.readString();
        this.receiverList = in.readArrayList(ShareReceiver.CREATOR.getClass().getClassLoader());
        this.type = in.readInt();
        this.countNum = in.readInt();
        this.localThumbPath = in.readArrayList(ShareReceiver.CREATOR.getClass().getClassLoader());
        this.resource = in.readString();
        this.createTime = in.readLong();
        this.source = in.readString();
        this.flversion = in.readLong();
        this.iversion = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.shareId);
        dest.writeString(this.ownerId);
        dest.writeString(this.shareName);
        dest.writeString(this.ownerAcc);
        dest.writeList(this.receiverList);
        dest.writeInt(this.type);
        dest.writeInt(this.countNum);
        dest.writeList(this.localThumbPath);
        dest.writeString(this.resource);
        dest.writeLong(this.createTime);
        dest.writeString(this.source);
        dest.writeLong(this.flversion);
        dest.writeInt(this.iversion);
    }
}
