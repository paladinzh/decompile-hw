package com.android.contacts.util;

public class Objects {
    public static boolean equal(Object a, Object b) {
        if (a != b) {
            return a != null ? a.equals(b) : false;
        } else {
            return true;
        }
    }
}
