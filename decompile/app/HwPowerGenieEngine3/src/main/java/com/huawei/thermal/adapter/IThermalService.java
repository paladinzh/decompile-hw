package com.huawei.thermal.adapter;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.powergenie.integration.adapter.NativeAdapter;

public interface IThermalService extends IInterface {

    public static abstract class Stub extends Binder implements IThermalService {
        public Stub() {
            attachInterface(this, "com.huawei.thermal.adapter.IThermalService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    data.enforceInterface("com.huawei.thermal.adapter.IThermalService");
                    int _result = onTemperatureChg(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.thermal.adapter.IThermalService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int onTemperatureChg(int i, int i2) throws RemoteException;
}
