package com.huawei.android.cg.vo;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class TagFileInfoGroup implements Parcelable {
    public static final Creator<TagFileInfoGroup> CREATOR = new Creator<TagFileInfoGroup>() {
        public TagFileInfoGroup createFromParcel(Parcel source) {
            return new TagFileInfoGroup(source);
        }

        public TagFileInfoGroup[] newArray(int size) {
            return new TagFileInfoGroup[size];
        }
    };
    private long batchCtime;
    private int photoNum;

    private TagFileInfoGroup(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        this.batchCtime = in.readLong();
        this.photoNum = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.batchCtime);
        dest.writeInt(this.photoNum);
    }
}
