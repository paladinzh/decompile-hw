package com.huawei.cryptosms;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICryptoMessageService extends IInterface {

    public static abstract class Stub extends Binder implements ICryptoMessageService {

        private static class Proxy implements ICryptoMessageService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int registerCallback(ICryptoMessageClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    if (client != null) {
                        iBinder = client.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int unregisterCallback(ICryptoMessageClient client) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    if (client != null) {
                        iBinder = client.asBinder();
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

            public int activate(int subid, String accountName, String userid, String serviceToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(subid);
                    _data.writeString(accountName);
                    _data.writeString(userid);
                    _data.writeString(serviceToken);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int deactivate(int subid, String userID, String accountPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(subid);
                    _data.writeString(userID);
                    _data.writeString(accountPassword);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int enrollMessage(byte[] text, int subid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeByteArray(text);
                    _data.writeInt(subid);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int localDeactivate(int subid, String userID, String accountPassword) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(subid);
                    _data.writeString(userID);
                    _data.writeString(accountPassword);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getState(int subid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(subid);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getCloudAccount(int subid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(subid);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String encryptData(String plainText) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeString(plainText);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String decryptData(String encryptedText) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeString(encryptedText);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getCryptoMessageVersion(int subid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(subid);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] encryptMessage(byte[] msg, byte[] receivedNum, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeByteArray(msg);
                    _data.writeByteArray(receivedNum);
                    _data.writeInt(subId);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] decryptMessage(byte[] msg, int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeByteArray(msg);
                    _data.writeInt(subId);
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean storePassword(String cipherText, String salt) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeString(cipherText);
                    _data.writeString(salt);
                    this.mRemote.transact(14, _data, _reply, 0);
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

            public boolean checkPassword(String cipherText) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeString(cipherText);
                    this.mRemote.transact(15, _data, _reply, 0);
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

            public boolean isPasswordEnabled() throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    this.mRemote.transact(16, _data, _reply, 0);
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

            public boolean storePasswordProtection(String question, String answerCipherText, String salt) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeString(question);
                    _data.writeString(answerCipherText);
                    _data.writeString(salt);
                    this.mRemote.transact(17, _data, _reply, 0);
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

            public String getProtectionQuestion() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkProtectionAnswer(String answerCipherText) throws RemoteException {
                boolean _result = false;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeString(answerCipherText);
                    this.mRemote.transact(19, _data, _reply, 0);
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

            public String getSalt(int index) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.cryptosms.ICryptoMessageService");
                    _data.writeInt(index);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.cryptosms.ICryptoMessageService");
        }

        public static ICryptoMessageService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.cryptosms.ICryptoMessageService");
            if (iin != null && (iin instanceof ICryptoMessageService)) {
                return (ICryptoMessageService) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _result;
            String _result2;
            byte[] _result3;
            boolean _result4;
            int i;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = registerCallback(com.huawei.cryptosms.ICryptoMessageClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = unregisterCallback(com.huawei.cryptosms.ICryptoMessageClient.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = activate(data.readInt(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = deactivate(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = enrollMessage(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = localDeactivate(data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result = getState(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 8:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result2 = getCloudAccount(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 9:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result2 = encryptData(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 10:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result2 = decryptData(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 11:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result3 = getCryptoMessageVersion(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case 12:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result3 = encryptMessage(data.createByteArray(), data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case 13:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result3 = decryptMessage(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result3);
                    return true;
                case 14:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result4 = storePassword(data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 15:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result4 = checkPassword(data.readString());
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 16:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result4 = isPasswordEnabled();
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 17:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result4 = storePasswordProtection(data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 18:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result2 = getProtectionQuestion();
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 19:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result4 = checkProtectionAnswer(data.readString());
                    reply.writeNoException();
                    if (_result4) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return true;
                case 20:
                    data.enforceInterface("com.huawei.cryptosms.ICryptoMessageService");
                    _result2 = getSalt(data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result2);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.cryptosms.ICryptoMessageService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int activate(int i, String str, String str2, String str3) throws RemoteException;

    boolean checkPassword(String str) throws RemoteException;

    boolean checkProtectionAnswer(String str) throws RemoteException;

    int deactivate(int i, String str, String str2) throws RemoteException;

    String decryptData(String str) throws RemoteException;

    byte[] decryptMessage(byte[] bArr, int i) throws RemoteException;

    String encryptData(String str) throws RemoteException;

    byte[] encryptMessage(byte[] bArr, byte[] bArr2, int i) throws RemoteException;

    int enrollMessage(byte[] bArr, int i) throws RemoteException;

    String getCloudAccount(int i) throws RemoteException;

    byte[] getCryptoMessageVersion(int i) throws RemoteException;

    String getProtectionQuestion() throws RemoteException;

    String getSalt(int i) throws RemoteException;

    int getState(int i) throws RemoteException;

    boolean isPasswordEnabled() throws RemoteException;

    int localDeactivate(int i, String str, String str2) throws RemoteException;

    int registerCallback(ICryptoMessageClient iCryptoMessageClient) throws RemoteException;

    boolean storePassword(String str, String str2) throws RemoteException;

    boolean storePasswordProtection(String str, String str2, String str3) throws RemoteException;

    int unregisterCallback(ICryptoMessageClient iCryptoMessageClient) throws RemoteException;
}
