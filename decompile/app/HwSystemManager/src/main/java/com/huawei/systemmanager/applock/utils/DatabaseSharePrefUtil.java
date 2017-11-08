package com.huawei.systemmanager.applock.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.google.common.base.Strings;
import com.huawei.systemmanager.applock.datacenter.AppLockProvider.PreferenceDataProvider;
import com.huawei.systemmanager.applock.datacenter.tbl.AppLockPreferenceTable;
import com.huawei.systemmanager.comm.misc.CursorHelper;

public class DatabaseSharePrefUtil {
    public static int getPref(Context context, String key, int dftValue, boolean isBackupPref) {
        String value = getPreferenceDataValue(context, key);
        if (!Strings.isNullOrEmpty(value)) {
            return Integer.parseInt(value);
        }
        if (isBackupPref) {
            writePreferenceDataValue(context, key, String.valueOf(dftValue), true);
        }
        return dftValue;
    }

    public static boolean getPref(Context context, String key, boolean dftValue, boolean isBackupPref) {
        String value = getPreferenceDataValue(context, key);
        if (!Strings.isNullOrEmpty(value)) {
            return Boolean.parseBoolean(value);
        }
        if (isBackupPref) {
            writePreferenceDataValue(context, key, String.valueOf(dftValue), true);
        }
        return dftValue;
    }

    public static String getPref(Context context, String key, String dftValue, boolean isBackupPref) {
        String value = getPreferenceDataValue(context, key);
        if (!Strings.isNullOrEmpty(value)) {
            return value;
        }
        if (isBackupPref) {
            writePreferenceDataValue(context, key, String.valueOf(dftValue), true);
        }
        return dftValue;
    }

    public static void setPref(Context context, String key, int value, boolean needBackup) {
        writePreferenceDataValue(context, key, String.valueOf(value), needBackup);
    }

    public static void setPref(Context context, String key, boolean value, boolean needBackup) {
        writePreferenceDataValue(context, key, String.valueOf(value), needBackup);
    }

    public static void setPref(Context context, String key, String value, boolean needBackup) {
        writePreferenceDataValue(context, key, String.valueOf(value), needBackup);
    }

    private static String getPreferenceDataValue(Context context, String prefKey) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(PreferenceDataProvider.APPLOCK_PREFERENCE_DATA_URI, new String[]{AppLockPreferenceTable.COL_PREF_KEY, AppLockPreferenceTable.COL_PREF_VALUE}, "prefkey = ?", new String[]{prefKey}, null);
            if (cursor == null || !cursor.moveToNext()) {
                CursorHelper.closeCursor(cursor);
                return null;
            }
            String string = cursor.getString(1);
            return string;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CursorHelper.closeCursor(cursor);
        }
    }

    private static void writePreferenceDataValue(Context context, String prefKey, String prefValue, boolean needBackup) {
        int i;
        ContentValues contentValues = new ContentValues();
        contentValues.put(AppLockPreferenceTable.COL_PREF_KEY, prefKey);
        contentValues.put(AppLockPreferenceTable.COL_PREF_VALUE, prefValue);
        String str = AppLockPreferenceTable.COL_PREF_BACKUP;
        if (needBackup) {
            i = 1;
        } else {
            i = 0;
        }
        contentValues.put(str, Integer.valueOf(i));
        context.getContentResolver().insert(PreferenceDataProvider.APPLOCK_PREFERENCE_DATA_URI, contentValues);
    }
}
