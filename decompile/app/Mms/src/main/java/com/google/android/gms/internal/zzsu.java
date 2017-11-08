package com.google.android.gms.internal;

import java.io.IOException;
import java.util.Arrays;

/* compiled from: Unknown */
public abstract class zzsu {
    protected volatile int zzbuu = -1;

    public static final <T extends zzsu> T mergeFrom(T msg, byte[] data) throws zzst {
        return mergeFrom(msg, data, 0, data.length);
    }

    public static final <T extends zzsu> T mergeFrom(T msg, byte[] data, int off, int len) throws zzst {
        try {
            zzsm zza = zzsm.zza(data, off, len);
            msg.mergeFrom(zza);
            zza.zzmn(0);
            return msg;
        } catch (zzst e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).");
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static final boolean messageNanoEquals(zzsu a, zzsu b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.getClass() != b.getClass()) {
            return false;
        }
        int serializedSize = a.getSerializedSize();
        if (b.getSerializedSize() != serializedSize) {
            return false;
        }
        byte[] bArr = new byte[serializedSize];
        byte[] bArr2 = new byte[serializedSize];
        toByteArray(a, bArr, 0, serializedSize);
        toByteArray(b, bArr2, 0, serializedSize);
        return Arrays.equals(bArr, bArr2);
    }

    public static final void toByteArray(zzsu msg, byte[] data, int offset, int length) {
        try {
            zzsn zzb = zzsn.zzb(data, offset, length);
            msg.writeTo(zzb);
            zzb.zzJo();
        } catch (Throwable e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
        }
    }

    public static final byte[] toByteArray(zzsu msg) {
        byte[] bArr = new byte[msg.getSerializedSize()];
        toByteArray(msg, bArr, 0, bArr.length);
        return bArr;
    }

    public zzsu clone() throws CloneNotSupportedException {
        return (zzsu) super.clone();
    }

    public int getCachedSize() {
        if (this.zzbuu < 0) {
            getSerializedSize();
        }
        return this.zzbuu;
    }

    public int getSerializedSize() {
        int zzz = zzz();
        this.zzbuu = zzz;
        return zzz;
    }

    public abstract zzsu mergeFrom(zzsm zzsm) throws IOException;

    public String toString() {
        return zzsv.zzf(this);
    }

    public void writeTo(zzsn output) throws IOException {
    }

    protected int zzz() {
        return 0;
    }
}
