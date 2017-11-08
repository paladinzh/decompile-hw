package com.huawei.health;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IStepDataReport extends IInterface {

    public static abstract class Stub extends Binder implements IStepDataReport {

        private static class Proxy implements IStepDataReport {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void report(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IStepDataReport");
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
        }

        public Stub() {
            attachInterface(this, "com.huawei.health.IStepDataReport");
        }

        public static IStepDataReport asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.health.IStepDataReport");
            if (iin != null && (iin instanceof IStepDataReport)) {
                return (IStepDataReport) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    Bundle bundle;
                    data.enforceInterface("com.huawei.health.IStepDataReport");
                    if (data.readInt() == 0) {
                        bundle = null;
                    } else {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    report(bundle);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.health.IStepDataReport");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void report(Bundle bundle) throws RemoteException;
}
