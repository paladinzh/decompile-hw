package com.google.android.gms.location;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

/* compiled from: Unknown */
public interface zzc extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzc {

        /* compiled from: Unknown */
        private static class zza implements zzc {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void onLocationAvailability(LocationAvailability state) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.ILocationCallback");
                    if (state == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        state.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void onLocationResult(LocationResult result) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.ILocationCallback");
                    if (result == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        result.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public zza() {
            attachInterface(this, "com.google.android.gms.location.ILocationCallback");
        }

        public static zzc zzce(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.ILocationCallback");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzc)) ? (zzc) queryLocalInterface : new zza(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            LocationAvailability locationAvailability = null;
            switch (code) {
                case 1:
                    LocationResult locationResult;
                    data.enforceInterface("com.google.android.gms.location.ILocationCallback");
                    if (data.readInt() != 0) {
                        locationResult = (LocationResult) LocationResult.CREATOR.createFromParcel(data);
                    }
                    onLocationResult(locationResult);
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.location.ILocationCallback");
                    if (data.readInt() != 0) {
                        locationAvailability = LocationAvailability.CREATOR.createFromParcel(data);
                    }
                    onLocationAvailability(locationAvailability);
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.location.ILocationCallback");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onLocationAvailability(LocationAvailability locationAvailability) throws RemoteException;

    void onLocationResult(LocationResult locationResult) throws RemoteException;
}
