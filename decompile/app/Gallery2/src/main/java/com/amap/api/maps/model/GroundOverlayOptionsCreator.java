package com.amap.api.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;

public class GroundOverlayOptionsCreator implements Creator<GroundOverlayOptions> {
    public static final int CONTENT_DESCRIPTION = 0;

    public GroundOverlayOptions createFromParcel(Parcel parcel) {
        boolean z = false;
        int readInt = parcel.readInt();
        BitmapDescriptor bitmapDescriptor = (BitmapDescriptor) parcel.readParcelable(BitmapDescriptor.class.getClassLoader());
        LatLng latLng = (LatLng) parcel.readParcelable(LatLng.class.getClassLoader());
        float readFloat = parcel.readFloat();
        float readFloat2 = parcel.readFloat();
        LatLngBounds latLngBounds = (LatLngBounds) parcel.readParcelable(LatLngBounds.class.getClassLoader());
        float readFloat3 = parcel.readFloat();
        float readFloat4 = parcel.readFloat();
        if (parcel.readByte() != (byte) 0) {
            z = true;
        }
        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions(readInt, null, latLng, readFloat, readFloat2, latLngBounds, readFloat3, readFloat4, z, parcel.readFloat(), parcel.readFloat(), parcel.readFloat());
        groundOverlayOptions.image(bitmapDescriptor);
        return groundOverlayOptions;
    }

    public GroundOverlayOptions[] newArray(int i) {
        return new GroundOverlayOptions[i];
    }
}
