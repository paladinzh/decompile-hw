package com.huawei.hsm.permission;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.util.Log;

public class SendBroadcastPermission {
    private static final String INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final String TAG = "SendBroadcastPermission";
    private static final String UNINSTALL_SHORTCUT_PERMISSION = "com.android.launcher.action.UNINSTALL_SHORTCUT";
    private int mPermissionType = 0;
    private int mPid = Binder.getCallingPid();
    private int mUid = Binder.getCallingUid();

    public SendBroadcastPermission(Context context) {
    }

    private void getPermissionType(Intent intent) {
        String action = intent.getAction();
        if (INSTALL_SHORTCUT_PERMISSION.equals(action) || UNINSTALL_SHORTCUT_PERMISSION.equals(action)) {
            this.mPermissionType = StubController.PERMISSION_EDIT_SHORTCUT;
        }
        Log.i(TAG, "action:" + action + ", mPermissionType:" + this.mPermissionType);
    }

    public boolean allowSendBroadcast(Intent intent) {
        getPermissionType(intent);
        if (this.mPermissionType == 0 || !StubController.checkPrecondition(this.mUid) || !isGlobalSwitchOn(intent, this.mUid, this.mPid)) {
            return true;
        }
        switch (StubController.holdForGetPermissionSelection(this.mPermissionType, this.mUid, this.mPid, null)) {
            case 0:
                Log.e(TAG, "SendBroadcastPermission holdForGetPermissionSelection error");
                return false;
            case 2:
                return false;
            default:
                return true;
        }
    }

    private boolean isGlobalSwitchOn(Intent intent, int uid, int pid) {
        return true;
    }
}
