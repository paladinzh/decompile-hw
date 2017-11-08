package com.huawei.systemmanager.preventmode;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHoldPreventService extends IInterface {

    public static abstract class Stub extends Binder implements IHoldPreventService {

        private static class Proxy implements IHoldPreventService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public boolean isPrevent(String phoneNumber, boolean isPhoneUsed) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.systemmanager.preventmode.IHoldPreventService");
                    _data.writeString(phoneNumber);
                    if (!isPhoneUsed) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] queryAllWhiteListPhoneNo() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.systemmanager.preventmode.IHoldPreventService");
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.systemmanager.preventmode.IHoldPreventService");
        }

        public static IHoldPreventService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.systemmanager.preventmode.IHoldPreventService");
            if (iin == null || !(iin instanceof IHoldPreventService)) {
                return new Proxy(obj);
            }
            return (IHoldPreventService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            switch (code) {
                case 1:
                    boolean _arg1;
                    data.enforceInterface("com.huawei.systemmanager.preventmode.IHoldPreventService");
                    String _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        _arg1 = true;
                    } else {
                        _arg1 = false;
                    }
                    boolean _result = isPrevent(_arg0, _arg1);
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.systemmanager.preventmode.IHoldPreventService");
                    String[] _result2 = queryAllWhiteListPhoneNo();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.systemmanager.preventmode.IHoldPreventService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean isPrevent(String str, boolean z) throws RemoteException;

    String[] queryAllWhiteListPhoneNo() throws RemoteException;
}
