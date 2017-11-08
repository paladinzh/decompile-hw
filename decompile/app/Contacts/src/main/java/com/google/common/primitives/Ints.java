package com.google.common.primitives;

import com.google.common.annotations.GwtCompatible;
import java.util.Arrays;

@GwtCompatible(emulated = true)
public final class Ints {
    private static final byte[] asciiDigits = new byte[128];

    private Ints() {
    }

    public static int saturatedCast(long value) {
        if (value > 2147483647L) {
            return Integer.MAX_VALUE;
        }
        if (value < -2147483648L) {
            return Integer.MIN_VALUE;
        }
        return (int) value;
    }

    static {
        int i;
        Arrays.fill(asciiDigits, (byte) -1);
        for (i = 0; i <= 9; i++) {
            asciiDigits[i + 48] = (byte) i;
        }
        for (i = 0; i <= 26; i++) {
            asciiDigits[i + 65] = (byte) (i + 10);
            asciiDigits[i + 97] = (byte) (i + 10);
        }
    }
}
