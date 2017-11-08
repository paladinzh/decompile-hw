package com.huawei.rcs.commonInterface;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IfMsgplusCb extends IInterface {

    public static abstract class Stub extends Binder implements IfMsgplusCb {

        private static class Proxy implements IfMsgplusCb {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void handleEvent(int respCode, Bundle res_obj) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.commonInterface.IfMsgplusCb");
                    _data.writeInt(respCode);
                    if (res_obj != null) {
                        _data.writeInt(1);
                        res_obj.writeToParcel(_data, 0);
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
            attachInterface(this, "com.huawei.rcs.commonInterface.IfMsgplusCb");
        }

        public static IfMsgplusCb asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.rcs.commonInterface.IfMsgplusCb");
            if (iin == null || !(iin instanceof IfMsgplusCb)) {
                return new Proxy(obj);
            }
            return (IfMsgplusCb) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    Bundle bundle;
                    data.enforceInterface("com.huawei.rcs.commonInterface.IfMsgplusCb");
                    int _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    handleEvent(_arg0, bundle);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.rcs.commonInterface.IfMsgplusCb");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void handleEvent(int i, Bundle bundle) throws RemoteException;
}
