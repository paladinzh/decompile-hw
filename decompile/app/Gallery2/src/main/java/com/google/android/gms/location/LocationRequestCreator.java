package com.google.android.gms.location;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class LocationRequestCreator implements Creator<LocationRequest> {
    static void a(LocationRequest locationRequest, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, locationRequest.mPriority);
        b.c(parcel, 1000, locationRequest.getVersionCode());
        b.a(parcel, 2, locationRequest.Lc);
        b.a(parcel, 3, locationRequest.Ld);
        b.a(parcel, 4, locationRequest.Le);
        b.a(parcel, 5, locationRequest.KV);
        b.c(parcel, 6, locationRequest.Lf);
        b.a(parcel, 7, locationRequest.Lg);
        b.D(parcel, p);
    }

    public LocationRequest createFromParcel(Parcel parcel) {
        boolean z = false;
        int o = a.o(parcel);
        int i = 102;
        long j = 3600000;
        long j2 = 600000;
        long j3 = Long.MAX_VALUE;
        int i2 = Integer.MAX_VALUE;
        float f = 0.0f;
        int i3 = 0;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    j = a.h(parcel, n);
                    break;
                case 3:
                    j2 = a.h(parcel, n);
                    break;
                case 4:
                    z = a.c(parcel, n);
                    break;
                case 5:
                    j3 = a.h(parcel, n);
                    break;
                case 6:
                    i2 = a.g(parcel, n);
                    break;
                case 7:
                    f = a.j(parcel, n);
                    break;
                case 1000:
                    i3 = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new LocationRequest(i3, i, j, j2, z, j3, i2, f);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public LocationRequest[] newArray(int size) {
        return new LocationRequest[size];
    }
}
