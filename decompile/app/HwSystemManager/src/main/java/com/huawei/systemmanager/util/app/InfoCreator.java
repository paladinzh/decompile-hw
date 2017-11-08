package com.huawei.systemmanager.util.app;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import com.huawei.systemmanager.util.HwLog;

public class InfoCreator {
    public static final InfoCreator DEFAULT_CREATE = new InfoCreator();
    public static final String TAG = "InfoCreator";

    public HsmPkgInfo createByPkgName(Context context, String pkgName) {
        PackageManager pm = context.getPackageManager();
        try {
            return new HsmPkgInfo(PackageManagerWrapper.getPackageInfo(pm, pkgName, 8256), pm);
        } catch (NameNotFoundException e) {
            HwLog.w(TAG, "get package info fail. not exists? " + pkgName);
            return null;
        }
    }
}
