package com.huawei.systemmanager.power.receiver.handle;

import android.app.ActivityManagerNative;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import com.huawei.systemmanager.optimize.smcs.SMCSDatabaseConstant.AdBlockColumns;
import com.huawei.systemmanager.power.comm.ActionConst;
import com.huawei.systemmanager.power.model.PowerModeControl;
import com.huawei.systemmanager.power.util.SysCoreUtils;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.NotificationID;
import java.util.List;

public class HandleExitSuperPowerMode implements IBroadcastHandler {
    private static final String TAG = HandleExitSuperPowerMode.class.getSimpleName();

    public void handleBroadcast(Context ctx, Intent intent) {
        exitSuperPowerMode(ctx);
    }

    private void exitSuperPowerMode(Context ctx) {
        HwLog.d(TAG, "exitSuperPowerMode!!!!");
        if (SysCoreUtils.IS_ATT) {
            ((NotificationManager) ctx.getSystemService("notification")).cancel(NotificationID.POWER_ATT);
        }
        SystemProperties.set("sys.super_power_save", "false");
        Intent homeIntent = new Intent("android.intent.action.MAIN", null);
        homeIntent.addCategory("android.intent.category.HOME");
        homeIntent.addFlags(270532608);
        int i = 0;
        while (i < 3) {
            try {
                ctx.startActivity(homeIntent);
                break;
            } catch (ActivityNotFoundException e) {
                HwLog.e(TAG, "Start home activity fail,try once more!");
                i++;
            }
        }
        Intent callPowerGenieIntent = new Intent(ActionConst.INTENT_USE_POWER_GENIE_CHANGE_MODE);
        callPowerGenieIntent.putExtra(AdBlockColumns.COLUMN_ENABLE, false);
        ctx.sendBroadcast(callPowerGenieIntent, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
        UserManager um = (UserManager) ctx.getSystemService("user");
        List<UserHandle> userHandleList = um.getUserProfiles();
        if (userHandleList != null && userHandleList.size() > 1) {
            for (UserHandle userHandle : userHandleList) {
                boolean isAFWrunning;
                int userId = userHandle.getIdentifier();
                UserInfo user = um.getUserInfo(userId);
                if (user == null) {
                    isAFWrunning = false;
                    continue;
                } else if (user.isManagedProfile()) {
                    isAFWrunning = true;
                    continue;
                } else {
                    isAFWrunning = user.isClonedProfile();
                    continue;
                }
                if (isAFWrunning) {
                    try {
                        ActivityManagerNative.getDefault().startUserInBackground(userId);
                        break;
                    } catch (RemoteException e2) {
                        HwLog.e(TAG, "Start AFW fail,try once more!");
                    }
                }
            }
        }
        if (Global.getInt(ctx.getContentResolver(), PowerModeControl.DB_PERCENT_SWITCH_STATUS_ENTER_SUPERSAVEMODE, 0) == 0) {
            System.putInt(ctx.getContentResolver(), PowerModeControl.DB_BATTERY_PERCENT_SWITCH, 0);
        }
    }
}
