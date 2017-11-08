package com.google.android.gms.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.dynamic.zzd;

/* compiled from: Unknown */
public interface zzpk extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzpk {

        /* compiled from: Unknown */
        private static class zza implements zzpk {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public boolean getBooleanFlagValue(String key, boolean defaultVal, int source) throws RemoteException {
                boolean z = false;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.flags.IFlagProvider");
                    obtain.writeString(key);
                    obtain.writeInt(!defaultVal ? 0 : 1);
                    obtain.writeInt(source);
                    this.zzoz.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        z = true;
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return z;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public int getIntFlagValue(String key, int defaultVal, int source) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.flags.IFlagProvider");
                    obtain.writeString(key);
                    obtain.writeInt(defaultVal);
                    obtain.writeInt(source);
                    this.zzoz.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    int readInt = obtain2.readInt();
                    return readInt;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public long getLongFlagValue(String key, long defaultVal, int source) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.flags.IFlagProvider");
                    obtain.writeString(key);
                    obtain.writeLong(defaultVal);
                    obtain.writeInt(source);
                    this.zzoz.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                    long readLong = obtain2.readLong();
                    return readLong;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public String getStringFlagValue(String key, String defaultVal, int source) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.flags.IFlagProvider");
                    obtain.writeString(key);
                    obtain.writeString(defaultVal);
                    obtain.writeInt(source);
                    this.zzoz.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    String readString = obtain2.readString();
                    return readString;
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void init(zzd context) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.flags.IFlagProvider");
                    if (context != null) {
                        iBinder = context.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zzoz.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public zza() {
            attachInterface(this, "com.google.android.gms.flags.IFlagProvider");
        }

        public static zzpk asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface queryLocalInterface = obj.queryLocalInterface("com.google.android.gms.flags.IFlagProvider");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzpk)) ? (zzpk) queryLocalInterface : new zza(obj);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.gms.flags.IFlagProvider");
                    init(com.google.android.gms.dynamic.zzd.zza.zzbs(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.flags.IFlagProvider");
                    boolean booleanFlagValue = getBooleanFlagValue(data.readString(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    if (booleanFlagValue) {
                        i = 1;
                    }
                    reply.writeInt(i);
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.flags.IFlagProvider");
                    int intFlagValue = getIntFlagValue(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(intFlagValue);
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.flags.IFlagProvider");
                    long longFlagValue = getLongFlagValue(data.readString(), data.readLong(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(longFlagValue);
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.gms.flags.IFlagProvider");
                    String stringFlagValue = getStringFlagValue(data.readString(), data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(stringFlagValue);
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.flags.IFlagProvider");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean getBooleanFlagValue(String str, boolean z, int i) throws RemoteException;

    int getIntFlagValue(String str, int i, int i2) throws RemoteException;

    long getLongFlagValue(String str, long j, int i) throws RemoteException;

    String getStringFlagValue(String str, String str2, int i) throws RemoteException;

    void init(zzd zzd) throws RemoteException;
}
