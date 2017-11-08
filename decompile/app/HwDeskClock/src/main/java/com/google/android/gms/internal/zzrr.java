package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public abstract class zzrr<M extends zzrr<M>> extends zzrx {
    protected zzrt zzbcd;

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzDn();
    }

    protected int zzB() {
        int i = 0;
        if (this.zzbcd == null) {
            return 0;
        }
        int i2 = 0;
        while (i < this.zzbcd.size()) {
            i2 += this.zzbcd.zzlB(i).zzB();
            i++;
        }
        return i2;
    }

    protected final int zzDm() {
        return (this.zzbcd == null || this.zzbcd.isEmpty()) ? 0 : this.zzbcd.hashCode();
    }

    public M zzDn() throws CloneNotSupportedException {
        zzrr zzrr = (zzrr) super.zzDo();
        zzrv.zza(this, zzrr);
        return zzrr;
    }

    public /* synthetic */ zzrx zzDo() throws CloneNotSupportedException {
        return zzDn();
    }

    public void zza(zzrq zzrq) throws IOException {
        if (this.zzbcd != null) {
            for (int i = 0; i < this.zzbcd.size(); i++) {
                this.zzbcd.zzlB(i).zza(zzrq);
            }
        }
    }

    protected final boolean zza(zzrp zzrp, int i) throws IOException {
        zzru zzru = null;
        int position = zzrp.getPosition();
        if (!zzrp.zzlj(i)) {
            return false;
        }
        int zzlE = zzsa.zzlE(i);
        zzrz zzrz = new zzrz(i, zzrp.zzy(position, zzrp.getPosition() - position));
        if (this.zzbcd != null) {
            zzru = this.zzbcd.zzlA(zzlE);
        } else {
            this.zzbcd = new zzrt();
        }
        if (zzru == null) {
            zzru = new zzru();
            this.zzbcd.zza(zzlE, zzru);
        }
        zzru.zza(zzrz);
        return true;
    }

    protected final boolean zza(M m) {
        boolean z = false;
        if (this.zzbcd != null && !this.zzbcd.isEmpty()) {
            return this.zzbcd.equals(m.zzbcd);
        }
        if (m.zzbcd == null || m.zzbcd.isEmpty()) {
            z = true;
        }
        return z;
    }
}
