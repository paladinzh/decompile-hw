package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public final class zzsa {
    public static final int[] zzbcq = new int[0];
    public static final long[] zzbcr = new long[0];
    public static final float[] zzbcs = new float[0];
    public static final double[] zzbct = new double[0];
    public static final boolean[] zzbcu = new boolean[0];
    public static final String[] zzbcv = new String[0];
    public static final byte[][] zzbcw = new byte[0][];
    public static final byte[] zzbcx = new byte[0];

    static int zzE(int i, int i2) {
        return (i << 3) | i2;
    }

    public static final int zzb(zzrp zzrp, int i) throws IOException {
        int i2 = 1;
        int position = zzrp.getPosition();
        zzrp.zzlj(i);
        while (zzrp.zzCV() == i) {
            zzrp.zzlj(i);
            i2++;
        }
        zzrp.zzln(position);
        return i2;
    }

    static int zzlD(int i) {
        return i & 7;
    }

    public static int zzlE(int i) {
        return i >>> 3;
    }
}
