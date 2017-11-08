package com.google.android.gms.common.data;

import android.database.CursorWindow;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.a;
import com.google.android.gms.common.internal.safeparcel.b;

/* compiled from: Unknown */
public class DataHolderCreator implements Creator<DataHolder> {
    static void a(DataHolder dataHolder, Parcel parcel, int i) {
        int p = b.p(parcel);
        b.a(parcel, 1, dataHolder.dH(), false);
        b.c(parcel, 1000, dataHolder.getVersionCode());
        b.a(parcel, 2, dataHolder.dI(), i, false);
        b.c(parcel, 3, dataHolder.getStatusCode());
        b.a(parcel, 4, dataHolder.getMetadata(), false);
        b.D(parcel, p);
    }

    public DataHolder createFromParcel(Parcel parcel) {
        int i = 0;
        Bundle bundle = null;
        int o = a.o(parcel);
        CursorWindow[] cursorWindowArr = null;
        String[] strArr = null;
        int i2 = 0;
        while (parcel.dataPosition() < o) {
            int n = a.n(parcel);
            switch (a.S(n)) {
                case 1:
                    strArr = a.x(parcel, n);
                    break;
                case 2:
                    cursorWindowArr = (CursorWindow[]) a.b(parcel, n, CursorWindow.CREATOR);
                    break;
                case 3:
                    i = a.g(parcel, n);
                    break;
                case 4:
                    bundle = a.o(parcel, n);
                    break;
                case 1000:
                    i2 = a.g(parcel, n);
                    break;
                default:
                    a.b(parcel, n);
                    break;
            }
        }
        if (parcel.dataPosition() == o) {
            DataHolder dataHolder = new DataHolder(i2, strArr, cursorWindowArr, i, bundle);
            dataHolder.validateContents();
            return dataHolder;
        }
        throw new a.a("Overread allowed size end=" + o, parcel);
    }

    public DataHolder[] newArray(int size) {
        return new DataHolder[size];
    }
}
