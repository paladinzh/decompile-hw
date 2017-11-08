package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.Objects;

public class LauncherPredicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "LauncherPredicate";
    private final LauncherStringPredicate mPredicate;
    private boolean mShouldFilter;

    public static class LauncherStringPredicate implements Predicate<String> {
        private String launcher;

        public LauncherStringPredicate(Context ctx) {
            this.launcher = LauncherPredicate.getDefaultLauncher(ctx);
            if (TextUtils.isEmpty(this.launcher)) {
                this.launcher = "com.huawei.android.launcher";
                HwLog.i(LauncherPredicate.TAG, "get default launcher is null, set hwlauncher");
                return;
            }
            HwLog.i(LauncherPredicate.TAG, "default launcher is " + this.launcher);
        }

        public boolean apply(String input) {
            if (TextUtils.isEmpty(input) || !Objects.equals(this.launcher, input)) {
                return false;
            }
            HwLog.i(LauncherPredicate.TAG, " LauncherStringPredicate :: PackageName = " + input);
            return true;
        }
    }

    public LauncherPredicate(Context ctx, boolean filter) {
        this.mPredicate = new LauncherStringPredicate(ctx);
        this.mShouldFilter = filter;
    }

    public boolean apply(ProcessAppItem input) {
        String pkg = input.getPackageName();
        if (this.mPredicate.apply(pkg)) {
            input.setKeyTask(true);
            if (this.mShouldFilter) {
                HwLog.i(TAG, "LauncherPredicate :: name = " + input.getName() + "; getPackageName = " + pkg);
                return false;
            }
        }
        return true;
    }

    private static String getDefaultLauncher(Context ctx) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo res = PackageManagerWrapper.resolveActivity(ctx.getPackageManager(), intent, 0);
        if (res.activityInfo == null) {
            return null;
        }
        String pkgName = res.activityInfo.packageName;
        if ("android".equals(pkgName)) {
            return null;
        }
        return pkgName;
    }
}
