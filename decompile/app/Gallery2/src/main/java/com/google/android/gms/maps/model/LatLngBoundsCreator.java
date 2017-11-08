package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class LatLngBoundsCreator implements Creator<LatLngBounds> {
    static void a(LatLngBounds latLngBounds, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, latLngBounds.getVersionCode());
        b.a(parcel, 2, latLngBounds.southwest, i, false);
        b.a(parcel, 3, latLngBounds.northeast, i, false);
        b.D(parcel, p);
    }

    public LatLngBounds createFromParcel(Parcel parcel) {
        int o = a.o(parcel);
        LatLng latLng = null;
        int i = 0;
        LatLng latLng2 = null;
        while (parcel.dataPosition() < o) {
            int i2;
            LatLng latLng3;
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    i2 = i;
                    LatLng latLng4 = (LatLng) a.a(parcel, n, LatLng.CREATOR);
                    latLng3 = latLng2;
                    latLng2 = latLng4;
                    continue;
                case 3:
                    latLng3 = (LatLng) a.a(parcel, n, LatLng.CREATOR);
                    latLng2 = latLng;
                    i2 = i;
                    continue;
                default:
                    a.b(parcel, n);
                    break;
            }
            latLng3 = latLng2;
            latLng2 = latLng;
            i2 = i;
            i = i2;
            latLng = latLng2;
            latLng2 = latLng3;
        }
        if (parcel.dataPosition() == o) {
            return new LatLngBounds(i, latLng, latLng2);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public LatLngBounds[] newArray(int size) {
        return new LatLngBounds[size];
    }
}
