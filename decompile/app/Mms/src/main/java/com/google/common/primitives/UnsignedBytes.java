package com.google.common.primitives;

import com.google.common.annotations.VisibleForTesting;
import java.util.Comparator;

public final class UnsignedBytes {

    @VisibleForTesting
    static class LexicographicalComparatorHolder {
        static final Comparator<byte[]> BEST_COMPARATOR = UnsignedBytes.lexicographicalComparatorJavaImpl();

        enum PureJavaComparator implements Comparator<byte[]> {
            INSTANCE;

            public int compare(byte[] left, byte[] right) {
                int minLength = Math.min(left.length, right.length);
                for (int i = 0; i < minLength; i++) {
                    int result = UnsignedBytes.compare(left[i], right[i]);
                    if (result != 0) {
                        return result;
                    }
                }
                return left.length - right.length;
            }
        }

        LexicographicalComparatorHolder() {
        }
    }

    private UnsignedBytes() {
    }

    public static int toInt(byte value) {
        return value & 255;
    }

    public static int compare(byte a, byte b) {
        return toInt(a) - toInt(b);
    }

    @VisibleForTesting
    static Comparator<byte[]> lexicographicalComparatorJavaImpl() {
        return PureJavaComparator.INSTANCE;
    }
}
