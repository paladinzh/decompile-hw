package com.fyusion.sdk.core.util;

/* compiled from: Unknown */
public class b {
    public static void a(float f, float f2) {
        if ((f >= f2 ? 1 : null) == null) {
            throw new RuntimeException("lhs = " + f + " must be greater-equal rhs = " + f2);
        }
    }

    public static void a(int i, int i2) {
        if (i != i2) {
            throw new RuntimeException("lhs = " + i + " must be equal rhs = " + i2);
        }
    }

    public static void a(boolean z) {
        a(z, "The value must be true");
    }

    public static void a(boolean z, String str) {
        if (!z) {
            throw new RuntimeException(str);
        }
    }

    public static void b(float f, float f2) {
        if ((f <= f2 ? 1 : null) == null) {
            throw new RuntimeException("lhs = " + f + " must be less-equal rhs = " + f2);
        }
    }

    public static void b(int i, int i2) {
        if (i == i2) {
            throw new RuntimeException("lhs = " + i + " must be unequal rhs = " + i2);
        }
    }

    public static void c(int i, int i2) {
        if (i <= i2) {
            throw new RuntimeException("lhs = " + i + " must be greater-than rhs = " + i2);
        }
    }
}
