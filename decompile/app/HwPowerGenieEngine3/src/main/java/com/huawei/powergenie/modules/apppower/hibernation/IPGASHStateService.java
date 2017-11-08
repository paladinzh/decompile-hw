package com.huawei.powergenie.modules.apppower.hibernation;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.powergenie.integration.adapter.NativeAdapter;
import java.util.List;
import java.util.Map;

public interface IPGASHStateService extends IInterface {

    public static abstract class Stub extends Binder implements IPGASHStateService {
        public Stub() {
            attachInterface(this, "com.huawei.powergenie.modules.apppower.hibernation.IPGASHStateService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            boolean _result;
            switch (code) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    data.enforceInterface("com.huawei.powergenie.modules.apppower.hibernation.IPGASHStateService");
                    Map _result2 = getAppsState(data.createStringArrayList());
                    reply.writeNoException();
                    reply.writeMap(_result2);
                    return true;
                case NativeAdapter.PLATFORM_HI /*2*/:
                    data.enforceInterface("com.huawei.powergenie.modules.apppower.hibernation.IPGASHStateService");
                    _result = hibernateApps(data.createStringArrayList());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    data.enforceInterface("com.huawei.powergenie.modules.apppower.hibernation.IPGASHStateService");
                    _result = wakeupApps(data.createStringArrayList());
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.powergenie.modules.apppower.hibernation.IPGASHStateService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Map getAppsState(List<String> list) throws RemoteException;

    boolean hibernateApps(List<String> list) throws RemoteException;

    boolean wakeupApps(List<String> list) throws RemoteException;
}
