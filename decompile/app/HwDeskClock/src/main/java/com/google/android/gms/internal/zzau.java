package com.google.android.gms.internal;

import android.accounts.Account;
import android.os.Binder;
import android.os.Bundle;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.auth.AccountChangeEventsRequest;
import com.google.android.gms.auth.AccountChangeEventsResponse;

/* compiled from: Unknown */
public interface zzau extends IInterface {

    /* compiled from: Unknown */
    public static abstract class zza extends Binder implements zzau {
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle bundle = null;
            Bundle zza;
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
                    Account account = data.readInt() == 0 ? null : (Account) Account.CREATOR.createFromParcel(data);
                    String readString2 = data.readString();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    }
                    zza = zza(account, readString2, bundle);
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
                case 1598968902:
                    reply.writeString("com.google.android.auth.IAuthManagerService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    Bundle zza(Account account, String str, Bundle bundle) throws RemoteException;

    Bundle zza(Bundle bundle) throws RemoteException;

    Bundle zza(String str, Bundle bundle) throws RemoteException;

    Bundle zza(String str, String str2, Bundle bundle) throws RemoteException;

    AccountChangeEventsResponse zza(AccountChangeEventsRequest accountChangeEventsRequest) throws RemoteException;
}
