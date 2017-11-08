package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public final class zzsm {
    private final byte[] buffer;
    private int zzbtZ;
    private int zzbua;
    private int zzbub;
    private int zzbuc;
    private int zzbud;
    private int zzbue = Integer.MAX_VALUE;
    private int zzbuf;
    private int zzbug = 64;
    private int zzbuh = 67108864;

    private zzsm(byte[] bArr, int i, int i2) {
        this.buffer = bArr;
        this.zzbtZ = i;
        this.zzbua = i + i2;
        this.zzbuc = i;
    }

    public static zzsm zzD(byte[] bArr) {
        return zza(bArr, 0, bArr.length);
    }

    private void zzJj() {
        this.zzbua += this.zzbub;
        int i = this.zzbua;
        if (i <= this.zzbue) {
            this.zzbub = 0;
            return;
        }
        this.zzbub = i - this.zzbue;
        this.zzbua -= this.zzbub;
    }

    public static zzsm zza(byte[] bArr, int i, int i2) {
        return new zzsm(bArr, i, i2);
    }

    public static long zzan(long j) {
        return (j >>> 1) ^ (-(1 & j));
    }

    public static int zzmp(int i) {
        return (i >>> 1) ^ (-(i & 1));
    }

    public int getPosition() {
        return this.zzbuc - this.zzbtZ;
    }

    public byte[] readBytes() throws IOException {
        int zzJf = zzJf();
        if (zzJf > this.zzbua - this.zzbuc || zzJf <= 0) {
            return zzJf != 0 ? zzmt(zzJf) : zzsx.zzbuD;
        } else {
            Object obj = new byte[zzJf];
            System.arraycopy(this.buffer, this.zzbuc, obj, 0, zzJf);
            this.zzbuc = zzJf + this.zzbuc;
            return obj;
        }
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(zzJi());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(zzJh());
    }

    public String readString() throws IOException {
        int zzJf = zzJf();
        if (zzJf > this.zzbua - this.zzbuc || zzJf <= 0) {
            return new String(zzmt(zzJf), "UTF-8");
        }
        String str = new String(this.buffer, this.zzbuc, zzJf, "UTF-8");
        this.zzbuc = zzJf + this.zzbuc;
        return str;
    }

    public int zzIX() throws IOException {
        if (zzJl()) {
            this.zzbud = 0;
            return 0;
        }
        this.zzbud = zzJf();
        if (this.zzbud != 0) {
            return this.zzbud;
        }
        throw zzst.zzJv();
    }

    public void zzIY() throws IOException {
        while (true) {
            int zzIX = zzIX();
            if (zzIX == 0 || !zzmo(zzIX)) {
                return;
            }
        }
    }

    public long zzIZ() throws IOException {
        return zzJg();
    }

    public long zzJa() throws IOException {
        return zzJg();
    }

    public int zzJb() throws IOException {
        return zzJf();
    }

    public boolean zzJc() throws IOException {
        return zzJf() != 0;
    }

    public int zzJd() throws IOException {
        return zzmp(zzJf());
    }

    public long zzJe() throws IOException {
        return zzan(zzJg());
    }

    public int zzJf() throws IOException {
        byte zzJm = zzJm();
        if (zzJm >= (byte) 0) {
            return zzJm;
        }
        int i = zzJm & 127;
        byte zzJm2 = zzJm();
        if (zzJm2 < (byte) 0) {
            i |= (zzJm2 & 127) << 7;
            zzJm2 = zzJm();
            if (zzJm2 < (byte) 0) {
                i |= (zzJm2 & 127) << 14;
                zzJm2 = zzJm();
                if (zzJm2 < (byte) 0) {
                    i |= (zzJm2 & 127) << 21;
                    zzJm2 = zzJm();
                    i |= zzJm2 << 28;
                    if (zzJm2 < (byte) 0) {
                        for (int i2 = 0; i2 < 5; i2++) {
                            if (zzJm() >= (byte) 0) {
                                return i;
                            }
                        }
                        throw zzst.zzJu();
                    }
                }
                i |= zzJm2 << 21;
            } else {
                i |= zzJm2 << 14;
            }
        } else {
            i |= zzJm2 << 7;
        }
        return i;
    }

    public long zzJg() throws IOException {
        long j = 0;
        for (int i = 0; i < 64; i += 7) {
            byte zzJm = zzJm();
            j |= ((long) (zzJm & 127)) << i;
            if ((zzJm & 128) == 0) {
                return j;
            }
        }
        throw zzst.zzJu();
    }

    public int zzJh() throws IOException {
        return (((zzJm() & 255) | ((zzJm() & 255) << 8)) | ((zzJm() & 255) << 16)) | ((zzJm() & 255) << 24);
    }

    public long zzJi() throws IOException {
        byte zzJm = zzJm();
        byte zzJm2 = zzJm();
        return ((((((((((long) zzJm2) & 255) << 8) | (((long) zzJm) & 255)) | ((((long) zzJm()) & 255) << 16)) | ((((long) zzJm()) & 255) << 24)) | ((((long) zzJm()) & 255) << 32)) | ((((long) zzJm()) & 255) << 40)) | ((((long) zzJm()) & 255) << 48)) | ((((long) zzJm()) & 255) << 56);
    }

    public int zzJk() {
        if (this.zzbue == Integer.MAX_VALUE) {
            return -1;
        }
        return this.zzbue - this.zzbuc;
    }

    public boolean zzJl() {
        return this.zzbuc == this.zzbua;
    }

    public byte zzJm() throws IOException {
        if (this.zzbuc != this.zzbua) {
            byte[] bArr = this.buffer;
            int i = this.zzbuc;
            this.zzbuc = i + 1;
            return bArr[i];
        }
        throw zzst.zzJs();
    }

    public void zza(zzsu zzsu) throws IOException {
        int zzJf = zzJf();
        if (this.zzbuf < this.zzbug) {
            zzJf = zzmq(zzJf);
            this.zzbuf++;
            zzsu.mergeFrom(this);
            zzmn(0);
            this.zzbuf--;
            zzmr(zzJf);
            return;
        }
        throw zzst.zzJy();
    }

    public void zza(zzsu zzsu, int i) throws IOException {
        if (this.zzbuf < this.zzbug) {
            this.zzbuf++;
            zzsu.mergeFrom(this);
            zzmn(zzsx.zzF(i, 4));
            this.zzbuf--;
            return;
        }
        throw zzst.zzJy();
    }

    public void zzmn(int i) throws zzst {
        if (this.zzbud != i) {
            throw zzst.zzJw();
        }
    }

    public boolean zzmo(int i) throws IOException {
        switch (zzsx.zzmI(i)) {
            case 0:
                zzJb();
                return true;
            case 1:
                zzJi();
                return true;
            case 2:
                zzmu(zzJf());
                return true;
            case 3:
                zzIY();
                zzmn(zzsx.zzF(zzsx.zzmJ(i), 4));
                return true;
            case 4:
                return false;
            case 5:
                zzJh();
                return true;
            default:
                throw zzst.zzJx();
        }
    }

    public int zzmq(int i) throws zzst {
        if (i >= 0) {
            int i2 = this.zzbuc + i;
            int i3 = this.zzbue;
            if (i2 <= i3) {
                this.zzbue = i2;
                zzJj();
                return i3;
            }
            throw zzst.zzJs();
        }
        throw zzst.zzJt();
    }

    public void zzmr(int i) {
        this.zzbue = i;
        zzJj();
    }

    public void zzms(int i) {
        if (i > this.zzbuc - this.zzbtZ) {
            throw new IllegalArgumentException("Position " + i + " is beyond current " + (this.zzbuc - this.zzbtZ));
        } else if (i >= 0) {
            this.zzbuc = this.zzbtZ + i;
        } else {
            throw new IllegalArgumentException("Bad position " + i);
        }
    }

    public byte[] zzmt(int i) throws IOException {
        if (i < 0) {
            throw zzst.zzJt();
        } else if (this.zzbuc + i > this.zzbue) {
            zzmu(this.zzbue - this.zzbuc);
            throw zzst.zzJs();
        } else if (i > this.zzbua - this.zzbuc) {
            throw zzst.zzJs();
        } else {
            Object obj = new byte[i];
            System.arraycopy(this.buffer, this.zzbuc, obj, 0, i);
            this.zzbuc += i;
            return obj;
        }
    }

    public void zzmu(int i) throws IOException {
        if (i < 0) {
            throw zzst.zzJt();
        } else if (this.zzbuc + i > this.zzbue) {
            zzmu(this.zzbue - this.zzbuc);
            throw zzst.zzJs();
        } else if (i > this.zzbua - this.zzbuc) {
            throw zzst.zzJs();
        } else {
            this.zzbuc += i;
        }
    }

    public byte[] zzz(int i, int i2) {
        if (i2 == 0) {
            return zzsx.zzbuD;
        }
        Object obj = new byte[i2];
        System.arraycopy(this.buffer, this.zzbtZ + i, obj, 0, i2);
        return obj;
    }
}
