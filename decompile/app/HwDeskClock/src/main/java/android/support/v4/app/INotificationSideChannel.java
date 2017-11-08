package android.support.v4.app;

import android.app.Notification;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INotificationSideChannel extends IInterface {

    public static abstract class Stub extends Binder implements INotificationSideChannel {
        public Stub() {
            attachInterface(this, "android.support.v4.app.INotificationSideChannel");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    Notification notification;
                    data.enforceInterface("android.support.v4.app.INotificationSideChannel");
                    String _arg0 = data.readString();
                    int _arg1 = data.readInt();
                    String _arg2 = data.readString();
                    if (data.readInt() != 0) {
                        notification = (Notification) Notification.CREATOR.createFromParcel(data);
                    } else {
                        notification = null;
                    }
                    notify(_arg0, _arg1, _arg2, notification);
                    return true;
                case 2:
                    data.enforceInterface("android.support.v4.app.INotificationSideChannel");
                    cancel(data.readString(), data.readInt(), data.readString());
                    return true;
                case 3:
                    data.enforceInterface("android.support.v4.app.INotificationSideChannel");
                    cancelAll(data.readString());
                    return true;
                case 1598968902:
                    reply.writeString("android.support.v4.app.INotificationSideChannel");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void cancel(String str, int i, String str2) throws RemoteException;

    void cancelAll(String str) throws RemoteException;

    void notify(String str, int i, String str2, Notification notification) throws RemoteException;
}
