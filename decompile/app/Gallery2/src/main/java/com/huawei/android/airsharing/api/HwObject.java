package com.huawei.android.airsharing.api;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HwObject implements Parcelable {
    public static final Creator<HwObject> CREATOR = new Creator<HwObject>() {
        public HwObject[] newArray(int size) {
            return new HwObject[size];
        }

        public HwObject createFromParcel(Parcel source) {
            HwObject object = new HwObject();
            object.setExtendObj(source.readValue(Object.class.getClassLoader()));
            return object;
        }
    };
    protected Object extendObj;

    public void setExtendObj(Object extendObj) {
        this.extendObj = extendObj;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.extendObj);
    }
}
