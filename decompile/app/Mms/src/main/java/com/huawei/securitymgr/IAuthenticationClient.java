package com.huawei.securitymgr;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthenticationClient extends IInterface {

    public static abstract class Stub extends Binder implements IAuthenticationClient {

        private static class Proxy implements IAuthenticationClient {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onMessage(int what, int arg1, int arg2, byte[] payload) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationClient");
                    _data.writeInt(what);
                    _data.writeInt(arg1);
                    _data.writeInt(arg2);
                    _data.writeByteArray(payload);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.securitymgr.IAuthenticationClient");
        }

        public static IAuthenticationClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.securitymgr.IAuthenticationClient");
            if (iin == null || !(iin instanceof IAuthenticationClient)) {
                return new Proxy(obj);
            }
            return (IAuthenticationClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationClient");
                    onMessage(data.readInt(), data.readInt(), data.readInt(), data.createByteArray());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.securitymgr.IAuthenticationClient");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onMessage(int i, int i2, int i3, byte[] bArr) throws RemoteException;
}
