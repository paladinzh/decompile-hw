package com.tencent.tmsecurelite.base;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITmsProvider extends IInterface {

    public static abstract class Stub extends Binder implements ITmsProvider {

        private static class Proxy implements ITmsProvider {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int ipcCall(int msgId, Bundle inBundle, Bundle inoutBundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsProvider");
                    _data.writeInt(msgId);
                    if (inBundle == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        inBundle.writeToParcel(_data, 0);
                    }
                    if (inoutBundle == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        inoutBundle.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        inoutBundle.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.tencent.tmsecurelite.base.ITmsProvider");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.tencent.tmsecurelite.base.ITmsProvider");
        }

        public static ITmsProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.tencent.tmsecurelite.base.ITmsProvider");
            if (iin != null && (iin instanceof ITmsProvider)) {
                return (ITmsProvider) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case 1:
                    Bundle bundle;
                    Bundle bundle2;
                    data.enforceInterface("com.tencent.tmsecurelite.base.ITmsProvider");
                    int _arg0 = data.readInt();
                    if (data.readInt() == 0) {
                        bundle = null;
                    } else {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    if (data.readInt() == 0) {
                        bundle2 = null;
                    } else {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    _result = ipcCall(_arg0, bundle, bundle2);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    if (bundle2 == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        bundle2.writeToParcel(reply, 1);
                    }
                    return true;
                case 2:
                    data.enforceInterface("com.tencent.tmsecurelite.base.ITmsProvider");
                    _result = getVersion();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.tencent.tmsecurelite.base.ITmsProvider");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int getVersion() throws RemoteException;

    int ipcCall(int i, Bundle bundle, Bundle bundle2) throws RemoteException;
}
