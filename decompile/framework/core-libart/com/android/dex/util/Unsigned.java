package com.android.dex.util;

public final class Unsigned {
    private Unsigned() {
    }

    public static int compare(short ushortA, short ushortB) {
        if (ushortA == ushortB) {
            return 0;
        }
        return (ushortA & 65535) < (ushortB & 65535) ? -1 : 1;
    }

    public static int compare(int uintA, int uintB) {
        if (uintA == uintB) {
            return 0;
        }
        return (((long) uintA) & 4294967295L) < (((long) uintB) & 4294967295L) ? -1 : 1;
    }
}
