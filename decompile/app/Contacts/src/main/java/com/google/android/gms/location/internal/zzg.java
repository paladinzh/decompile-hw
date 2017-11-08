package com.google.android.gms.location.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: Unknown */
public interface zzg extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzg {

        /* compiled from: Unknown */
        private static class zza implements zzg {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(FusedLocationProviderResult fusedLocationProviderResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.internal.IFusedLocationProviderCallback");
                    if (fusedLocationProviderResult == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        fusedLocationProviderResult.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public zza() {
            attachInterface(this, "com.google.android.gms.location.internal.IFusedLocationProviderCallback");
        }

        public static zzg zzch(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.internal.IFusedLocationProviderCallback");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzg)) ? (zzg) queryLocalInterface : new zza(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.location.internal.IFusedLocationProviderCallback");
                    zza(data.readInt() == 0 ? null : (FusedLocationProviderResult) FusedLocationProviderResult.CREATOR.createFromParcel(data));
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.location.internal.IFusedLocationProviderCallback");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(FusedLocationProviderResult fusedLocationProviderResult) throws RemoteException;
}
