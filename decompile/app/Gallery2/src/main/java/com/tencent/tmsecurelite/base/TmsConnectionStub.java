package com.tencent.tmsecurelite.base;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.tencent.tmsecurelite.base.ITmsProvider.Stub;
import com.tencent.tmsecurelite.commom.TmsCallbackStub;

public abstract class TmsConnectionStub extends Binder implements ITmsConnection {
    public static ITmsConnection asInterface(IBinder binder) {
        if (binder == null) {
            return null;
        }
        IInterface iInterface = binder.queryLocalInterface("com.tencent.tmsecurelite.base.ITmsConnection");
        if (iInterface != null && (iInterface instanceof ITmsConnection)) {
            return (ITmsConnection) iInterface;
        }
        return new TmsConnectionProxy(binder);
    }

    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        boolean state;
        int reuslt;
        switch (code) {
            case 1:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                state = checkPermission(data.readString(), data.readInt());
                reply.writeNoException();
                reply.writeInt(!state ? 0 : 1);
                break;
            case 2:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                state = checkVersion(data.readInt());
                reply.writeNoException();
                reply.writeInt(!state ? 0 : 1);
                break;
            case 3:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                updateTmsConfigAsync(TmsCallbackStub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            case 4:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                Bundle _reply = (Bundle) Bundle.CREATOR.createFromParcel(data);
                int result = sendTmsRequest(data.readInt(), (Bundle) Bundle.CREATOR.createFromParcel(data), _reply);
                reply.writeNoException();
                reply.writeInt(result);
                _reply.writeToParcel(reply, 1);
                break;
            case 5:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                reuslt = sendTmsCallback(data.readInt(), (Bundle) Bundle.CREATOR.createFromParcel(data), TmsCallbackExStub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(reuslt);
                break;
            case 6:
                data.enforceInterface("com.tencent.tmsecurelite.base.ITmsConnection");
                reuslt = setProvider(Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(reuslt);
                break;
        }
        return true;
    }

    public boolean checkVersion(int version) {
        return 3 >= version;
    }
}
