package com.google.android.gms.common.stats;

import android.os.SystemClock;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

/* compiled from: Unknown */
public class zze {
    private final long zzafR;
    private final int zzafS;
    private final SimpleArrayMap<String, Long> zzafT;

    public zze() {
        this.zzafR = 60000;
        this.zzafS = 10;
        this.zzafT = new SimpleArrayMap(10);
    }

    public zze(int i, long j) {
        this.zzafR = j;
        this.zzafS = i;
        this.zzafT = new SimpleArrayMap();
    }

    private void zzb(long j, long j2) {
        for (int size = this.zzafT.size() - 1; size >= 0; size--) {
            if ((j2 - ((Long) this.zzafT.valueAt(size)).longValue() <= j ? 1 : null) == null) {
                this.zzafT.removeAt(size);
            }
        }
    }

    public Long zzcy(String str) {
        Long l;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long j = this.zzafR;
        synchronized (this) {
            while (this.zzafT.size() >= this.zzafS) {
                zzb(j, elapsedRealtime);
                j /= 2;
                Log.w("ConnectionTracker", "The max capacity " + this.zzafS + " is not enough. Current durationThreshold is: " + j);
            }
            l = (Long) this.zzafT.put(str, Long.valueOf(elapsedRealtime));
        }
        return l;
    }

    public boolean zzcz(String str) {
        boolean z;
        synchronized (this) {
            z = this.zzafT.remove(str) != null;
        }
        return z;
    }
}
