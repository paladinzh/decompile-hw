package com.huawei.systemmanager.addviewmonitor;

import android.content.Context;
import android.content.Intent;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class HwDefAddviewReceiver extends HsmBroadcastReceiver {
    private static final String ACTION_USER_RESETSETTINGS = "com.huawei.systemmanager.action.RESET_USER_SETTINGS";
    private static final String TAG = "HwDefAddviewReceiver";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onReceive(Context context, Intent intent) {
        if (context != null && intent != null && ACTION_USER_RESETSETTINGS.equals(intent.getAction())) {
            HwLog.i(TAG, "onReceive: User settings is reset . action = " + intent.getAction());
            sendToBackground(context, intent);
        }
    }

    public void doInBackground(Context context, Intent intent) {
        AddViewAppManager.getInstance(context).resetAddViewPermissionFirstFlag();
        AddViewAppManager.getInstance(context).initAddViewAppList();
    }
}
