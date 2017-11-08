package com.google.android.gms.playlog.internal;

import com.google.android.gms.internal.zzsb.zzd;
import java.util.ArrayList;

/* compiled from: Unknown */
public class zzb {
    private final ArrayList<zza> zzaKM;
    private int zzaKN;

    /* compiled from: Unknown */
    public static class zza {
        public final PlayLoggerContext zzaKO;
        public final LogEvent zzaKP;
        public final zzd zzaKQ;
    }

    public zzb() {
        this(100);
    }

    public zzb(int i) {
        this.zzaKM = new ArrayList();
        this.zzaKN = i;
    }

    public void clear() {
        this.zzaKM.clear();
    }

    public boolean isEmpty() {
        return this.zzaKM.isEmpty();
    }

    public ArrayList<zza> zzyE() {
        return this.zzaKM;
    }
}
