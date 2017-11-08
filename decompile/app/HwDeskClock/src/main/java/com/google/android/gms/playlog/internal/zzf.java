package com.google.android.gms.playlog.internal;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.internal.zzb;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.internal.zzrx;
import com.google.android.gms.playlog.internal.zzb.zza;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: Unknown */
public class zzf extends zzj<zza> {
    private final String zzOZ;
    private final zzd zzaLb;
    private final zzb zzaLc;
    private boolean zzaLd;
    private final Object zzpc;

    private void zzyG() {
        PlayLoggerContext playLoggerContext = null;
        boolean z = false;
        if (!this.zzaLd) {
            z = true;
        }
        zzb.zzY(z);
        if (!this.zzaLc.isEmpty()) {
            try {
                List arrayList = new ArrayList();
                Iterator it = this.zzaLc.zzyE().iterator();
                while (it.hasNext()) {
                    zza zza = (zza) it.next();
                    if (zza.zzaKQ == null) {
                        PlayLoggerContext playLoggerContext2;
                        if (zza.zzaKO.equals(playLoggerContext)) {
                            arrayList.add(zza.zzaKP);
                            playLoggerContext2 = playLoggerContext;
                        } else {
                            if (!arrayList.isEmpty()) {
                                ((zza) zzoC()).zza(this.zzOZ, playLoggerContext, arrayList);
                                arrayList.clear();
                            }
                            PlayLoggerContext playLoggerContext3 = zza.zzaKO;
                            arrayList.add(zza.zzaKP);
                            playLoggerContext2 = playLoggerContext3;
                        }
                        playLoggerContext = playLoggerContext2;
                    } else {
                        ((zza) zzoC()).zza(this.zzOZ, zza.zzaKO, zzrx.zzf(zza.zzaKQ));
                    }
                }
                if (!arrayList.isEmpty()) {
                    ((zza) zzoC()).zza(this.zzOZ, playLoggerContext, arrayList);
                }
                this.zzaLc.clear();
            } catch (RemoteException e) {
                Log.e("PlayLoggerImpl", "Couldn't send cached log events to AndroidLog service.  Retaining in memory cache.");
            }
        }
    }

    public void stop() {
        synchronized (this.zzpc) {
            this.zzaLb.zzan(false);
            disconnect();
        }
    }

    protected /* synthetic */ IInterface zzV(IBinder iBinder) {
        return zzdu(iBinder);
    }

    void zzao(boolean z) {
        synchronized (this.zzpc) {
            boolean z2 = this.zzaLd;
            this.zzaLd = z;
            if (z2 && !this.zzaLd) {
                zzyG();
            }
        }
    }

    protected zza zzdu(IBinder iBinder) {
        return zza.zza.zzdt(iBinder);
    }

    protected String zzfA() {
        return "com.google.android.gms.playlog.service.START";
    }

    protected String zzfB() {
        return "com.google.android.gms.playlog.internal.IPlayLogService";
    }
}
