package com.huawei.systemmanager.netassistant.traffic.datasaver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.content.HsmBroadcastReceiver;

public class DataSaverReceiver extends HsmBroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        sendToBackground(context, intent);
    }

    public void doInBackground(Context context, Intent intent) {
        Utility.checkBroadcast(context, intent);
        if ("android.intent.action.PACKAGE_ADDED".equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                String pkgName = data.getSchemeSpecificPart();
                DataSaverDataCenter.initDaSaverProtectedList(context, pkgName, getUid(context, pkgName));
            }
        }
    }

    private int getUid(Context context, String pkgName) {
        int uid = -1;
        try {
            return context.getPackageManager().getApplicationInfo(pkgName, 128).uid;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return uid;
        }
    }
}
