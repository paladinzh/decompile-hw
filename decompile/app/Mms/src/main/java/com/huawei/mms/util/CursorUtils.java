package com.huawei.mms.util;

import android.database.AbstractCursor;
import android.database.AbstractWindowedCursor;
import android.database.Cursor;
import android.database.CursorWrapper;
import java.lang.reflect.Field;

public class CursorUtils {
    private static Field sAbsRowIdFeild;
    private static Field sAbsWindowFeild;

    static {
        sAbsRowIdFeild = null;
        sAbsWindowFeild = null;
        try {
            sAbsRowIdFeild = AbstractCursor.class.getDeclaredField("mRowIdColumnIndex");
            sAbsRowIdFeild.setAccessible(true);
            sAbsWindowFeild = AbstractWindowedCursor.class.getDeclaredField("mWindow");
            sAbsWindowFeild.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (SecurityException e2) {
            e2.printStackTrace();
        }
    }

    private static void clearAbsCsrRowIdIdx(AbstractCursor c) {
        if (sAbsRowIdFeild != null) {
            try {
                sAbsRowIdFeild.setInt(c, -1);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
                e2.printStackTrace();
            }
        }
    }

    static Cursor getDepCursor(Cursor c) {
        if (c instanceof CursorWrapper) {
            return getDepCursor(((CursorWrapper) c).getWrappedCursor());
        }
        return c;
    }

    public static Cursor getFastCursor(Cursor org) {
        return org;
    }

    public static void clearCusorRowIdIdx(Cursor c) {
        Cursor dep = getDepCursor(c);
        if (dep instanceof AbstractCursor) {
            clearAbsCsrRowIdIdx((AbstractCursor) dep);
        }
    }
}
