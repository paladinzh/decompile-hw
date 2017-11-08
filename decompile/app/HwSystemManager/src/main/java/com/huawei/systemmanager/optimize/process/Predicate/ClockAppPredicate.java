package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.List;

public class ClockAppPredicate extends FutureTaskPredicate<ArrayList<String>, ProcessAppItem> {
    private static final String TAG = "ClockAppPredicate";
    private final Context mContext;

    public ClockAppPredicate(Context ctx) {
        this.mContext = ctx;
    }

    public boolean apply(ProcessAppItem input) {
        boolean z = false;
        if (input == null) {
            return false;
        }
        ArrayList<String> clockApps = (ArrayList) getResult();
        if (clockApps == null) {
            HwLog.e(TAG, getClass().getSimpleName() + " clockApps is null, must be something wrong!");
            clockApps = new ArrayList();
        }
        String pkg = input.getPackageName();
        boolean bFind = clockApps.contains(pkg);
        if (bFind) {
            HwLog.d(TAG, "should not kill " + pkg + ", it is in clock app list!");
        }
        if (!bFind) {
            z = true;
        }
        return z;
    }

    protected ArrayList<String> doInbackground() {
        return getClockApp(this.mContext);
    }

    private ArrayList<String> getClockApp(Context context) {
        ArrayList<String> clockList = new ArrayList();
        List<ResolveInfo> alarmActivity = PackageManagerWrapper.queryIntentActivities(this.mContext.getPackageManager(), new Intent("android.intent.action.SET_ALARM"), 0);
        if (alarmActivity != null) {
            for (ResolveInfo temp : alarmActivity) {
                ComponentInfo ci = temp.activityInfo != null ? temp.activityInfo : temp.serviceInfo;
                if (!(ci == null || ci.packageName == null)) {
                    clockList.add(ci.packageName);
                }
            }
        }
        HwLog.d(TAG, "All clock apps: " + clockList);
        return clockList;
    }
}
