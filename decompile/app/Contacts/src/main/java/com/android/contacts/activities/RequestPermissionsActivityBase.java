package com.android.contacts.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Trace;
import com.android.contacts.hap.AccountsDataManager;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class RequestPermissionsActivityBase extends Activity {
    private static final String[] CONTACTS_ALL_DANGEROUS_PERMISSIONS = new String[]{"android.permission.READ_CONTACTS", "android.permission.READ_CALL_LOG", "android.permission.SEND_SMS", "android.permission.READ_CALENDAR", "android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"};
    private Intent mPreviousActivityIntent;

    protected abstract String[] getDesiredPermissions();

    protected abstract String[] getRequiredPermissions();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mPreviousActivityIntent = (Intent) getIntent().getExtras().get("previous_intent");
        if (savedInstanceState == null) {
            requestPermission();
        }
    }

    private static void startRequestPermissionActivity(Activity activity, Class<?> newActivityClass) {
        Intent intent = new Intent(activity, newActivityClass);
        intent.putExtra("previous_intent", activity.getIntent());
        activity.startActivity(intent);
        activity.finish();
    }

    protected static boolean startPermissionActivity(Activity activity, String[] requiredPermissions, Class<?> newActivityClass) {
        if (hasPermissions(activity, requiredPermissions)) {
            return false;
        }
        startRequestPermissionActivity(activity, newActivityClass);
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (permissions == null || permissions.length <= 0 || !isAllGranted(permissions, grantResults)) {
            try {
                startActivityForResult(createRequestPermissionIntent(permissions, getPackageName()), 10);
                return;
            } catch (Exception e) {
                HwLog.e("RequestPermissionsActivityBase", "Activity not find!");
                finish();
                return;
            }
        }
        initWhenPermissionGranted();
        finish();
    }

    private boolean isAllGranted(String[] permissions, int[] grantResult) {
        int i = 0;
        while (i < permissions.length) {
            if (grantResult[i] != 0 && isPermissionRequired(permissions[i])) {
                return false;
            }
            i++;
        }
        return true;
    }

    private boolean isAllGranted(String[] permissions) {
        for (String checkSelfPermission : permissions) {
            if (checkSelfPermission(checkSelfPermission) != 0) {
                return false;
            }
        }
        return true;
    }

    public static Intent createRequestPermissionIntent(String[] permissions, String packageName) {
        Intent intent = new Intent("huawei.intent.action.REQUEST_PERMISSIONS");
        intent.setPackage("com.huawei.systemmanager");
        intent.putExtra("KEY_HW_PERMISSION_ARRAY", permissions);
        intent.putExtra("KEY_HW_PERMISSION_PKG", packageName);
        return intent;
    }

    private boolean isPermissionRequired(String p) {
        return Arrays.asList(getRequiredPermissions()).contains(p);
    }

    private void requestPermission() {
        Trace.beginSection("requestPermissions");
        try {
            ArrayList<String> unsatisfiedPermissions = new ArrayList();
            for (String permission : getDesiredPermissions()) {
                if (checkSelfPermission(permission) != 0) {
                    unsatisfiedPermissions.add(permission);
                }
            }
            if (unsatisfiedPermissions.size() == 0) {
                finish();
                return;
            }
            requestPermissions((String[]) unsatisfiedPermissions.toArray(new String[unsatisfiedPermissions.size()]), 1000);
            Trace.endSection();
        } finally {
            Trace.endSection();
        }
    }

    public static boolean hasPermissions(Context context, String[] permissions) {
        Trace.beginSection("hasPermission");
        try {
            for (String permission : permissions) {
                if (context.checkSelfPermission(permission) != 0) {
                    return false;
                }
            }
            Trace.endSection();
            return true;
        } finally {
            Trace.endSection();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 10:
                if (-1 == resultCode && isAllGranted(getRequiredPermissions())) {
                    initWhenPermissionGranted();
                }
                finish();
                return;
            default:
                return;
        }
    }

    private void initWhenPermissionGranted() {
        if (this.mPreviousActivityIntent != null) {
            this.mPreviousActivityIntent.setFlags(65536);
            startActivity(this.mPreviousActivityIntent);
        }
        SimFactoryManager.initSimFactoryManager();
        AccountTypeManager.getInstance(getApplicationContext()).hiCloudServiceLogOnOff();
        AccountsDataManager.getInstance(getApplicationContext()).preLoadAccountsDataInBackground();
    }
}
