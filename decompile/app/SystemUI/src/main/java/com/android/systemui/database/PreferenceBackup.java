package com.android.systemui.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.Settings.Secure;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.android.systemui.utils.UserSwitchUtils;

class PreferenceBackup {
    PreferenceBackup() {
    }

    static Cursor matrixCursorOfQsOrder(Context ctx) {
        String value = Secure.getStringForUser(ctx.getContentResolver(), "sysui_qs_tiles", UserSwitchUtils.getCurrentUser());
        if (value == null) {
            HwLog.e("PreferenceBackup", "matrixCursorOfQsOrder null data from settings");
            value = ctx.getString(PerfAdjust.getQuickSettingsTilesDefault());
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{"preference_key", "preference_value"});
        cursor.addRow(new Object[]{"sysui_qs_tiles", value});
        return cursor;
    }

    static boolean restoreQsOrder(Context ctx, ContentValues values) {
        String prefKey = values.getAsString("preference_key");
        String prefValue = values.getAsString("preference_value");
        HwLog.i("PreferenceBackup", "restoreQsOrder " + prefKey + ", " + prefValue);
        if (!"sysui_qs_tiles".equals(prefKey) || prefValue == null) {
            HwLog.e("PreferenceBackup", "restoreQsOrder error: " + values);
            return false;
        }
        Secure.putStringForUser(ctx.getContentResolver(), "sysui_qs_tiles", prefValue, UserSwitchUtils.getCurrentUser());
        return true;
    }
}
