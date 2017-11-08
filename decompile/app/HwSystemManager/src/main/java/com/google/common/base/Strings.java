package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.javax.annotation.Nullable;

@GwtCompatible
public final class Strings {
    private Strings() {
    }

    public static String nullToEmpty(@Nullable String string) {
        return string == null ? "" : string;
    }

    public static boolean isNullOrEmpty(@Nullable String string) {
        return string == null || string.length() == 0;
    }

    public static String padStart(String string, int minLength, char padChar) {
        Preconditions.checkNotNull(string);
        if (string.length() >= minLength) {
            return string;
        }
        StringBuilder sb = new StringBuilder(minLength);
        for (int i = string.length(); i < minLength; i++) {
            sb.append(padChar);
        }
        sb.append(string);
        return sb.toString();
    }

    public static String repeat(String string, int count) {
        Preconditions.checkNotNull(string);
        if (count <= 1) {
            boolean z;
            if (count >= 0) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkArgument(z, "invalid count: %s", Integer.valueOf(count));
            if (count == 0) {
                string = "";
            }
            return string;
        }
        int len = string.length();
        long longSize = ((long) len) * ((long) count);
        int size = (int) longSize;
        if (((long) size) != longSize) {
            throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);
        }
        char[] array = new char[size];
        string.getChars(0, len, array, 0);
        int n = len;
        while (n < size - n) {
            System.arraycopy(array, 0, array, n, n);
            n <<= 1;
        }
        System.arraycopy(array, 0, array, n, size - n);
        return new String(array);
    }
}
