package com.amap.api.maps.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import cn.com.xy.sms.sdk.constant.Constant;

public class CircleOptionsCreator implements Creator<CircleOptions> {
    public CircleOptions createFromParcel(Parcel parcel) {
        boolean z = true;
        CircleOptions circleOptions = new CircleOptions();
        Bundle readBundle = parcel.readBundle();
        circleOptions.center(new LatLng(readBundle.getDouble(Constant.LOACTION_LATITUDE), readBundle.getDouble(Constant.LOACTION_LONGITUDE)));
        circleOptions.radius(parcel.readDouble());
        circleOptions.strokeWidth(parcel.readFloat());
        circleOptions.strokeColor(parcel.readInt());
        circleOptions.fillColor(parcel.readInt());
        circleOptions.zIndex((float) parcel.readInt());
        if (parcel.readByte() != (byte) 1) {
            z = false;
        }
        circleOptions.visible(z);
        circleOptions.a = parcel.readString();
        return circleOptions;
    }

    public CircleOptions[] newArray(int i) {
        return new CircleOptions[i];
    }
}
