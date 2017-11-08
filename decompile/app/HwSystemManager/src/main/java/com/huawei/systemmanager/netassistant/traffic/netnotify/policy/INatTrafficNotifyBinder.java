package com.huawei.systemmanager.netassistant.traffic.netnotify.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INatTrafficNotifyBinder extends IInterface {

    public static abstract class Stub extends Binder implements INatTrafficNotifyBinder {
        private static final String DESCRIPTOR = "com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyBinder";
        static final int TRANSACTION_notifyDailyWarn = 5;
        static final int TRANSACTION_notifyMonthLimit = 3;
        static final int TRANSACTION_notifyMonthWarn = 4;
        static final int TRANSACTION_registerTrafficNotifyListener = 1;
        static final int TRANSACTION_unRegisterTrafficNotifyListener = 2;

        private static class Proxy implements INatTrafficNotifyBinder {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void registerTrafficNotifyListener(INatTrafficNotifyListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unRegisterTrafficNotifyListener(INatTrafficNotifyListener listener) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (listener != null) {
                        iBinder = listener.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyMonthLimit(String imsi, String iface, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(iface);
                    _data.writeLong(bytes);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyMonthWarn(String imsi, String iface, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(iface);
                    _data.writeLong(bytes);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyDailyWarn(String imsi, String iface, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(iface);
                    _data.writeLong(bytes);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INatTrafficNotifyBinder asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INatTrafficNotifyBinder)) {
                return new Proxy(obj);
            }
            return (INatTrafficNotifyBinder) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    registerTrafficNotifyListener(com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    unRegisterTrafficNotifyListener(com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    notifyMonthLimit(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    notifyMonthWarn(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    notifyDailyWarn(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void notifyDailyWarn(String str, String str2, long j) throws RemoteException;

    void notifyMonthLimit(String str, String str2, long j) throws RemoteException;

    void notifyMonthWarn(String str, String str2, long j) throws RemoteException;

    void registerTrafficNotifyListener(INatTrafficNotifyListener iNatTrafficNotifyListener) throws RemoteException;

    void unRegisterTrafficNotifyListener(INatTrafficNotifyListener iNatTrafficNotifyListener) throws RemoteException;
}
