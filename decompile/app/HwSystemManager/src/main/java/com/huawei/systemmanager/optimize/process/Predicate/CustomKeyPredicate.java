package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.SmcsDbHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Map;

public class CustomKeyPredicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "CustomKeyPredicate";
    private StringCustomKeyPredicate mPredicate;

    public static class StringCustomKeyPredicate extends FutureTaskPredicate<Map<String, Boolean>, String> {
        private final Context mContext;

        public StringCustomKeyPredicate(Context ctx) {
            this.mContext = ctx;
            executeTask();
        }

        protected Map<String, Boolean> doInbackground() {
            return SmcsDbHelper.getDefaultKeyTaskPkgs(this.mContext);
        }

        public boolean apply(String arg0) {
            Map<String, Boolean> resultData = (Map) getResult();
            if (resultData == null) {
                return false;
            }
            Boolean keyTask = (Boolean) resultData.get(arg0);
            if (keyTask == null || !keyTask.booleanValue()) {
                return false;
            }
            HwLog.i(CustomKeyPredicate.TAG, "key task include qinqingguanhuai : " + arg0);
            return true;
        }
    }

    public CustomKeyPredicate(Context ctx) {
        this.mPredicate = new StringCustomKeyPredicate(ctx);
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        Map<String, Boolean> resultData = (Map) this.mPredicate.getResult();
        if (resultData == null) {
            return true;
        }
        String pkg = input.getPackageName();
        Boolean keyTask = (Boolean) resultData.get(pkg);
        if (keyTask == null) {
            return true;
        }
        input.setKeyTask(keyTask.booleanValue());
        HwLog.i(TAG, pkg + " set custom keytask:" + keyTask);
        return true;
    }
}
