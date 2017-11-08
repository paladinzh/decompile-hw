package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;

public class NonProtectedListAppPredicate extends FutureTaskPredicate<ArrayList<String>, ProcessAppItem> {
    private static final String TAG = "NonProtectedListAppPredicate";
    private final Context mContext;

    public NonProtectedListAppPredicate(Context ctx) {
        this.mContext = ctx;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        ArrayList<String> inProtectPkgs = (ArrayList) getResult();
        if (inProtectPkgs == null) {
            HwLog.e(TAG, getClass().getSimpleName() + " protectMap is null, must be something wrong!");
            inProtectPkgs = new ArrayList();
        }
        String pkg = input.getPackageName();
        boolean bFind = inProtectPkgs.contains(pkg);
        if (!bFind) {
            HwLog.d(TAG, "should not kill " + pkg + ", it is not in protect list!");
        }
        return bFind;
    }

    protected ArrayList<String> doInbackground() {
        return SmcsDbHelper.getAllControlled(this.mContext);
    }
}
