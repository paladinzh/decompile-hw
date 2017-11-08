package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.ProtectAppControl;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.Map;

public class ProtectControlledPredicate extends FutureTaskPredicate<Map<String, Boolean>, ProcessAppItem> {
    private static final String TAG = "ProtectControlledPredicate";
    private final Context mContext;
    private Map<String, Boolean> mDefaultControlledMap;

    private ProtectControlledPredicate(Context ctx, boolean cache) {
        this.mContext = ctx;
    }

    protected Map<String, Boolean> doInbackground() throws Exception {
        this.mDefaultControlledMap = SmcsDbHelper.getAllDefaultControledMap(this.mContext);
        return SmcsDbHelper.getRecordProtectAppFromDb(this.mContext, null);
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        String pkgName = input.getPackageName();
        boolean controlled = checkControlledInProtectMap(pkgName);
        if (!controlled) {
            controlled = ensureControlled(pkgName);
            if (controlled) {
                HwLog.w(TAG, "ensureControlled return true, something wrong, pkg is default controlled, pkg:" + pkgName);
            }
        }
        if (pkgName != null && pkgName.equalsIgnoreCase("com.huawei.health")) {
            HwLog.i(TAG, "the package : com.huawei.healthis " + (controlled ? SMCSXMLHelper.ATTR_CONTROLLED : "not controlled"));
        }
        return controlled;
    }

    public static ProtectControlledPredicate create(Context ctx, boolean cache) {
        ProtectControlledPredicate pre = new ProtectControlledPredicate(ctx, cache);
        pre.executeTask();
        return pre;
    }

    private boolean checkControlledInProtectMap(String pkg) {
        Map<String, Boolean> result = (Map) getResult();
        if (result == null) {
            HwLog.e(TAG, "apply, getResult() is null!");
            return false;
        } else if (((Boolean) result.get(pkg)) == null) {
            return false;
        } else {
            return true;
        }
    }

    private boolean ensureControlled(String pkg) {
        if (this.mDefaultControlledMap != null) {
            Boolean controlled = (Boolean) this.mDefaultControlledMap.get(pkg);
            if (controlled != null) {
                return controlled.booleanValue();
            }
        }
        return ProtectAppControl.isDefalutControlled(HsmPackageManager.getInstance().getPkgInfo(pkg));
    }
}
