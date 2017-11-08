package com.huawei.powergenie.integration.adapter.pged;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.powergenie.integration.adapter.NativeAdapter;

public interface IPgedBinderListener extends IInterface {

    public static abstract class Stub extends Binder implements IPgedBinderListener {

        private static class Proxy implements IPgedBinderListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int onKstateCallback(int len, byte[] buf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    _data.writeInt(len);
                    _data.writeByteArray(buf);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readByteArray(buf);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int onNetRecalledMsgCallback(int len, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    _data.writeInt(len);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readIntArray(uids);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int onMessageCallback(String msg, int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    _data.writeString(msg);
                    _data.writeInt(action);
                    this.mRemote.transact(3, _data, _reply, 0);
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
            attachInterface(this, "com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
        }

        public static IPgedBinderListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
            if (iin == null || !(iin instanceof IPgedBinderListener)) {
                return new Proxy(obj);
            }
            return (IPgedBinderListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            int _result;
            switch (code) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    _arg0 = data.readInt();
                    byte[] _arg1 = data.createByteArray();
                    _result = onKstateCallback(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeByteArray(_arg1);
                    return true;
                case NativeAdapter.PLATFORM_HI /*2*/:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    _arg0 = data.readInt();
                    int[] _arg12 = data.createIntArray();
                    _result = onNetRecalledMsgCallback(_arg0, _arg12);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeIntArray(_arg12);
                    return true;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    _result = onMessageCallback(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int onKstateCallback(int i, byte[] bArr) throws RemoteException;

    int onMessageCallback(String str, int i) throws RemoteException;

    int onNetRecalledMsgCallback(int i, int[] iArr) throws RemoteException;
}
