package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class LogMetricInfo implements Parcelable {
    public static final Creator<LogMetricInfo> CREATOR = new Creator<LogMetricInfo>() {
        public LogMetricInfo createFromParcel(Parcel in) {
            return new LogMetricInfo(in);
        }

        public LogMetricInfo[] newArray(int size) {
            return new LogMetricInfo[size];
        }
    };
    public String description;
    public String[] files;
    public long id;
    public String logDetailedInfo;
    public String path;
    public String zipTime;

    public LogMetricInfo() {
        this.id = 0;
        this.description = null;
        this.files = null;
        this.path = null;
        this.zipTime = null;
        this.logDetailedInfo = null;
    }

    public LogMetricInfo(long id, String path, String description, String[] files, String zipTime, String logDetailedInfo) {
        this.id = id;
        this.path = path;
        this.description = description;
        this.zipTime = zipTime;
        this.logDetailedInfo = logDetailedInfo;
        if (files == null || files.length == 0) {
            this.files = null;
            return;
        }
        this.files = new String[files.length];
        int length = files.length;
        for (int i = 0; i < length; i++) {
            this.files[i] = files[i];
        }
    }

    private LogMetricInfo(Parcel in) {
        this.id = in.readLong();
        this.path = in.readString();
        this.description = in.readString();
        this.files = in.createStringArray();
        this.zipTime = in.readString();
        this.logDetailedInfo = in.readString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.path);
        dest.writeString(this.description);
        dest.writeStringArray(this.files);
        dest.writeString(this.zipTime);
        dest.writeString(this.logDetailedInfo);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id = ").append(this.id).append("\n");
        sb.append("path = ").append(this.path).append("\n");
        sb.append("description = ").append(this.description).append("\n");
        if (this.files == null) {
            return sb.toString();
        }
        int length = this.files.length;
        for (int i = 0; i < length; i++) {
            sb.append("files[").append(i).append("]=").append(this.files[i]).append("\n");
        }
        sb.append("zipTime = ").append(this.zipTime).append("\n");
        sb.append("logDetailedInfo = ").append(this.logDetailedInfo).append("\n");
        return sb.toString();
    }
}
