package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public abstract class zzso<M extends zzso<M>> extends zzsu {
    protected zzsq zzbuj;

    public /* synthetic */ zzsu clone() throws CloneNotSupportedException {
        return zzJp();
    }

    /* renamed from: clone */
    public /* synthetic */ Object m0clone() throws CloneNotSupportedException {
        return zzJp();
    }

    public void writeTo(zzsn output) throws IOException {
        if (this.zzbuj != null) {
            for (int i = 0; i < this.zzbuj.size(); i++) {
                this.zzbuj.zzmG(i).writeTo(output);
            }
        }
    }

    public M zzJp() throws CloneNotSupportedException {
        zzso zzso = (zzso) super.clone();
        zzss.zza(this, zzso);
        return zzso;
    }

    public final <T> T zza(zzsp<M, T> zzsp) {
        T t = null;
        if (this.zzbuj == null) {
            return null;
        }
        zzsr zzmF = this.zzbuj.zzmF(zzsx.zzmJ(zzsp.tag));
        if (zzmF != null) {
            t = zzmF.zzb(zzsp);
        }
        return t;
    }

    protected final boolean zza(zzsm zzsm, int i) throws IOException {
        zzsr zzsr = null;
        int position = zzsm.getPosition();
        if (!zzsm.zzmo(i)) {
            return false;
        }
        int zzmJ = zzsx.zzmJ(i);
        zzsw zzsw = new zzsw(i, zzsm.zzz(position, zzsm.getPosition() - position));
        if (this.zzbuj != null) {
            zzsr = this.zzbuj.zzmF(zzmJ);
        } else {
            this.zzbuj = new zzsq();
        }
        if (zzsr == null) {
            zzsr = new zzsr();
            this.zzbuj.zza(zzmJ, zzsr);
        }
        zzsr.zza(zzsw);
        return true;
    }

    protected int zzz() {
        int i = 0;
        if (this.zzbuj == null) {
            return 0;
        }
        int i2 = 0;
        while (i < this.zzbuj.size()) {
            i2 += this.zzbuj.zzmG(i).zzz();
            i++;
        }
        return i2;
    }
}
