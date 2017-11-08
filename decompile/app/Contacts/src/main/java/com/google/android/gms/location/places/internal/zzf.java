package com.google.android.gms.location.places.internal;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.location.places.NearbyAlertRequest;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.PlaceRequest;

/* compiled from: Unknown */
public interface zzf extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzf {

        /* compiled from: Unknown */
        private static class zza implements zzf {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(NearbyAlertRequest nearbyAlertRequest, PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    if (nearbyAlertRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        nearbyAlertRequest.writeToParcel(obtain, 0);
                    }
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (pendingIntent == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        pendingIntent.writeToParcel(obtain, 0);
                    }
                    if (zzi != null) {
                        iBinder = zzi.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    if (placeFilter == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeFilter.writeToParcel(obtain, 0);
                    }
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (zzi != null) {
                        iBinder = zzi.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceReport placeReport, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    if (placeReport == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeReport.writeToParcel(obtain, 0);
                    }
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (zzi != null) {
                        iBinder = zzi.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceRequest placeRequest, PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    if (placeRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeRequest.writeToParcel(obtain, 0);
                    }
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (pendingIntent == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        pendingIntent.writeToParcel(obtain, 0);
                    }
                    if (zzi != null) {
                        iBinder = zzi.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (pendingIntent == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        pendingIntent.writeToParcel(obtain, 0);
                    }
                    if (zzi != null) {
                        iBinder = zzi.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zzb(PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (pendingIntent == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        pendingIntent.writeToParcel(obtain, 0);
                    }
                    if (zzi != null) {
                        iBinder = zzi.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static zzf zzcm(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzf)) ? (zzf) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PlacesParams placesParams = null;
            PlacesParams zzfo;
            PendingIntent pendingIntent;
            PlacesParams zzfo2;
            switch (code) {
                case 2:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    PlaceRequest placeRequest = data.readInt() == 0 ? null : (PlaceRequest) PlaceRequest.CREATOR.createFromParcel(data);
                    zzfo = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zza(placeRequest, zzfo, pendingIntent, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    zzfo2 = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zza(zzfo2, pendingIntent, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    NearbyAlertRequest zzfd = data.readInt() == 0 ? null : NearbyAlertRequest.CREATOR.zzfd(data);
                    zzfo = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zza(zzfd, zzfo, pendingIntent, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    zzfo2 = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zzb(zzfo2, pendingIntent, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    PlaceFilter zzfe = data.readInt() == 0 ? null : PlaceFilter.CREATOR.zzfe(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(zzfe, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    PlaceReport placeReport = data.readInt() == 0 ? null : (PlaceReport) PlaceReport.CREATOR.createFromParcel(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(placeReport, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.location.places.internal.IGooglePlaceDetectionService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(NearbyAlertRequest nearbyAlertRequest, PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException;

    void zza(PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(PlaceReport placeReport, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(PlaceRequest placeRequest, PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException;

    void zza(PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException;

    void zzb(PlacesParams placesParams, PendingIntent pendingIntent, zzi zzi) throws RemoteException;
}
