package com.huawei.systemmanager.optimize.process.Predicate;

import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;

public class PersistentFlagPerdicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "PersistentFlagPerdicate";

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        if (!input.isPersistent()) {
            return true;
        }
        HwLog.i(TAG, input.getPackageName() + "is persistent, did not show");
        return false;
    }
}
