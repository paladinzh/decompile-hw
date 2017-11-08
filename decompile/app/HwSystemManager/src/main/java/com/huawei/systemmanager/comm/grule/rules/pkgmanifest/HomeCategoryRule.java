package com.huawei.systemmanager.comm.grule.rules.pkgmanifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import com.huawei.systemmanager.comm.grule.rules.IRule;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;

public class HomeCategoryRule implements IRule<String> {
    private static final String TAG = HomeCategoryRule.class.getSimpleName();

    public boolean match(Context context, String pkgName) {
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        for (ResolveInfo info : PackageManagerWrapper.queryIntentActivities(context.getPackageManager(), homeIntent, 32)) {
            if (info.activityInfo.packageName.equals(pkgName)) {
                return true;
            }
        }
        return false;
    }
}
