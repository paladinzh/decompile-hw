package com.google.android.gms.location.places.internal;

import android.app.PendingIntent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.NearbyAlertRequest;
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceReport;
import com.google.android.gms.location.places.PlaceRequest;
import com.google.android.gms.location.places.UserDataType;
import com.google.android.gms.location.places.personalized.PlaceAlias;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.List;

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

            public void zza(AddPlaceRequest addPlaceRequest, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (addPlaceRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        addPlaceRequest.writeToParcel(obtain, 0);
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
                    this.zzoz.transact(14, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(NearbyAlertRequest nearbyAlertRequest, PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
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
                    this.zzoz.transact(11, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceReport placeReport, PlacesParams placesParams) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
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
                    this.zzoz.transact(15, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceRequest placeRequest, PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
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
                    this.zzoz.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(UserDataType userDataType, LatLngBounds latLngBounds, List<String> list, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (userDataType == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        userDataType.writeToParcel(obtain, 0);
                    }
                    if (latLngBounds == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        latLngBounds.writeToParcel(obtain, 0);
                    }
                    obtain.writeStringList(list);
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
                    this.zzoz.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
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
                    this.zzoz.transact(10, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceAlias placeAlias, PlacesParams placesParams, com.google.android.gms.location.places.personalized.zza zza) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (placeAlias == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeAlias.writeToParcel(obtain, 0);
                    }
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (zza != null) {
                        iBinder = zza.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(21, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(PlaceAlias placeAlias, String str, String str2, PlacesParams placesParams, com.google.android.gms.location.places.personalized.zza zza) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (placeAlias == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placeAlias.writeToParcel(obtain, 0);
                    }
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (zza != null) {
                        iBinder = zza.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(16, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(LatLng latLng, PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (latLng == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        latLng.writeToParcel(obtain, 0);
                    }
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
                    this.zzoz.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(LatLng latLng, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (latLng == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        latLng.writeToParcel(obtain, 0);
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
                    this.zzoz.transact(22, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(LatLngBounds latLngBounds, int i, String str, PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    if (latLngBounds == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        latLngBounds.writeToParcel(obtain, 0);
                    }
                    obtain.writeInt(i);
                    obtain.writeString(str);
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
                    this.zzoz.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(String str, int i, int i2, int i3, PlacesParams placesParams, zzh zzh) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
                    obtain.writeInt(i2);
                    obtain.writeInt(i3);
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (zzh != null) {
                        iBinder = zzh.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(20, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(String str, int i, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeString(str);
                    obtain.writeInt(i);
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
                    this.zzoz.transact(18, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(String str, PlacesParams placesParams, zzh zzh) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeString(str);
                    if (placesParams == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        placesParams.writeToParcel(obtain, 0);
                    }
                    if (zzh != null) {
                        iBinder = zzh.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(19, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(String str, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeString(str);
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
                    this.zzoz.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(String str, LatLngBounds latLngBounds, AutocompleteFilter autocompleteFilter, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeString(str);
                    if (latLngBounds == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        latLngBounds.writeToParcel(obtain, 0);
                    }
                    if (autocompleteFilter == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        autocompleteFilter.writeToParcel(obtain, 0);
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
                    this.zzoz.transact(13, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(List<String> list, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeStringList(list);
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

            public void zzb(PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
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
                    this.zzoz.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zzb(PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
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
                    this.zzoz.transact(12, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zzb(String str, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeString(str);
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

            public void zzb(List<String> list, PlacesParams placesParams, zzi zzi) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    obtain.writeStringList(list);
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
                    this.zzoz.transact(17, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static zzg zzcn(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzg)) ? (zzg) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PlacesParams placesParams = null;
            int readInt;
            String readString;
            LatLng zzfz;
            List createStringArrayList;
            PlacesParams zzfo;
            PendingIntent pendingIntent;
            PlacesParams zzfo2;
            switch (code) {
                case 2:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    LatLngBounds zzfy = data.readInt() == 0 ? null : LatLngBounds.CREATOR.zzfy(data);
                    readInt = data.readInt();
                    String readString2 = data.readString();
                    PlaceFilter zzfe = data.readInt() == 0 ? null : PlaceFilter.CREATOR.zzfe(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(zzfy, readInt, readString2, zzfe, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(readString, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zzfz = data.readInt() == 0 ? null : LatLng.CREATOR.zzfz(data);
                    PlaceFilter zzfe2 = data.readInt() == 0 ? null : PlaceFilter.CREATOR.zzfe(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(zzfz, zzfe2, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    PlaceFilter zzfe3 = data.readInt() == 0 ? null : PlaceFilter.CREATOR.zzfe(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zzb(zzfe3, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zzb(readString, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    createStringArrayList = data.createStringArrayList();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(createStringArrayList, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zza(data.readInt() == 0 ? null : UserDataType.CREATOR.zzfj(data), data.readInt() == 0 ? null : LatLngBounds.CREATOR.zzfy(data), data.createStringArrayList(), data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data), com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    PlaceRequest placeRequest = data.readInt() == 0 ? null : (PlaceRequest) PlaceRequest.CREATOR.createFromParcel(data);
                    zzfo = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zza(placeRequest, zzfo, pendingIntent);
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zzfo2 = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zza(zzfo2, pendingIntent);
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    NearbyAlertRequest zzfd = data.readInt() == 0 ? null : NearbyAlertRequest.CREATOR.zzfd(data);
                    zzfo = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zza(zzfd, zzfo, pendingIntent);
                    reply.writeNoException();
                    return true;
                case 12:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zzfo2 = data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data);
                    if (data.readInt() != 0) {
                        pendingIntent = (PendingIntent) PendingIntent.CREATOR.createFromParcel(data);
                    }
                    zzb(zzfo2, pendingIntent);
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zza(data.readString(), data.readInt() == 0 ? null : LatLngBounds.CREATOR.zzfy(data), data.readInt() == 0 ? null : AutocompleteFilter.CREATOR.zzfb(data), data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data), com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    AddPlaceRequest addPlaceRequest = data.readInt() == 0 ? null : (AddPlaceRequest) AddPlaceRequest.CREATOR.createFromParcel(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(addPlaceRequest, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    PlaceReport placeReport = data.readInt() == 0 ? null : (PlaceReport) PlaceReport.CREATOR.createFromParcel(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(placeReport, placesParams);
                    reply.writeNoException();
                    return true;
                case 16:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zza(data.readInt() == 0 ? null : PlaceAlias.CREATOR.zzfq(data), data.readString(), data.readString(), data.readInt() == 0 ? null : PlacesParams.CREATOR.zzfo(data), com.google.android.gms.location.places.personalized.zza.zza.zzcr(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 17:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    createStringArrayList = data.createStringArrayList();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zzb(createStringArrayList, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    readString = data.readString();
                    int readInt2 = data.readInt();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(readString, readInt2, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 19:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(readString, placesParams, com.google.android.gms.location.places.internal.zzh.zza.zzco(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    String readString3 = data.readString();
                    readInt = data.readInt();
                    int readInt3 = data.readInt();
                    int readInt4 = data.readInt();
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(readString3, readInt, readInt3, readInt4, placesParams, com.google.android.gms.location.places.internal.zzh.zza.zzco(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 21:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    PlaceAlias zzfq = data.readInt() == 0 ? null : PlaceAlias.CREATOR.zzfq(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(zzfq, placesParams, com.google.android.gms.location.places.personalized.zza.zza.zzcr(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 22:
                    data.enforceInterface("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    zzfz = data.readInt() == 0 ? null : LatLng.CREATOR.zzfz(data);
                    if (data.readInt() != 0) {
                        placesParams = PlacesParams.CREATOR.zzfo(data);
                    }
                    zza(zzfz, placesParams, com.google.android.gms.location.places.internal.zzi.zza.zzcp(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.location.places.internal.IGooglePlacesService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(AddPlaceRequest addPlaceRequest, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(NearbyAlertRequest nearbyAlertRequest, PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException;

    void zza(PlaceReport placeReport, PlacesParams placesParams) throws RemoteException;

    void zza(PlaceRequest placeRequest, PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException;

    void zza(UserDataType userDataType, LatLngBounds latLngBounds, List<String> list, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException;

    void zza(PlaceAlias placeAlias, PlacesParams placesParams, com.google.android.gms.location.places.personalized.zza zza) throws RemoteException;

    void zza(PlaceAlias placeAlias, String str, String str2, PlacesParams placesParams, com.google.android.gms.location.places.personalized.zza zza) throws RemoteException;

    void zza(LatLng latLng, PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(LatLng latLng, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(LatLngBounds latLngBounds, int i, String str, PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(String str, int i, int i2, int i3, PlacesParams placesParams, zzh zzh) throws RemoteException;

    void zza(String str, int i, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(String str, PlacesParams placesParams, zzh zzh) throws RemoteException;

    void zza(String str, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(String str, LatLngBounds latLngBounds, AutocompleteFilter autocompleteFilter, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zza(List<String> list, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zzb(PlaceFilter placeFilter, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zzb(PlacesParams placesParams, PendingIntent pendingIntent) throws RemoteException;

    void zzb(String str, PlacesParams placesParams, zzi zzi) throws RemoteException;

    void zzb(List<String> list, PlacesParams placesParams, zzi zzi) throws RemoteException;
}
