package com.google.android.gms.internal;

/* compiled from: Unknown */
class zzrt implements Cloneable {
    private static final zzru zzbcg = new zzru();
    private int mSize;
    private boolean zzbch;
    private int[] zzbci;
    private zzru[] zzbcj;

    public zzrt() {
        this(10);
    }

    public zzrt(int i) {
        this.zzbch = false;
        int idealIntArraySize = idealIntArraySize(i);
        this.zzbci = new int[idealIntArraySize];
        this.zzbcj = new zzru[idealIntArraySize];
        this.mSize = 0;
    }

    private void gc() {
        int i = this.mSize;
        int[] iArr = this.zzbci;
        zzru[] zzruArr = this.zzbcj;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            zzru zzru = zzruArr[i3];
            if (zzru != zzbcg) {
                if (i3 != i2) {
                    iArr[i2] = iArr[i3];
                    zzruArr[i2] = zzru;
                    zzruArr[i3] = null;
                }
                i2++;
            }
        }
        this.zzbch = false;
        this.mSize = i2;
    }

    private int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    private int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    private boolean zza(int[] iArr, int[] iArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (iArr[i2] != iArr2[i2]) {
                return false;
            }
        }
        return true;
    }

    private boolean zza(zzru[] zzruArr, zzru[] zzruArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (!zzruArr[i2].equals(zzruArr2[i2])) {
                return false;
            }
        }
        return true;
    }

    private int zzlC(int i) {
        int i2 = 0;
        int i3 = this.mSize - 1;
        while (i2 <= i3) {
            int i4 = (i2 + i3) >>> 1;
            int i5 = this.zzbci[i4];
            if (i5 < i) {
                i2 = i4 + 1;
            } else if (i5 <= i) {
                return i4;
            } else {
                i3 = i4 - 1;
            }
        }
        return i2 ^ -1;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzDp();
    }

    public boolean equals(Object o) {
        boolean z = true;
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzrt)) {
            return false;
        }
        zzrt zzrt = (zzrt) o;
        if (size() != zzrt.size()) {
            return false;
        }
        if (zza(this.zzbci, zzrt.zzbci, this.mSize)) {
            if (!zza(this.zzbcj, zzrt.zzbcj, this.mSize)) {
            }
            return z;
        }
        z = false;
        return z;
    }

    public int hashCode() {
        if (this.zzbch) {
            gc();
        }
        int i = 17;
        for (int i2 = 0; i2 < this.mSize; i2++) {
            i = (((i * 31) + this.zzbci[i2]) * 31) + this.zzbcj[i2].hashCode();
        }
        return i;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        if (this.zzbch) {
            gc();
        }
        return this.mSize;
    }

    public final zzrt zzDp() {
        int i = 0;
        int size = size();
        zzrt zzrt = new zzrt(size);
        System.arraycopy(this.zzbci, 0, zzrt.zzbci, 0, size);
        while (i < size) {
            if (this.zzbcj[i] != null) {
                zzrt.zzbcj[i] = this.zzbcj[i].zzDq();
            }
            i++;
        }
        zzrt.mSize = size;
        return zzrt;
    }

    public void zza(int i, zzru zzru) {
        int zzlC = zzlC(i);
        if (zzlC < 0) {
            zzlC ^= -1;
            if (zzlC < this.mSize && this.zzbcj[zzlC] == zzbcg) {
                this.zzbci[zzlC] = i;
                this.zzbcj[zzlC] = zzru;
                return;
            }
            if (this.zzbch && this.mSize >= this.zzbci.length) {
                gc();
                zzlC = zzlC(i) ^ -1;
            }
            if (this.mSize >= this.zzbci.length) {
                int idealIntArraySize = idealIntArraySize(this.mSize + 1);
                Object obj = new int[idealIntArraySize];
                Object obj2 = new zzru[idealIntArraySize];
                System.arraycopy(this.zzbci, 0, obj, 0, this.zzbci.length);
                System.arraycopy(this.zzbcj, 0, obj2, 0, this.zzbcj.length);
                this.zzbci = obj;
                this.zzbcj = obj2;
            }
            if (this.mSize - zzlC != 0) {
                System.arraycopy(this.zzbci, zzlC, this.zzbci, zzlC + 1, this.mSize - zzlC);
                System.arraycopy(this.zzbcj, zzlC, this.zzbcj, zzlC + 1, this.mSize - zzlC);
            }
            this.zzbci[zzlC] = i;
            this.zzbcj[zzlC] = zzru;
            this.mSize++;
        } else {
            this.zzbcj[zzlC] = zzru;
        }
    }

    public zzru zzlA(int i) {
        int zzlC = zzlC(i);
        return (zzlC >= 0 && this.zzbcj[zzlC] != zzbcg) ? this.zzbcj[zzlC] : null;
    }

    public zzru zzlB(int i) {
        if (this.zzbch) {
            gc();
        }
        return this.zzbcj[i];
    }
}
