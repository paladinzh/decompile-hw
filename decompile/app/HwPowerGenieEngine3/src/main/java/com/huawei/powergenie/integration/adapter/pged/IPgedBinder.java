package com.huawei.powergenie.integration.adapter.pged;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.powergenie.integration.adapter.NativeAdapter;

public interface IPgedBinder extends IInterface {

    public static abstract class Stub extends Binder implements IPgedBinder {

        private static class Proxy implements IPgedBinder {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int registerListener(IPgedBinderListener listener, int type) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(type);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unregisterListener(IPgedBinderListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int doFreezer(int action, int[] pids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _data.writeInt(action);
                    _data.writeIntArray(pids);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int setKstateMask(int mask) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _data.writeInt(mask);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getKstateSync(int command) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _data.writeInt(command);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int netPacketListener(int len, int[] uids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _data.writeInt(len);
                    _data.writeIntArray(uids);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyBastet(int action, int[] pids) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _data.writeInt(action);
                    _data.writeIntArray(pids);
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public int getProcUTime(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _data.writeInt(pid);
                    this.mRemote.transact(8, _data, _reply, 0);
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
            attachInterface(this, "com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
        }

        public static IPgedBinder asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
            if (iin == null || !(iin instanceof IPgedBinder)) {
                return new Proxy(obj);
            }
            return (IPgedBinder) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            switch (code) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = registerListener(com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case NativeAdapter.PLATFORM_HI /*2*/:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = unregisterListener(com.huawei.powergenie.integration.adapter.pged.IPgedBinderListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case NativeAdapter.PLATFORM_K3V3 /*3*/:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = doFreezer(data.readInt(), data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = setKstateMask(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = getKstateSync(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = netPacketListener(data.readInt(), data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    notifyBastet(data.readInt(), data.createIntArray());
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    _result = getProcUTime(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.powergenie.integration.adapter.pged.IPgedBinder");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int doFreezer(int i, int[] iArr) throws RemoteException;

    int getKstateSync(int i) throws RemoteException;

    int getProcUTime(int i) throws RemoteException;

    int netPacketListener(int i, int[] iArr) throws RemoteException;

    void notifyBastet(int i, int[] iArr) throws RemoteException;

    int registerListener(IPgedBinderListener iPgedBinderListener, int i) throws RemoteException;

    int setKstateMask(int i) throws RemoteException;

    int unregisterListener(IPgedBinderListener iPgedBinderListener) throws RemoteException;
}
