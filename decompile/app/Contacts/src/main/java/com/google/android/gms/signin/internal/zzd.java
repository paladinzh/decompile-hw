package com.google.android.gms.signin.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Status;

/* compiled from: Unknown */
public interface zzd extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzd {

        /* compiled from: Unknown */
        private static class zza implements zzd {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public void zza(ConnectionResult connectionResult, AuthAccountResult authAccountResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (connectionResult == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        connectionResult.writeToParcel(obtain, 0);
                    }
                    if (authAccountResult == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        authAccountResult.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(Status status, GoogleSignInAccount googleSignInAccount) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (status == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        status.writeToParcel(obtain, 0);
                    }
                    if (googleSignInAccount == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        googleSignInAccount.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zzb(SignInResponse signInResponse) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (signInResponse == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        signInResponse.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zzbl(Status status) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (status == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        status.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zzbm(Status status) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (status == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        status.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public zza() {
            attachInterface(this, "com.google.android.gms.signin.internal.ISignInCallbacks");
        }

        public static zzd zzea(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.signin.internal.ISignInCallbacks");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzd)) ? (zzd) queryLocalInterface : new zza(iBinder);
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            SignInResponse signInResponse = null;
            Status status;
            switch (code) {
                case 3:
                    AuthAccountResult authAccountResult;
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInCallbacks");
                    ConnectionResult connectionResult = data.readInt() == 0 ? null : (ConnectionResult) ConnectionResult.CREATOR.createFromParcel(data);
                    if (data.readInt() != 0) {
                        authAccountResult = (AuthAccountResult) AuthAccountResult.CREATOR.createFromParcel(data);
                    }
                    zza(connectionResult, authAccountResult);
                    reply.writeNoException();
                    return true;
                case 4:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (data.readInt() != 0) {
                        status = (Status) Status.CREATOR.createFromParcel(data);
                    }
                    zzbl(status);
                    reply.writeNoException();
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (data.readInt() != 0) {
                        status = (Status) Status.CREATOR.createFromParcel(data);
                    }
                    zzbm(status);
                    reply.writeNoException();
                    return true;
                case 7:
                    GoogleSignInAccount googleSignInAccount;
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInCallbacks");
                    Status status2 = data.readInt() == 0 ? null : (Status) Status.CREATOR.createFromParcel(data);
                    if (data.readInt() != 0) {
                        googleSignInAccount = (GoogleSignInAccount) GoogleSignInAccount.CREATOR.createFromParcel(data);
                    }
                    zza(status2, googleSignInAccount);
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInCallbacks");
                    if (data.readInt() != 0) {
                        signInResponse = (SignInResponse) SignInResponse.CREATOR.createFromParcel(data);
                    }
                    zzb(signInResponse);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.signin.internal.ISignInCallbacks");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(ConnectionResult connectionResult, AuthAccountResult authAccountResult) throws RemoteException;

    void zza(Status status, GoogleSignInAccount googleSignInAccount) throws RemoteException;

    void zzb(SignInResponse signInResponse) throws RemoteException;

    void zzbl(Status status) throws RemoteException;

    void zzbm(Status status) throws RemoteException;
}
