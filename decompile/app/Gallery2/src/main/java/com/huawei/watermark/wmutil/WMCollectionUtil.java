package com.huawei.watermark.wmutil;

import java.util.Collection;
import java.util.Map;
import java.util.Vector;

public class WMCollectionUtil {
    public static <T extends Collection<?>> boolean isEmptyCollection(T collection) {
        return collection == null || collection.size() == 0;
    }

    public static <T extends Map<?, ?>> boolean isEmptyCollection(T collection) {
        return collection == null || collection.size() == 0;
    }

    public static <T> boolean isEmptyCollection(T[] collection) {
        return collection == null || collection.length == 0;
    }

    public static boolean isEmptyCollection(char[] data) {
        return data == null || data.length == 0;
    }

    public static Vector copyVector(Vector in) {
        Vector out = new Vector();
        for (int i = 0; i < in.size(); i++) {
            out.add(in.elementAt(i));
        }
        return out;
    }
}
