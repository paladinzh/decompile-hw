package com.google.android.gms.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
class zzsr implements Cloneable {
    private zzsp<?, ?> zzbuq;
    private Object zzbur;
    private List<zzsw> zzbus = new ArrayList();

    zzsr() {
    }

    private byte[] toByteArray() throws IOException {
        byte[] bArr = new byte[zzz()];
        writeTo(zzsn.zzE(bArr));
        return bArr;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzJr();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzsr)) {
            return false;
        }
        zzsr zzsr = (zzsr) o;
        if (this.zzbur != null && zzsr.zzbur != null) {
            return this.zzbuq == zzsr.zzbuq ? this.zzbuq.zzbuk.isArray() ? !(this.zzbur instanceof byte[]) ? !(this.zzbur instanceof int[]) ? !(this.zzbur instanceof long[]) ? !(this.zzbur instanceof float[]) ? !(this.zzbur instanceof double[]) ? !(this.zzbur instanceof boolean[]) ? Arrays.deepEquals((Object[]) this.zzbur, (Object[]) zzsr.zzbur) : Arrays.equals((boolean[]) this.zzbur, (boolean[]) zzsr.zzbur) : Arrays.equals((double[]) this.zzbur, (double[]) zzsr.zzbur) : Arrays.equals((float[]) this.zzbur, (float[]) zzsr.zzbur) : Arrays.equals((long[]) this.zzbur, (long[]) zzsr.zzbur) : Arrays.equals((int[]) this.zzbur, (int[]) zzsr.zzbur) : Arrays.equals((byte[]) this.zzbur, (byte[]) zzsr.zzbur) : this.zzbur.equals(zzsr.zzbur) : false;
        } else {
            if (this.zzbus != null && zzsr.zzbus != null) {
                return this.zzbus.equals(zzsr.zzbus);
            }
            try {
                return Arrays.equals(toByteArray(), zzsr.toByteArray());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public int hashCode() {
        try {
            return Arrays.hashCode(toByteArray()) + 527;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    void writeTo(zzsn output) throws IOException {
        if (this.zzbur == null) {
            for (zzsw writeTo : this.zzbus) {
                writeTo.writeTo(output);
            }
            return;
        }
        this.zzbuq.zza(this.zzbur, output);
    }

    public final zzsr zzJr() {
        int i = 0;
        zzsr zzsr = new zzsr();
        try {
            zzsr.zzbuq = this.zzbuq;
            if (this.zzbus != null) {
                zzsr.zzbus.addAll(this.zzbus);
            } else {
                zzsr.zzbus = null;
            }
            if (this.zzbur != null) {
                if (this.zzbur instanceof zzsu) {
                    zzsr.zzbur = ((zzsu) this.zzbur).clone();
                } else if (this.zzbur instanceof byte[]) {
                    zzsr.zzbur = ((byte[]) this.zzbur).clone();
                } else if (this.zzbur instanceof byte[][]) {
                    byte[][] bArr = (byte[][]) this.zzbur;
                    Object obj = new byte[bArr.length][];
                    zzsr.zzbur = obj;
                    for (int i2 = 0; i2 < bArr.length; i2++) {
                        obj[i2] = (byte[]) bArr[i2].clone();
                    }
                } else if (this.zzbur instanceof boolean[]) {
                    zzsr.zzbur = ((boolean[]) this.zzbur).clone();
                } else if (this.zzbur instanceof int[]) {
                    zzsr.zzbur = ((int[]) this.zzbur).clone();
                } else if (this.zzbur instanceof long[]) {
                    zzsr.zzbur = ((long[]) this.zzbur).clone();
                } else if (this.zzbur instanceof float[]) {
                    zzsr.zzbur = ((float[]) this.zzbur).clone();
                } else if (this.zzbur instanceof double[]) {
                    zzsr.zzbur = ((double[]) this.zzbur).clone();
                } else if (this.zzbur instanceof zzsu[]) {
                    zzsu[] zzsuArr = (zzsu[]) this.zzbur;
                    Object obj2 = new zzsu[zzsuArr.length];
                    zzsr.zzbur = obj2;
                    while (i < zzsuArr.length) {
                        obj2[i] = zzsuArr[i].clone();
                        i++;
                    }
                }
            }
            return zzsr;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    void zza(zzsw zzsw) {
        this.zzbus.add(zzsw);
    }

    <T> T zzb(zzsp<?, T> zzsp) {
        if (this.zzbur == null) {
            this.zzbuq = zzsp;
            this.zzbur = zzsp.zzJ(this.zzbus);
            this.zzbus = null;
        } else if (this.zzbuq != zzsp) {
            throw new IllegalStateException("Tried to getExtension with a differernt Extension.");
        }
        return this.zzbur;
    }

    int zzz() {
        if (this.zzbur != null) {
            return this.zzbuq.zzY(this.zzbur);
        }
        int i = 0;
        for (zzsw zzz : this.zzbus) {
            i = zzz.zzz() + i;
        }
        return i;
    }
}
