package com.tencent.tmsecurelite.base;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import com.tencent.tmsecurelite.commom.ITmsCallback;

public final class TmsConnectionProxy implements ITmsConnection {
    private IBinder mRemote;

    public TmsConnectionProxy(IBinder binder) {
        this.mRemote = binder;
    }

    public IBinder asBinder() {
        return this.mRemote;
    }

    public boolean checkVersion(int version) throws RemoteException {
        boolean z = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            data.writeInt(version);
            this.mRemote.transact(2, data, reply, 0);
            reply.readException();
            if (reply.readInt() == 1) {
                z = true;
            }
            data.recycle();
            reply.recycle();
            return z;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public void updateTmsConfigAsync(ITmsCallback callback) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            data.writeStrongBinder((IBinder) callback);
            this.mRemote.transact(3, data, reply, 0);
            reply.readException();
        } finally {
            data.recycle();
            reply.recycle();
        }
    }

    public boolean checkPermission(String pkg, int moduleId) throws RemoteException {
        boolean z = false;
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            data.writeString(pkg);
            data.writeInt(moduleId);
            this.mRemote.transact(1, data, reply, 0);
            reply.readException();
            if (reply.readInt() == 1) {
                z = true;
            }
            data.recycle();
            reply.recycle();
            return z;
        } catch (Throwable th) {
            data.recycle();
            reply.recycle();
        }
    }

    public int sendTmsRequest(int cmdId, Bundle data, Bundle reply) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            _data.writeInt(cmdId);
            data.writeToParcel(_data, 0);
            reply.writeToParcel(_data, 0);
            this.mRemote.transact(4, _data, _reply, 0);
            _reply.readException();
            int result = _reply.readInt();
            reply.readFromParcel(_reply);
            return result;
        } finally {
            _data.recycle();
            _reply.recycle();
        }
    }

    public int sendTmsCallback(int cmdId, Bundle data, ITmsCallbackEx callback) throws RemoteException {
        Parcel _data = Parcel.obtain();
        Parcel _reply = Parcel.obtain();
        try {
            _data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            _data.writeInt(cmdId);
            data.writeToParcel(_data, 0);
            _data.writeStrongBinder((IBinder) callback);
            this.mRemote.transact(5, _data, _reply, 0);
            _reply.readException();
            int result = _reply.readInt();
            return result;
        } finally {
            _data.recycle();
            _reply.recycle();
        }
    }

    public int setProvider(ITmsProvider provider) throws RemoteException {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsConnection");
            data.writeStrongBinder((IBinder) provider);
            this.mRemote.transact(6, data, reply, 0);
            reply.readException();
            int result = reply.readInt();
            return result;
        } finally {
            data.recycle();
            reply.recycle();
        }
    }
}
