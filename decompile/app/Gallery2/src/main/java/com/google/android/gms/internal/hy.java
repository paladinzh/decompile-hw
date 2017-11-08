package com.google.android.gms.internal;

import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.List;

/* compiled from: Unknown */
public class hy implements Creator<hx> {
    static void a(hx hxVar, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, hxVar.getId(), false);
        b.a(parcel, 2, hxVar.gE(), false);
        b.a(parcel, 3, hxVar.gF(), i, false);
        b.a(parcel, 4, hxVar.gx(), i, false);
        b.a(parcel, 5, hxVar.gy());
        b.a(parcel, 6, hxVar.gz(), i, false);
        b.a(parcel, 7, hxVar.gG(), false);
        b.a(parcel, 8, hxVar.gA(), i, false);
        b.a(parcel, 9, hxVar.gB());
        b.a(parcel, 10, hxVar.getRating());
        b.c(parcel, 11, hxVar.gC());
        b.a(parcel, 12, hxVar.gD());
        b.b(parcel, 13, hxVar.gw(), false);
        b.c(parcel, 1000, hxVar.wj);
        b.D(parcel, p);
    }

    public hx aB(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        String str = null;
        List list = null;
        Bundle bundle = null;
        hz hzVar = null;
        LatLng latLng = null;
        float f = 0.0f;
        LatLngBounds latLngBounds = null;
        String str2 = null;
        Uri uri = null;
        boolean z = false;
        float f2 = 0.0f;
        int i2 = 0;
        long j = 0;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    str = a.m(parcel, n);
                    break;
                case 2:
                    bundle = a.o(parcel, n);
                    break;
                case 3:
                    hzVar = (hz) a.a(parcel, n, hz.CREATOR);
                    break;
                case 4:
                    latLng = (LatLng) a.a(parcel, n, LatLng.CREATOR);
                    break;
                case 5:
                    f = a.j(parcel, n);
                    break;
                case 6:
                    latLngBounds = (LatLngBounds) a.a(parcel, n, LatLngBounds.CREATOR);
                    break;
                case 7:
                    str2 = a.m(parcel, n);
                    break;
                case 8:
                    uri = (Uri) a.a(parcel, n, Uri.CREATOR);
                    break;
                case 9:
                    z = a.c(parcel, n);
                    break;
                case 10:
                    f2 = a.j(parcel, n);
                    break;
                case 11:
                    i2 = a.g(parcel, n);
                    break;
                case 12:
                    j = a.h(parcel, n);
                    break;
                case 13:
                    list = a.c(parcel, n, ht.CREATOR);
                    break;
                case 1000:
                    i = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            return new hx(i, str, list, bundle, hzVar, latLng, f, latLngBounds, str2, uri, z, f2, i2, j);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public hx[] bw(int i) {
        return new hx[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return aB(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return bw(x0);
    }
}
