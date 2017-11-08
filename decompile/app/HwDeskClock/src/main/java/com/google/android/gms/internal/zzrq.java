package com.google.android.gms.internal;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

/* compiled from: Unknown */
public final class zzrq {
    private final ByteBuffer zzbcc;

    /* compiled from: Unknown */
    public static class zza extends IOException {
        zza(int i, int i2) {
            super("CodedOutputStream was writing to a flat byte array and ran out of space (pos " + i + " limit " + i2 + ").");
        }
    }

    private zzrq(ByteBuffer byteBuffer) {
        this.zzbcc = byteBuffer;
    }

    private zzrq(byte[] bArr, int i, int i2) {
        this(ByteBuffer.wrap(bArr, i, i2));
    }

    public static zzrq zzA(byte[] bArr) {
        return zzb(bArr, 0, bArr.length);
    }

    public static int zzB(int i, int i2) {
        return zzlv(i) + zzls(i2);
    }

    public static int zzC(int i, int i2) {
        return zzlv(i) + zzlt(i2);
    }

    public static int zzC(byte[] bArr) {
        return zzlx(bArr.length) + bArr.length;
    }

    public static int zzY(long j) {
        return zzab(j);
    }

    public static int zzZ(long j) {
        return zzab(zzad(j));
    }

    private static int zza(CharSequence charSequence, int i) {
        int length = charSequence.length();
        int i2 = 0;
        int i3 = i;
        while (i3 < length) {
            char charAt = charSequence.charAt(i3);
            if (charAt >= 'ࠀ') {
                i2 += 2;
                if ('?' <= charAt && charAt <= '?') {
                    if (Character.codePointAt(charSequence, i3) >= 65536) {
                        i3++;
                    } else {
                        throw new IllegalArgumentException("Unpaired surrogate at index " + i3);
                    }
                }
            }
            i2 += (127 - charAt) >>> 31;
            i3++;
        }
        return i2;
    }

    private static int zza(CharSequence charSequence, byte[] bArr, int i, int i2) {
        int i3 = 0;
        int length = charSequence.length();
        int i4 = i + i2;
        while (i3 < length && i3 + i < i4) {
            char charAt = charSequence.charAt(i3);
            if (charAt >= '') {
                break;
            }
            bArr[i + i3] = (byte) ((byte) charAt);
            i3++;
        }
        if (i3 == length) {
            return i + length;
        }
        int i5 = i + i3;
        while (i3 < length) {
            int i6;
            int charAt2 = charSequence.charAt(i3);
            if (charAt2 < 128 && i5 < i4) {
                i6 = i5 + 1;
                bArr[i5] = (byte) ((byte) charAt2);
            } else {
                int i7;
                if (charAt2 < 2048 && i5 <= i4 - 2) {
                    i7 = i5 + 1;
                    bArr[i5] = (byte) ((byte) ((charAt2 >>> 6) | 960));
                } else {
                    if (charAt2 < 55296 || 57343 < charAt2) {
                        if (i5 <= i4 - 3) {
                            i6 = i5 + 1;
                            bArr[i5] = (byte) ((byte) ((charAt2 >>> 12) | 480));
                            i5 = i6 + 1;
                            bArr[i6] = (byte) ((byte) (((charAt2 >>> 6) & 63) | 128));
                            i6 = i5 + 1;
                            bArr[i5] = (byte) ((byte) ((charAt2 & 63) | 128));
                        }
                    }
                    if (i5 > i4 - 4) {
                        throw new ArrayIndexOutOfBoundsException("Failed writing " + charAt2 + " at index " + i5);
                    }
                    if (i3 + 1 != charSequence.length()) {
                        i3++;
                        charAt = charSequence.charAt(i3);
                        if (Character.isSurrogatePair(charAt2, charAt)) {
                            charAt2 = Character.toCodePoint(charAt2, charAt);
                            i6 = i5 + 1;
                            bArr[i5] = (byte) ((byte) ((charAt2 >>> 18) | 240));
                            i5 = i6 + 1;
                            bArr[i6] = (byte) ((byte) (((charAt2 >>> 12) & 63) | 128));
                            i7 = i5 + 1;
                            bArr[i5] = (byte) ((byte) (((charAt2 >>> 6) & 63) | 128));
                        }
                    }
                    throw new IllegalArgumentException("Unpaired surrogate at index " + (i3 - 1));
                }
                i6 = i7 + 1;
                bArr[i7] = (byte) ((byte) ((charAt2 & 63) | 128));
            }
            i3++;
            i5 = i6;
        }
        return i5;
    }

    private static void zza(CharSequence charSequence, ByteBuffer byteBuffer) {
        if (byteBuffer.isReadOnly()) {
            throw new ReadOnlyBufferException();
        } else if (byteBuffer.hasArray()) {
            try {
                byteBuffer.position(zza(charSequence, byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining()) - byteBuffer.arrayOffset());
            } catch (Throwable e) {
                BufferOverflowException bufferOverflowException = new BufferOverflowException();
                bufferOverflowException.initCause(e);
                throw bufferOverflowException;
            }
        } else {
            zzb(charSequence, byteBuffer);
        }
    }

    public static int zzab(long j) {
        return (-128 & j) == 0 ? 1 : (-16384 & j) == 0 ? 2 : (-2097152 & j) == 0 ? 3 : (-268435456 & j) == 0 ? 4 : (-34359738368L & j) == 0 ? 5 : (-4398046511104L & j) == 0 ? 6 : (-562949953421312L & j) == 0 ? 7 : (-72057594037927936L & j) == 0 ? 8 : (Long.MIN_VALUE & j) == 0 ? 9 : 10;
    }

    public static long zzad(long j) {
        return (j << 1) ^ (j >> 63);
    }

    public static int zzax(boolean z) {
        return 1;
    }

    public static int zzb(int i, double d) {
        return zzlv(i) + zzj(d);
    }

    public static int zzb(int i, zzrx zzrx) {
        return (zzlv(i) * 2) + zzd(zzrx);
    }

    public static int zzb(int i, byte[] bArr) {
        return zzlv(i) + zzC(bArr);
    }

    public static zzrq zzb(byte[] bArr, int i, int i2) {
        return new zzrq(bArr, i, i2);
    }

    private static void zzb(CharSequence charSequence, ByteBuffer byteBuffer) {
        int i = 0;
        int length = charSequence.length();
        while (i < length) {
            char charAt = charSequence.charAt(i);
            if (charAt < '') {
                byteBuffer.put((byte) charAt);
            } else if (charAt < 'ࠀ') {
                byteBuffer.put((byte) ((charAt >>> 6) | 960));
                byteBuffer.put((byte) ((charAt & 63) | 128));
            } else if (charAt >= '?' && '?' >= charAt) {
                if (i + 1 != charSequence.length()) {
                    i++;
                    char charAt2 = charSequence.charAt(i);
                    if (Character.isSurrogatePair(charAt, charAt2)) {
                        int toCodePoint = Character.toCodePoint(charAt, charAt2);
                        byteBuffer.put((byte) ((toCodePoint >>> 18) | 240));
                        byteBuffer.put((byte) (((toCodePoint >>> 12) & 63) | 128));
                        byteBuffer.put((byte) (((toCodePoint >>> 6) & 63) | 128));
                        byteBuffer.put((byte) ((toCodePoint & 63) | 128));
                    }
                }
                throw new IllegalArgumentException("Unpaired surrogate at index " + (i - 1));
            } else {
                byteBuffer.put((byte) ((charAt >>> 12) | 480));
                byteBuffer.put((byte) (((charAt >>> 6) & 63) | 128));
                byteBuffer.put((byte) ((charAt & 63) | 128));
            }
            i++;
        }
    }

    public static int zzc(int i, float f) {
        return zzlv(i) + zzj(f);
    }

    public static int zzc(int i, zzrx zzrx) {
        return zzlv(i) + zze(zzrx);
    }

    public static int zzc(int i, boolean z) {
        return zzlv(i) + zzax(z);
    }

    private static int zzc(CharSequence charSequence) {
        int length = charSequence.length();
        int i = 0;
        while (i < length && charSequence.charAt(i) < '') {
            i++;
        }
        int i2 = i;
        i = length;
        while (i2 < length) {
            char charAt = charSequence.charAt(i2);
            if (charAt >= 'ࠀ') {
                i += zza(charSequence, i2);
                break;
            }
            i2++;
            i = ((127 - charAt) >>> 31) + i;
        }
        if (i >= length) {
            return i;
        }
        throw new IllegalArgumentException("UTF-8 length does not fit in int: " + (((long) i) + 4294967296L));
    }

    public static int zzd(int i, long j) {
        return zzlv(i) + zzY(j);
    }

    public static int zzd(zzrx zzrx) {
        return zzrx.zzDz();
    }

    public static int zze(int i, long j) {
        return zzlv(i) + zzZ(j);
    }

    public static int zze(zzrx zzrx) {
        int zzDz = zzrx.zzDz();
        return zzDz + zzlx(zzDz);
    }

    public static int zzfy(String str) {
        int zzc = zzc((CharSequence) str);
        return zzc + zzlx(zzc);
    }

    public static int zzj(double d) {
        return 8;
    }

    public static int zzj(float f) {
        return 4;
    }

    public static int zzl(int i, String str) {
        return zzlv(i) + zzfy(str);
    }

    public static int zzls(int i) {
        return i < 0 ? 10 : zzlx(i);
    }

    public static int zzlt(int i) {
        return zzlx(zzlz(i));
    }

    public static int zzlv(int i) {
        return zzlx(zzsa.zzE(i, 0));
    }

    public static int zzlx(int i) {
        return (i & -128) != 0 ? (i & -16384) != 0 ? (-2097152 & i) != 0 ? (-268435456 & i) != 0 ? 5 : 4 : 3 : 2 : 1;
    }

    public static int zzlz(int i) {
        return (i << 1) ^ (i >> 31);
    }

    public void zzA(int i, int i2) throws IOException {
        zzD(i, 0);
        zzlr(i2);
    }

    public void zzB(byte[] bArr) throws IOException {
        zzlw(bArr.length);
        zzD(bArr);
    }

    public void zzD(int i, int i2) throws IOException {
        zzlw(zzsa.zzE(i, i2));
    }

    public void zzD(byte[] bArr) throws IOException {
        zzc(bArr, 0, bArr.length);
    }

    public int zzDk() {
        return this.zzbcc.remaining();
    }

    public void zzDl() {
        if (zzDk() != 0) {
            throw new IllegalStateException("Did not write as much data as expected.");
        }
    }

    public void zzW(long j) throws IOException {
        zzaa(j);
    }

    public void zzX(long j) throws IOException {
        zzaa(zzad(j));
    }

    public void zza(int i, double d) throws IOException {
        zzD(i, 1);
        zzi(d);
    }

    public void zza(int i, zzrx zzrx) throws IOException {
        zzD(i, 2);
        zzc(zzrx);
    }

    public void zza(int i, byte[] bArr) throws IOException {
        zzD(i, 2);
        zzB(bArr);
    }

    public void zzaa(long j) throws IOException {
        while ((-128 & j) != 0) {
            zzlu((((int) j) & 127) | 128);
            j >>>= 7;
        }
        zzlu((int) j);
    }

    public void zzac(long j) throws IOException {
        zzlu(((int) j) & 255);
        zzlu(((int) (j >> 8)) & 255);
        zzlu(((int) (j >> 16)) & 255);
        zzlu(((int) (j >> 24)) & 255);
        zzlu(((int) (j >> 32)) & 255);
        zzlu(((int) (j >> 40)) & 255);
        zzlu(((int) (j >> 48)) & 255);
        zzlu(((int) (j >> 56)) & 255);
    }

    public void zzaw(boolean z) throws IOException {
        int i = 0;
        if (z) {
            i = 1;
        }
        zzlu(i);
    }

    public void zzb(byte b) throws IOException {
        if (this.zzbcc.hasRemaining()) {
            this.zzbcc.put(b);
            return;
        }
        throw new zza(this.zzbcc.position(), this.zzbcc.limit());
    }

    public void zzb(int i, float f) throws IOException {
        zzD(i, 5);
        zzi(f);
    }

    public void zzb(int i, long j) throws IOException {
        zzD(i, 0);
        zzW(j);
    }

    public void zzb(int i, String str) throws IOException {
        zzD(i, 2);
        zzfx(str);
    }

    public void zzb(int i, boolean z) throws IOException {
        zzD(i, 0);
        zzaw(z);
    }

    public void zzb(zzrx zzrx) throws IOException {
        zzrx.zza(this);
    }

    public void zzc(int i, long j) throws IOException {
        zzD(i, 0);
        zzX(j);
    }

    public void zzc(zzrx zzrx) throws IOException {
        zzlw(zzrx.zzDy());
        zzrx.zza(this);
    }

    public void zzc(byte[] bArr, int i, int i2) throws IOException {
        if (this.zzbcc.remaining() < i2) {
            throw new zza(this.zzbcc.position(), this.zzbcc.limit());
        }
        this.zzbcc.put(bArr, i, i2);
    }

    public void zzfx(String str) throws IOException {
        try {
            int zzlx = zzlx(str.length());
            if (zzlx != zzlx(str.length() * 3)) {
                zzlw(zzc((CharSequence) str));
                zza((CharSequence) str, this.zzbcc);
                return;
            }
            int position = this.zzbcc.position();
            this.zzbcc.position(position + zzlx);
            zza((CharSequence) str, this.zzbcc);
            int position2 = this.zzbcc.position();
            this.zzbcc.position(position);
            zzlw((position2 - position) - zzlx);
            this.zzbcc.position(position2);
        } catch (BufferOverflowException e) {
            throw new zza(this.zzbcc.position(), this.zzbcc.limit());
        }
    }

    public void zzi(double d) throws IOException {
        zzac(Double.doubleToLongBits(d));
    }

    public void zzi(float f) throws IOException {
        zzly(Float.floatToIntBits(f));
    }

    public void zzlq(int i) throws IOException {
        if (i < 0) {
            zzaa((long) i);
        } else {
            zzlw(i);
        }
    }

    public void zzlr(int i) throws IOException {
        zzlw(zzlz(i));
    }

    public void zzlu(int i) throws IOException {
        zzb((byte) i);
    }

    public void zzlw(int i) throws IOException {
        while ((i & -128) != 0) {
            zzlu((i & 127) | 128);
            i >>>= 7;
        }
        zzlu(i);
    }

    public void zzly(int i) throws IOException {
        zzlu(i & 255);
        zzlu((i >> 8) & 255);
        zzlu((i >> 16) & 255);
        zzlu((i >> 24) & 255);
    }

    public void zzz(int i, int i2) throws IOException {
        zzD(i, 0);
        zzlq(i2);
    }
}
