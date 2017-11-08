package com.coremedia.iso;

public class Hex {
    private static final char[] DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static String encodeHex(byte[] data) {
        return encodeHex(data, 0);
    }

    public static String encodeHex(byte[] data, int group) {
        int i = 0;
        int l = data.length;
        int i2 = l << 1;
        if (group > 0) {
            i = l / group;
        }
        char[] out = new char[(i + i2)];
        int i3 = 0;
        int j = 0;
        while (i3 < l) {
            int i4;
            if (group <= 0 || i3 % group != 0 || j <= 0) {
                i4 = j;
            } else {
                i4 = j + 1;
                out[j] = '-';
            }
            j = i4 + 1;
            out[i4] = DIGITS[(data[i3] & 240) >>> 4];
            i4 = j + 1;
            out[j] = DIGITS[data[i3] & 15];
            i3++;
            j = i4;
        }
        return new String(out);
    }
}
