package com.android.mms.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import com.android.messaging.util.OsUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwBackgroundLoader;
import java.util.ArrayList;

public class PermissionCheckActivity extends Activity {
    private static PendingTasks sPendingTasks = new PendingTasks();
    private ActionBar mActionBar;
    private boolean mGetResultForSettings = false;
    private long mRequestTimeMillis = 0;

    private static class PendingTasks {
        boolean mGotoConversationWhenFinish;
        private boolean mInChecking;
        private ArrayList<Runnable> mTasks;

        private PendingTasks() {
            this.mTasks = new ArrayList();
            this.mGotoConversationWhenFinish = false;
            this.mInChecking = false;
        }

        private synchronized void addTask(Runnable r) {
            this.mTasks.add(r);
        }

        private synchronized Runnable getTask() {
            if (this.mTasks.size() == 0) {
                return null;
            }
            return (Runnable) this.mTasks.remove(0);
        }

        private synchronized int getSize() {
            return this.mTasks.size();
        }

        private void handlePendingTasks() {
            while (true) {
                Runnable r = PermissionCheckActivity.sPendingTasks.getTask();
                if (r != null) {
                    HwBackgroundLoader.getInst().postTaskDelayed(r, 1000);
                } else {
                    return;
                }
            }
        }

        private synchronized boolean isNeedGotoConversation() {
            return this.mGotoConversationWhenFinish;
        }

        private synchronized boolean setNeedGotoConversation(boolean gotoConv) {
            boolean ret;
            ret = this.mGotoConversationWhenFinish;
            this.mGotoConversationWhenFinish = gotoConv;
            return ret;
        }

        private synchronized void clearTasks() {
            this.mTasks.clear();
        }

        private synchronized boolean setInChecking(boolean start) {
            boolean ret;
            ret = this.mInChecking;
            this.mInChecking = start;
            return ret;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLog.d("PermissionCheckActivity", "PermissionCheckActivity lifecycle onCreate");
        overridePendingTransition(0, 0);
        this.mActionBar = getActionBar();
        this.mActionBar.setTitle(R.string.app_label);
        if (!redirectIfNeeded()) {
        }
    }

    public void onResume() {
        super.onResume();
        MLog.d("PermissionCheckActivity", "PermissionCheckActivity lifecycle onResume");
        if (!redirectIfNeeded()) {
            if (this.mGetResultForSettings) {
                finishAndRemoveTask();
            }
            tryRequestPermission();
        }
    }

    public void onPause() {
        super.onPause();
        MLog.d("PermissionCheckActivity", "PermissionCheckActivity lifecycle onPause");
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        MLog.d("PermissionCheckActivity", "PermissionCheckActivity lifecycle onNewIntent");
    }

    protected void onDestroy() {
        super.onDestroy();
        MLog.d("PermissionCheckActivity", "PermissionCheckActivity lifecycle onDestroy");
        sPendingTasks.setInChecking(false);
    }

    private void tryRequestPermission() {
        if (sPendingTasks.setInChecking(true)) {
            MLog.w("PermissionCheckActivity", "tryRequestPermission already in checking");
            return;
        }
        String[] missingPermissions = OsUtil.getMissingRequiredPermissions();
        if (missingPermissions.length == 0) {
            redirect();
            return;
        }
        overridePendingTransition(0, 0);
        this.mRequestTimeMillis = SystemClock.elapsedRealtime();
        Intent intent = new Intent("huawei.intent.action.REQUEST_MULTI_PERMISSIONS");
        intent.setPackage("com.huawei.systemmanager");
        intent.putExtra("KEY_HW_PERMISSION_ARRAY", missingPermissions);
        try {
            startActivityForResult(intent, 1);
        } catch (Exception e) {
            MLog.e("PermissionCheckActivity", "requestPermissions cause a Exception ", (Throwable) e);
            requestPermissions(missingPermissions, 1);
        }
        OsUtil.setFirstLaunch(false);
        MLog.w("PermissionCheckActivity", "start requestPermissions.");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        sPendingTasks.setInChecking(false);
        if (requestCode != 1) {
            MLog.w("PermissionCheckActivity", "onRequestPermissionsResult return " + requestCode);
            finishAndRemoveTask();
            return;
        }
        String[] missingPermissions = OsUtil.getMissingRequiredPermissions();
        if (missingPermissions.length == 0) {
            MLog.w("PermissionCheckActivity", "onRequestPermissionsResult accquired Permissions");
            return;
        }
        sPendingTasks.clearTasks();
        if (SystemClock.elapsedRealtime() - this.mRequestTimeMillis < 500) {
            MLog.w("PermissionCheckActivity", "onRequestPermissionsResult user permenent reject");
            if (!recheckUserRejectPermissions(this, missingPermissions)) {
                gotoPackageSettings(this);
                finishAndRemoveTask();
            }
        } else {
            MLog.w("PermissionCheckActivity", "onRequestPermissionsResult user reject ");
            finishAndRemoveTask();
        }
    }

    public static boolean recheckUserRejectPermissions(Activity act, String[] rejectPermissions) {
        Intent intent = new Intent("huawei.intent.action.REQUEST_PERMISSIONS");
        intent.setPackage("com.huawei.systemmanager");
        intent.putExtra("KEY_HW_PERMISSION_ARRAY", rejectPermissions);
        intent.putExtra("KEY_HW_PERMISSION_PKG", act.getPackageName());
        try {
            act.startActivityForResult(intent, 2);
            return true;
        } catch (ActivityNotFoundException e) {
            MLog.e("PermissionCheckActivity", "recheckUserRejectPermissions: Exception", (Throwable) e);
            return false;
        }
    }

    public static void gotoPackageSettings(Activity act) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + act.getPackageName()));
        intent.setFlags(268435456);
        act.startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (1 == requestCode && data == null) {
            if (resultCode == 0) {
                MLog.i("PermissionCheckActivity", "request all permissons: RESULT_CANCELED");
                finishAndRemoveTask();
                return;
            } else if (100 == resultCode) {
                MLog.i("PermissionCheckActivity", "request all permissons: RESULT_SETTING");
                this.mGetResultForSettings = true;
                sPendingTasks.setInChecking(false);
                return;
            }
        }
        if (2 == requestCode) {
            if (resultCode == 0) {
                MLog.i("PermissionCheckActivity", "recheckUserRejectPermissions: RESULT_CANCELED");
                finishAndRemoveTask();
            } else if (-1 == resultCode) {
                MLog.i("PermissionCheckActivity", "User has grant permissions in package installer");
            }
        }
    }

    private boolean redirectIfNeeded() {
        if (!OsUtil.hasRequiredPermissions()) {
            return false;
        }
        redirect();
        return true;
    }

    private void redirect() {
        MLog.w("PermissionCheckActivity", "Finish check permission and handle pending task. " + sPendingTasks.getSize());
        if (OsUtil.isFirstLaunch()) {
            OsUtil.setFirstLaunch(false);
        }
        sPendingTasks.handlePendingTasks();
        finishAndRemoveTask();
        if (sPendingTasks.setNeedGotoConversation(false)) {
            Intent intent = getIntent().getSelector();
            if (intent == null || intent.getComponent() == null) {
                intent = new Intent("android.intent.action.MAIN");
            }
            if ("android.intent.action.SEND".equals(intent.getAction())) {
                intent.setClipData(null);
            }
            intent.setPackage(getPackageName());
            startActivity(intent);
            overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_open_exit);
            MLog.d("PermissionCheckActivity", "rediret to class name: " + intent.getComponent().getClassName(), new Exception("rediret"));
        }
    }

    public void finishAndRemoveTask() {
        super.finishAndRemoveTask();
        MLog.d("PermissionCheckActivity", "PermissionCheckActivity lifecycle finishAndRemoveTask");
    }

    public static boolean markActivityStaring(boolean needGotoConversaion) {
        boolean ret = sPendingTasks.isNeedGotoConversation() || sPendingTasks.getSize() > 0;
        if (needGotoConversaion) {
            sPendingTasks.setNeedGotoConversation(true);
        }
        return ret;
    }

    public static void setPendingTask(Runnable r) {
        if (sPendingTasks.getSize() > 0) {
            MLog.w("PermissionCheckActivity", "A pending task is already cached");
        }
        sPendingTasks.addTask(r);
    }
}
