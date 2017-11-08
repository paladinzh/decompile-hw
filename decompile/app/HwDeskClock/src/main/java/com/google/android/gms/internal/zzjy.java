package com.google.android.gms.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.auth.api.proxy.ProxyGrpcRequest;
import com.google.android.gms.auth.api.proxy.ProxyRequest;

/* compiled from: Unknown */
public interface zzjy extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzjy {

        /* compiled from: Unknown */
        private static class zza implements zzjy {
            private IBinder zznI;

            zza(IBinder iBinder) {
                this.zznI = iBinder;
            }

            public IBinder asBinder() {
                return this.zznI;
            }

            public void zza(zzjx zzjx, ProxyGrpcRequest proxyGrpcRequest) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.internal.IAuthService");
                    if (zzjx != null) {
                        iBinder = zzjx.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (proxyGrpcRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        proxyGrpcRequest.writeToParcel(obtain, 0);
                    }
                    this.zznI.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(zzjx zzjx, ProxyRequest proxyRequest) throws RemoteException {
                IBinder iBinder = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.internal.IAuthService");
                    if (zzjx != null) {
                        iBinder = zzjx.asBinder();
                    }
                    obtain.writeStrongBinder(iBinder);
                    if (proxyRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        proxyRequest.writeToParcel(obtain, 0);
                    }
                    this.zznI.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static zzjy zzaw(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.auth.api.internal.IAuthService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzjy)) ? (zzjy) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ProxyGrpcRequest proxyGrpcRequest = null;
            zzjx zzav;
            switch (code) {
                case 1:
                    ProxyRequest proxyRequest;
                    data.enforceInterface("com.google.android.gms.auth.api.internal.IAuthService");
                    zzav = com.google.android.gms.internal.zzjx.zza.zzav(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        proxyRequest = (ProxyRequest) ProxyRequest.CREATOR.createFromParcel(data);
                    }
                    zza(zzav, proxyRequest);
                    reply.writeNoException();
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.gms.auth.api.internal.IAuthService");
                    zzav = com.google.android.gms.internal.zzjx.zza.zzav(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        proxyGrpcRequest = (ProxyGrpcRequest) ProxyGrpcRequest.CREATOR.createFromParcel(data);
                    }
                    zza(zzav, proxyGrpcRequest);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.auth.api.internal.IAuthService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(zzjx zzjx, ProxyGrpcRequest proxyGrpcRequest) throws RemoteException;

    void zza(zzjx zzjx, ProxyRequest proxyRequest) throws RemoteException;
}
