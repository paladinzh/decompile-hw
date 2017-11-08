package com.huawei.systemmanager.optimize.process.Predicate;

import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;

public class SystemIdPredicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "SystemIdPredicate";

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        int uid = input.getUid();
        if (uid > 10000) {
            return true;
        }
        HwLog.i(TAG, input.getPackageName() + "uid is " + uid + ", did not show");
        return false;
    }
}
