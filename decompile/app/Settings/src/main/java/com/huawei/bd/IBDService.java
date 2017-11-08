package com.huawei.bd;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBDService extends IInterface {

    public static abstract class Stub extends Binder implements IBDService {

        private static class Proxy implements IBDService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int sendAppActionData(String pkgName, int eventID, String eventMsg, int priority) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.bd.IBDService");
                    _data.writeString(pkgName);
                    _data.writeInt(eventID);
                    _data.writeString(eventMsg);
                    _data.writeInt(priority);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int sendAccumulativeData(String pkgName, int eventID, int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.bd.IBDService");
                    _data.writeString(pkgName);
                    _data.writeInt(eventID);
                    _data.writeInt(count);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.bd.IBDService");
        }

        public static IBDService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.bd.IBDService");
            if (iin != null && (iin instanceof IBDService)) {
                return (IBDService) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.bd.IBDService");
                    _result = sendAppActionData(data.readString(), data.readInt(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.bd.IBDService");
                    _result = sendAccumulativeData(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.bd.IBDService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int sendAccumulativeData(String str, int i, int i2) throws RemoteException;

    int sendAppActionData(String str, int i, String str2, int i2) throws RemoteException;
}
