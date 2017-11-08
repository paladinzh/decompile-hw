package com.google.android.gms.internal;

import java.util.Arrays;

/* compiled from: Unknown */
public final class zzss {
    public static final Object zzbut = new Object();

    public static boolean equals(float[] field1, float[] field2) {
        boolean z = false;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (field2 == null || field2.length == 0) {
            z = true;
        }
        return z;
    }

    public static boolean equals(int[] field1, int[] field2) {
        boolean z = false;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (field2 == null || field2.length == 0) {
            z = true;
        }
        return z;
    }

    public static boolean equals(long[] field1, long[] field2) {
        boolean z = false;
        if (field1 != null && field1.length != 0) {
            return Arrays.equals(field1, field2);
        }
        if (field2 == null || field2.length == 0) {
            z = true;
        }
        return z;
    }

    public static boolean equals(Object[] field1, Object[] field2) {
        if (field1 != null) {
            int length = field1.length;
        } else {
            boolean z = false;
        }
        int length2 = field2 != null ? field2.length : 0;
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 < length && field1[i2] == null) {
                i2++;
            } else {
                int i3 = i;
                while (i3 < length2 && field2[i3] == null) {
                    i3++;
                }
                boolean z2 = i2 >= length;
                boolean z3 = i3 >= length2;
                if (z2 && z3) {
                    return true;
                }
                if (z2 != z3 || !field1[i2].equals(field2[i3])) {
                    return false;
                }
                i = i3 + 1;
                i2++;
            }
        }
    }

    public static int hashCode(float[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(int[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(long[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(Object[] field) {
        int i = 0;
        int length = field != null ? field.length : 0;
        for (int i2 = 0; i2 < length; i2++) {
            Object obj = field[i2];
            if (obj != null) {
                i = (i * 31) + obj.hashCode();
            }
        }
        return i;
    }

    public static int zza(byte[][] bArr) {
        int i = 0;
        int length = bArr != null ? bArr.length : 0;
        for (int i2 = 0; i2 < length; i2++) {
            byte[] bArr2 = bArr[i2];
            if (bArr2 != null) {
                i = (i * 31) + Arrays.hashCode(bArr2);
            }
        }
        return i;
    }

    public static void zza(zzso zzso, zzso zzso2) {
        if (zzso.zzbuj != null) {
            zzso2.zzbuj = zzso.zzbuj.zzJq();
        }
    }

    public static boolean zza(byte[][] bArr, byte[][] bArr2) {
        if (bArr != null) {
            int length = bArr.length;
        } else {
            boolean z = false;
        }
        int length2 = bArr2 != null ? bArr2.length : 0;
        int i = 0;
        int i2 = 0;
        while (true) {
            if (i2 < length && bArr[i2] == null) {
                i2++;
            } else {
                int i3 = i;
                while (i3 < length2 && bArr2[i3] == null) {
                    i3++;
                }
                boolean z2 = i2 >= length;
                boolean z3 = i3 >= length2;
                if (z2 && z3) {
                    return true;
                }
                if (z2 != z3 || !Arrays.equals(bArr[i2], bArr2[i3])) {
                    return false;
                }
                i = i3 + 1;
                i2++;
            }
        }
    }
}
