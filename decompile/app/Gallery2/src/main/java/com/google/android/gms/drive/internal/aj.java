package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.drive.DriveId;

/* compiled from: Unknown */
public class aj implements Creator<RemoveEventListenerRequest> {
    static void a(RemoveEventListenerRequest removeEventListenerRequest, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, removeEventListenerRequest.wj);
        b.a(parcel, 2, removeEventListenerRequest.CS, i, false);
        b.c(parcel, 3, removeEventListenerRequest.Dm);
        b.D(parcel, p);
    }

    public RemoveEventListenerRequest X(Parcel parcel) {
        int o = a.o(parcel);
        int i = 0;
        int i2 = 0;
        DriveId driveId = null;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i = a.g(parcel, n);
                    break;
                case 2:
                    driveId = (DriveId) a.a(parcel, n, DriveId.CREATOR);
                    break;
                case 3:
                    i2 = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
            int i3 = i2;
            driveId = driveId;
            i2 = i3;
        }
        if (parcel.dataPosition() == o) {
            return new RemoveEventListenerRequest(i, driveId, i2);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public RemoveEventListenerRequest[] aC(int i) {
        return new RemoveEventListenerRequest[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return X(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return aC(x0);
    }
}
