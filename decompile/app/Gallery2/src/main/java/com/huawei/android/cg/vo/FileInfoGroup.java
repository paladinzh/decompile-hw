package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class FileInfoGroup implements Parcelable {
    public static final Creator<FileInfoGroup> CREATOR = new Creator<FileInfoGroup>() {
        public FileInfoGroup createFromParcel(Parcel source) {
            return new FileInfoGroup(source);
        }

        public FileInfoGroup[] newArray(int size) {
            return new FileInfoGroup[size];
        }
    };
    private String albumId;
    private long batchCtime;
    private int batchId;
    private String createrAccount;
    private String createrId;
    private String createrNickName;
    private int photoNum;
    private String userId;
    private int videoNum;

    public String getCreaterAccount() {
        return this.createrAccount;
    }

    public String getCreaterNickName() {
        return this.createrNickName;
    }

    public int getBatchId() {
        return this.batchId;
    }

    public long getBatchCtime() {
        return this.batchCtime;
    }

    public int getPhotoNum() {
        return this.photoNum;
    }

    public int getVideoNum() {
        return this.videoNum;
    }

    private FileInfoGroup(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.albumId = in.readString();
        this.userId = in.readString();
        this.createrId = in.readString();
        this.createrAccount = in.readString();
        this.createrNickName = in.readString();
        this.batchId = in.readInt();
        this.batchCtime = in.readLong();
        this.photoNum = in.readInt();
        this.videoNum = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.albumId);
        dest.writeString(this.userId);
        dest.writeString(this.createrId);
        dest.writeString(this.createrAccount);
        dest.writeString(this.createrNickName);
        dest.writeInt(this.batchId);
        dest.writeLong(this.batchCtime);
        dest.writeInt(this.photoNum);
        dest.writeInt(this.videoNum);
    }
}
