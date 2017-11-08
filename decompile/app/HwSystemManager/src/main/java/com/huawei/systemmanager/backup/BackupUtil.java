package com.huawei.systemmanager.backup;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.text.TextUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgUtils;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class BackupUtil {
    public static final String TAG = "BackupUtil";

    public static Cursor getPreferenceCursor(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        ContentValues values = new ContentValues();
        for (Entry<String, ?> entry : map.entrySet()) {
            values.put((String) entry.getKey(), (String) entry.getValue());
        }
        return getPreferenceCursor(values);
    }

    public static Cursor getPreferenceCursor(ContentValues values) {
        if (values == null) {
            HwLog.w(TAG, "getPreferenceCursor : No Preference values");
            return null;
        }
        Set<String> keyStringSet = values.keySet();
        if (keyStringSet == null || keyStringSet.size() <= 0) {
            HwLog.w(TAG, "getPreferenceCursor : Invalid Preference values");
            return null;
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{BackupConst.PREFERENCE_KEY, BackupConst.PREFERENCE_VALUE});
        for (String keyString : keyStringSet) {
            String valueString = values.getAsString(keyString);
            cursor.addRow(new Object[]{keyString, valueString});
            HwLog.v(TAG, String.format("getPreferenceCursor:%1$s = %2$s", new Object[]{keyString, valueString}));
        }
        HwLog.v(TAG, "getPreferenceCursor : count = " + cursor.getCount());
        return cursor;
    }

    public static Boolean checkPackageExists(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            HwLog.w(TAG, "checkPackageExists : Invalid context or package name");
            return Boolean.valueOf(false);
        }
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return Boolean.valueOf(true);
        } catch (NameNotFoundException e) {
            return Boolean.valueOf(false);
        }
    }

    public static int getPackageUid(Context context, String packageName) {
        return HsmPkgUtils.getPackageUid(packageName);
    }

    public static Cursor getAddViewPermissionsCursor(ContentValues values) {
        return getPreferenceCursor(values);
    }
}
