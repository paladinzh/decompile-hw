package com.huawei.rcs.incallui.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IMessagePlusService extends IInterface {

    public static abstract class Stub extends Binder implements IMessagePlusService {
        public Stub() {
            attachInterface(this, "com.huawei.rcs.incallui.service.IMessagePlusService");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.rcs.incallui.service.IMessagePlusService");
                    registerSendListener(com.huawei.rcs.incallui.service.ISendListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.huawei.rcs.incallui.service.IMessagePlusService");
                    unregisterSendListener(com.huawei.rcs.incallui.service.ISendListener.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.huawei.rcs.incallui.service.IMessagePlusService");
                    sendMessageWithSubId(data.readString(), data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.rcs.incallui.service.IMessagePlusService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void registerSendListener(ISendListener iSendListener) throws RemoteException;

    void sendMessageWithSubId(String str, String str2, String str3, int i) throws RemoteException;

    void unregisterSendListener(ISendListener iSendListener) throws RemoteException;
}
