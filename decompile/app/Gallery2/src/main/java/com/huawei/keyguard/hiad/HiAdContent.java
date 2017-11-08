package com.huawei.keyguard.hiad;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HiAdContent implements Parcelable {
    public static final Creator<HiAdContent> CREATOR = new Creator<HiAdContent>() {
        public HiAdContent createFromParcel(Parcel source) {
            return new HiAdContent(source);
        }

        public HiAdContent[] newArray(int size) {
            return new HiAdContent[size];
        }
    };
    private String clickMonitorUrl;
    private String contentId;
    private long downloadId = -1;
    private long endTime;
    private long fileSize;
    private String impMonitorUrl;
    private String md5;
    private String metaData;
    private String paramFromServer;
    private String previewMd5;
    private String previewSha256;
    private String previewUrl;
    private String sha256;
    private String url;

    public HiAdContent(Parcel in) {
        readFromParcel(in);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.clickMonitorUrl);
        dest.writeString(this.contentId);
        dest.writeLong(this.endTime);
        dest.writeString(this.impMonitorUrl);
        dest.writeString(this.metaData);
        dest.writeString(this.paramFromServer);
        dest.writeString(this.url);
        dest.writeString(this.md5);
        dest.writeString(this.sha256);
        dest.writeLong(this.fileSize);
        dest.writeString(this.previewUrl);
        dest.writeString(this.previewMd5);
        dest.writeString(this.previewSha256);
        dest.writeLong(this.downloadId);
    }

    public void readFromParcel(Parcel in) {
        this.clickMonitorUrl = in.readString();
        this.contentId = in.readString();
        this.endTime = in.readLong();
        this.impMonitorUrl = in.readString();
        this.metaData = in.readString();
        this.paramFromServer = in.readString();
        this.url = in.readString();
        this.md5 = in.readString();
        this.sha256 = in.readString();
        this.fileSize = in.readLong();
        this.previewUrl = in.readString();
        this.previewMd5 = in.readString();
        this.previewSha256 = in.readString();
        this.downloadId = in.readLong();
    }
}
