package com.googlecode.mp4parser.util;

public class CastUtils {
    public static int l2i(long l) {
        if (l <= 2147483647L && l >= -2147483648L) {
            return (int) l;
        }
        throw new RuntimeException("A cast to int has gone wrong. Please contact the mp4parser discussion group (" + l + ")");
    }
}
