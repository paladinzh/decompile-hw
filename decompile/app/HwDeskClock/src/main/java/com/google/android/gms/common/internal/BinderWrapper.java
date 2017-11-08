package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

/* compiled from: Unknown */
public final class BinderWrapper implements Parcelable {
    public static final Creator<BinderWrapper> CREATOR = new Creator<BinderWrapper>() {
        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return zzad(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return zzbx(i);
        }

        public BinderWrapper zzad(Parcel parcel) {
            return new BinderWrapper(parcel);
        }

        public BinderWrapper[] zzbx(int i) {
            return new BinderWrapper[i];
        }
    };
    private IBinder zzacF;

    public BinderWrapper() {
        this.zzacF = null;
    }

    public BinderWrapper(IBinder binder) {
        this.zzacF = null;
        this.zzacF = binder;
    }

    private BinderWrapper(Parcel in) {
        this.zzacF = null;
        this.zzacF = in.readStrongBinder();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.zzacF);
    }
}
