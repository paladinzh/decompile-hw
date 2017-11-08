package com.google.android.gms.maps.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.b;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

/* compiled from: Unknown */
public interface IProjectionDelegate extends IInterface {

    /* compiled from: Unknown */
    public static abstract class a extends Binder implements IProjectionDelegate {

        /* compiled from: Unknown */
        private static class a implements IProjectionDelegate {
            private IBinder ky;

            a(IBinder iBinder) {
                this.ky = iBinder;
            }

            public IBinder asBinder() {
                return this.ky;
            }

            public LatLng fromScreenLocation(b point) throws RemoteException {
                LatLng latLng = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.IProjectionDelegate");
                    obtain.writeStrongBinder(point == null ? null : point.asBinder());
                    this.ky.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        latLng = LatLng.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return latLng;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public VisibleRegion getVisibleRegion() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.IProjectionDelegate");
                    this.ky.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    VisibleRegion createFromParcel = obtain2.readInt() == 0 ? null : VisibleRegion.CREATOR.createFromParcel(obtain2);
                    obtain2.recycle();
                    obtain.recycle();
                    return createFromParcel;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public b toScreenLocation(LatLng location) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.IProjectionDelegate");
                    if (location == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        location.writeToParcel(obtain, 0);
                    }
                    this.ky.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    b G = com.google.android.gms.dynamic.b.a.G(obtain2.readStrongBinder());
                    return G;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static IProjectionDelegate ak(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.maps.internal.IProjectionDelegate");
            return (queryLocalInterface != null && (queryLocalInterface instanceof IProjectionDelegate)) ? (IProjectionDelegate) queryLocalInterface : new a(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.maps.internal.IProjectionDelegate");
                    LatLng fromScreenLocation = fromScreenLocation(com.google.android.gms.dynamic.b.a.G(data.readStrongBinder()));
                    reply.writeNoException();
                    if (fromScreenLocation == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        fromScreenLocation.writeToParcel(reply, 1);
                    }
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.maps.internal.IProjectionDelegate");
                    b toScreenLocation = toScreenLocation(data.readInt() == 0 ? null : LatLng.CREATOR.createFromParcel(data));
                    reply.writeNoException();
                    if (toScreenLocation != null) {
                        iBinder = toScreenLocation.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.maps.internal.IProjectionDelegate");
                    VisibleRegion visibleRegion = getVisibleRegion();
                    reply.writeNoException();
                    if (visibleRegion == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        visibleRegion.writeToParcel(reply, 1);
                    }
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.maps.internal.IProjectionDelegate");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    LatLng fromScreenLocation(b bVar) throws RemoteException;

    VisibleRegion getVisibleRegion() throws RemoteException;

    b toScreenLocation(LatLng latLng) throws RemoteException;
}
