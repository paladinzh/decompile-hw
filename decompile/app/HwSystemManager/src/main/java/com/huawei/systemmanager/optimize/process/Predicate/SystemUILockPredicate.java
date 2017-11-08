package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.HwRecentsLockUtils;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;

public class SystemUILockPredicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "SystemUILockPre";
    private final Map<String, Boolean> persistentTask;

    private SystemUILockPredicate(Context context) {
        this.persistentTask = HwRecentsLockUtils.search(context);
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        String pkgName = input.getPackageName();
        Boolean shouldLock = (Boolean) this.persistentTask.get(pkgName);
        if (shouldLock == null || !shouldLock.booleanValue()) {
            return true;
        }
        HwLog.i(TAG, "did not remove systemUi locked pkg:" + pkgName);
        return false;
    }

    public static SystemUILockPredicate create(Context ctx) {
        return new SystemUILockPredicate(ctx);
    }
}
