package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRecentsSystemUserCallbacks extends IInterface {

    public static abstract class Stub extends Binder implements IRecentsSystemUserCallbacks {

        private static class Proxy implements IRecentsSystemUserCallbacks {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public void registerNonSystemUserCallbacks(IBinder nonSystemUserCallbacks, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    _data.writeStrongBinder(nonSystemUserCallbacks);
                    _data.writeInt(userId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void updateRecentsVisibility(boolean visible) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    if (!visible) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void startScreenPinning(int taskId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    _data.writeInt(taskId);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendRecentsDrawnEvent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendDockingTopTaskEvent(int dragMode, Rect initialRect) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    _data.writeInt(dragMode);
                    if (initialRect != null) {
                        _data.writeInt(1);
                        initialRect.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void sendLaunchRecentsEvent() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, "com.android.systemui.recents.IRecentsSystemUserCallbacks");
        }

        public static IRecentsSystemUserCallbacks asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
            if (iin == null || !(iin instanceof IRecentsSystemUserCallbacks)) {
                return new Proxy(obj);
            }
            return (IRecentsSystemUserCallbacks) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            switch (code) {
                case 1:
                    data.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    registerNonSystemUserCallbacks(data.readStrongBinder(), data.readInt());
                    return true;
                case 2:
                    data.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    }
                    updateRecentsVisibility(_arg0);
                    return true;
                case 3:
                    data.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    startScreenPinning(data.readInt());
                    return true;
                case 4:
                    data.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendRecentsDrawnEvent();
                    return true;
                case 5:
                    Rect rect;
                    data.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    int _arg02 = data.readInt();
                    if (data.readInt() != 0) {
                        rect = (Rect) Rect.CREATOR.createFromParcel(data);
                    } else {
                        rect = null;
                    }
                    sendDockingTopTaskEvent(_arg02, rect);
                    return true;
                case 6:
                    data.enforceInterface("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    sendLaunchRecentsEvent();
                    return true;
                case 1598968902:
                    reply.writeString("com.android.systemui.recents.IRecentsSystemUserCallbacks");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void registerNonSystemUserCallbacks(IBinder iBinder, int i) throws RemoteException;

    void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException;

    void sendLaunchRecentsEvent() throws RemoteException;

    void sendRecentsDrawnEvent() throws RemoteException;

    void startScreenPinning(int i) throws RemoteException;

    void updateRecentsVisibility(boolean z) throws RemoteException;
}
