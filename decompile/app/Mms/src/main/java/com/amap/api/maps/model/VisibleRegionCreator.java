package com.amap.api.maps.model;

import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public class VisibleRegionCreator implements Creator<VisibleRegion> {
    public static final int CONTENT_DESCRIPTION = 0;

    public VisibleRegion createFromParcel(Parcel parcel) {
        LatLng latLng;
        LatLng latLng2;
        LatLng latLng3;
        Object obj;
        BadParcelableException badParcelableException;
        LatLng latLng4;
        Object obj2;
        LatLngBounds latLngBounds;
        LatLng latLng5;
        LatLngBounds latLngBounds2 = null;
        int readInt = parcel.readInt();
        try {
            LatLng latLng6 = (LatLng) parcel.readParcelable(LatLng.class.getClassLoader());
            try {
                latLng = (LatLng) parcel.readParcelable(LatLng.class.getClassLoader());
                try {
                    latLng2 = (LatLng) parcel.readParcelable(LatLng.class.getClassLoader());
                } catch (BadParcelableException e) {
                    latLng3 = null;
                    obj = latLng6;
                    badParcelableException = e;
                    latLng2 = null;
                    badParcelableException.printStackTrace();
                    latLng4 = latLng3;
                    latLng3 = latLng2;
                    latLng2 = latLng;
                    obj2 = latLngBounds;
                    latLng5 = latLng4;
                    return new VisibleRegion(readInt, latLng, latLng2, latLng3, latLng5, latLngBounds2);
                }
            } catch (BadParcelableException e2) {
                latLng3 = null;
                latLng2 = null;
                obj = latLng6;
                badParcelableException = e2;
                latLng = null;
                badParcelableException.printStackTrace();
                latLng4 = latLng3;
                latLng3 = latLng2;
                latLng2 = latLng;
                obj2 = latLngBounds;
                latLng5 = latLng4;
                return new VisibleRegion(readInt, latLng, latLng2, latLng3, latLng5, latLngBounds2);
            }
            try {
                latLng3 = (LatLng) parcel.readParcelable(LatLng.class.getClassLoader());
                try {
                    latLngBounds2 = (LatLngBounds) parcel.readParcelable(LatLngBounds.class.getClassLoader());
                    latLng5 = latLng3;
                    latLng3 = latLng2;
                    latLng2 = latLng;
                    latLng = latLng6;
                } catch (BadParcelableException e3) {
                    BadParcelableException badParcelableException2 = e3;
                    obj = latLng6;
                    badParcelableException = badParcelableException2;
                    badParcelableException.printStackTrace();
                    latLng4 = latLng3;
                    latLng3 = latLng2;
                    latLng2 = latLng;
                    obj2 = latLngBounds;
                    latLng5 = latLng4;
                    return new VisibleRegion(readInt, latLng, latLng2, latLng3, latLng5, latLngBounds2);
                }
            } catch (BadParcelableException e4) {
                obj = latLng6;
                badParcelableException = e4;
                latLng3 = null;
                badParcelableException.printStackTrace();
                latLng4 = latLng3;
                latLng3 = latLng2;
                latLng2 = latLng;
                obj2 = latLngBounds;
                latLng5 = latLng4;
                return new VisibleRegion(readInt, latLng, latLng2, latLng3, latLng5, latLngBounds2);
            }
        } catch (BadParcelableException e5) {
            badParcelableException = e5;
            latLng3 = null;
            latLng2 = null;
            latLng = null;
            latLngBounds = null;
            badParcelableException.printStackTrace();
            latLng4 = latLng3;
            latLng3 = latLng2;
            latLng2 = latLng;
            obj2 = latLngBounds;
            latLng5 = latLng4;
            return new VisibleRegion(readInt, latLng, latLng2, latLng3, latLng5, latLngBounds2);
        }
        return new VisibleRegion(readInt, latLng, latLng2, latLng3, latLng5, latLngBounds2);
    }

    public VisibleRegion[] newArray(int i) {
        return new VisibleRegion[i];
    }

    static void a(VisibleRegion visibleRegion, Parcel parcel, int i) {
        parcel.writeInt(visibleRegion.a());
        parcel.writeParcelable(visibleRegion.nearLeft, i);
        parcel.writeParcelable(visibleRegion.nearRight, i);
        parcel.writeParcelable(visibleRegion.farLeft, i);
        parcel.writeParcelable(visibleRegion.farRight, i);
        parcel.writeParcelable(visibleRegion.latLngBounds, i);
    }
}
