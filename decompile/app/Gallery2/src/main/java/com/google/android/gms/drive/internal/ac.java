package com.google.android.gms.drive.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;
import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.ConflictEvent;

/* compiled from: Unknown */
public class ac implements Creator<OnEventResponse> {
    static void a(OnEventResponse onEventResponse, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.c(parcel, 1, onEventResponse.wj);
        b.c(parcel, 2, onEventResponse.Dm);
        b.a(parcel, 3, onEventResponse.Eb, i, false);
        b.a(parcel, 4, onEventResponse.Ec, i, false);
        b.D(parcel, p);
    }

    public OnEventResponse Q(Parcel parcel) {
        int o = a.o(parcel);
        ChangeEvent changeEvent = null;
        int i = 0;
        int i2 = 0;
        ConflictEvent conflictEvent = null;
        while (parcel.dataPosition() < o) {
            int i3;
            ChangeEvent changeEvent2;
            ConflictEvent conflictEvent2;
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    i2 = a.g(parcel, n);
                    break;
                case 2:
                    i = a.g(parcel, n);
                    break;
                case 3:
                    i3 = i;
                    i = i2;
                    ConflictEvent conflictEvent3 = conflictEvent;
                    changeEvent2 = (ChangeEvent) a.a(parcel, n, ChangeEvent.CREATOR);
                    conflictEvent2 = conflictEvent3;
                    continue;
                case 4:
                    conflictEvent2 = (ConflictEvent) a.a(parcel, n, ConflictEvent.CREATOR);
                    changeEvent2 = changeEvent;
                    i3 = i;
                    i = i2;
                    continue;
                default:
                    a.b(parcel, n);
                    break;
            }
            conflictEvent2 = conflictEvent;
            changeEvent2 = changeEvent;
            i3 = i;
            i = i2;
            i2 = i;
            i = i3;
            changeEvent = changeEvent2;
            conflictEvent = conflictEvent2;
        }
        if (parcel.dataPosition() == o) {
            return new OnEventResponse(i2, i, changeEvent, conflictEvent);
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public OnEventResponse[] av(int i) {
        return new OnEventResponse[i];
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return Q(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return av(x0);
    }
}
