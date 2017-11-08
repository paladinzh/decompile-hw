package com.huawei.android.quickaction;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IQuickActionResult extends IInterface {

    public static abstract class Stub extends Binder implements IQuickActionResult {

        private static class Proxy implements IQuickActionResult {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void sendResult(List<QuickAction> actions) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.android.quickaction.IQuickActionResult");
                    _data.writeTypedList(actions);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.android.quickaction.IQuickActionResult");
        }

        public static IQuickActionResult asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.android.quickaction.IQuickActionResult");
            if (iin != null && (iin instanceof IQuickActionResult)) {
                return (IQuickActionResult) iin;
            }
            return new Proxy(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.huawei.android.quickaction.IQuickActionResult");
                    sendResult(data.createTypedArrayList(QuickAction.CREATOR));
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.android.quickaction.IQuickActionResult");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void sendResult(List<QuickAction> list) throws RemoteException;
}
