package com.huawei.systemmanager.rainbow.db.recommend;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class RecommendItem implements Parcelable {
    public static final Creator<RecommendItem> CREATOR = new Creator<RecommendItem>() {
        public RecommendItem createFromParcel(Parcel source) {
            return new RecommendItem(source);
        }

        public RecommendItem[] newArray(int size) {
            return new RecommendItem[size];
        }
    };
    private int mConfigItemId;
    private int mConfigType;
    private int mPercentage;

    public RecommendItem(int itemId, int type, int percentage) {
        this.mConfigItemId = itemId;
        this.mConfigType = type;
        this.mPercentage = percentage;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mConfigItemId);
        dest.writeInt(this.mConfigType);
        dest.writeInt(this.mPercentage);
    }

    public int getConfigItemId() {
        return this.mConfigItemId;
    }

    public int getConfigType() {
        return this.mConfigType;
    }

    public int getPercentage() {
        return this.mPercentage;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{ ");
        buf.append("itemId[").append(this.mConfigItemId).append("] ");
        buf.append("type[").append(this.mConfigType).append("] ");
        buf.append("percent[").append(this.mPercentage).append("] ");
        buf.append("} ");
        return buf.toString();
    }

    private RecommendItem(Parcel source) {
        this.mConfigItemId = source.readInt();
        this.mConfigType = source.readInt();
        this.mPercentage = source.readInt();
    }
}
