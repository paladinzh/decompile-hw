package com.huawei.systemmanager.spacecleanner.engine.hwscanner;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.ProcessFilterPolicy;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwAppProcessTrash;
import com.huawei.systemmanager.spacecleanner.engine.trash.AppProcessTrash;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class ProcessScanTask extends Task {
    private static final String TAG = "ProcessScanTask";
    private static final Function<ProcessAppItem, AppProcessTrash> sTransFunc = new Function<ProcessAppItem, AppProcessTrash>() {
        public AppProcessTrash apply(ProcessAppItem processAppItem) {
            return HwAppProcessTrash.create(processAppItem);
        }
    };

    public ProcessScanTask(Context context) {
        super(context);
    }

    public List<Integer> getSupportTrashType() {
        return Lists.newArrayList(Integer.valueOf(32768));
    }

    protected void doTask(ScanParams p) {
        onPublishStart();
        String currentTaskTopPkg = getCurrentTopStackPkg();
        for (ProcessAppItem item : ProcessFilterPolicy.getRunningApps(getContext())) {
            if (!item.isKeyProcess()) {
                if (Objects.equal(item.getPackageName(), currentTaskTopPkg)) {
                    HwLog.i(TAG, "Its current task top pkg, did not show. pkg:" + currentTaskTopPkg);
                } else {
                    onPublishItemUpdate((AppProcessTrash) sTransFunc.apply(item));
                }
            }
        }
        onPublishEnd();
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 6;
    }

    private String getCurrentTopStackPkg() {
        String topStackPkg = "";
        try {
            List<RunningTaskInfo> taskInfos = ((ActivityManager) getContext().getSystemService("activity")).getRunningTasks(1);
            if (taskInfos != null && taskInfos.size() > 0) {
                topStackPkg = ((RunningTaskInfo) taskInfos.get(0)).baseActivity.getPackageName();
            }
        } catch (Exception ex) {
            HwLog.e(TAG, "preTopTask catch exception: " + ex.getMessage());
        }
        return topStackPkg;
    }

    public boolean isNormal() {
        return true;
    }
}
