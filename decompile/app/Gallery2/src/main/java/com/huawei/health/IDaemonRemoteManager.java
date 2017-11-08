package com.huawei.health;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IDaemonRemoteManager extends IInterface {

    public static abstract class Stub extends Binder implements IDaemonRemoteManager {

        private static class Proxy implements IDaemonRemoteManager {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getVersion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setStepCounterSwitchStatus(boolean status) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (status) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getStepCounterSwitchStatus() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isStepCounterWorking() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStepCounterClass() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerStepReportCallback(IStepDataReport cb) throws RemoteException {
                IBinder iBinder = null;
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unRegisterStepReportCallback(IStepDataReport cb) throws RemoteException {
                IBinder iBinder = null;
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setBaseData(long timeStamp, int steps, int distances, int calories) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    _data.writeLong(timeStamp);
                    _data.writeInt(steps);
                    _data.writeInt(distances);
                    _data.writeInt(calories);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setNotificationEnable(boolean show) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (show) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isNotificationShown() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void flushCacheToDB(IResultCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setUserInfo(Bundle userInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (userInfo == null) {
                        _data.writeInt(0);
                    } else {
                        _data.writeInt(1);
                        userInfo.writeToParcel(_data, 0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getSleepData(IResultCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getTodaySportData(IResultCallback callback) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.health.IDaemonRemoteManager");
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.health.IDaemonRemoteManager");
        }

        public static IDaemonRemoteManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.health.IDaemonRemoteManager");
            if (iin != null && (iin instanceof IDaemonRemoteManager)) {
                return (IDaemonRemoteManager) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0;
            boolean _result;
            int i;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    String _result2 = getVersion();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    if (data.readInt() == 0) {
                        _arg0 = false;
                    } else {
                        _arg0 = true;
                    }
                    setStepCounterSwitchStatus(_arg0);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    _result = getStepCounterSwitchStatus();
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    _result = isStepCounterWorking();
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    int _result3 = getStepCounterClass();
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    _result = registerStepReportCallback(com.huawei.health.IStepDataReport.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    _result = unRegisterStepReportCallback(com.huawei.health.IStepDataReport.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    setBaseData(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    if (data.readInt() == 0) {
                        _arg0 = false;
                    } else {
                        _arg0 = true;
                    }
                    setNotificationEnable(_arg0);
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    _result = isNotificationShown();
                    reply.writeNoException();
                    if (_result) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    flushCacheToDB(com.huawei.health.IResultCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 12:
                    Bundle bundle;
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    if (data.readInt() == 0) {
                        bundle = null;
                    } else {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    setUserInfo(bundle);
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    getSleepData(com.huawei.health.IResultCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.health.IDaemonRemoteManager");
                    getTodaySportData(com.huawei.health.IResultCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.health.IDaemonRemoteManager");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void flushCacheToDB(IResultCallback iResultCallback) throws RemoteException;

    void getSleepData(IResultCallback iResultCallback) throws RemoteException;

    int getStepCounterClass() throws RemoteException;

    boolean getStepCounterSwitchStatus() throws RemoteException;

    void getTodaySportData(IResultCallback iResultCallback) throws RemoteException;

    String getVersion() throws RemoteException;

    boolean isNotificationShown() throws RemoteException;

    boolean isStepCounterWorking() throws RemoteException;

    boolean registerStepReportCallback(IStepDataReport iStepDataReport) throws RemoteException;

    void setBaseData(long j, int i, int i2, int i3) throws RemoteException;

    void setNotificationEnable(boolean z) throws RemoteException;

    void setStepCounterSwitchStatus(boolean z) throws RemoteException;

    void setUserInfo(Bundle bundle) throws RemoteException;

    boolean unRegisterStepReportCallback(IStepDataReport iStepDataReport) throws RemoteException;
}
