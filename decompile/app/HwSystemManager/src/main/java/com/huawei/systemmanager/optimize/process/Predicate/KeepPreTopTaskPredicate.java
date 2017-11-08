package com.huawei.systemmanager.optimize.process.Predicate;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeepPreTopTaskPredicate extends FutureTaskPredicate<Set<String>, ProcessAppItem> {
    private static final String TAG = "KeepPreTopTaskPredicateextends";
    private Context mCtx;

    public KeepPreTopTaskPredicate(Context ctx) {
        this.mCtx = ctx;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        if (!((Set) getResult()).contains(input.getPackageName())) {
            return true;
        }
        HwLog.i(TAG, "KeepPreTopTaskPredicateextends " + input.getName() + ", pkg=" + input.getPackageName());
        return false;
    }

    protected Set<String> doInbackground() throws Exception {
        Set<String> result = new HashSet();
        RunningTaskInfo preTopTask = getPrevTopTaskInfo();
        if (preTopTask != null) {
            result.add(preTopTask.baseActivity.getPackageName());
            result.add(preTopTask.topActivity.getPackageName());
        }
        return result;
    }

    private RunningTaskInfo getPrevTopTaskInfo() {
        List<RunningTaskInfo> taskInfos = ((ActivityManager) this.mCtx.getSystemService("activity")).getRunningTasks(2);
        if (taskInfos == null || taskInfos.size() != 2) {
            return null;
        }
        return (RunningTaskInfo) taskInfos.get(1);
    }
}
