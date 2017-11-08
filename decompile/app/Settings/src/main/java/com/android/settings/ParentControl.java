package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.widget.Button;
import java.util.HashSet;

public class ParentControl {
    public static final Uri STATUS_URI = Uri.parse("content://com.huawei.parentcontrol/childmode_status");
    private static Intent parentControlIntent = new Intent();
    private static HashSet<String> restrictedApps = new HashSet();

    static {
        restrictedApps.add("com.android.browser");
        restrictedApps.add("com.android.phone");
        restrictedApps.add("com.android.mms");
        restrictedApps.add("com.huawei.parentcontrol");
        parentControlIntent.setClassName("com.huawei.parentcontrol", "com.huawei.parentcontrol.ui.activity.HomeActivity");
    }

    public static int getParentControlStatus(Context context) {
        int status = 0;
        if (context == null || context.getContentResolver() == null) {
            return 0;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.parse("content://com.huawei.parentcontrol/childmode_status"), null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                status = cursor.getInt(cursor.getColumnIndex("value"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            Log.e("ParentControl", "Query failed, URI = content://com.huawei.parentcontrol/childmode_status");
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return status;
    }

    public static boolean isChildModeOn(Context context) {
        return isChildModeOn(getParentControlStatus(context));
    }

    public static boolean isChildModeOn(int status) {
        return status == 1;
    }

    public static void enablePref(Preference pref, boolean enabled, int status) {
        if (!isChildModeOn(status) || !enabled) {
            pref.setEnabled(enabled);
        }
    }

    public static void enableButton(Button button, boolean enabled, Context context) {
        enableButton(button, enabled, getParentControlStatus(context));
    }

    public static void enableButton(Button button, boolean enabled, int status) {
        if (!isChildModeOn(status) || !enabled) {
            button.setEnabled(enabled);
        }
    }

    public static boolean isRestrictedApp(String pkgName) {
        return restrictedApps.contains(pkgName);
    }

    public static boolean isParentControlValid(Context context) {
        boolean z = false;
        if (context == null) {
            Log.e("ParentControl", "Null context, failed to get status of ParentControl!");
            return false;
        }
        if (UserHandle.myUserId() == 0) {
            z = Utils.hasIntentActivity(context.getPackageManager(), parentControlIntent);
        }
        return z;
    }
}
