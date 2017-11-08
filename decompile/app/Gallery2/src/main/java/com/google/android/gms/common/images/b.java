package com.google.android.gms.common.images;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;

/* compiled from: Unknown */
public class b implements Creator<WebImage> {
    static void a(WebImage webImage, Parcel parcel, int i) {
        int p = com.google.android.gms.common.internal.safeparcel.b.p(parcel);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 1, webImage.getVersionCode());
        com.google.android.gms.common.internal.safeparcel.b.a(parcel, 2, webImage.getUrl(), i, false);
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 3, webImage.getWidth());
        com.google.android.gms.common.internal.safeparcel.b.c(parcel, 4, webImage.getHeight());
        com.google.android.gms.common.internal.safeparcel.b.D(parcel, p);
    }

    public WebImage[] M(int i) {
        return new WebImage[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return l(x0);
    }

    public WebImage l(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        Uri uri = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i2 = a.g(parcel, n);
                    break;
                case 2:
                    uri = (Uri) a.a(parcel, n, Uri.CREATOR);
                    break;
                case 3:
                    i = a.g(parcel, n);
                    break;
                case 4:
                    i3 = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
            int i4 = i3;
            i3 = i;
            uri = uri;
            i = i3;
            i3 = i4;
        }
        if (parcel.dataPosition() == o) {
            return new WebImage(i2, uri, i, i3);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return M(x0);
    }
}
