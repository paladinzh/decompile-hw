package com.huawei.lcagent.client;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.telephony.MSimTelephonyConstants;

public class CompressInfo implements Parcelable {
    public static final int COLLECT_LOG = 0;
    public static final int COMPRESS_LOG = 1;
    public static final Creator<CompressInfo> CREATOR = new Creator<CompressInfo>() {
        public CompressInfo createFromParcel(Parcel in) {
            return new CompressInfo(in);
        }

        public CompressInfo[] newArray(int size) {
            return new CompressInfo[size];
        }
    };
    public static final int FINISHED = 2;
    public String description;
    public String path;
    public int progress;
    public int status;

    public CompressInfo() {
        this.status = 0;
        this.progress = 0;
        this.path = MSimTelephonyConstants.MY_RADIO_PLATFORM;
        this.description = MSimTelephonyConstants.MY_RADIO_PLATFORM;
    }

    private CompressInfo(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }

    public void setCompressInfo(int status, int progress, String path, String description) {
        this.status = status;
        this.progress = progress;
        this.path = path;
        this.description = description;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.status);
        dest.writeInt(this.progress);
        dest.writeString(this.path);
        dest.writeString(this.description);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("status = ").append(this.status).append("\n");
        sb.append("progress = ").append(this.progress).append("%").append("\n");
        sb.append("path = ").append(this.path).append("\n");
        sb.append("description = ").append(this.description).append("\n");
        return sb.toString();
    }

    public void readFromParcel(Parcel in) {
        this.status = in.readInt();
        this.progress = in.readInt();
        this.path = in.readString();
        this.description = in.readString();
    }
}
