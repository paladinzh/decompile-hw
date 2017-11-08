package com.google.android.gms.location.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.location.LocationSettingsResult;

/* compiled from: Unknown */
public interface zzj extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzj {

        /* compiled from: Unknown */
        private static class zza implements zzj {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(LocationSettingsResult locationSettingsResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.internal.ISettingsCallbacks");
                    if (locationSettingsResult == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        locationSettingsResult.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public zza() {
            attachInterface(this, "com.google.android.gms.location.internal.ISettingsCallbacks");
        }

        public static zzj zzck(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.internal.ISettingsCallbacks");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzj)) ? (zzj) queryLocalInterface : new zza(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.location.internal.ISettingsCallbacks");
                    zza(data.readInt() == 0 ? null : (LocationSettingsResult) LocationSettingsResult.CREATOR.createFromParcel(data));
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.location.internal.ISettingsCallbacks");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(LocationSettingsResult locationSettingsResult) throws RemoteException;
}
