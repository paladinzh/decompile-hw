package com.huawei.android.quickaction;

import android.content.ComponentName;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IQuickActionService extends IInterface {

    public static abstract class Stub extends Binder implements IQuickActionService {
        private static final String DESCRIPTOR = "com.huawei.android.quickaction.IQuickActionService";
        static final int TRANSACTION_getQuickActions = 1;

        private static class Proxy implements IQuickActionService {
            private IBinder mRemote;

            Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void getQuickActions(ComponentName componentName, IQuickActionResult iQuickActionResult) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (componentName == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        componentName.writeToParcel(obtain, 0);
                    }
                    if (iQuickActionResult != null) {
                        iBinder = iQuickActionResult.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.mRemote.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IQuickActionService asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            if (queryLocalInterface != null && (queryLocalInterface instanceof IQuickActionService)) {
                return (IQuickActionService) queryLocalInterface;
            }
            return new Proxy(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            switch (i) {
                case 1:
                    ComponentName componentName;
                    parcel.enforceInterface(DESCRIPTOR);
                    if (parcel.readInt() == 0) {
                        componentName = null;
                    } else {
                        componentName = (ComponentName) ComponentName.CREATOR.createFromParcel(parcel);
                    }
                    getQuickActions(componentName, com.huawei.android.quickaction.IQuickActionResult.Stub.asInterface(parcel.readStrongBinder()));
                    return true;
                case 1598968902:
                    parcel2.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(i, parcel, parcel2, i2);
            }
        }
    }

    void getQuickActions(ComponentName componentName, IQuickActionResult iQuickActionResult) throws RemoteException;
}
