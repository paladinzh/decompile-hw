package com.huawei.systemmanager.comm.collections;

public class ArrayChecker {
    public static final boolean isEmpty(String[] array) {
        if (array == null || array.length <= 0) {
            return true;
        }
        return false;
    }
}
