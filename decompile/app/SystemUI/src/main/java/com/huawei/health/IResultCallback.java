package com.huawei.health;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IResultCallback extends IInterface {

    public static abstract class Stub extends Binder implements IResultCallback {

        private static class Proxy implements IResultCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onSuccess(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IResultCallback");
                    if (bundle == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onFailed(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IResultCallback");
                    if (bundle == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onServiceException(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IResultCallback");
                    if (bundle == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.health.IResultCallback");
        }

        public static IResultCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.health.IResultCallback");
            if (iin != null && (iin instanceof IResultCallback)) {
                return (IResultCallback) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle bundle;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.health.IResultCallback");
                    if (data.readInt() == 0) {
                        bundle = null;
                    } else {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    onSuccess(bundle);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.health.IResultCallback");
                    if (data.readInt() == 0) {
                        bundle = null;
                    } else {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    onFailed(bundle);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.health.IResultCallback");
                    if (data.readInt() == 0) {
                        bundle = null;
                    } else {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    onServiceException(bundle);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.health.IResultCallback");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onFailed(Bundle bundle) throws RemoteException;

    void onServiceException(Bundle bundle) throws RemoteException;

    void onSuccess(Bundle bundle) throws RemoteException;
}
