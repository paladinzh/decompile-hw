package com.huawei.android.totemweather.aidl;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRequestCityWeather extends IInterface {

    public static abstract class Stub extends Binder implements IRequestCityWeather {

        private static class Proxy implements IRequestCityWeather {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void requestWeatherByLocation(RequestData requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestWeatherByCityId(RequestData requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void getWeatherByType(RequestData requestData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerCallBack(IRequestCallBack callback, String packageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterCallBack(IRequestCallBack callback, String packageName) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeString(packageName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestWeatherByLocationAndSourceType(RequestData requestData, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(type);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void requestWeatherWithLocation(RequestData requestData, int sourceType, int parsePhaseType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (requestData != null) {
                        _data.writeInt(1);
                        requestData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(sourceType);
                    _data.writeInt(parsePhaseType);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.android.totemweather.aidl.IRequestCityWeather");
        }

        public static IRequestCityWeather asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
            if (iin == null || !(iin instanceof IRequestCityWeather)) {
                return new Proxy(obj);
            }
            return (IRequestCityWeather) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RequestData requestData;
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (data.readInt() != 0) {
                        requestData = (RequestData) RequestData.CREATOR.createFromParcel(data);
                    } else {
                        requestData = null;
                    }
                    requestWeatherByLocation(requestData);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (data.readInt() != 0) {
                        requestData = (RequestData) RequestData.CREATOR.createFromParcel(data);
                    } else {
                        requestData = null;
                    }
                    requestWeatherByCityId(requestData);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (data.readInt() != 0) {
                        requestData = (RequestData) RequestData.CREATOR.createFromParcel(data);
                    } else {
                        requestData = null;
                    }
                    getWeatherByType(requestData);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    registerCallBack(com.huawei.android.totemweather.aidl.IRequestCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 5:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    unregisterCallBack(com.huawei.android.totemweather.aidl.IRequestCallBack.Stub.asInterface(data.readStrongBinder()), data.readString());
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (data.readInt() != 0) {
                        requestData = (RequestData) RequestData.CREATOR.createFromParcel(data);
                    } else {
                        requestData = null;
                    }
                    requestWeatherByLocationAndSourceType(requestData, data.readInt());
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    if (data.readInt() != 0) {
                        requestData = (RequestData) RequestData.CREATOR.createFromParcel(data);
                    } else {
                        requestData = null;
                    }
                    requestWeatherWithLocation(requestData, data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.android.totemweather.aidl.IRequestCityWeather");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void getWeatherByType(RequestData requestData) throws RemoteException;

    void registerCallBack(IRequestCallBack iRequestCallBack, String str) throws RemoteException;

    void requestWeatherByCityId(RequestData requestData) throws RemoteException;

    void requestWeatherByLocation(RequestData requestData) throws RemoteException;

    void requestWeatherByLocationAndSourceType(RequestData requestData, int i) throws RemoteException;

    void requestWeatherWithLocation(RequestData requestData, int i, int i2) throws RemoteException;

    void unregisterCallBack(IRequestCallBack iRequestCallBack, String str) throws RemoteException;
}
