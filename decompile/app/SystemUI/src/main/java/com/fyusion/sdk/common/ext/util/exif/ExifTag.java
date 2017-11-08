package com.fyusion.sdk.common.ext.util.exif;

import fyusion.vislib.BuildConfig;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Locale;

/* compiled from: Unknown */
public class ExifTag {
    private static Charset a = Charset.forName("US-ASCII");
    private static final int[] b = new int[11];
    private static final SimpleDateFormat j = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    private final short c;
    private final short d;
    private boolean e;
    private int f;
    private int g;
    private Object h = null;
    private int i;

    static {
        b[1] = 1;
        b[2] = 1;
        b[3] = 2;
        b[4] = 4;
        b[5] = 8;
        b[7] = 1;
        b[9] = 4;
        b[10] = 8;
    }

    ExifTag(short s, short s2, int i, int i2, boolean z) {
        this.c = (short) s;
        this.d = (short) s2;
        this.f = i;
        this.e = z;
        this.g = i2;
    }

    private static String a(short s) {
        switch (s) {
            case (short) 1:
                return "UNSIGNED_BYTE";
            case (short) 2:
                return "ASCII";
            case (short) 3:
                return "UNSIGNED_SHORT";
            case (short) 4:
                return "UNSIGNED_LONG";
            case (short) 5:
                return "UNSIGNED_RATIONAL";
            case (short) 7:
                return "UNDEFINED";
            case (short) 9:
                return "LONG";
            case (short) 10:
                return "RATIONAL";
            default:
                return BuildConfig.FLAVOR;
        }
    }

    private boolean a(int[] iArr) {
        for (int i : iArr) {
            if (i > 65535 || i < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean a(long[] jArr) {
        int length = jArr.length;
        int i = 0;
        while (i < length) {
            long j = jArr[i];
            if (!(j < 0)) {
                if (j <= 4294967295L) {
                    i++;
                }
            }
            return true;
        }
        return false;
    }

    private boolean a(Rational[] rationalArr) {
        int length = rationalArr.length;
        int i = 0;
        while (i < length) {
            Rational rational = rationalArr[i];
            if (!(rational.getNumerator() < 0)) {
                if (!(rational.getDenominator() < 0)) {
                    if (!(rational.getNumerator() > 4294967295L)) {
                        if (rational.getDenominator() <= 4294967295L) {
                            i++;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean b(int[] iArr) {
        for (int i : iArr) {
            if (i < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean b(Rational[] rationalArr) {
        int length = rationalArr.length;
        int i = 0;
        while (i < length) {
            Rational rational = rationalArr[i];
            if (!(rational.getNumerator() < -2147483648L)) {
                if (!(rational.getDenominator() < -2147483648L)) {
                    if (!(rational.getNumerator() > 2147483647L)) {
                        if (rational.getDenominator() <= 2147483647L) {
                            i++;
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean f(int i) {
        return this.e && this.f != i;
    }

    public static int getElementSize(short s) {
        return b[s];
    }

    public static boolean isValidIfd(int i) {
        return i == 0 || i == 1 || i == 2 || i == 3 || i == 4;
    }

    public static boolean isValidType(short s) {
        return s == (short) 1 || s == (short) 2 || s == (short) 3 || s == (short) 4 || s == (short) 5 || s == (short) 7 || s == (short) 9 || s == (short) 10;
    }

    protected void a(int i) {
        this.g = i;
    }

    protected void a(boolean z) {
        this.e = z;
    }

    protected void a(byte[] bArr) {
        a(bArr, 0, bArr.length);
    }

    protected void a(byte[] bArr, int i, int i2) {
        if (this.d == (short) 7 || this.d == (short) 1) {
            Object obj = this.h;
            if (i2 > this.f) {
                i2 = this.f;
            }
            System.arraycopy(obj, 0, bArr, i, i2);
            return;
        }
        throw new IllegalArgumentException("Cannot get BYTE value from " + a(this.d));
    }

    protected byte[] a() {
        return (byte[]) this.h;
    }

    protected int b() {
        return this.i;
    }

    protected void b(int i) {
        this.f = i;
    }

    protected long c(int i) {
        if (this.h instanceof long[]) {
            return ((long[]) this.h)[i];
        }
        if (this.h instanceof byte[]) {
            return (long) ((byte[]) this.h)[i];
        }
        throw new IllegalArgumentException("Cannot get integer value from " + a(this.d));
    }

    protected boolean c() {
        return this.e;
    }

    protected Rational d(int i) {
        if (this.d == (short) 10 || this.d == (short) 5) {
            return ((Rational[]) this.h)[i];
        }
        throw new IllegalArgumentException("Cannot get RATIONAL value from " + a(this.d));
    }

    protected void e(int i) {
        this.i = i;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null || !(obj instanceof ExifTag)) {
            return false;
        }
        ExifTag exifTag = (ExifTag) obj;
        if (exifTag.c != this.c || exifTag.f != this.f || exifTag.d != this.d) {
            return false;
        }
        if (this.h != null) {
            return exifTag.h != null ? !(this.h instanceof long[]) ? !(this.h instanceof Rational[]) ? !(this.h instanceof byte[]) ? this.h.equals(exifTag.h) : exifTag.h instanceof byte[] ? Arrays.equals((byte[]) this.h, (byte[]) exifTag.h) : false : exifTag.h instanceof Rational[] ? Arrays.equals((Rational[]) this.h, (Rational[]) exifTag.h) : false : exifTag.h instanceof long[] ? Arrays.equals((long[]) this.h, (long[]) exifTag.h) : false : false;
        } else {
            if (exifTag.h == null) {
                z = true;
            }
            return z;
        }
    }

    public String forceGetValueAsString() {
        if (this.h == null) {
            return BuildConfig.FLAVOR;
        }
        if (this.h instanceof byte[]) {
            return this.d != (short) 2 ? Arrays.toString((byte[]) this.h) : new String((byte[]) this.h, a);
        } else {
            if (this.h instanceof long[]) {
                return ((long[]) ((long[]) this.h)).length != 1 ? Arrays.toString((long[]) this.h) : String.valueOf(((long[]) this.h)[0]);
            } else {
                if (!(this.h instanceof Object[])) {
                    return this.h.toString();
                }
                if (((Object[]) this.h).length != 1) {
                    return Arrays.toString((Object[]) this.h);
                }
                Object obj = ((Object[]) this.h)[0];
                return obj != null ? obj.toString() : BuildConfig.FLAVOR;
            }
        }
    }

    public int getComponentCount() {
        return this.f;
    }

    public int getDataSize() {
        return getComponentCount() * getElementSize(getDataType());
    }

    public short getDataType() {
        return this.d;
    }

    public int getIfd() {
        return this.g;
    }

    public short getTagId() {
        return this.c;
    }

    public Object getValue() {
        return this.h;
    }

    public int getValueAsInt(int i) {
        int[] valueAsInts = getValueAsInts();
        return (valueAsInts != null && valueAsInts.length >= 1) ? valueAsInts[0] : i;
    }

    public int[] getValueAsInts() {
        if (this.h == null || !(this.h instanceof long[])) {
            return null;
        }
        long[] jArr = (long[]) this.h;
        int[] iArr = new int[jArr.length];
        for (int i = 0; i < jArr.length; i++) {
            iArr[i] = (int) jArr[i];
        }
        return iArr;
    }

    public String getValueAsString() {
        return this.h != null ? !(this.h instanceof String) ? !(this.h instanceof byte[]) ? null : new String((byte[]) this.h, a) : (String) this.h : null;
    }

    public boolean hasValue() {
        return this.h != null;
    }

    public boolean setValue(byte b) {
        return setValue(new byte[]{(byte) b});
    }

    public boolean setValue(int i) {
        return setValue(new int[]{i});
    }

    public boolean setValue(long j) {
        return setValue(new long[]{j});
    }

    public boolean setValue(Rational rational) {
        return setValue(new Rational[]{rational});
    }

    public boolean setValue(Object obj) {
        int i = 0;
        if (obj == null) {
            return false;
        }
        if (obj instanceof Short) {
            return setValue(((Short) obj).shortValue() & 65535);
        }
        if (obj instanceof String) {
            return setValue((String) obj);
        }
        if (obj instanceof int[]) {
            return setValue((int[]) obj);
        }
        if (obj instanceof long[]) {
            return setValue((long[]) obj);
        }
        if (obj instanceof Rational) {
            return setValue((Rational) obj);
        }
        if (obj instanceof Rational[]) {
            return setValue((Rational[]) obj);
        }
        if (obj instanceof byte[]) {
            return setValue((byte[]) obj);
        }
        if (obj instanceof Integer) {
            return setValue(((Integer) obj).intValue());
        }
        if (obj instanceof Long) {
            return setValue(((Long) obj).longValue());
        }
        if (obj instanceof Byte) {
            return setValue(((Byte) obj).byteValue());
        }
        int[] iArr;
        int i2;
        if (obj instanceof Short[]) {
            Short[] shArr = (Short[]) obj;
            iArr = new int[shArr.length];
            for (i2 = 0; i2 < shArr.length; i2++) {
                iArr[i2] = shArr[i2] != null ? shArr[i2].shortValue() & 65535 : 0;
            }
            return setValue(iArr);
        } else if (obj instanceof Integer[]) {
            Integer[] numArr = (Integer[]) obj;
            iArr = new int[numArr.length];
            for (i2 = 0; i2 < numArr.length; i2++) {
                iArr[i2] = numArr[i2] != null ? numArr[i2].intValue() : 0;
            }
            return setValue(iArr);
        } else if (obj instanceof Long[]) {
            Long[] lArr = (Long[]) obj;
            long[] jArr = new long[lArr.length];
            while (i < lArr.length) {
                jArr[i] = lArr[i] != null ? lArr[i].longValue() : 0;
                i++;
            }
            return setValue(jArr);
        } else if (!(obj instanceof Byte[])) {
            return false;
        } else {
            Byte[] bArr = (Byte[]) obj;
            byte[] bArr2 = new byte[bArr.length];
            for (i2 = 0; i2 < bArr.length; i2++) {
                bArr2[i2] = (byte) (bArr[i2] != null ? bArr[i2].byteValue() : 0);
            }
            return setValue(bArr2);
        }
    }

    public boolean setValue(String str) {
        if (this.d != (short) 2 && this.d != (short) 7) {
            return false;
        }
        Object bytes = str.getBytes(a);
        if (bytes.length <= 0) {
            if (this.d == (short) 2 && this.f == 1) {
                bytes = new byte[]{null};
            }
        } else if (!(bytes[bytes.length - 1] == (byte) 0 || this.d == (short) 7)) {
            bytes = Arrays.copyOf(bytes, bytes.length + 1);
        }
        int length = bytes.length;
        if (f(length)) {
            return false;
        }
        this.f = length;
        this.h = bytes;
        return true;
    }

    public boolean setValue(byte[] bArr) {
        return setValue(bArr, 0, bArr.length);
    }

    public boolean setValue(byte[] bArr, int i, int i2) {
        if (f(i2)) {
            return false;
        }
        if (this.d != (short) 1 && this.d != (short) 7) {
            return false;
        }
        this.h = new byte[i2];
        System.arraycopy(bArr, i, this.h, 0, i2);
        this.f = i2;
        return true;
    }

    public boolean setValue(int[] iArr) {
        int i = 0;
        if (f(iArr.length)) {
            return false;
        }
        if (this.d != (short) 3 && this.d != (short) 9 && this.d != (short) 4) {
            return false;
        }
        if (this.d == (short) 3 && a(iArr)) {
            return false;
        }
        if (this.d == (short) 4 && b(iArr)) {
            return false;
        }
        Object obj = new long[iArr.length];
        while (i < iArr.length) {
            obj[i] = (long) iArr[i];
            i++;
        }
        this.h = obj;
        this.f = iArr.length;
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean setValue(long[] jArr) {
        if (f(jArr.length) || this.d != (short) 4 || a(jArr)) {
            return false;
        }
        this.h = jArr;
        this.f = jArr.length;
        return true;
    }

    public boolean setValue(Rational[] rationalArr) {
        if (f(rationalArr.length)) {
            return false;
        }
        if (this.d != (short) 5 && this.d != (short) 10) {
            return false;
        }
        if (this.d == (short) 5 && a(rationalArr)) {
            return false;
        }
        if (this.d == (short) 10 && b(rationalArr)) {
            return false;
        }
        this.h = rationalArr;
        this.f = rationalArr.length;
        return true;
    }

    public String toString() {
        return String.format(Locale.US, "tag id: %04X%n", new Object[]{Short.valueOf(this.c)}) + "ifd id: " + this.g + "\ntype: " + a(this.d) + "\ncount: " + this.f + "\noffset: " + this.i + "\nvalue: " + forceGetValueAsString() + "\n";
    }
}
