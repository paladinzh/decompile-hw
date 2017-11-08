package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import android.provider.Telephony.Sms;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;

public class DefaultSmsPredicate extends FutureTaskPredicate<String, ProcessAppItem> {
    private static final String TAG = "DefaultSmsPredicate";
    private final Context mContext;

    public DefaultSmsPredicate(Context ctx) {
        this.mContext = ctx;
    }

    public boolean apply(ProcessAppItem item) {
        if (item == null) {
            return false;
        }
        String pkg = item.getPackageName();
        boolean filter = false;
        if (pkg != null && pkg.equals(getResult())) {
            filter = true;
        }
        if (!filter) {
            return true;
        }
        HwLog.i(TAG, "DefaultSmsPredicate = " + item.getName() + ", pkg=" + pkg);
        return false;
    }

    protected String doInbackground() throws Exception {
        return getDefaultSmsPackage();
    }

    private String getDefaultSmsPackage() {
        String defaultPkgName = null;
        try {
            defaultPkgName = Sms.getDefaultSmsPackage(this.mContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return defaultPkgName;
    }
}
