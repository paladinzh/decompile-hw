package com.huawei.permissionmanager.utils;

import android.util.SparseIntArray;

public class PermissionMap {
    private static SparseIntArray mPermissionIdMap = null;

    public static synchronized SparseIntArray getPermissionIdMap() {
        SparseIntArray sparseIntArray;
        synchronized (PermissionMap.class) {
            if (mPermissionIdMap == null) {
                mPermissionIdMap = new SparseIntArray();
                mPermissionIdMap.put(11, 1);
                mPermissionIdMap.put(12, 4);
                mPermissionIdMap.put(13, 2);
                mPermissionIdMap.put(14, 2048);
                mPermissionIdMap.put(15, 8);
                mPermissionIdMap.put(16, 16);
                mPermissionIdMap.put(27, 128);
                mPermissionIdMap.put(26, 1024);
                mPermissionIdMap.put(24, 32);
                mPermissionIdMap.put(23, 64);
                mPermissionIdMap.put(25, 8192);
                mPermissionIdMap.put(17, 16384);
                mPermissionIdMap.put(19, 32768);
                mPermissionIdMap.put(29, 4194304);
                mPermissionIdMap.put(28, 2097152);
                mPermissionIdMap.put(30, 8388608);
                mPermissionIdMap.put(31, 16777216);
                mPermissionIdMap.put(32, 33554432);
                mPermissionIdMap.put(33, 67108864);
                mPermissionIdMap.put(34, 134217728);
                mPermissionIdMap.put(35, ShareCfg.PERMISSION_MODIFY_CALENDAR);
                mPermissionIdMap.put(37, 1048576);
                mPermissionIdMap.put(38, 1073741824);
            }
            sparseIntArray = mPermissionIdMap;
        }
        return sparseIntArray;
    }
}
