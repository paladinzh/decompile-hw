package com.tencent.tmsecurelite.base;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;

public abstract class TmsCallbackExStub extends Binder implements ITmsCallbackEx {
    public TmsCallbackExStub() {
        attachInterface(this, "com.tencent.tmsecurelite.base.ITmsCallbackEx");
    }

    public static ITmsCallbackEx asInterface(IBinder binder) {
        if (binder == null) {
            return null;
        }
        IInterface iInterface = binder.queryLocalInterface("com.tencent.tmsecurelite.base.ITmsCallbackEx");
        if (iInterface != null && (iInterface instanceof ITmsCallbackEx)) {
            return (ITmsCallbackEx) iInterface;
        }
        return new TmsCallbackExProxy(binder);
    }

    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsCallbackEx");
                onCallback((Message) data.readParcelable(TmsCallbackExStub.class.getClassLoader()));
                reply.writeNoException();
                break;
        }
        return true;
    }
}
