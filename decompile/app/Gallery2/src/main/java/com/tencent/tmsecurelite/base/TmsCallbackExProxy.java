package com.tencent.tmsecurelite.base;

import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;

public class TmsCallbackExProxy implements ITmsCallbackEx {
    private IBinder mRemote;

    public TmsCallbackExProxy(IBinder binder) {
        this.mRemote = binder;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public void onCallback(Message msg) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsCallbackEx");
            data.writeParcelable(msg, 0);
            this.mRemote.transact(1, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
