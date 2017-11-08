package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.List;

public class AlbumInfo implements Parcelable {
    public static final Creator<AlbumInfo> CREATOR = new Creator<AlbumInfo>() {
        public AlbumInfo createFromParcel(Parcel source) {
            return new AlbumInfo(source);
        }

        public AlbumInfo[] newArray(int size) {
            return new AlbumInfo[size];
        }
    };
    private String albumID;
    private String albumName;
    private long createTime;
    private long flversion;
    private int iversion;
    private List<String> localThumbPath;
    private String lpath;
    private int photoNum;
    private String source;
    private long totalSize;

    public String toString() {
        return "AlbumInfo [albumID=" + this.albumID + ", albumName=" + this.albumName + ", createTime=" + this.createTime + ", photoNum=" + this.photoNum + ", localThumbPath=" + this.localThumbPath + ", source=" + this.source + ", flversion=" + this.flversion + ", iversion=" + this.iversion + "]";
    }

    private AlbumInfo(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.albumID = in.readString();
        this.albumName = in.readString();
        this.createTime = in.readLong();
        this.photoNum = in.readInt();
        this.lpath = in.readString();
        this.localThumbPath = in.readArrayList(CREATOR.getClass().getClassLoader());
        this.source = in.readString();
        this.flversion = in.readLong();
        this.iversion = in.readInt();
        this.totalSize = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.albumID);
        dest.writeString(this.albumName);
        dest.writeLong(this.createTime);
        dest.writeInt(this.photoNum);
        dest.writeString(this.lpath);
        dest.writeList(this.localThumbPath);
        dest.writeString(this.source);
        dest.writeLong(this.flversion);
        dest.writeInt(this.iversion);
        dest.writeLong(this.totalSize);
    }
}
