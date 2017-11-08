package com.huawei.securitymgr;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAuthenticationService extends IInterface {

    public static abstract class Stub extends Binder implements IAuthenticationService {

        private static class Proxy implements IAuthenticationService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public boolean open(IAuthenticationClient client, int authenciationType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(authenciationType);
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

            public int startIdentify(IAuthenticationClient client, int[] ids, byte[] challenge) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeIntArray(ids);
                    _data.writeByteArray(challenge);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void abort(IAuthenticationClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void release(IAuthenticationClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getIds(IAuthenticationClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getAuthenticateSupportTypes() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getEnable(int authenciationType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    _data.writeInt(authenciationType);
                    this.mRemote.transact(7, _data, _reply, 0);
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

            public int getAuthenticationState(int stateType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    _data.writeInt(stateType);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean getAssociation(String appPkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    _data.writeString(appPkgName);
                    this.mRemote.transact(9, _data, _reply, 0);
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

            public String getDescription(IAuthenticationClient client, int id) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int[] getIdsByPrivacyMode(IAuthenticationClient client, int mode) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(mode);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    int[] _result = _reply.createIntArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getPrivacyMode(IAuthenticationClient client, int id) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(id);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean setVibratorSwitch(IAuthenticationClient client, int switchType, boolean switchState) throws RemoteException {
                IBinder iBinder = null;
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(switchType);
                    if (switchState) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(13, _data, _reply, 0);
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

            public long getVibratorTime(IAuthenticationClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int startIdentifyForSign(IAuthenticationClient client, int[] ids, byte[] challenge, int keyType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeIntArray(ids);
                    _data.writeByteArray(challenge);
                    _data.writeInt(keyType);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getIdentifySignedData(IAuthenticationClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void binderDied() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int MMIFingerprintTest(IAuthenticationClient client, int testType) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.securitymgr.IAuthenticationService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(testType);
                    this.mRemote.transact(18, _data, _reply, 0);
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
            attachInterface(this, "com.huawei.securitymgr.IAuthenticationService");
        }

        public static IAuthenticationService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.securitymgr.IAuthenticationService");
            if (iin == null || !(iin instanceof IAuthenticationService)) {
                return new Proxy(obj);
            }
            return (IAuthenticationService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            int _result2;
            int[] _result3;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result = open(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result2 = startIdentify(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.createIntArray(), data.createByteArray());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    abort(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    release(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result3 = getIds(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result3 = getAuthenticateSupportTypes();
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result = getEnable(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result2 = getAuthenticationState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result = getAssociation(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    String _result4 = getDescription(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result3 = getIdsByPrivacyMode(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result3);
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result2 = getPrivacyMode(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result = setVibratorSwitch(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    long _result5 = getVibratorTime(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeLong(_result5);
                    return true;
                case 15:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result2 = startIdentifyForSign(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.createIntArray(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 16:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    byte[] _result6 = getIdentifySignedData(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeByteArray(_result6);
                    return true;
                case 17:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    binderDied();
                    reply.writeNoException();
                    return true;
                case 18:
                    data.enforceInterface("com.huawei.securitymgr.IAuthenticationService");
                    _result2 = MMIFingerprintTest(com.huawei.securitymgr.IAuthenticationClient.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.securitymgr.IAuthenticationService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int MMIFingerprintTest(IAuthenticationClient iAuthenticationClient, int i) throws RemoteException;

    void abort(IAuthenticationClient iAuthenticationClient) throws RemoteException;

    void binderDied() throws RemoteException;

    boolean getAssociation(String str) throws RemoteException;

    int[] getAuthenticateSupportTypes() throws RemoteException;

    int getAuthenticationState(int i) throws RemoteException;

    String getDescription(IAuthenticationClient iAuthenticationClient, int i) throws RemoteException;

    boolean getEnable(int i) throws RemoteException;

    byte[] getIdentifySignedData(IAuthenticationClient iAuthenticationClient) throws RemoteException;

    int[] getIds(IAuthenticationClient iAuthenticationClient) throws RemoteException;

    int[] getIdsByPrivacyMode(IAuthenticationClient iAuthenticationClient, int i) throws RemoteException;

    int getPrivacyMode(IAuthenticationClient iAuthenticationClient, int i) throws RemoteException;

    long getVibratorTime(IAuthenticationClient iAuthenticationClient) throws RemoteException;

    boolean open(IAuthenticationClient iAuthenticationClient, int i) throws RemoteException;

    void release(IAuthenticationClient iAuthenticationClient) throws RemoteException;

    boolean setVibratorSwitch(IAuthenticationClient iAuthenticationClient, int i, boolean z) throws RemoteException;

    int startIdentify(IAuthenticationClient iAuthenticationClient, int[] iArr, byte[] bArr) throws RemoteException;

    int startIdentifyForSign(IAuthenticationClient iAuthenticationClient, int[] iArr, byte[] bArr, int i) throws RemoteException;
}
