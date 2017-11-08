package com.google.android.gms.common.data;

import com.google.android.gms.common.internal.zzw;
import com.google.android.gms.common.internal.zzx;

/* compiled from: Unknown */
public abstract class zzc {
    protected final DataHolder zzYX;
    protected int zzabh;
    private int zzabi;

    public zzc(DataHolder dataHolder, int i) {
        this.zzYX = (DataHolder) zzx.zzv(dataHolder);
        zzbm(i);
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof zzc)) {
            return false;
        }
        zzc zzc = (zzc) obj;
        if (zzw.equal(Integer.valueOf(zzc.zzabh), Integer.valueOf(this.zzabh)) && zzw.equal(Integer.valueOf(zzc.zzabi), Integer.valueOf(this.zzabi)) && zzc.zzYX == this.zzYX) {
            z = true;
        }
        return z;
    }

    protected byte[] getByteArray(String column) {
        return this.zzYX.zzg(column, this.zzabh, this.zzabi);
    }

    protected int getInteger(String column) {
        return this.zzYX.zzc(column, this.zzabh, this.zzabi);
    }

    protected String getString(String column) {
        return this.zzYX.zzd(column, this.zzabh, this.zzabi);
    }

    public int hashCode() {
        return zzw.hashCode(Integer.valueOf(this.zzabh), Integer.valueOf(this.zzabi), this.zzYX);
    }

    protected void zzbm(int i) {
        boolean z = false;
        if (i >= 0 && i < this.zzYX.getCount()) {
            z = true;
        }
        zzx.zzY(z);
        this.zzabh = i;
        this.zzabi = this.zzYX.zzbo(this.zzabh);
    }
}
