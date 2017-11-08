package com.google.android.gms.playlog.internal;

import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.internal.zzsz.zzd;
import java.util.ArrayList;

/* compiled from: Unknown */
public class zzb {
    private final ArrayList<zza> zzbdE;
    private int zzbdF;

    /* compiled from: Unknown */
    public static class zza {
        public final PlayLoggerContext zzbdG;
        public final LogEvent zzbdH;
        public final zzd zzbdI;

        private zza(PlayLoggerContext playLoggerContext, LogEvent logEvent) {
            this.zzbdG = (PlayLoggerContext) zzx.zzz(playLoggerContext);
            this.zzbdH = (LogEvent) zzx.zzz(logEvent);
            this.zzbdI = null;
        }
    }

    public zzb() {
        this(100);
    }

    public zzb(int i) {
        this.zzbdE = new ArrayList();
        this.zzbdF = i;
    }

    private void zzEV() {
        while (getSize() > getCapacity()) {
            this.zzbdE.remove(0);
        }
    }

    public void clear() {
        this.zzbdE.clear();
    }

    public int getCapacity() {
        return this.zzbdF;
    }

    public int getSize() {
        return this.zzbdE.size();
    }

    public boolean isEmpty() {
        return this.zzbdE.isEmpty();
    }

    public ArrayList<zza> zzEU() {
        return this.zzbdE;
    }

    public void zza(PlayLoggerContext playLoggerContext, LogEvent logEvent) {
        this.zzbdE.add(new zza(playLoggerContext, logEvent));
        zzEV();
    }
}
