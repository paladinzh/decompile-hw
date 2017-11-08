package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TagInfo implements Parcelable {
    public static final Creator<TagInfo> CREATOR = new Creator<TagInfo>() {
        public TagInfo createFromParcel(Parcel source) {
            return new TagInfo(source);
        }

        public TagInfo[] newArray(int size) {
            return new TagInfo[size];
        }
    };
    protected String categoryId;
    protected long createTime;
    protected String ext1;
    protected String faceFileId;
    protected int fileNum;
    protected String localPath;
    protected String tagId;
    protected String tagName;
    protected long version;

    public String getTagId() {
        return this.tagId;
    }

    public String getTagName() {
        return this.tagName;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public String getExt1() {
        return this.ext1;
    }

    public String toString() {
        return "TagInfo [tagId=" + this.tagId + ", tagName=" + this.tagName + ", createTime=" + this.createTime + ", categoryId=" + this.categoryId + ", faceFileId =" + this.faceFileId + ", version=" + this.version + ", fileNum =" + this.fileNum + ", localPath =" + this.localPath + ", ext1 =" + this.ext1 + "]";
    }

    private TagInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.tagId = in.readString();
        this.tagName = in.readString();
        this.createTime = in.readLong();
        this.categoryId = in.readString();
        this.faceFileId = in.readString();
        this.version = in.readLong();
        this.fileNum = in.readInt();
        this.localPath = in.readString();
        this.ext1 = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.tagId);
        dest.writeString(this.tagName);
        dest.writeLong(this.createTime);
        dest.writeString(this.categoryId);
        dest.writeString(this.faceFileId);
        dest.writeLong(this.version);
        dest.writeInt(this.fileNum);
        dest.writeString(this.localPath);
        dest.writeString(this.ext1);
    }
}
