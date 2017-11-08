package com.google.android.gms.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
class zzru implements Cloneable {
    private zzrs<?, ?> zzbck;
    private Object zzbcl;
    private List<zzrz> zzbcm = new ArrayList();

    zzru() {
    }

    private byte[] toByteArray() throws IOException {
        byte[] bArr = new byte[zzB()];
        zza(zzrq.zzA(bArr));
        return bArr;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzDq();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzru)) {
            return false;
        }
        zzru zzru = (zzru) o;
        if (this.zzbcl != null && zzru.zzbcl != null) {
            return this.zzbck == zzru.zzbck ? this.zzbck.zzbce.isArray() ? !(this.zzbcl instanceof byte[]) ? !(this.zzbcl instanceof int[]) ? !(this.zzbcl instanceof long[]) ? !(this.zzbcl instanceof float[]) ? !(this.zzbcl instanceof double[]) ? !(this.zzbcl instanceof boolean[]) ? Arrays.deepEquals((Object[]) this.zzbcl, (Object[]) zzru.zzbcl) : Arrays.equals((boolean[]) this.zzbcl, (boolean[]) zzru.zzbcl) : Arrays.equals((double[]) this.zzbcl, (double[]) zzru.zzbcl) : Arrays.equals((float[]) this.zzbcl, (float[]) zzru.zzbcl) : Arrays.equals((long[]) this.zzbcl, (long[]) zzru.zzbcl) : Arrays.equals((int[]) this.zzbcl, (int[]) zzru.zzbcl) : Arrays.equals((byte[]) this.zzbcl, (byte[]) zzru.zzbcl) : this.zzbcl.equals(zzru.zzbcl) : false;
        } else {
            if (this.zzbcm != null && zzru.zzbcm != null) {
                return this.zzbcm.equals(zzru.zzbcm);
            }
            try {
                return Arrays.equals(toByteArray(), zzru.toByteArray());
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

    int zzB() {
        if (this.zzbcl != null) {
            return this.zzbck.zzS(this.zzbcl);
        }
        int i = 0;
        for (zzrz zzB : this.zzbcm) {
            i = zzB.zzB() + i;
        }
        return i;
    }

    public final zzru zzDq() {
        int i = 0;
        zzru zzru = new zzru();
        try {
            zzru.zzbck = this.zzbck;
            if (this.zzbcm != null) {
                zzru.zzbcm.addAll(this.zzbcm);
            } else {
                zzru.zzbcm = null;
            }
            if (this.zzbcl != null) {
                if (this.zzbcl instanceof zzrx) {
                    zzru.zzbcl = ((zzrx) this.zzbcl).zzDo();
                } else if (this.zzbcl instanceof byte[]) {
                    zzru.zzbcl = ((byte[]) this.zzbcl).clone();
                } else if (this.zzbcl instanceof byte[][]) {
                    byte[][] bArr = (byte[][]) this.zzbcl;
                    Object obj = new byte[bArr.length][];
                    zzru.zzbcl = obj;
                    for (int i2 = 0; i2 < bArr.length; i2++) {
                        obj[i2] = (byte[]) bArr[i2].clone();
                    }
                } else if (this.zzbcl instanceof boolean[]) {
                    zzru.zzbcl = ((boolean[]) this.zzbcl).clone();
                } else if (this.zzbcl instanceof int[]) {
                    zzru.zzbcl = ((int[]) this.zzbcl).clone();
                } else if (this.zzbcl instanceof long[]) {
                    zzru.zzbcl = ((long[]) this.zzbcl).clone();
                } else if (this.zzbcl instanceof float[]) {
                    zzru.zzbcl = ((float[]) this.zzbcl).clone();
                } else if (this.zzbcl instanceof double[]) {
                    zzru.zzbcl = ((double[]) this.zzbcl).clone();
                } else if (this.zzbcl instanceof zzrx[]) {
                    zzrx[] zzrxArr = (zzrx[]) this.zzbcl;
                    Object obj2 = new zzrx[zzrxArr.length];
                    zzru.zzbcl = obj2;
                    while (i < zzrxArr.length) {
                        obj2[i] = zzrxArr[i].zzDo();
                        i++;
                    }
                }
            }
            return zzru;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    void zza(zzrq zzrq) throws IOException {
        if (this.zzbcl == null) {
            for (zzrz zza : this.zzbcm) {
                zza.zza(zzrq);
            }
            return;
        }
        this.zzbck.zza(this.zzbcl, zzrq);
    }

    void zza(zzrz zzrz) {
        this.zzbcm.add(zzrz);
    }
}
