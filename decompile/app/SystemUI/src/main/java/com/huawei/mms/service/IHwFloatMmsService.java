package com.huawei.mms.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface IHwFloatMmsService extends IInterface {

    public static abstract class Stub extends Binder implements IHwFloatMmsService {

        private static class Proxy implements IHwFloatMmsService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public boolean is7bitEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
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

            public boolean isMultipartSmsEnabled() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
                    this.mRemote.transact(2, _data, _reply, 0);
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

            public boolean isAlertLongSmsEnable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
                    this.mRemote.transact(3, _data, _reply, 0);
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

            public int getSmsToMmsTextThreshhold() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public CharSequence replaceAlphabetForGsm7Bit(CharSequence s, int start, int end) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    CharSequence charSequence;
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
                    if (s != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(s, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(start);
                    _data.writeInt(end);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(_reply);
                    } else {
                        charSequence = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return charSequence;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String getHuaweiNameFromSnippet(String snippet) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
                    _data.writeString(snippet);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NameMatchResult getNameMatchedContact(String name) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NameMatchResult nameMatchResult;
                    _data.writeInterfaceToken("com.huawei.mms.service.IHwFloatMmsService");
                    _data.writeString(name);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        nameMatchResult = (NameMatchResult) NameMatchResult.CREATOR.createFromParcel(_reply);
                    } else {
                        nameMatchResult = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return nameMatchResult;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.mms.service.IHwFloatMmsService");
        }

        public static IHwFloatMmsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.mms.service.IHwFloatMmsService");
            if (iin == null || !(iin instanceof IHwFloatMmsService)) {
                return new Proxy(obj);
            }
            return (IHwFloatMmsService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _result;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    _result = is7bitEnable();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    _result = isMultipartSmsEnabled();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    _result = isAlertLongSmsEnable();
                    reply.writeNoException();
                    reply.writeInt(_result ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    int _result2 = getSmsToMmsTextThreshhold();
                    reply.writeNoException();
                    reply.writeInt(_result2);
                    return true;
                case 5:
                    CharSequence charSequence;
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    if (data.readInt() != 0) {
                        charSequence = (CharSequence) TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                    } else {
                        charSequence = null;
                    }
                    CharSequence _result3 = replaceAlphabetForGsm7Bit(charSequence, data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result3 != null) {
                        reply.writeInt(1);
                        TextUtils.writeToParcel(_result3, reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    String _result4 = getHuaweiNameFromSnippet(data.readString());
                    reply.writeNoException();
                    reply.writeString(_result4);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.mms.service.IHwFloatMmsService");
                    NameMatchResult _result5 = getNameMatchedContact(data.readString());
                    reply.writeNoException();
                    if (_result5 != null) {
                        reply.writeInt(1);
                        _result5.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.mms.service.IHwFloatMmsService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    String getHuaweiNameFromSnippet(String str) throws RemoteException;

    NameMatchResult getNameMatchedContact(String str) throws RemoteException;

    int getSmsToMmsTextThreshhold() throws RemoteException;

    boolean is7bitEnable() throws RemoteException;

    boolean isAlertLongSmsEnable() throws RemoteException;

    boolean isMultipartSmsEnabled() throws RemoteException;

    CharSequence replaceAlphabetForGsm7Bit(CharSequence charSequence, int i, int i2) throws RemoteException;
}
