package com.huawei.rcs.incallui.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ISendListener extends IInterface {

    public static abstract class Stub extends Binder implements ISendListener {

        private static class Proxy implements ISendListener {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onAttachmentStateChanged(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.incallui.service.ISendListener");
                    _data.writeInt(state);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onMmsSendState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.incallui.service.ISendListener");
                    _data.writeInt(state);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSmsSendState(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.rcs.incallui.service.ISendListener");
                    _data.writeInt(state);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.rcs.incallui.service.ISendListener");
        }

        public static ISendListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.rcs.incallui.service.ISendListener");
            if (iin == null || !(iin instanceof ISendListener)) {
                return new Proxy(obj);
            }
            return (ISendListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.rcs.incallui.service.ISendListener");
                    onAttachmentStateChanged(data.readInt());
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.rcs.incallui.service.ISendListener");
                    onMmsSendState(data.readInt());
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.rcs.incallui.service.ISendListener");
                    onSmsSendState(data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.rcs.incallui.service.ISendListener");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onAttachmentStateChanged(int i) throws RemoteException;

    void onMmsSendState(int i) throws RemoteException;

    void onSmsSendState(int i) throws RemoteException;
}
