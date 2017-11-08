package com.google.android.gms.internal;

import java.io.IOException;
import java.lang.reflect.Array;

/* compiled from: Unknown */
public class zzrs<M extends zzrr<M>, T> {
    public final int tag;
    protected final int type;
    protected final Class<T> zzbce;
    protected final boolean zzbcf;

    int zzS(Object obj) {
        return !this.zzbcf ? zzU(obj) : zzT(obj);
    }

    protected int zzT(Object obj) {
        int i = 0;
        int length = Array.getLength(obj);
        for (int i2 = 0; i2 < length; i2++) {
            if (Array.get(obj, i2) != null) {
                i += zzU(Array.get(obj, i2));
            }
        }
        return i;
    }

    protected int zzU(Object obj) {
        int zzlE = zzsa.zzlE(this.tag);
        switch (this.type) {
            case 10:
                return zzrq.zzb(zzlE, (zzrx) obj);
            case 11:
                return zzrq.zzc(zzlE, (zzrx) obj);
            default:
                throw new IllegalArgumentException("Unknown type " + this.type);
        }
    }

    void zza(Object obj, zzrq zzrq) throws IOException {
        if (this.zzbcf) {
            zzc(obj, zzrq);
        } else {
            zzb(obj, zzrq);
        }
    }

    protected void zzb(Object obj, zzrq zzrq) {
        try {
            zzrq.zzlw(this.tag);
            switch (this.type) {
                case 10:
                    zzrx zzrx = (zzrx) obj;
                    int zzlE = zzsa.zzlE(this.tag);
                    zzrq.zzb(zzrx);
                    zzrq.zzD(zzlE, 4);
                    return;
                case 11:
                    zzrq.zzc((zzrx) obj);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException(e);
    }

    protected void zzc(Object obj, zzrq zzrq) {
        int length = Array.getLength(obj);
        for (int i = 0; i < length; i++) {
            Object obj2 = Array.get(obj, i);
            if (obj2 != null) {
                zzb(obj2, zzrq);
            }
        }
    }
}
