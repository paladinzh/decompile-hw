package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class SettingsProp implements Parcelable {
    public static final Creator<SettingsProp> CREATOR = new Creator<SettingsProp>() {
        public SettingsProp createFromParcel(Parcel source) {
            return new SettingsProp(source);
        }

        public SettingsProp[] newArray(int size) {
            return new SettingsProp[size];
        }
    };
    private int autoLcdNum;
    private String downloadPath;
    private String externalRootPath;
    private String internalRootPath;
    private String lcdCachePath;
    private int lcdHeight;
    private int lcdWidth;
    private String thumbCachePath;
    private int thumbHeight;
    private int thumbWidth;

    public void setThumbCachePath(String thumbCachePath) {
        this.thumbCachePath = thumbCachePath;
    }

    public void setLcdCachePath(String lcdCachePath) {
        this.lcdCachePath = lcdCachePath;
    }

    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setThumbWidth(int thumbWidth) {
        this.thumbWidth = thumbWidth;
    }

    public void setThumbHeight(int thumbHeight) {
        this.thumbHeight = thumbHeight;
    }

    public void setLcdWidth(int lcdWidth) {
        this.lcdWidth = lcdWidth;
    }

    public void setLcdHeight(int lcdHeight) {
        this.lcdHeight = lcdHeight;
    }

    public void setInternalRootPath(String path) {
        this.internalRootPath = path;
    }

    public void setExternalRootPath(String path) {
        this.externalRootPath = path;
    }

    public void setAutoLcdNum(int number) {
        this.autoLcdNum = number;
    }

    public String toString() {
        return "SettingsProp [thumbWidth=" + this.thumbWidth + ", thumbHeight=" + this.thumbHeight + ", lcdWidth=" + this.lcdWidth + ", lcdHeight=" + this.lcdHeight + ", internalRootPath=" + this.internalRootPath + ", externalRootPath=" + this.externalRootPath + ", thumbCachePath=" + this.thumbCachePath + ", lcdCachePath=" + this.lcdCachePath + ", downloadPath=" + this.downloadPath + "]";
    }

    private SettingsProp(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.thumbWidth = in.readInt();
        this.thumbHeight = in.readInt();
        this.lcdWidth = in.readInt();
        this.lcdHeight = in.readInt();
        this.internalRootPath = in.readString();
        this.externalRootPath = in.readString();
        this.thumbCachePath = in.readString();
        this.lcdCachePath = in.readString();
        this.downloadPath = in.readString();
        this.autoLcdNum = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.thumbWidth);
        dest.writeInt(this.thumbHeight);
        dest.writeInt(this.lcdWidth);
        dest.writeInt(this.lcdHeight);
        dest.writeString(this.internalRootPath);
        dest.writeString(this.externalRootPath);
        dest.writeString(this.thumbCachePath);
        dest.writeString(this.lcdCachePath);
        dest.writeString(this.downloadPath);
        dest.writeInt(this.autoLcdNum);
    }
}
