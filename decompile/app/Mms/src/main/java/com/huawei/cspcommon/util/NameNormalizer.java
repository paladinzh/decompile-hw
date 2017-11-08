package com.huawei.cspcommon.util;

import java.text.CollationKey;
import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.Locale;

public class NameNormalizer {
    private static final char[] FIRST_CHAR = new char[256];
    private static final char[] HEX_DIGITS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] SECOND_CHAR = new char[256];
    private static final RuleBasedCollator sCompressingCollator = ((RuleBasedCollator) Collator.getInstance(Locale.getDefault()));

    static {
        sCompressingCollator.setStrength(0);
        sCompressingCollator.setDecomposition(1);
        for (int i = 0; i < 256; i++) {
            FIRST_CHAR[i] = HEX_DIGITS[(i >> 4) & 15];
            SECOND_CHAR[i] = HEX_DIGITS[i & 15];
        }
    }

    public static String normalize(String name) {
        CollationKey key = sCompressingCollator.getCollationKey(name);
        if (key != null) {
            return encodeHex(key.toByteArray(), true);
        }
        return "";
    }

    public static String encodeHex(byte[] array, boolean zeroTerminated) {
        if (array == null) {
            return "";
        }
        char[] cArray = new char[(array.length * 2)];
        int j = 0;
        int i = 0;
        while (i < array.length) {
            int index = array[i] & 255;
            if (zeroTerminated && index == 0 && i == array.length - 1) {
                break;
            }
            int i2 = j + 1;
            cArray[j] = FIRST_CHAR[index];
            j = i2 + 1;
            cArray[i2] = SECOND_CHAR[index];
            i++;
        }
        return new String(cArray, 0, j);
    }
}
