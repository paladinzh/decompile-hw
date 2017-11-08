package com.google.android.gms.internal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.WorkSource;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.internal.zzd;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.stats.zzg;
import com.google.android.gms.common.stats.zzi;

/* compiled from: Unknown */
public class zzrp {
    private static boolean DEBUG = false;
    private static String TAG = "WakeLock";
    private static String zzbhl = "*gcore*:";
    private final Context mContext;
    private final String zzanQ;
    private final WakeLock zzbhm;
    private WorkSource zzbhn;
    private final int zzbho;
    private final String zzbhp;
    private boolean zzbhq;
    private int zzbhr;
    private int zzbhs;

    public zzrp(Context context, int i, String str) {
        this(context, i, str, null, context != null ? context.getPackageName() : null);
    }

    @SuppressLint({"UnwrappedWakeLock"})
    public zzrp(Context context, int i, String str, String str2, String str3) {
        this.zzbhq = true;
        zzx.zzh(str, "Wake lock name can NOT be empty");
        this.zzbho = i;
        this.zzbhp = str2;
        this.mContext = context.getApplicationContext();
        if (zzni.zzcV(str3) || "com.google.android.gms" == str3) {
            this.zzanQ = str;
        } else {
            this.zzanQ = zzbhl + str;
        }
        this.zzbhm = ((PowerManager) context.getSystemService("power")).newWakeLock(i, str);
        if (zznj.zzaA(this.mContext)) {
            if (zzni.zzcV(str3)) {
                if (zzd.zzakE && zzlz.isInitialized()) {
                    Log.e(TAG, "callingPackage is not supposed to be empty for wakelock " + this.zzanQ + "!", new IllegalArgumentException());
                    str3 = "com.google.android.gms";
                } else {
                    str3 = context.getPackageName();
                }
            }
            this.zzbhn = zznj.zzl(context, str3);
            zzc(this.zzbhn);
        }
    }

    private void zzfJ(String str) {
        boolean zzfK = zzfK(str);
        String zzn = zzn(str, zzfK);
        if (DEBUG) {
            Log.d(TAG, "Release:\n mWakeLockName: " + this.zzanQ + "\n mSecondaryName: " + this.zzbhp + "\nmReferenceCounted: " + this.zzbhq + "\nreason: " + str + "\n mOpenEventCount" + this.zzbhs + "\nuseWithReason: " + zzfK + "\ntrackingName: " + zzn);
        }
        synchronized (this) {
            if (this.zzbhq) {
                int i = this.zzbhr - 1;
                this.zzbhr = i;
                if (i != 0) {
                    if (zzfK) {
                    }
                }
                zzi.zzrZ().zza(this.mContext, zzg.zza(this.zzbhm, zzn), 8, this.zzanQ, zzn, this.zzbho, zznj.zzb(this.zzbhn));
                this.zzbhs--;
            }
            if (!this.zzbhq) {
                if (this.zzbhs != 1) {
                }
                zzi.zzrZ().zza(this.mContext, zzg.zza(this.zzbhm, zzn), 8, this.zzanQ, zzn, this.zzbho, zznj.zzb(this.zzbhn));
                this.zzbhs--;
            }
        }
    }

    private boolean zzfK(String str) {
        return (TextUtils.isEmpty(str) || str.equals(this.zzbhp)) ? false : true;
    }

    private void zzj(String str, long j) {
        boolean zzfK = zzfK(str);
        String zzn = zzn(str, zzfK);
        if (DEBUG) {
            Log.d(TAG, "Acquire:\n mWakeLockName: " + this.zzanQ + "\n mSecondaryName: " + this.zzbhp + "\nmReferenceCounted: " + this.zzbhq + "\nreason: " + str + "\nmOpenEventCount" + this.zzbhs + "\nuseWithReason: " + zzfK + "\ntrackingName: " + zzn + "\ntimeout: " + j);
        }
        synchronized (this) {
            if (this.zzbhq) {
                int i = this.zzbhr;
                this.zzbhr = i + 1;
                if (i != 0) {
                    if (zzfK) {
                    }
                }
                zzi.zzrZ().zza(this.mContext, zzg.zza(this.zzbhm, zzn), 7, this.zzanQ, zzn, this.zzbho, zznj.zzb(this.zzbhn), j);
                this.zzbhs++;
            }
            if (!this.zzbhq) {
                if (this.zzbhs != 0) {
                }
                zzi.zzrZ().zza(this.mContext, zzg.zza(this.zzbhm, zzn), 7, this.zzanQ, zzn, this.zzbho, zznj.zzb(this.zzbhn), j);
                this.zzbhs++;
            }
        }
    }

    private String zzn(String str, boolean z) {
        return !this.zzbhq ? this.zzbhp : !z ? this.zzbhp : str;
    }

    public void acquire(long timeout) {
        if (!zzne.zzsg() && this.zzbhq) {
            Log.wtf(TAG, "Do not acquire with timeout on reference counted WakeLocks before ICS. wakelock: " + this.zzanQ);
        }
        zzj(null, timeout);
        this.zzbhm.acquire(timeout);
    }

    public boolean isHeld() {
        return this.zzbhm.isHeld();
    }

    public void release() {
        zzfJ(null);
        this.zzbhm.release();
    }

    public void setReferenceCounted(boolean value) {
        this.zzbhm.setReferenceCounted(value);
        this.zzbhq = value;
    }

    public void zzc(WorkSource workSource) {
        if (zznj.zzaA(this.mContext) && workSource != null) {
            if (this.zzbhn == null) {
                this.zzbhn = workSource;
            } else {
                this.zzbhn.add(workSource);
            }
            this.zzbhm.setWorkSource(this.zzbhn);
        }
    }
}
