package com.google.android.gms.common.data;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

/* compiled from: Unknown */
public class zzd<T extends SafeParcelable> extends AbstractDataBuffer<T> {
    private static final String[] zzabj = new String[]{"data"};
    private final Creator<T> zzabk;

    public /* synthetic */ Object get(int i) {
        return zzbn(i);
    }

    public T zzbn(int i) {
        byte[] zzg = this.zzYX.zzg("data", i, this.zzYX.zzbo(i));
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(zzg, 0, zzg.length);
        obtain.setDataPosition(0);
        SafeParcelable safeParcelable = (SafeParcelable) this.zzabk.createFromParcel(obtain);
        obtain.recycle();
        return safeParcelable;
    }
}
