package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class TagFileInfo implements Parcelable {
    public static final Creator<TagFileInfo> CREATOR = new Creator<TagFileInfo>() {
        public TagFileInfo createFromParcel(Parcel source) {
            return new TagFileInfo(source);
        }

        public TagFileInfo[] newArray(int size) {
            return new TagFileInfo[size];
        }
    };
    protected List<String> albumList;
    protected String categoryId;
    protected long createTime;
    protected String faceFileId;
    protected String faceId;
    protected String faceUrl;
    protected String fileId;
    protected String fileUrl;
    protected String hash;
    protected int height;
    protected String localBigThumbPath;
    protected String localRealPath;
    protected String localThumbPath;
    protected int nlinks;
    protected String otype;
    protected String spConfidence;
    protected String tagId;
    protected String thumbUrl;
    protected long tversion;
    protected int width;
    protected int x;
    protected int y;

    public String getHash() {
        return this.hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public List<String> getAlbumList() {
        return this.albumList;
    }

    public String getTagId() {
        return this.tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getThumbUrl() {
        return this.thumbUrl;
    }

    public String getFaceId() {
        return this.faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getSpConfidence() {
        return this.spConfidence;
    }

    public String toString() {
        return "TagFileInfo [hash=" + this.hash + ", fileID=" + this.fileId + ", createTime=" + this.createTime + ", albumList=" + this.albumList + ", tagId=" + this.tagId + ", categoryId=" + this.categoryId + ", thumbUrl=" + this.thumbUrl + ", faceId=" + this.faceId + ", x=" + this.x + ", y=" + this.y + ", width=" + this.width + ", height=" + this.height + ", faceFileId=" + this.faceFileId + ", tversion=" + this.tversion + ", otype=" + this.otype + ", nlinks=" + this.nlinks + ", localThumbPath=" + this.localThumbPath + ", localBigThumbPath=" + this.localBigThumbPath + ", localRealPath=" + this.localRealPath + ", fileUrl=" + this.fileUrl + ", faceUrl=" + this.faceUrl + "spConfidence=" + this.spConfidence + "]";
    }

    private TagFileInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.hash = in.readString();
        this.fileId = in.readString();
        this.createTime = in.readLong();
        this.albumList = new ArrayList();
        in.readList(this.albumList, ClassLoader.getSystemClassLoader());
        this.tagId = in.readString();
        this.categoryId = in.readString();
        this.thumbUrl = in.readString();
        this.faceId = in.readString();
        this.x = in.readInt();
        this.y = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
        this.faceFileId = in.readString();
        this.tversion = in.readLong();
        this.localThumbPath = in.readString();
        this.localBigThumbPath = in.readString();
        this.localRealPath = in.readString();
        this.nlinks = in.readInt();
        this.spConfidence = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.hash);
        dest.writeString(this.fileId);
        dest.writeLong(this.createTime);
        dest.writeList(this.albumList);
        dest.writeString(this.tagId);
        dest.writeString(this.categoryId);
        dest.writeString(this.thumbUrl);
        dest.writeString(this.faceId);
        dest.writeInt(this.x);
        dest.writeInt(this.y);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.faceFileId);
        dest.writeLong(this.tversion);
        dest.writeString(this.localThumbPath);
        dest.writeString(this.localBigThumbPath);
        dest.writeString(this.localRealPath);
        dest.writeInt(this.nlinks);
        dest.writeString(this.spConfidence);
    }
}
