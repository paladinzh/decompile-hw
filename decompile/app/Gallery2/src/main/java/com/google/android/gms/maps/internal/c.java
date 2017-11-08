package com.google.android.gms.maps.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.b;
import com.google.android.gms.maps.GoogleMapOptions;

/* compiled from: Unknown */
public interface c extends IInterface {

    /* compiled from: Unknown */
    public static abstract class a extends Binder implements c {

        /* compiled from: Unknown */
        private static class a implements c {
            private IBinder ky;

            a(IBinder iBinder) {
                this.ky = iBinder;
            }

            public IMapViewDelegate a(b bVar, GoogleMapOptions googleMapOptions) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    if (bVar != null) {
                        iBinder = bVar.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (googleMapOptions == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        googleMapOptions.writeToParcel(obtain, 0);
                    }
                    this.ky.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    IMapViewDelegate Z = com.google.android.gms.maps.internal.IMapViewDelegate.a.Z(obtain2.readStrongBinder());
                    return Z;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void a(b bVar, int i) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    if (bVar != null) {
                        iBinder = bVar.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    obtain.writeInt(i);
                    this.ky.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public IBinder asBinder() {
                return this.ky;
            }

            public void e(b bVar) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    if (bVar != null) {
                        iBinder = bVar.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.ky.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public IMapFragmentDelegate f(b bVar) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    if (bVar != null) {
                        iBinder = bVar.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.ky.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    IMapFragmentDelegate Y = com.google.android.gms.maps.internal.IMapFragmentDelegate.a.Y(obtain2.readStrongBinder());
                    return Y;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public ICameraUpdateFactoryDelegate gY() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    this.ky.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    ICameraUpdateFactoryDelegate S = com.google.android.gms.maps.internal.ICameraUpdateFactoryDelegate.a.S(obtain2.readStrongBinder());
                    return S;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public com.google.android.gms.maps.model.internal.a gZ() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.maps.internal.ICreator");
                    this.ky.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    com.google.android.gms.maps.model.internal.a an = com.google.android.gms.maps.model.internal.a.a.an(obtain2.readStrongBinder());
                    return an;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static c U(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.maps.internal.ICreator");
            return (queryLocalInterface != null && (queryLocalInterface instanceof c)) ? (c) queryLocalInterface : new a(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    e(com.google.android.gms.dynamic.b.a.G(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    IMapFragmentDelegate f = f(com.google.android.gms.dynamic.b.a.G(data.readStrongBinder()));
                    reply.writeNoException();
                    if (f != null) {
                        iBinder = f.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    IMapViewDelegate a = a(com.google.android.gms.dynamic.b.a.G(data.readStrongBinder()), data.readInt() == 0 ? null : GoogleMapOptions.CREATOR.createFromParcel(data));
                    reply.writeNoException();
                    if (a != null) {
                        iBinder = a.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    ICameraUpdateFactoryDelegate gY = gY();
                    reply.writeNoException();
                    if (gY != null) {
                        iBinder = gY.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    com.google.android.gms.maps.model.internal.a gZ = gZ();
                    reply.writeNoException();
                    if (gZ != null) {
                        iBinder = gZ.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.gms.maps.internal.ICreator");
                    a(com.google.android.gms.dynamic.b.a.G(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.maps.internal.ICreator");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IMapViewDelegate a(b bVar, GoogleMapOptions googleMapOptions) throws RemoteException;

    void a(b bVar, int i) throws RemoteException;

    void e(b bVar) throws RemoteException;

    IMapFragmentDelegate f(b bVar) throws RemoteException;

    ICameraUpdateFactoryDelegate gY() throws RemoteException;

    com.google.android.gms.maps.model.internal.a gZ() throws RemoteException;
}
