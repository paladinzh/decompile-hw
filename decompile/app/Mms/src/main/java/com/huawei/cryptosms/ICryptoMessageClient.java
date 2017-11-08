package com.huawei.cryptosms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICryptoMessageClient extends IInterface {

    public static abstract class Stub extends Binder implements ICryptoMessageClient {

        private static class Proxy implements ICryptoMessageClient {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onMessage(int what, int arg1, String arg2) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageClient");
                    _data.writeInt(what);
                    _data.writeInt(arg1);
                    _data.writeString(arg2);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.cryptosms.ICryptoMessageClient");
        }

        public static ICryptoMessageClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.cryptosms.ICryptoMessageClient");
            if (iin != null && (iin instanceof ICryptoMessageClient)) {
                return (ICryptoMessageClient) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageClient");
                    onMessage(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.cryptosms.ICryptoMessageClient");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onMessage(int i, int i2, String str) throws RemoteException;
}
