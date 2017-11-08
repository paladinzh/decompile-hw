package com.huawei.android.quickaction;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQuickActionService extends IInterface {

    public static abstract class Stub extends Binder implements IQuickActionService {
        public Stub() {
            attachInterface(this, "com.huawei.android.quickaction.IQuickActionService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    ComponentName componentName;
                    data.enforceInterface("com.huawei.android.quickaction.IQuickActionService");
                    if (data.readInt() == 0) {
                        componentName = null;
                    } else {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(data);
                    }
                    getQuickActions(componentName, com.huawei.android.quickaction.IQuickActionResult.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.android.quickaction.IQuickActionService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getQuickActions(ComponentName componentName, IQuickActionResult iQuickActionResult) throws RemoteException;
}
