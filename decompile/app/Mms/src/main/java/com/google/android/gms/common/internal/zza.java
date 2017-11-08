package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.zze;

/* compiled from: Unknown */
public class zza extends com.google.android.gms.common.internal.zzp.zza {
    private Context mContext;
    private Account zzTI;
    int zzakz;

    public static Account zza(zzp zzp) {
        Account account = null;
        if (zzp != null) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                account = zzp.getAccount();
            } catch (RemoteException e) {
                Log.w("AccountAccessor", "Remote account accessor probably died");
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
        return account;
    }

    public boolean equals(Object o) {
        return this != o ? o instanceof zza ? this.zzTI.equals(((zza) o).zzTI) : false : true;
    }

    public Account getAccount() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == this.zzakz) {
            return this.zzTI;
        }
        if (zze.zzf(this.mContext, callingUid)) {
            this.zzakz = callingUid;
            return this.zzTI;
        }
        throw new SecurityException("Caller is not GooglePlayServices");
    }
}
