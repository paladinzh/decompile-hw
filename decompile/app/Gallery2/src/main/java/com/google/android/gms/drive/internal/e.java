package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.drive.Contents;

/* compiled from: Unknown */
public class e implements Creator<CloseContentsRequest> {
    static void a(CloseContentsRequest closeContentsRequest, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, closeContentsRequest.wj);
        b.a(parcel, 2, closeContentsRequest.Dq, i, false);
        b.a(parcel, 3, closeContentsRequest.Dr, false);
        b.D(parcel, p);
    }

    public CloseContentsRequest F(Parcel parcel) {
        Contents contents = null;
        int o = a.o(parcel);
        int i = 0;
        Boolean bool = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    contents = (Contents) a.a(parcel, n, Contents.CREATOR);
                    break;
                case 3:
                    bool = a.d(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
            Boolean bool2 = bool;
            contents = contents;
            bool = bool2;
        }
        if (parcel.dataPosition() == o) {
            return new CloseContentsRequest(i, contents, bool);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public CloseContentsRequest[] ak(int i) {
        return new CloseContentsRequest[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return F(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return ak(x0);
    }
}
