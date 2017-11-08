package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public abstract class zzrx {
    protected volatile int zzbco = -1;

    public static final <T extends zzrx> T zza(T t, byte[] bArr) throws zzrw {
        return zzb(t, bArr, 0, bArr.length);
    }

    public static final void zza(zzrx zzrx, byte[] bArr, int i, int i2) {
        try {
            zzrq zzb = zzrq.zzb(bArr, i, i2);
            zzrx.zza(zzb);
            zzb.zzDl();
        } catch (Throwable e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
        }
    }

    public static final <T extends zzrx> T zzb(T t, byte[] bArr, int i, int i2) throws zzrw {
        try {
            zzrp zza = zzrp.zza(bArr, i, i2);
            t.zzb(zza);
            zza.zzli(0);
            return t;
        } catch (zzrw e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).");
        }
    }

    public static final byte[] zzf(zzrx zzrx) {
        byte[] bArr = new byte[zzrx.zzDz()];
        zza(zzrx, bArr, 0, bArr.length);
        return bArr;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzDo();
    }

    public String toString() {
        return zzry.zzg(this);
    }

    protected int zzB() {
        return 0;
    }

    public zzrx zzDo() throws CloneNotSupportedException {
        return (zzrx) super.clone();
    }

    public int zzDy() {
        if (this.zzbco < 0) {
            zzDz();
        }
        return this.zzbco;
    }

    public int zzDz() {
        int zzB = zzB();
        this.zzbco = zzB;
        return zzB;
    }

    public void zza(zzrq zzrq) throws IOException {
    }

    public abstract zzrx zzb(zzrp zzrp) throws IOException;
}
