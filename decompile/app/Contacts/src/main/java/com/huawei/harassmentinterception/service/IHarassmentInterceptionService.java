package com.huawei.harassmentinterception.service;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHarassmentInterceptionService extends IInterface {

    public static abstract class Stub extends Binder implements IHarassmentInterceptionService {

        private static class Proxy implements IHarassmentInterceptionService {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public int setPhoneNumberBlockList(Bundle blocknumberlist, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (blocknumberlist != null) {
                        _data.writeInt(1);
                        blocknumberlist.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int addPhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int removePhoneNumberBlockItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String[] queryPhoneNumberBlockItem() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    String[] _result = _reply.createStringArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPhoneNumberFromBlockItem(Bundle checknumber, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (checknumber != null) {
                        _data.writeInt(1);
                        checknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int checkPhoneNumberFromWhiteItem(Bundle checknumber, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (checknumber != null) {
                        _data.writeInt(1);
                        checknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int removePhoneNumberFromWhiteItem(Bundle blocknumber, int type, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (blocknumber != null) {
                        _data.writeInt(1);
                        blocknumber.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    _data.writeInt(source);
                    this.mRemote.transact(7, _data, _reply, 0);
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
            attachInterface(this, "com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
        }

        public static IHarassmentInterceptionService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
            if (iin == null || !(iin instanceof IHarassmentInterceptionService)) {
                return new Proxy(obj);
            }
            return (IHarassmentInterceptionService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle bundle;
            int _result;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = setPhoneNumberBlockList(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = addPhoneNumberBlockItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = removePhoneNumberBlockItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    String[] _result2 = queryPhoneNumberBlockItem();
                    reply.writeNoException();
                    reply.writeStringArray(_result2);
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = checkPhoneNumberFromBlockItem(bundle, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = checkPhoneNumberFromWhiteItem(bundle, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = removePhoneNumberFromWhiteItem(bundle, data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.harassmentinterception.service.IHarassmentInterceptionService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    int addPhoneNumberBlockItem(Bundle bundle, int i, int i2) throws RemoteException;

    int checkPhoneNumberFromBlockItem(Bundle bundle, int i) throws RemoteException;

    int checkPhoneNumberFromWhiteItem(Bundle bundle, int i) throws RemoteException;

    String[] queryPhoneNumberBlockItem() throws RemoteException;

    int removePhoneNumberBlockItem(Bundle bundle, int i, int i2) throws RemoteException;

    int removePhoneNumberFromWhiteItem(Bundle bundle, int i, int i2) throws RemoteException;

    int setPhoneNumberBlockList(Bundle bundle, int i, int i2) throws RemoteException;
}
