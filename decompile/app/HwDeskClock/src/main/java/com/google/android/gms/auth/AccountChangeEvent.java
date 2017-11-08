package com.google.android.gms.auth;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.android.deskclock.alarmclock.MetaballPath;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public class AccountChangeEvent implements SafeParcelable {
    public static final Creator<AccountChangeEvent> CREATOR = new zza();
    final int mVersion;
    final long zzQD;
    final String zzQE;
    final int zzQF;
    final int zzQG;
    final String zzQH;

    AccountChangeEvent(int version, long id, String accountName, int changeType, int eventIndex, String changeData) {
        this.mVersion = version;
        this.zzQD = id;
        this.zzQE = (String) zzx.zzv(accountName);
        this.zzQF = changeType;
        this.zzQG = eventIndex;
        this.zzQH = changeData;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object that) {
        boolean z = true;
        if (that == this) {
            return true;
        }
        if (!(that instanceof AccountChangeEvent)) {
            return false;
        }
        AccountChangeEvent accountChangeEvent = (AccountChangeEvent) that;
        if (this.mVersion == accountChangeEvent.mVersion && this.zzQD == accountChangeEvent.zzQD && zzw.equal(this.zzQE, accountChangeEvent.zzQE) && this.zzQF == accountChangeEvent.zzQF && this.zzQG == accountChangeEvent.zzQG) {
            if (!zzw.equal(this.zzQH, accountChangeEvent.zzQH)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.mVersion), Long.valueOf(this.zzQD), this.zzQE, Integer.valueOf(this.zzQF), Integer.valueOf(this.zzQG), this.zzQH);
    }

    public String toString() {
        String str = "UNKNOWN";
        switch (this.zzQF) {
            case 1:
                str = "ADDED";
                break;
            case 2:
                str = "REMOVED";
                break;
            case 3:
                str = "RENAMED_FROM";
                break;
            case MetaballPath.POINT_NUM /*4*/:
                str = "RENAMED_TO";
                break;
        }
        return "AccountChangeEvent {accountName = " + this.zzQE + ", changeType = " + str + ", changeData = " + this.zzQH + ", eventIndex = " + this.zzQG + "}";
    }

    public void writeToParcel(Parcel dest, int flags) {
        zza.zza(this, dest, flags);
    }
}
