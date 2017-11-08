package com.amap.api.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import java.util.ArrayList;

public class PolylineOptionsCreator implements Creator<PolylineOptions> {
    public PolylineOptions createFromParcel(Parcel parcel) {
        PolylineOptions polylineOptions = new PolylineOptions();
        Iterable arrayList = new ArrayList();
        parcel.readTypedList(arrayList, LatLng.CREATOR);
        float readFloat = parcel.readFloat();
        int readInt = parcel.readInt();
        float readFloat2 = parcel.readFloat();
        polylineOptions.a = parcel.readString();
        boolean[] zArr = new boolean[3];
        parcel.readBooleanArray(zArr);
        BitmapDescriptor bitmapDescriptor = (BitmapDescriptor) parcel.readParcelable(BitmapDescriptor.class.getClassLoader());
        polylineOptions.addAll(arrayList);
        polylineOptions.width(readFloat);
        polylineOptions.color(readInt);
        polylineOptions.zIndex(readFloat2);
        polylineOptions.visible(zArr[0]);
        polylineOptions.setDottedLine(zArr[1]);
        polylineOptions.geodesic(zArr[2]);
        polylineOptions.useGradient(zArr[3]);
        polylineOptions.setCustomTexture(bitmapDescriptor);
        polylineOptions.setCustomTextureList(parcel.readArrayList(BitmapDescriptor.class.getClassLoader()));
        polylineOptions.setCustomTextureIndex(parcel.readArrayList(Integer.class.getClassLoader()));
        polylineOptions.colorValues(parcel.readArrayList(Integer.class.getClassLoader()));
        return polylineOptions;
    }

    public PolylineOptions[] newArray(int i) {
        return new PolylineOptions[i];
    }
}
