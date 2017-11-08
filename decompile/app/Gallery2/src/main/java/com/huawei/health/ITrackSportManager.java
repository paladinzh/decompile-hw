package com.huawei.health;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITrackSportManager extends IInterface {

    public static abstract class Stub extends Binder implements ITrackSportManager {

        private static class Proxy implements ITrackSportManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int isTrackWorking() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerDataCallback(ITrackDataReport cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unRegisterDataCallback(ITrackDataReport cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void startSport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void pauseSport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void stopSport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resumeSport() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.ITrackSportManager");
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.health.ITrackSportManager");
        }

        public static ITrackSportManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.health.ITrackSportManager");
            if (iin != null && (iin instanceof ITrackSportManager)) {
                return (ITrackSportManager) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    int _result = isTrackWorking();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    registerDataCallback(com.huawei.health.ITrackDataReport.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    unRegisterDataCallback(com.huawei.health.ITrackDataReport.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    startSport();
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    pauseSport();
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    stopSport();
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.health.ITrackSportManager");
                    resumeSport();
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.health.ITrackSportManager");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int isTrackWorking() throws RemoteException;

    void pauseSport() throws RemoteException;

    void registerDataCallback(ITrackDataReport iTrackDataReport) throws RemoteException;

    void resumeSport() throws RemoteException;

    void startSport() throws RemoteException;

    void stopSport() throws RemoteException;

    void unRegisterDataCallback(ITrackDataReport iTrackDataReport) throws RemoteException;
}
