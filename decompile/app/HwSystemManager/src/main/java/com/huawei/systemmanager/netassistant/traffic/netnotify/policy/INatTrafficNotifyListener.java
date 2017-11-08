package com.huawei.systemmanager.netassistant.traffic.netnotify.policy;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INatTrafficNotifyListener extends IInterface {

    public static abstract class Stub extends Binder implements INatTrafficNotifyListener {
        private static final String DESCRIPTOR = "com.huawei.systemmanager.netassistant.traffic.netnotify.policy.INatTrafficNotifyListener";
        static final int TRANSACTION_onDailyWarningReached = 3;
        static final int TRANSACTION_onMonthLimitReached = 1;
        static final int TRANSACTION_onMonthWarningReached = 2;

        private static class Proxy implements INatTrafficNotifyListener {
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

            public void onMonthLimitReached(String imsi, String iface, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(iface);
                    _data.writeLong(bytes);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onMonthWarningReached(String imsi, String iface, long bytes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(imsi);
                    _data.writeString(iface);
                    _data.writeLong(bytes);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onDailyWarningReached(String imsi, String iface, long bytes) throws RemoteException {
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
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INatTrafficNotifyListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INatTrafficNotifyListener)) {
                return new Proxy(obj);
            }
            return (INatTrafficNotifyListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    onMonthLimitReached(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    onMonthWarningReached(data.readString(), data.readString(), data.readLong());
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    onDailyWarningReached(data.readString(), data.readString(), data.readLong());
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

    void onDailyWarningReached(String str, String str2, long j) throws RemoteException;

    void onMonthLimitReached(String str, String str2, long j) throws RemoteException;

    void onMonthWarningReached(String str, String str2, long j) throws RemoteException;
}
