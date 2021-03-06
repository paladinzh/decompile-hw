package java.lang;

import java.util.Locale;
import sun.misc.VM;
import sun.net.www.protocol.http.AuthenticationInfo;
import sun.util.locale.LanguageTag;

public final class Integer extends Number implements Comparable<Integer> {
    static final /* synthetic */ boolean -assertionsDisabled;
    public static final int BYTES = 4;
    static final char[] DigitOnes = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    static final char[] DigitTens = new char[]{'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1', '1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3', '3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5', '5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6', '6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8', '8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'};
    public static final int MAX_VALUE = Integer.MAX_VALUE;
    public static final int MIN_VALUE = Integer.MIN_VALUE;
    public static final int SIZE = 32;
    private static final String[] SMALL_NEG_VALUES = new String[100];
    private static final String[] SMALL_NONNEG_VALUES = new String[100];
    public static final Class<Integer> TYPE = int[].class.getComponentType();
    static final char[] digits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', AuthenticationInfo.PROXY_AUTHENTICATION, 'q', 'r', AuthenticationInfo.SERVER_AUTHENTICATION, 't', 'u', 'v', 'w', Locale.PRIVATE_USE_EXTENSION, 'y', 'z'};
    private static final long serialVersionUID = 1360826667806852920L;
    static final int[] sizeTable = new int[]{9, 99, 999, 9999, 99999, 999999, 9999999, 99999999, 999999999, Integer.MAX_VALUE};
    private final int value;

    private static class IntegerCache {
        static final Integer[] cache = new Integer[((high + 128) + 1)];
        static final int high;
        static final int low = -128;

        static {
            int h = 127;
            String integerCacheHighPropValue = VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                h = Math.min(Math.max(Integer.parseInt(integerCacheHighPropValue), 127), 2147483518);
            }
            high = h;
            int j = low;
            int k = 0;
            while (k < cache.length) {
                int j2 = j + 1;
                cache[k] = new Integer(j);
                k++;
                j = j2;
            }
        }

        private IntegerCache() {
        }
    }

    static {
        boolean z;
        if (Integer.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public static String toString(int i, int radix) {
        boolean negative = false;
        if (radix < 2 || radix > 36) {
            radix = 10;
        }
        if (radix == 10) {
            return toString(i);
        }
        int charPos;
        int charPos2;
        char[] buf = new char[33];
        if (i < 0) {
            negative = true;
        }
        if (negative) {
            charPos = 32;
        } else {
            i = -i;
            charPos = 32;
        }
        while (i <= (-radix)) {
            int q = i / radix;
            charPos2 = charPos - 1;
            buf[charPos] = digits[(radix * q) - i];
            i = q;
            charPos = charPos2;
        }
        buf[charPos] = digits[-i];
        if (negative) {
            charPos2 = charPos - 1;
            buf[charPos2] = '-';
        } else {
            charPos2 = charPos;
        }
        return new String(buf, charPos2, 33 - charPos2);
    }

    public static String toHexString(int i) {
        return toUnsignedString(i, 4);
    }

    public static String toOctalString(int i) {
        return toUnsignedString(i, 3);
    }

    public static String toBinaryString(int i) {
        return toUnsignedString(i, 1);
    }

    private static String toUnsignedString(int i, int shift) {
        char[] buf = new char[32];
        int charPos = 32;
        int mask = (1 << shift) - 1;
        do {
            charPos--;
            buf[charPos] = digits[i & mask];
            i >>>= shift;
        } while (i != 0);
        return new String(buf, charPos, 32 - charPos);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String toString(int i) {
        if (i == Integer.MIN_VALUE) {
            return "-2147483648";
        }
        boolean negative;
        boolean small;
        if (i < 0) {
            negative = true;
        } else {
            negative = false;
        }
        String[] smallValues;
        String str;
        if (!negative) {
            if (i < 100) {
            }
            small = false;
            if (small) {
                int size = negative ? stringSize(-i) + 1 : stringSize(i);
                char[] buf = new char[size];
                getChars(i, size, buf);
                return new String(buf);
            }
            smallValues = negative ? SMALL_NEG_VALUES : SMALL_NONNEG_VALUES;
            if (negative) {
                i = -i;
                if (smallValues[i] == null) {
                    if (i >= 10) {
                        str = new String(new char[]{'-', DigitOnes[i]});
                    } else {
                        str = new String(new char[]{'-', DigitTens[i], DigitOnes[i]});
                    }
                    smallValues[i] = str;
                }
            } else if (smallValues[i] == null) {
                if (i >= 10) {
                    str = new String(new char[]{DigitOnes[i]});
                } else {
                    str = new String(new char[]{DigitTens[i], DigitOnes[i]});
                }
                smallValues[i] = str;
            }
            return smallValues[i];
        }
        small = true;
        if (small) {
            if (negative) {
            }
            char[] buf2 = new char[size];
            getChars(i, size, buf2);
            return new String(buf2);
        }
        if (negative) {
        }
        if (negative) {
            i = -i;
            if (smallValues[i] == null) {
                if (i >= 10) {
                    str = new String(new char[]{'-', DigitTens[i], DigitOnes[i]});
                } else {
                    str = new String(new char[]{'-', DigitOnes[i]});
                }
                smallValues[i] = str;
            }
        } else if (smallValues[i] == null) {
            if (i >= 10) {
                str = new String(new char[]{DigitTens[i], DigitOnes[i]});
            } else {
                str = new String(new char[]{DigitOnes[i]});
            }
            smallValues[i] = str;
        }
        return smallValues[i];
    }

    static void getChars(int i, int index, char[] buf) {
        int charPos = index;
        char sign = '\u0000';
        if (i < 0) {
            sign = '-';
            i = -i;
        }
        while (i >= 65536) {
            int q = i / 100;
            int r = i - (((q << 6) + (q << 5)) + (q << 2));
            i = q;
            charPos--;
            buf[charPos] = DigitOnes[r];
            charPos--;
            buf[charPos] = DigitTens[r];
        }
        do {
            q = (52429 * i) >>> 19;
            charPos--;
            buf[charPos] = digits[i - ((q << 3) + (q << 1))];
            i = q;
        } while (q != 0);
        if (sign != '\u0000') {
            buf[charPos - 1] = sign;
        }
    }

    static int stringSize(int x) {
        int i = 0;
        while (x > sizeTable[i]) {
            i++;
        }
        return i + 1;
    }

    public static int parseInt(String s, int radix) throws NumberFormatException {
        if (s == null) {
            throw new NumberFormatException("null");
        } else if (radix < 2) {
            throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
        } else if (radix > 36) {
            throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
        } else {
            int result = 0;
            boolean negative = false;
            int i = 0;
            int len = s.length();
            int limit = -2147483647;
            if (len > 0) {
                char firstChar = s.charAt(0);
                if (firstChar < '0') {
                    if (firstChar == '-') {
                        negative = true;
                        limit = Integer.MIN_VALUE;
                    } else if (firstChar != '+') {
                        throw NumberFormatException.forInputString(s);
                    }
                    if (len == 1) {
                        throw NumberFormatException.forInputString(s);
                    }
                    i = 1;
                }
                int multmin = limit / radix;
                int i2 = i;
                while (i2 < len) {
                    i = i2 + 1;
                    int digit = Character.digit(s.charAt(i2), radix);
                    if (digit < 0) {
                        throw NumberFormatException.forInputString(s);
                    } else if (result < multmin) {
                        throw NumberFormatException.forInputString(s);
                    } else {
                        result *= radix;
                        if (result < limit + digit) {
                            throw NumberFormatException.forInputString(s);
                        }
                        result -= digit;
                        i2 = i;
                    }
                }
                return negative ? result : -result;
            } else {
                throw NumberFormatException.forInputString(s);
            }
        }
    }

    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s, 10);
    }

    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return valueOf(parseInt(s, radix));
    }

    public static Integer valueOf(String s) throws NumberFormatException {
        return valueOf(parseInt(s, 10));
    }

    public static Integer valueOf(int i) {
        if (!-assertionsDisabled) {
            if ((IntegerCache.high >= 127 ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (i < -128 || i > IntegerCache.high) {
            return new Integer(i);
        }
        return IntegerCache.cache[i + 128];
    }

    public Integer(int value) {
        this.value = value;
    }

    public Integer(String s) throws NumberFormatException {
        this.value = parseInt(s, 10);
    }

    public byte byteValue() {
        return (byte) this.value;
    }

    public short shortValue() {
        return (short) this.value;
    }

    public int intValue() {
        return this.value;
    }

    public long longValue() {
        return (long) this.value;
    }

    public float floatValue() {
        return (float) this.value;
    }

    public double doubleValue() {
        return (double) this.value;
    }

    public String toString() {
        return toString(this.value);
    }

    public int hashCode() {
        return this.value;
    }

    public static int hashCode(int value) {
        return value;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof Integer)) {
            return false;
        }
        if (this.value == ((Integer) obj).intValue()) {
            z = true;
        }
        return z;
    }

    public static Integer getInteger(String nm) {
        return getInteger(nm, null);
    }

    public static Integer getInteger(String nm, int val) {
        Integer result = getInteger(nm, null);
        return result == null ? valueOf(val) : result;
    }

    public static Integer getInteger(String nm, Integer val) {
        String str = null;
        try {
            str = System.getProperty(nm);
        } catch (IllegalArgumentException e) {
        } catch (NullPointerException e2) {
        }
        if (str != null) {
            try {
                return decode(str);
            } catch (NumberFormatException e3) {
            }
        }
        return val;
    }

    public static Integer decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        if (nm.length() == 0) {
            throw new NumberFormatException("Zero length string");
        }
        char firstChar = nm.charAt(0);
        if (firstChar == '-') {
            negative = true;
            index = 1;
        } else if (firstChar == '+') {
            index = 1;
        }
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("#", index)) {
            index++;
            radix = 16;
        } else if (nm.startsWith("0", index) && nm.length() > index + 1) {
            index++;
            radix = 8;
        }
        if (nm.startsWith(LanguageTag.SEP, index) || nm.startsWith("+", index)) {
            throw new NumberFormatException("Sign character in wrong position");
        }
        try {
            Integer result = valueOf(nm.substring(index), radix);
            if (negative) {
                return valueOf(-result.intValue());
            }
            return result;
        } catch (NumberFormatException e) {
            String constant;
            if (negative) {
                constant = LanguageTag.SEP + nm.substring(index);
            } else {
                constant = nm.substring(index);
            }
            return valueOf(constant, radix);
        }
    }

    public int compareTo(Integer anotherInteger) {
        return compare(this.value, anotherInteger.value);
    }

    public static int compare(int x, int y) {
        if (x < y) {
            return -1;
        }
        return x == y ? 0 : 1;
    }

    public static int highestOneBit(int i) {
        i |= i >> 1;
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return i - (i >>> 1);
    }

    public static int lowestOneBit(int i) {
        return (-i) & i;
    }

    public static int numberOfLeadingZeros(int i) {
        if (i == 0) {
            return 32;
        }
        int n = 1;
        if ((i >>> 16) == 0) {
            n = 17;
            i <<= 16;
        }
        if ((i >>> 24) == 0) {
            n += 8;
            i <<= 8;
        }
        if ((i >>> 28) == 0) {
            n += 4;
            i <<= 4;
        }
        if ((i >>> 30) == 0) {
            n += 2;
            i <<= 2;
        }
        return n - (i >>> 31);
    }

    public static int numberOfTrailingZeros(int i) {
        if (i == 0) {
            return 32;
        }
        int n = 31;
        int y = i << 16;
        if (y != 0) {
            n = 15;
            i = y;
        }
        y = i << 8;
        if (y != 0) {
            n -= 8;
            i = y;
        }
        y = i << 4;
        if (y != 0) {
            n -= 4;
            i = y;
        }
        y = i << 2;
        if (y != 0) {
            n -= 2;
            i = y;
        }
        return n - ((i << 1) >>> 31);
    }

    public static int bitCount(int i) {
        i -= (i >>> 1) & 1431655765;
        i = (i & 858993459) + ((i >>> 2) & 858993459);
        i = ((i >>> 4) + i) & 252645135;
        i += i >>> 8;
        return (i + (i >>> 16)) & 63;
    }

    public static int rotateLeft(int i, int distance) {
        return (i << distance) | (i >>> (-distance));
    }

    public static int rotateRight(int i, int distance) {
        return (i >>> distance) | (i << (-distance));
    }

    public static int reverse(int i) {
        i = ((i & 1431655765) << 1) | ((i >>> 1) & 1431655765);
        i = ((i & 858993459) << 2) | ((i >>> 2) & 858993459);
        i = ((i & 252645135) << 4) | ((i >>> 4) & 252645135);
        return (((i << 24) | ((i & 65280) << 8)) | ((i >>> 8) & 65280)) | (i >>> 24);
    }

    public static int signum(int i) {
        return (i >> 31) | ((-i) >>> 31);
    }

    public static int reverseBytes(int i) {
        return (((i >>> 24) | ((i >> 8) & 65280)) | ((i << 8) & 16711680)) | (i << 24);
    }

    public static int sum(int a, int b) {
        return a + b;
    }

    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    public static int min(int a, int b) {
        return Math.min(a, b);
    }
}
