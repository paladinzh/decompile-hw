package com.huawei.mms.service;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

public interface IHwFloatMmsService extends IInterface {

    public static abstract class Stub extends Binder implements IHwFloatMmsService {
        public Stub() {
            attachInterface(this, "com.huawei.mms.service.IHwFloatMmsService");
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
