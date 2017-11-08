package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class CategoryInfo implements Parcelable {
    public static final Creator<CategoryInfo> CREATOR = new Creator<CategoryInfo>() {
        public CategoryInfo createFromParcel(Parcel source) {
            return new CategoryInfo(source);
        }

        public CategoryInfo[] newArray(int size) {
            return new CategoryInfo[size];
        }
    };
    protected List<String> albumList;
    protected String categoryId;
    protected String categoryName;
    protected long createTime;
    protected String hash;
    protected String localPath;
    protected int photoNum;
    protected int tagNum;

    public String getCategoryId() {
        return this.categoryId;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public int getPhotoNum() {
        return this.photoNum;
    }

    public String getHash() {
        return this.hash;
    }

    public List<String> getAlbumList() {
        return this.albumList;
    }

    public String toString() {
        return "TagInfo [categoryId=" + this.categoryId + ", categoryName=" + this.categoryName + ", photoNum=" + this.photoNum + ", tagNum=" + this.tagNum + ", localPath=" + this.localPath + ", createTime=" + this.createTime + ", hash=" + this.hash + ", albumList=" + this.albumList + "]";
    }

    private CategoryInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.categoryId = in.readString();
        this.categoryName = in.readString();
        this.photoNum = in.readInt();
        this.tagNum = in.readInt();
        this.localPath = in.readString();
        this.createTime = in.readLong();
        this.hash = in.readString();
        this.albumList = new ArrayList();
        in.readList(this.albumList, ClassLoader.getSystemClassLoader());
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.categoryId);
        dest.writeString(this.categoryName);
        dest.writeInt(this.photoNum);
        dest.writeInt(this.tagNum);
        dest.writeString(this.localPath);
        dest.writeLong(this.createTime);
        dest.writeString(this.hash);
        dest.writeList(this.albumList);
    }
}
