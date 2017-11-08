package com.huawei.keyguard.hiad;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHiAdService extends IInterface {

    public static abstract class Stub extends Binder implements IHiAdService {
        public Stub() {
            attachInterface(this, "com.huawei.keyguard.hiad.IHiAdService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    HiAdInfo hiAdInfo;
                    data.enforceInterface("com.huawei.keyguard.hiad.IHiAdService");
                    if (data.readInt() != 0) {
                        hiAdInfo = (HiAdInfo) HiAdInfo.CREATOR.createFromParcel(data);
                    } else {
                        hiAdInfo = null;
                    }
                    int _result = transferHiAdInfo(hiAdInfo);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.keyguard.hiad.IHiAdService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int transferHiAdInfo(HiAdInfo hiAdInfo) throws RemoteException;
}
