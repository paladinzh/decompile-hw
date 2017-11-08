package com.google.android.gms.internal;

import android.support.annotation.Nullable;
import com.google.android.gms.common.api.Api.ApiOptions.Optional;

/* compiled from: Unknown */
public final class zzro implements Optional {
    public static final zzro zzbgV = new zza().zzFJ();
    private final boolean zzXa;
    private final boolean zzXc;
    private final String zzXd;
    private final String zzXe;
    private final boolean zzbgW;
    private final boolean zzbgX;

    /* compiled from: Unknown */
    public static final class zza {
        private String zzbdY;
        private boolean zzbgY;
        private boolean zzbgZ;
        private boolean zzbha;
        private String zzbhb;
        private boolean zzbhc;

        public zzro zzFJ() {
            return new zzro(this.zzbgY, this.zzbgZ, this.zzbdY, this.zzbha, this.zzbhb, this.zzbhc);
        }
    }

    private zzro(boolean z, boolean z2, String str, boolean z3, String str2, boolean z4) {
        this.zzbgW = z;
        this.zzXa = z2;
        this.zzXd = str;
        this.zzXc = z3;
        this.zzbgX = z4;
        this.zzXe = str2;
    }

    public boolean zzFH() {
        return this.zzbgW;
    }

    public boolean zzFI() {
        return this.zzbgX;
    }

    public boolean zzmO() {
        return this.zzXa;
    }

    public boolean zzmQ() {
        return this.zzXc;
    }

    public String zzmR() {
        return this.zzXd;
    }

    @Nullable
    public String zzmS() {
        return this.zzXe;
    }
}
