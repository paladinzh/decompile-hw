package com.google.android.gms.playlog.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

/* compiled from: Unknown */
public interface zza extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zza {

        /* compiled from: Unknown */
        private static class zza implements zza {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(String str, PlayLoggerContext playLoggerContext, LogEvent logEvent) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.playlog.internal.IPlayLogService");
                    obtain.writeString(str);
                    if (playLoggerContext == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        playLoggerContext.writeToParcel(obtain, 0);
                    }
                    if (logEvent == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        logEvent.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(2, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void zza(String str, PlayLoggerContext playLoggerContext, List<LogEvent> list) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.playlog.internal.IPlayLogService");
                    obtain.writeString(str);
                    if (playLoggerContext == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        playLoggerContext.writeToParcel(obtain, 0);
                    }
                    obtain.writeTypedList(list);
                    this.zzoz.transact(3, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }

            public void zza(String str, PlayLoggerContext playLoggerContext, byte[] bArr) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.playlog.internal.IPlayLogService");
                    obtain.writeString(str);
                    if (playLoggerContext == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        playLoggerContext.writeToParcel(obtain, 0);
                    }
                    obtain.writeByteArray(bArr);
                    this.zzoz.transact(4, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static zza zzdN(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.playlog.internal.IPlayLogService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zza)) ? (zza) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            PlayLoggerContext playLoggerContext = null;
            String readString;
            switch (code) {
                case 2:
                    LogEvent zzgy;
                    data.enforceInterface("com.google.android.gms.playlog.internal.IPlayLogService");
                    String readString2 = data.readString();
                    PlayLoggerContext zzgz = data.readInt() == 0 ? null : PlayLoggerContext.CREATOR.zzgz(data);
                    if (data.readInt() != 0) {
                        zzgy = LogEvent.CREATOR.zzgy(data);
                    }
                    zza(readString2, zzgz, zzgy);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.playlog.internal.IPlayLogService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        playLoggerContext = PlayLoggerContext.CREATOR.zzgz(data);
                    }
                    zza(readString, playLoggerContext, data.createTypedArrayList(LogEvent.CREATOR));
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.playlog.internal.IPlayLogService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        playLoggerContext = PlayLoggerContext.CREATOR.zzgz(data);
                    }
                    zza(readString, playLoggerContext, data.createByteArray());
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.playlog.internal.IPlayLogService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(String str, PlayLoggerContext playLoggerContext, LogEvent logEvent) throws RemoteException;

    void zza(String str, PlayLoggerContext playLoggerContext, List<LogEvent> list) throws RemoteException;

    void zza(String str, PlayLoggerContext playLoggerContext, byte[] bArr) throws RemoteException;
}
