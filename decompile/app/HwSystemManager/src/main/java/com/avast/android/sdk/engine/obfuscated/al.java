package com.avast.android.sdk.engine.obfuscated;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/* compiled from: Unknown */
public class al {

    /* compiled from: Unknown */
    private enum a {
        INT(Integer.TYPE, 4),
        DOUBLE(Double.TYPE, 8),
        SHORT(Short.TYPE, 2),
        LONG(Long.TYPE, 8),
        FLOAT(Float.TYPE, 4),
        CHAR(Character.TYPE, 1),
        BOOLEAN(Boolean.TYPE, 1),
        BYTE(Byte.TYPE, 1);
        
        private static final Map<Class, a> i = null;
        private final Class j;
        private final int k;

        static {
            i = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                i.put(aVar.a(), aVar);
            }
        }

        private a(Class cls, int i) {
            this.j = cls;
            this.k = i;
        }

        private final Class a() {
            return this.j;
        }

        private final int b() {
            return this.k;
        }

        private static final a b(Class cls) {
            return (a) i.get(cls);
        }
    }

    public static Object a(byte[] bArr, ByteOrder byteOrder, Class<?> cls, int i) {
        return b(bArr, byteOrder, cls, i);
    }

    public static String a(byte[] bArr) {
        if (bArr == null) {
            return null;
        }
        String str = "";
        for (byte b : bArr) {
            str = str + Integer.toString((b & 255) + 256, 16).substring(1);
        }
        return str;
    }

    public static byte[] a(String str) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream;
        Throwable th;
        if (str == null) {
            return null;
        }
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                int length = str.length();
                for (int i = 0; i < length; i += 2) {
                    if (i + 1 >= length) {
                        byteArrayOutputStream.write((byte) (Character.digit(str.charAt(i), 16) << 4));
                    } else {
                        byteArrayOutputStream.write((byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16)));
                    }
                }
                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return toByteArray;
            } catch (Throwable th2) {
                th = th2;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th;
        }
    }

    public static byte[] a(byte[] bArr, String str) throws IOException {
        Throwable th;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byteArrayOutputStream.write(bArr);
                byteArrayOutputStream.write(a(str));
                byte[] toByteArray = byteArrayOutputStream.toByteArray();
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                return toByteArray;
            } catch (Throwable th2) {
                th = th2;
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            byteArrayOutputStream = null;
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            throw th;
        }
    }

    private static Object b(byte[] bArr, ByteOrder byteOrder, Class<?> cls, int i) {
        if (bArr == null || cls == null) {
            throw new NullPointerException("Provided byte array and/or primitive type class is null");
        } else if (bArr.length >= i + 1) {
            a a = a.b(cls);
            if (a == null) {
                throw new IllegalArgumentException("Class is not a primitive type class");
            } else if (bArr.length < a.b()) {
                throw new IllegalArgumentException("Provided byte array (" + bArr.length + ") is smaller than the primitive type requires (" + a.b() + ")");
            } else if (bArr.length < a.b() + i) {
                throw new IllegalArgumentException("Can't parse requested primitive type from given position");
            } else if (a.BYTE.equals(a)) {
                return Byte.valueOf(bArr[i]);
            } else {
                if (a.BOOLEAN.equals(a)) {
                    return bArr[i] != (byte) 0 ? Boolean.valueOf(true) : Boolean.valueOf(false);
                } else {
                    ByteBuffer wrap = ByteBuffer.wrap(bArr, i, a.b());
                    wrap.order(byteOrder);
                    switch (am.a[a.ordinal()]) {
                        case 1:
                            return Integer.valueOf(wrap.getInt());
                        case 2:
                            return Double.valueOf(wrap.getDouble());
                        case 3:
                            return Short.valueOf(wrap.getShort());
                        case 4:
                            return Long.valueOf(wrap.getLong());
                        case 5:
                            return Float.valueOf(wrap.getFloat());
                        case 6:
                            return Character.valueOf(wrap.getChar());
                        default:
                            throw new IllegalStateException("Terrible failure happened, primitive type recognized, but wrong value set somehow, this should never happen :>");
                    }
                }
            }
        } else {
            throw new IndexOutOfBoundsException("Byte array length is " + bArr.length + ", requested index " + i);
        }
    }
}
