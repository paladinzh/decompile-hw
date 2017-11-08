package com.google.android.gms.auth.api.credentials.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.auth.api.credentials.CredentialRequest;

/* compiled from: Unknown */
public interface zzh extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzh {

        /* compiled from: Unknown */
        private static class zza implements zzh {
            private IBinder zznI;

            zza(IBinder iBinder) {
                this.zznI = iBinder;
            }

            public IBinder asBinder() {
                return this.zznI;
            }

            public void zza(zzg zzg) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    if (zzg != null) {
                        iBinder = zzg.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    this.zznI.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(zzg zzg, CredentialRequest credentialRequest) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    if (zzg != null) {
                        iBinder = zzg.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (credentialRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        credentialRequest.writeToParcel(obtain, 0);
                    }
                    this.zznI.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(zzg zzg, DeleteRequest deleteRequest) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    if (zzg != null) {
                        iBinder = zzg.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (deleteRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        deleteRequest.writeToParcel(obtain, 0);
                    }
                    this.zznI.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(zzg zzg, SaveRequest saveRequest) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    if (zzg != null) {
                        iBinder = zzg.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (saveRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        saveRequest.writeToParcel(obtain, 0);
                    }
                    this.zznI.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static zzh zzat(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzh)) ? (zzh) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DeleteRequest deleteRequest = null;
            zzg zzas;
            switch (code) {
                case 1:
                    CredentialRequest credentialRequest;
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zzas = com.google.android.gms.auth.api.credentials.internal.zzg.zza.zzas(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        credentialRequest = (CredentialRequest) CredentialRequest.CREATOR.createFromParcel(data);
                    }
                    zza(zzas, credentialRequest);
                    reply.writeNoException();
                    return true;
                case 2:
                    SaveRequest saveRequest;
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zzas = com.google.android.gms.auth.api.credentials.internal.zzg.zza.zzas(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        saveRequest = (SaveRequest) SaveRequest.CREATOR.createFromParcel(data);
                    }
                    zza(zzas, saveRequest);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zzas = com.google.android.gms.auth.api.credentials.internal.zzg.zza.zzas(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        deleteRequest = (DeleteRequest) DeleteRequest.CREATOR.createFromParcel(data);
                    }
                    zza(zzas, deleteRequest);
                    reply.writeNoException();
                    return true;
                case MetaballPath.POINT_NUM /*4*/:
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zza(com.google.android.gms.auth.api.credentials.internal.zzg.zza.zzas(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(zzg zzg) throws RemoteException;

    void zza(zzg zzg, CredentialRequest credentialRequest) throws RemoteException;

    void zza(zzg zzg, DeleteRequest deleteRequest) throws RemoteException;

    void zza(zzg zzg, SaveRequest saveRequest) throws RemoteException;
}
