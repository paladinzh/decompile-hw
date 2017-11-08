package com.google.android.gms.internal;

import android.accounts.Account;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.auth.AccountChangeEventsRequest;
import com.google.android.gms.auth.AccountChangeEventsResponse;

/* compiled from: Unknown */
public interface zzas extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzas {

        /* compiled from: Unknown */
        private static class zza implements zzas {
            private IBinder zzoz;

            zza(IBinder iBinder) {
                this.zzoz = iBinder;
            }

            public IBinder asBinder() {
                return this.zzoz;
            }

            public Bundle zza(Account account) throws RemoteException {
                Bundle bundle = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.auth.IAuthManagerService");
                    if (account == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        account.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return bundle;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public Bundle zza(Account account, String str, Bundle bundle) throws RemoteException {
                Bundle bundle2 = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.auth.IAuthManagerService");
                    if (account == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        account.writeToParcel(obtain, 0);
                    }
                    obtain.writeString(str);
                    if (bundle == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return bundle2;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public Bundle zza(Bundle bundle) throws RemoteException {
                Bundle bundle2 = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.auth.IAuthManagerService");
                    if (bundle == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(6, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return bundle2;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public Bundle zza(String str, Bundle bundle) throws RemoteException {
                Bundle bundle2 = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.auth.IAuthManagerService");
                    obtain.writeString(str);
                    if (bundle == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return bundle2;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public Bundle zza(String str, String str2, Bundle bundle) throws RemoteException {
                Bundle bundle2 = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.auth.IAuthManagerService");
                    obtain.writeString(str);
                    obtain.writeString(str2);
                    if (bundle == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        bundle.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return bundle2;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public AccountChangeEventsResponse zza(AccountChangeEventsRequest accountChangeEventsRequest) throws RemoteException {
                AccountChangeEventsResponse accountChangeEventsResponse = null;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.auth.IAuthManagerService");
                    if (accountChangeEventsRequest == null) {
                        obtain.writeInt(0);
                    } else {
                        obtain.writeInt(1);
                        accountChangeEventsRequest.writeToParcel(obtain, 0);
                    }
                    this.zzoz.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                    if (obtain2.readInt() != 0) {
                        accountChangeEventsResponse = (AccountChangeEventsResponse) AccountChangeEventsResponse.CREATOR.createFromParcel(obtain2);
                    }
                    obtain2.recycle();
                    obtain.recycle();
                    return accountChangeEventsResponse;
                } catch (Throwable th) {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static zzas zza(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.auth.IAuthManagerService");
            return (queryLocalInterface != null && (queryLocalInterface instanceof zzas)) ? (zzas) queryLocalInterface : new zza(iBinder);
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Account account = null;
            Bundle zza;
            Bundle bundle;
            switch (code) {
                case 1:
                    data.enforceInterface("com.google.android.auth.IAuthManagerService");
                    zza = zza(data.readString(), data.readString(), data.readInt() == 0 ? null : (Bundle) Bundle.CREATOR.createFromParcel(data));
                    reply.writeNoException();
                    if (zza == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        zza.writeToParcel(reply, 1);
                    }
                    return true;
                case 2:
                    data.enforceInterface("com.google.android.auth.IAuthManagerService");
                    String readString = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    zza = zza(readString, bundle);
                    reply.writeNoException();
                    if (zza == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        zza.writeToParcel(reply, 1);
                    }
                    return true;
                case 3:
                    AccountChangeEventsRequest accountChangeEventsRequest;
                    data.enforceInterface("com.google.android.auth.IAuthManagerService");
                    if (data.readInt() != 0) {
                        accountChangeEventsRequest = (AccountChangeEventsRequest) AccountChangeEventsRequest.CREATOR.createFromParcel(data);
                    }
                    AccountChangeEventsResponse zza2 = zza(accountChangeEventsRequest);
                    reply.writeNoException();
                    if (zza2 == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        zza2.writeToParcel(reply, 1);
                    }
                    return true;
                case 5:
                    data.enforceInterface("com.google.android.auth.IAuthManagerService");
                    Account account2 = data.readInt() == 0 ? null : (Account) Account.CREATOR.createFromParcel(data);
                    String readString2 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    zza = zza(account2, readString2, bundle);
                    reply.writeNoException();
                    if (zza == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        zza.writeToParcel(reply, 1);
                    }
                    return true;
                case 6:
                    data.enforceInterface("com.google.android.auth.IAuthManagerService");
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    zza = zza(bundle);
                    reply.writeNoException();
                    if (zza == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        zza.writeToParcel(reply, 1);
                    }
                    return true;
                case 7:
                    data.enforceInterface("com.google.android.auth.IAuthManagerService");
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    }
                    zza = zza(account);
                    reply.writeNoException();
                    if (zza == null) {
                        reply.writeInt(0);
                    } else {
                        reply.writeInt(1);
                        zza.writeToParcel(reply, 1);
                    }
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.auth.IAuthManagerService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Bundle zza(Account account) throws RemoteException;

    Bundle zza(Account account, String str, Bundle bundle) throws RemoteException;

    Bundle zza(Bundle bundle) throws RemoteException;

    Bundle zza(String str, Bundle bundle) throws RemoteException;

    Bundle zza(String str, String str2, Bundle bundle) throws RemoteException;

    AccountChangeEventsResponse zza(AccountChangeEventsRequest accountChangeEventsRequest) throws RemoteException;
}
