package com.huawei.systemmanager.optimize.process.Predicate;

import android.os.SystemProperties;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;

public class ProcADJPredicate implements Predicate<ProcessAppItem> {
    static final String RO_SMART_TRIM_ADJ = "ro.smart_trim.adj";
    static final int SMART_TRIM_ADJ_DEFAULT = 3;
    static final int SMART_TRIM_ADJ_LIMIT = SystemProperties.getInt(RO_SMART_TRIM_ADJ, 3);
    private static final String TAG = "ProcADJPredicate";

    public boolean apply(ProcessAppItem input) {
        boolean z = false;
        if (input == null) {
            return false;
        }
        boolean filter;
        if (input.getADJ() <= SMART_TRIM_ADJ_LIMIT) {
            filter = true;
        } else {
            filter = false;
        }
        if (filter) {
            HwLog.i(TAG, "package name= " + input.getPackageName() + ", adj=" + input.getADJ() + " <= SMART_TRIM_ADJ_LIMIT");
        }
        if (!filter) {
            z = true;
        }
        return z;
    }
}
