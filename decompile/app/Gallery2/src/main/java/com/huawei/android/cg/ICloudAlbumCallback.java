package com.huawei.android.cg;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICloudAlbumCallback extends IInterface {

    public static abstract class Stub extends Binder implements ICloudAlbumCallback {

        private static class Proxy implements ICloudAlbumCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onResult(int id, Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.cg.ICloudAlbumCallback");
                    _data.writeInt(id);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.android.cg.ICloudAlbumCallback");
        }

        public static ICloudAlbumCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.android.cg.ICloudAlbumCallback");
            if (iin == null || !(iin instanceof ICloudAlbumCallback)) {
                return new Proxy(obj);
            }
            return (ICloudAlbumCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    Bundle bundle;
                    data.enforceInterface("com.huawei.android.cg.ICloudAlbumCallback");
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    onResult(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.android.cg.ICloudAlbumCallback");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onResult(int i, Bundle bundle) throws RemoteException;
}
