package com.google.android.gms.internal;

import com.android.deskclock.alarmclock.MetaballPath;
import java.io.IOException;

/* compiled from: Unknown */
public final class zzrp {
    private final byte[] buffer;
    private int zzbbT;
    private int zzbbU;
    private int zzbbV;
    private int zzbbW;
    private int zzbbX;
    private int zzbbY = Integer.MAX_VALUE;
    private int zzbbZ;
    private int zzbca = 64;
    private int zzbcb = 67108864;

    private zzrp(byte[] bArr, int i, int i2) {
        this.buffer = bArr;
        this.zzbbT = i;
        this.zzbbU = i + i2;
        this.zzbbW = i;
    }

    private void zzDg() {
        this.zzbbU += this.zzbbV;
        int i = this.zzbbU;
        if (i <= this.zzbbY) {
            this.zzbbV = 0;
            return;
        }
        this.zzbbV = i - this.zzbbY;
        this.zzbbU -= this.zzbbV;
    }

    public static long zzV(long j) {
        return (j >>> 1) ^ (-(1 & j));
    }

    public static zzrp zza(byte[] bArr, int i, int i2) {
        return new zzrp(bArr, i, i2);
    }

    public static int zzlk(int i) {
        return (i >>> 1) ^ (-(i & 1));
    }

    public int getPosition() {
        return this.zzbbW - this.zzbbT;
    }

    public byte[] readBytes() throws IOException {
        int zzDc = zzDc();
        if (zzDc > this.zzbbU - this.zzbbW || zzDc <= 0) {
            return zzlo(zzDc);
        }
        Object obj = new byte[zzDc];
        System.arraycopy(this.buffer, this.zzbbW, obj, 0, zzDc);
        this.zzbbW = zzDc + this.zzbbW;
        return obj;
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(zzDf());
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(zzDe());
    }

    public String readString() throws IOException {
        int zzDc = zzDc();
        if (zzDc > this.zzbbU - this.zzbbW || zzDc <= 0) {
            return new String(zzlo(zzDc), "UTF-8");
        }
        String str = new String(this.buffer, this.zzbbW, zzDc, "UTF-8");
        this.zzbbW = zzDc + this.zzbbW;
        return str;
    }

    public int zzCV() throws IOException {
        if (zzDi()) {
            this.zzbbX = 0;
            return 0;
        }
        this.zzbbX = zzDc();
        if (this.zzbbX != 0) {
            return this.zzbbX;
        }
        throw zzrw.zzDu();
    }

    public void zzCW() throws IOException {
        while (true) {
            int zzCV = zzCV();
            if (zzCV == 0 || !zzlj(zzCV)) {
                return;
            }
        }
    }

    public long zzCX() throws IOException {
        return zzDd();
    }

    public int zzCY() throws IOException {
        return zzDc();
    }

    public boolean zzCZ() throws IOException {
        return zzDc() != 0;
    }

    public int zzDa() throws IOException {
        return zzlk(zzDc());
    }

    public long zzDb() throws IOException {
        return zzV(zzDd());
    }

    public int zzDc() throws IOException {
        byte zzDj = zzDj();
        if (zzDj >= (byte) 0) {
            return zzDj;
        }
        int i = zzDj & 127;
        byte zzDj2 = zzDj();
        if (zzDj2 < (byte) 0) {
            i |= (zzDj2 & 127) << 7;
            zzDj2 = zzDj();
            if (zzDj2 < (byte) 0) {
                i |= (zzDj2 & 127) << 14;
                zzDj2 = zzDj();
                if (zzDj2 < (byte) 0) {
                    i |= (zzDj2 & 127) << 21;
                    zzDj2 = zzDj();
                    i |= zzDj2 << 28;
                    if (zzDj2 < (byte) 0) {
                        for (int i2 = 0; i2 < 5; i2++) {
                            if (zzDj() >= (byte) 0) {
                                return i;
                            }
                        }
                        throw zzrw.zzDt();
                    }
                }
                i |= zzDj2 << 21;
            } else {
                i |= zzDj2 << 14;
            }
        } else {
            i |= zzDj2 << 7;
        }
        return i;
    }

    public long zzDd() throws IOException {
        long j = 0;
        for (int i = 0; i < 64; i += 7) {
            byte zzDj = zzDj();
            j |= ((long) (zzDj & 127)) << i;
            if ((zzDj & 128) == 0) {
                return j;
            }
        }
        throw zzrw.zzDt();
    }

    public int zzDe() throws IOException {
        return (((zzDj() & 255) | ((zzDj() & 255) << 8)) | ((zzDj() & 255) << 16)) | ((zzDj() & 255) << 24);
    }

    public long zzDf() throws IOException {
        byte zzDj = zzDj();
        byte zzDj2 = zzDj();
        return ((((((((((long) zzDj2) & 255) << 8) | (((long) zzDj) & 255)) | ((((long) zzDj()) & 255) << 16)) | ((((long) zzDj()) & 255) << 24)) | ((((long) zzDj()) & 255) << 32)) | ((((long) zzDj()) & 255) << 40)) | ((((long) zzDj()) & 255) << 48)) | ((((long) zzDj()) & 255) << 56);
    }

    public int zzDh() {
        if (this.zzbbY == Integer.MAX_VALUE) {
            return -1;
        }
        return this.zzbbY - this.zzbbW;
    }

    public boolean zzDi() {
        return this.zzbbW == this.zzbbU;
    }

    public byte zzDj() throws IOException {
        if (this.zzbbW != this.zzbbU) {
            byte[] bArr = this.buffer;
            int i = this.zzbbW;
            this.zzbbW = i + 1;
            return bArr[i];
        }
        throw zzrw.zzDr();
    }

    public void zza(zzrx zzrx) throws IOException {
        int zzDc = zzDc();
        if (this.zzbbZ < this.zzbca) {
            zzDc = zzll(zzDc);
            this.zzbbZ++;
            zzrx.zzb(this);
            zzli(0);
            this.zzbbZ--;
            zzlm(zzDc);
            return;
        }
        throw zzrw.zzDx();
    }

    public void zzli(int i) throws zzrw {
        if (this.zzbbX != i) {
            throw zzrw.zzDv();
        }
    }

    public boolean zzlj(int i) throws IOException {
        switch (zzsa.zzlD(i)) {
            case 0:
                zzCY();
                return true;
            case 1:
                zzDf();
                return true;
            case 2:
                zzlp(zzDc());
                return true;
            case 3:
                zzCW();
                zzli(zzsa.zzE(zzsa.zzlE(i), 4));
                return true;
            case MetaballPath.POINT_NUM /*4*/:
                return false;
            case 5:
                zzDe();
                return true;
            default:
                throw zzrw.zzDw();
        }
    }

    public int zzll(int i) throws zzrw {
        if (i >= 0) {
            int i2 = this.zzbbW + i;
            int i3 = this.zzbbY;
            if (i2 <= i3) {
                this.zzbbY = i2;
                zzDg();
                return i3;
            }
            throw zzrw.zzDr();
        }
        throw zzrw.zzDs();
    }

    public void zzlm(int i) {
        this.zzbbY = i;
        zzDg();
    }

    public void zzln(int i) {
        if (i > this.zzbbW - this.zzbbT) {
            throw new IllegalArgumentException("Position " + i + " is beyond current " + (this.zzbbW - this.zzbbT));
        } else if (i >= 0) {
            this.zzbbW = this.zzbbT + i;
        } else {
            throw new IllegalArgumentException("Bad position " + i);
        }
    }

    public byte[] zzlo(int i) throws IOException {
        if (i < 0) {
            throw zzrw.zzDs();
        } else if (this.zzbbW + i > this.zzbbY) {
            zzlp(this.zzbbY - this.zzbbW);
            throw zzrw.zzDr();
        } else if (i > this.zzbbU - this.zzbbW) {
            throw zzrw.zzDr();
        } else {
            Object obj = new byte[i];
            System.arraycopy(this.buffer, this.zzbbW, obj, 0, i);
            this.zzbbW += i;
            return obj;
        }
    }

    public void zzlp(int i) throws IOException {
        if (i < 0) {
            throw zzrw.zzDs();
        } else if (this.zzbbW + i > this.zzbbY) {
            zzlp(this.zzbbY - this.zzbbW);
            throw zzrw.zzDr();
        } else if (i > this.zzbbU - this.zzbbW) {
            throw zzrw.zzDr();
        } else {
            this.zzbbW += i;
        }
    }

    public byte[] zzy(int i, int i2) {
        if (i2 == 0) {
            return zzsa.zzbcx;
        }
        Object obj = new byte[i2];
        System.arraycopy(this.buffer, this.zzbbT + i, obj, 0, i2);
        return obj;
    }
}
