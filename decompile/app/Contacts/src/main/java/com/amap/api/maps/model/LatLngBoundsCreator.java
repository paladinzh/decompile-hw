package com.amap.api.maps.model;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public class LatLngBoundsCreator implements Creator<LatLngBounds> {
    public static final int CONTENT_DESCRIPTION = 0;

    public LatLngBounds createFromParcel(Parcel parcel) {
        LatLng latLng;
        LatLng latLng2;
        BadParcelableException badParcelableException;
        int readInt = parcel.readInt();
        try {
            latLng = (LatLng) parcel.readParcelable(LatLngBounds.class.getClassLoader());
            try {
                latLng2 = (LatLng) parcel.readParcelable(LatLngBounds.class.getClassLoader());
            } catch (BadParcelableException e) {
                BadParcelableException badParcelableException2 = e;
                latLng2 = latLng;
                badParcelableException = badParcelableException2;
                badParcelableException.printStackTrace();
                latLng = latLng2;
                latLng2 = null;
                return new LatLngBounds(readInt, latLng, latLng2);
            }
        } catch (BadParcelableException e2) {
            badParcelableException = e2;
            latLng2 = null;
            badParcelableException.printStackTrace();
            latLng = latLng2;
            latLng2 = null;
            return new LatLngBounds(readInt, latLng, latLng2);
        }
        return new LatLngBounds(readInt, latLng, latLng2);
    }

    public LatLngBounds[] newArray(int i) {
        return new LatLngBounds[i];
    }

    static void a(LatLngBounds latLngBounds, Parcel parcel, int i) {
        parcel.writeInt(latLngBounds.a());
        parcel.writeParcelable(latLngBounds.southwest, i);
        parcel.writeParcelable(latLngBounds.northeast, i);
    }
}
