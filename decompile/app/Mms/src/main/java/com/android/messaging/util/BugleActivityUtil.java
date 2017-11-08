package com.android.messaging.util;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.android.mms.ui.PermissionCheckActivity;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;

public class BugleActivityUtil {
    public static boolean onActivityCreate(Activity activity) {
        return redirectToPermissionCheckIfNeeded(activity);
    }

    public static boolean redirectToPermissionCheckIfNeeded(Activity activity) {
        if (OsUtil.hasRequiredPermissions()) {
            if (OsUtil.isFirstLaunch()) {
                OsUtil.setFirstLaunch(false);
            }
            return false;
        }
        MLog.w("BugleActivityUtil", "Need check permission when start " + activity);
        PermissionCheckActivity.markActivityStaring(true);
        Intent intent = new Intent(activity, PermissionCheckActivity.class);
        intent.setFlags(268468224);
        intent.setSelector(activity.getIntent());
        activity.overridePendingTransition(0, 0);
        activity.startActivity(intent);
        activity.finishAndRemoveTask();
        return true;
    }

    public static boolean checkPermissionIfNeeded(Context context, Runnable onCheckFinish) {
        if (OsUtil.hasRequiredPermissions()) {
            if (onCheckFinish != null) {
                onCheckFinish.run();
            }
            if (OsUtil.isFirstLaunch()) {
                OsUtil.setFirstLaunch(false);
            }
            return false;
        }
        if (onCheckFinish != null) {
            PermissionCheckActivity.setPendingTask(onCheckFinish);
        }
        MLog.w("BugleActivityUtil", "Need check permission in background", new Exception("checkPermissionInBackgroud"));
        return true;
    }

    public static boolean checkPermissionWithOutCheckFirstLaunch(Context context, Runnable onCheckFinish) {
        if (OsUtil.hasStrictRequiredPermissions()) {
            if (onCheckFinish != null) {
                onCheckFinish.run();
            }
            return false;
        }
        if (onCheckFinish != null) {
            PermissionCheckActivity.setPendingTask(onCheckFinish);
        }
        MLog.w("BugleActivityUtil", "Need check permission in background without first launch check", new Exception("checkPermissionInBackgroud without first launch check"));
        return true;
    }

    public static boolean onActivityResume(Context context, Activity activity) {
        return checkHasSmsPermissionsForUser(context, activity);
    }

    private static boolean checkHasSmsPermissionsForUser(Context context, final Activity activity) {
        if (!OsUtil.isAtLeastL()) {
            return true;
        }
        if (((UserManager) context.getSystemService("user")).hasUserRestriction("no_sms")) {
            Log.e("OsUtil", "Buggle checkHasSmsPermissionsForUser Disable " + Process.myUid());
            int messageId = R.string.requires_sms_permissions_toast;
            UserInfo currentUser = null;
            try {
                currentUser = ActivityManagerNative.getDefault().getCurrentUser();
            } catch (RemoteException e) {
                Log.e("BugleActivityUtil", "get current user info failed for RemoteException");
            }
            if (currentUser != null && currentUser.isGuest()) {
                messageId = R.string.requires_sms_permissions_message;
            }
            new Builder(activity).setMessage(messageId).setCancelable(false).setNegativeButton(R.string.requires_sms_permissions_close_button, new OnClickListener() {
                public void onClick(DialogInterface dialog, int button) {
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(new Intent("ACTION_BROAD_CAST_FINISH"));
                    activity.finish();
                }
            }).show();
            return false;
        }
        Log.e("OsUtil", "Buggle checkHasSmsPermissionsForUser Enable " + Process.myUid());
        return true;
    }
}
