package com.tencent.tmsecurelite.commom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import org.json.JSONException;

public abstract class TmsCallbackStub extends Binder implements ITmsCallback {
    public static ITmsCallback asInterface(IBinder binder) {
        if (binder == null) {
            return null;
        }
        IInterface iInterface = binder.queryLocalInterface("com.tencent.tmsecurelite.ITmsCallback");
        if (iInterface != null && (iInterface instanceof ITmsCallback)) {
            return (ITmsCallback) iInterface;
        }
        return new TmsCallbackProxy(binder);
    }

    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case 1:
                int err = data.readInt();
                DataEntity dataEntity = null;
                try {
                    dataEntity = new DataEntity(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                onResultGot(err, dataEntity);
                reply.writeNoException();
                break;
            case 2:
                onArrayResultGot(data.readInt(), DataEntity.readFromParcel(data));
                reply.writeNoException();
                break;
        }
        return true;
    }
}
