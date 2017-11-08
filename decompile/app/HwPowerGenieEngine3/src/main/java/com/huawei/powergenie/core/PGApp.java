package com.huawei.powergenie.core;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.UserHandle;
import android.util.Log;

public class PGApp extends Application {
    public void onCreate() {
        super.onCreate();
        if (UserHandle.myUserId() != 0) {
            Log.i("PGApp", "not owner, so return");
            return;
        }
        String myselfPkg = "com.huawei.powergenie";
        PackageManager pm = getPackageManager();
        try {
            Log.i("PGApp", "version: " + pm.getPackageInfo("com.huawei.powergenie", 0).versionName);
        } catch (NameNotFoundException e) {
            Log.e("PGApp", "not find myself!");
        }
        if (pm.getApplicationEnabledSetting("com.huawei.powergenie") == 2) {
            pm.setApplicationEnabledSetting("com.huawei.powergenie", 1, 1);
            Log.e("PGApp", "enable myself!");
        }
        startService(new Intent(this, CoreService.class));
    }

    public void onLowMemory() {
        super.onLowMemory();
        Log.i("PGApp", "onLowMemory...");
    }
}
