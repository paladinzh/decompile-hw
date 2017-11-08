package com.huawei.systemmanager.optimize.process.Predicate;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class HighLocationAppPredicate extends FutureTaskPredicate<List<String>, ProcessAppItem> {
    private static final String TAG = "HighLocationAppPredicate";
    private final Context mContext;

    public HighLocationAppPredicate(Context ctx) {
        this.mContext = ctx;
    }

    public boolean apply(ProcessAppItem input) {
        boolean z = false;
        if (input == null) {
            return false;
        }
        List<String> highLocationApps = (List) getResult();
        if (highLocationApps == null) {
            HwLog.e(TAG, getClass().getSimpleName() + " highLocationApps is null, must be something wrong!");
            highLocationApps = Lists.newArrayList();
        }
        String pkg = input.getPackageName();
        boolean bFind = highLocationApps.contains(pkg);
        if (bFind) {
            HwLog.d(TAG, "should not kill " + pkg + ", it is in highLocationApps list now!");
        }
        if (!bFind) {
            z = true;
        }
        return z;
    }

    protected List<String> doInbackground() {
        List<String> result = Lists.newArrayList();
        List<PackageOps> pkgOps = ((AppOpsManager) this.mContext.getSystemService("appops")).getPackagesForOps(new int[]{42});
        if (pkgOps != null) {
            for (PackageOps ops : pkgOps) {
                for (OpEntry entry : ops.getOps()) {
                    if (entry.isRunning()) {
                        result.add(ops.getPackageName());
                        break;
                    }
                }
            }
        }
        HwLog.d(TAG, "doInbackground result: " + result);
        return result;
    }
}
