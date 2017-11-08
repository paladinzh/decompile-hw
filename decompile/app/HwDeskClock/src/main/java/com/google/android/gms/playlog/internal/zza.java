package com.google.android.gms.playlog.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.deskclock.alarmclock.MetaballPath;
import java.util.List;

/* compiled from: Unknown */
public interface zza extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zza {

        /* compiled from: Unknown */
        private static class zza implements zza {
            private IBinder zznI;

            zza(IBinder iBinder) {
                this.zznI = iBinder;
            }

            public IBinder asBinder() {
                return this.zznI;
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
                    this.zznI.transact(2, obtain, null, 1);
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
                    this.zznI.transact(3, obtain, null, 1);
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
                    this.zznI.transact(4, obtain, null, 1);
                } finally {
                    obtain.recycle();
                }
            }
        }

        public static zza zzdt(IBinder iBinder) {
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
                    LogEvent zzfP;
                    data.enforceInterface("com.google.android.gms.playlog.internal.IPlayLogService");
                    String readString2 = data.readString();
                    PlayLoggerContext zzfQ = data.readInt() == 0 ? null : PlayLoggerContext.CREATOR.zzfQ(data);
                    if (data.readInt() != 0) {
                        zzfP = LogEvent.CREATOR.zzfP(data);
                    }
                    zza(readString2, zzfQ, zzfP);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.playlog.internal.IPlayLogService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        playLoggerContext = PlayLoggerContext.CREATOR.zzfQ(data);
                    }
                    zza(readString, playLoggerContext, data.createTypedArrayList(LogEvent.CREATOR));
                    return true;
                case MetaballPath.POINT_NUM /*4*/:
                    data.enforceInterface("com.google.android.gms.playlog.internal.IPlayLogService");
                    readString = data.readString();
                    if (data.readInt() != 0) {
                        playLoggerContext = PlayLoggerContext.CREATOR.zzfQ(data);
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
