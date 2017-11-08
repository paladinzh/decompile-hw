package com.huawei.android.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwMediaInfo implements Parcelable {
    public static final Creator<HwMediaInfo> CREATOR = new Creator<HwMediaInfo>() {
        public HwMediaInfo[] newArray(int size) {
            return new HwMediaInfo[size];
        }

        public HwMediaInfo createFromParcel(Parcel source) {
            HwMediaInfo mediaInfo = new HwMediaInfo();
            mediaInfo.setName(source.readString());
            mediaInfo.setTitle(source.readString());
            mediaInfo.setMediaInfoType(EHwMediaInfoType.valueOf(source.readString()));
            mediaInfo.setUrl(source.readString());
            mediaInfo.setMimeType(source.readString());
            mediaInfo.setAddDate(source.readString());
            mediaInfo.setIconUri(source.readString());
            mediaInfo.setDuration(source.readString());
            mediaInfo.setArtist(source.readString());
            mediaInfo.setSize(source.readLong());
            mediaInfo.setWidth(source.readInt());
            mediaInfo.setHeight(source.readInt());
            mediaInfo.setPosition(source.readString());
            mediaInfo.setVolume(source.readInt());
            mediaInfo.setExtendObj(source.readValue(Object.class.getClassLoader()));
            return mediaInfo;
        }
    };
    protected String addDate;
    protected String artist;
    protected String duration;
    protected Object extendObj;
    protected int height;
    protected String iconUri;
    protected EHwMediaInfoType mediaInfoType;
    protected String mimeType;
    protected String name;
    protected String position;
    protected long size;
    protected String title;
    protected String url;
    protected int volume;
    protected int width;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public EHwMediaInfoType getMediaInfoType() {
        return this.mediaInfoType;
    }

    public void setMediaInfoType(EHwMediaInfoType mediaInfoType) {
        this.mediaInfoType = mediaInfoType;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setAddDate(String addDate) {
        this.addDate = addDate;
    }

    public void setIconUri(String iconUri) {
        this.iconUri = iconUri;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getPosition() {
        return this.position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getVolume() {
        return this.volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public void setExtendObj(Object extendObj) {
        this.extendObj = extendObj;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.title);
        dest.writeString(this.mediaInfoType.toString());
        dest.writeString(this.url);
        dest.writeString(this.mimeType);
        dest.writeString(this.addDate);
        dest.writeString(this.iconUri);
        dest.writeString(this.duration);
        dest.writeString(this.artist);
        dest.writeLong(this.size);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeString(this.position);
        dest.writeInt(this.volume);
        dest.writeValue(this.extendObj);
    }
}
