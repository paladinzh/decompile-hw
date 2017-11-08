package com.huawei.systemmanager.comm.misc;

import android.database.Cursor;

public class CursorHelper {
    public static boolean checkCursorValid(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        if (cursor != null) {
            cursor.close();
        }
        return false;
    }

    public static boolean checkCursorValidAndClose(Cursor cursor) {
        if (cursor == null || cursor.getCount() <= 0) {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
        cursor.close();
        return true;
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
}
