package com.android.contacts.activities;

import android.app.Activity;

public class RequestPermissionsActivity extends RequestPermissionsActivityBase {
    private static final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG"};

    protected String[] getRequiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    protected String[] getDesiredPermissions() {
        return REQUIRED_PERMISSIONS;
    }

    public static boolean startPermissionActivity(Activity activity) {
        return RequestPermissionsActivityBase.startPermissionActivity(activity, REQUIRED_PERMISSIONS, RequestPermissionsActivity.class);
    }
}
