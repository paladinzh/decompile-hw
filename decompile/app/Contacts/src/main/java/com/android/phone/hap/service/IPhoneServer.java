package com.android.phone.hap.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPhoneServer extends IInterface {

    public static abstract class Stub extends Binder implements IPhoneServer {

        private static class Proxy implements IPhoneServer {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getIpPrefix(int sub) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.phone.hap.service.IPhoneServer");
                    _data.writeInt(sub);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.android.phone.hap.service.IPhoneServer");
        }

        public static IPhoneServer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.android.phone.hap.service.IPhoneServer");
            if (iin == null || !(iin instanceof IPhoneServer)) {
                return new Proxy(obj);
            }
            return (IPhoneServer) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.android.phone.hap.service.IPhoneServer");
                    String _result = getIpPrefix(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.android.phone.hap.service.IPhoneServer");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String getIpPrefix(int i) throws RemoteException;
}
