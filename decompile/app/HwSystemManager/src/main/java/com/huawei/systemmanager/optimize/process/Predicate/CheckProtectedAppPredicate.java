package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.Map;

public class CheckProtectedAppPredicate extends FutureTaskPredicate<Map<String, Boolean>, ProcessAppItem> {
    private static final String TAG = "CheckProtectedAppPredicate";
    private final Context mContext;
    private final boolean mProtected;

    public CheckProtectedAppPredicate(Context ctx, boolean checkProtect) {
        this.mContext = ctx;
        this.mProtected = checkProtect;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        Boolean valueOf;
        Map<String, Boolean> protectMap = (Map) getResult();
        if (protectMap == null) {
            HwLog.e(TAG, getClass().getSimpleName() + " protectMap is null, must be something wrong!");
            protectMap = Collections.emptyMap();
        }
        Boolean protect = (Boolean) protectMap.get(input.getPackageName());
        if (this.mProtected) {
            valueOf = Boolean.valueOf(checkProteced(protect, input));
        } else {
            valueOf = checkUnProtected(protect, input);
        }
        return valueOf.booleanValue();
    }

    private Object checkUnProtected(Boolean protect, ProcessAppItem input) {
        if (protect == null) {
            HwLog.e(TAG, "input.getPackageName() = " + input.getPackageName() + "; should not be killed; protect == null");
            return Boolean.valueOf(false);
        } else if (!protect.booleanValue()) {
            return Boolean.valueOf(true);
        } else {
            HwLog.e(TAG, "input.getPackageName() = " + input.getPackageName() + "; should not be killed; protect");
            return Boolean.valueOf(false);
        }
    }

    private boolean checkProteced(Boolean protect, ProcessAppItem input) {
        if (protect == null) {
            input.setKeyTask(true);
        } else {
            input.setProtect(protect.booleanValue());
            input.setKeyTask(false);
        }
        return true;
    }

    protected Map<String, Boolean> doInbackground() {
        return SmcsDbHelper.getRecordProtectAppFromDb(this.mContext, null);
    }

    public static CheckProtectedAppPredicate create(Context ctx, boolean checkProtect) {
        CheckProtectedAppPredicate pre = new CheckProtectedAppPredicate(ctx, checkProtect);
        pre.executeTask();
        return pre;
    }
}
