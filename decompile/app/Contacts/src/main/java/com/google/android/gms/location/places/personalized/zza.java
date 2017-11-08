package com.google.android.gms.location.places.personalized;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: Unknown */
public interface zza extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zza {

        /* compiled from: Unknown */
        private static class zza implements zza {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(PlaceAliasResult placeAliasResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.personalized.IPlaceAliasCallbacks");
                    if (placeAliasResult == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeAliasResult.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void zzb(PlaceAliasResult placeAliasResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.personalized.IPlaceAliasCallbacks");
                    if (placeAliasResult == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeAliasResult.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(3, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static zza zzcr(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.places.personalized.IPlaceAliasCallbacks");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zza)) ? (zza) queryLocalInterface : new zza(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PlaceAliasResult placeAliasResult = null;
            switch (code) {
                case 2:
                    data.enforceInterface("com.google.android.gms.location.places.personalized.IPlaceAliasCallbacks");
                    if (data.readInt() != 0) {
                        placeAliasResult = (PlaceAliasResult) PlaceAliasResult.CREATOR.createFromParcel(data);
                    }
                    zza(placeAliasResult);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.location.places.personalized.IPlaceAliasCallbacks");
                    if (data.readInt() != 0) {
                        placeAliasResult = (PlaceAliasResult) PlaceAliasResult.CREATOR.createFromParcel(data);
                    }
                    zzb(placeAliasResult);
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.location.places.personalized.IPlaceAliasCallbacks");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(PlaceAliasResult placeAliasResult) throws RemoteException;

    void zzb(PlaceAliasResult placeAliasResult) throws RemoteException;
}
