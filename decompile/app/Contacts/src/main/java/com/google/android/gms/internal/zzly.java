package com.google.android.gms.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.clearcut.LogEventParcelable;

/* compiled from: Unknown */
public interface zzly extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzly {

        /* compiled from: Unknown */
        private static class zza implements zzly {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(zzlx zzlx, LogEventParcelable logEventParcelable) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.clearcut.internal.IClearcutLoggerService");
                    if (zzlx != null) {
                        iBinder = zzlx.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (logEventParcelable == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        logEventParcelable.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(1, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static zzly zzaM(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.clearcut.internal.IClearcutLoggerService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzly)) ? (zzly) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.clearcut.internal.IClearcutLoggerService");
                    zza(com.google.android.gms.internal.zzlx.zza.zzaL(data.readStrongBinder()), data.readInt() == 0 ? null : LogEventParcelable.CREATOR.zzaf(data));
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.clearcut.internal.IClearcutLoggerService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(zzlx zzlx, LogEventParcelable logEventParcelable) throws RemoteException;
}
