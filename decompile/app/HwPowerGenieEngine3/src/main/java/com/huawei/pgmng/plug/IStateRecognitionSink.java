package com.huawei.pgmng.plug;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.powergenie.integration.adapter.NativeAdapter;

public interface IStateRecognitionSink extends IInterface {

    public static abstract class Stub extends Binder implements IStateRecognitionSink {

        private static class Proxy implements IStateRecognitionSink {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.huawei.pgmng.plug.IStateRecognitionSink");
                    _data.writeInt(stateType);
                    _data.writeInt(eventType);
                    _data.writeInt(pid);
                    _data.writeString(pkg);
                    _data.writeInt(uid);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.huawei.pgmng.plug.IStateRecognitionSink");
        }

        public static IStateRecognitionSink asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.huawei.pgmng.plug.IStateRecognitionSink");
            if (iin == null || !(iin instanceof IStateRecognitionSink)) {
                return new Proxy(obj);
            }
            return (IStateRecognitionSink) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case NativeAdapter.PLATFORM_MTK /*1*/:
                    data.enforceInterface("com.huawei.pgmng.plug.IStateRecognitionSink");
                    onStateChanged(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readInt());
                    return true;
                case 1598968902:
                    reply.writeString("com.huawei.pgmng.plug.IStateRecognitionSink");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onStateChanged(int i, int i2, int i3, String str, int i4) throws RemoteException;
}
