package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesUtil;

/* compiled from: Unknown */
public class zza extends com.google.android.gms.common.internal.zzp.zza {
    private Context mContext;
    private Account zzOY;
    int zzacC;

    public static Account zzb(zzp zzp) {
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
        return this != o ? o instanceof zza ? this.zzOY.equals(((zza) o).zzOY) : false : true;
    }

    public Account getAccount() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == this.zzacC) {
            return this.zzOY;
        }
        if (GooglePlayServicesUtil.zze(this.mContext, callingUid)) {
            this.zzacC = callingUid;
            return this.zzOY;
        }
        throw new SecurityException("Caller is not GooglePlayServices");
    }
}
