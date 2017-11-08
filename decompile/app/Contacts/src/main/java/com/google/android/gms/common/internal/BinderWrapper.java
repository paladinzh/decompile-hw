package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.annotation.KeepName;

@KeepName
/* compiled from: Unknown */
public final class BinderWrapper implements Parcelable {
    public static final Creator<BinderWrapper> CREATOR = new Creator<BinderWrapper>() {
        public /* synthetic */ Object createFromParcel(Parcel parcel) {
            return zzan(parcel);
        }

        public /* synthetic */ Object[] newArray(int i) {
            return zzbQ(i);
        }

        public BinderWrapper zzan(Parcel parcel) {
            return new BinderWrapper(parcel);
        }

        public BinderWrapper[] zzbQ(int i) {
            return new BinderWrapper[i];
        }
    };
    private IBinder zzakD;

    public BinderWrapper() {
        this.zzakD = null;
    }

    public BinderWrapper(IBinder binder) {
        this.zzakD = null;
        this.zzakD = binder;
    }

    private BinderWrapper(Parcel in) {
        this.zzakD = null;
        this.zzakD = in.readStrongBinder();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(this.zzakD);
    }
}
