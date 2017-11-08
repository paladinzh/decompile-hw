package tmsdkobf;

import java.nio.ByteBuffer;

public final class ft {
    private static final byte[] mA;
    private static final byte[] mB;

    public static boolean a(boolean z, boolean z2) {
        return z == z2;
    }

    public static boolean a(short s, short s2) {
        return s == s2;
    }

    public static boolean equals(int i, int i2) {
        return i == i2;
    }

    public static boolean a(long j, long j2) {
        return j == j2;
    }

    public static boolean equals(float f, float f2) {
        return f == f2;
    }

    public static boolean equals(Object obj, Object obj2) {
        return obj.equals(obj2);
    }

    public static <T extends Comparable<T>> int a(T t, T t2) {
        return t.compareTo(t2);
    }

    public static byte[] a(ByteBuffer byteBuffer) {
        Object obj = new byte[byteBuffer.position()];
        System.arraycopy(byteBuffer.array(), 0, obj, 0, obj.length);
        return obj;
    }

    static {
        int i = 0;
        byte[] bArr = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70};
        byte[] bArr2 = new byte[256];
        byte[] bArr3 = new byte[256];
        while (i < 256) {
            bArr2[i] = (byte) bArr[i >>> 4];
            bArr3[i] = (byte) bArr[i & 15];
            i++;
        }
        mA = bArr2;
        mB = bArr3;
    }
}
