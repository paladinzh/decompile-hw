package com.google.android.gms.internal;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ReadOnlyBufferException;

/* compiled from: Unknown */
public final class zzsn {
    private final ByteBuffer zzbui;

    /* compiled from: Unknown */
    public static class zza extends IOException {
        zza(int i, int i2) {
            super("CodedOutputStream was writing to a flat byte array and ran out of space (pos " + i + " limit " + i2 + ").");
        }
    }

    private zzsn(ByteBuffer byteBuffer) {
        this.zzbui = byteBuffer;
        this.zzbui.order(ByteOrder.LITTLE_ENDIAN);
    }

    private zzsn(byte[] bArr, int i, int i2) {
        this(ByteBuffer.wrap(bArr, i, i2));
    }

    public static int zzC(int i, int i2) {
        return zzmA(i) + zzmx(i2);
    }

    public static int zzD(int i, int i2) {
        return zzmA(i) + zzmy(i2);
    }

    public static zzsn zzE(byte[] bArr) {
        return zzb(bArr, 0, bArr.length);
    }

    public static int zzG(byte[] bArr) {
        return zzmC(bArr.length) + bArr.length;
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
                        if (55296 <= charAt2 && charAt2 <= 57343) {
                            if (i3 + 1 == charSequence.length() || !Character.isSurrogatePair(charAt2, charSequence.charAt(i3 + 1))) {
                                throw new IllegalArgumentException("Unpaired surrogate at index " + i3);
                            }
                        }
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

    public static int zzaA(boolean z) {
        return 1;
    }

    public static int zzar(long j) {
        return zzav(j);
    }

    public static int zzas(long j) {
        return zzav(j);
    }

    public static int zzat(long j) {
        return zzav(zzax(j));
    }

    public static int zzav(long j) {
        return (-128 & j) == 0 ? 1 : (-16384 & j) == 0 ? 2 : (-2097152 & j) == 0 ? 3 : (-268435456 & j) == 0 ? 4 : (-34359738368L & j) == 0 ? 5 : (-4398046511104L & j) == 0 ? 6 : (-562949953421312L & j) == 0 ? 7 : (-72057594037927936L & j) == 0 ? 8 : (Long.MIN_VALUE & j) == 0 ? 9 : 10;
    }

    public static long zzax(long j) {
        return (j << 1) ^ (j >> 63);
    }

    public static int zzb(int i, double d) {
        return zzmA(i) + zzl(d);
    }

    public static int zzb(int i, zzsu zzsu) {
        return (zzmA(i) * 2) + zzd(zzsu);
    }

    public static int zzb(int i, byte[] bArr) {
        return zzmA(i) + zzG(bArr);
    }

    public static zzsn zzb(byte[] bArr, int i, int i2) {
        return new zzsn(bArr, i, i2);
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
        return zzmA(i) + zzk(f);
    }

    public static int zzc(int i, zzsu zzsu) {
        return zzmA(i) + zze(zzsu);
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
        return zzmA(i) + zzas(j);
    }

    public static int zzd(zzsu zzsu) {
        return zzsu.getSerializedSize();
    }

    public static int zze(int i, long j) {
        return zzmA(i) + zzat(j);
    }

    public static int zze(zzsu zzsu) {
        int serializedSize = zzsu.getSerializedSize();
        return serializedSize + zzmC(serializedSize);
    }

    public static int zzf(int i, boolean z) {
        return zzmA(i) + zzaA(z);
    }

    public static int zzgO(String str) {
        int zzc = zzc((CharSequence) str);
        return zzc + zzmC(zzc);
    }

    public static int zzk(float f) {
        return 4;
    }

    public static int zzl(double d) {
        return 8;
    }

    public static int zzmA(int i) {
        return zzmC(zzsx.zzF(i, 0));
    }

    public static int zzmC(int i) {
        return (i & -128) != 0 ? (i & -16384) != 0 ? (-2097152 & i) != 0 ? (-268435456 & i) != 0 ? 5 : 4 : 3 : 2 : 1;
    }

    public static int zzmE(int i) {
        return (i << 1) ^ (i >> 31);
    }

    public static int zzmx(int i) {
        return i < 0 ? 10 : zzmC(i);
    }

    public static int zzmy(int i) {
        return zzmC(zzmE(i));
    }

    public static int zzo(int i, String str) {
        return zzmA(i) + zzgO(str);
    }

    public void zzA(int i, int i2) throws IOException {
        zzE(i, 0);
        zzmv(i2);
    }

    public void zzB(int i, int i2) throws IOException {
        zzE(i, 0);
        zzmw(i2);
    }

    public void zzE(int i, int i2) throws IOException {
        zzmB(zzsx.zzF(i, i2));
    }

    public void zzF(byte[] bArr) throws IOException {
        zzmB(bArr.length);
        zzH(bArr);
    }

    public void zzH(byte[] bArr) throws IOException {
        zzc(bArr, 0, bArr.length);
    }

    public int zzJn() {
        return this.zzbui.remaining();
    }

    public void zzJo() {
        if (zzJn() != 0) {
            throw new IllegalStateException("Did not write as much data as expected.");
        }
    }

    public void zza(int i, double d) throws IOException {
        zzE(i, 1);
        zzk(d);
    }

    public void zza(int i, long j) throws IOException {
        zzE(i, 0);
        zzao(j);
    }

    public void zza(int i, zzsu zzsu) throws IOException {
        zzE(i, 2);
        zzc(zzsu);
    }

    public void zza(int i, byte[] bArr) throws IOException {
        zzE(i, 2);
        zzF(bArr);
    }

    public void zzao(long j) throws IOException {
        zzau(j);
    }

    public void zzap(long j) throws IOException {
        zzau(j);
    }

    public void zzaq(long j) throws IOException {
        zzau(zzax(j));
    }

    public void zzau(long j) throws IOException {
        while ((-128 & j) != 0) {
            zzmz((((int) j) & 127) | 128);
            j >>>= 7;
        }
        zzmz((int) j);
    }

    public void zzaw(long j) throws IOException {
        if (this.zzbui.remaining() >= 8) {
            this.zzbui.putLong(j);
            return;
        }
        throw new zza(this.zzbui.position(), this.zzbui.limit());
    }

    public void zzaz(boolean z) throws IOException {
        int i = 0;
        if (z) {
            i = 1;
        }
        zzmz(i);
    }

    public void zzb(byte b) throws IOException {
        if (this.zzbui.hasRemaining()) {
            this.zzbui.put(b);
            return;
        }
        throw new zza(this.zzbui.position(), this.zzbui.limit());
    }

    public void zzb(int i, float f) throws IOException {
        zzE(i, 5);
        zzj(f);
    }

    public void zzb(int i, long j) throws IOException {
        zzE(i, 0);
        zzap(j);
    }

    public void zzb(zzsu zzsu) throws IOException {
        zzsu.writeTo(this);
    }

    public void zzc(int i, long j) throws IOException {
        zzE(i, 0);
        zzaq(j);
    }

    public void zzc(zzsu zzsu) throws IOException {
        zzmB(zzsu.getCachedSize());
        zzsu.writeTo(this);
    }

    public void zzc(byte[] bArr, int i, int i2) throws IOException {
        if (this.zzbui.remaining() < i2) {
            throw new zza(this.zzbui.position(), this.zzbui.limit());
        }
        this.zzbui.put(bArr, i, i2);
    }

    public void zze(int i, boolean z) throws IOException {
        zzE(i, 0);
        zzaz(z);
    }

    public void zzgN(String str) throws IOException {
        try {
            int zzmC = zzmC(str.length());
            if (zzmC != zzmC(str.length() * 3)) {
                zzmB(zzc((CharSequence) str));
                zza((CharSequence) str, this.zzbui);
                return;
            }
            int position = this.zzbui.position();
            if (this.zzbui.remaining() >= zzmC) {
                this.zzbui.position(position + zzmC);
                zza((CharSequence) str, this.zzbui);
                int position2 = this.zzbui.position();
                this.zzbui.position(position);
                zzmB((position2 - position) - zzmC);
                this.zzbui.position(position2);
                return;
            }
            throw new zza(zzmC + position, this.zzbui.limit());
        } catch (Throwable e) {
            zza zza = new zza(this.zzbui.position(), this.zzbui.limit());
            zza.initCause(e);
            throw zza;
        }
    }

    public void zzj(float f) throws IOException {
        zzmD(Float.floatToIntBits(f));
    }

    public void zzk(double d) throws IOException {
        zzaw(Double.doubleToLongBits(d));
    }

    public void zzmB(int i) throws IOException {
        while ((i & -128) != 0) {
            zzmz((i & 127) | 128);
            i >>>= 7;
        }
        zzmz(i);
    }

    public void zzmD(int i) throws IOException {
        if (this.zzbui.remaining() >= 4) {
            this.zzbui.putInt(i);
            return;
        }
        throw new zza(this.zzbui.position(), this.zzbui.limit());
    }

    public void zzmv(int i) throws IOException {
        if (i < 0) {
            zzau((long) i);
        } else {
            zzmB(i);
        }
    }

    public void zzmw(int i) throws IOException {
        zzmB(zzmE(i));
    }

    public void zzmz(int i) throws IOException {
        zzb((byte) i);
    }

    public void zzn(int i, String str) throws IOException {
        zzE(i, 2);
        zzgN(str);
    }
}
