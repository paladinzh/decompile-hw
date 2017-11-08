package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.rubbish.SoftRubModel;

public class TmsAppScanTask extends Task {
    private static final String TAG = "TmsAppScanTask";
    private final DeepcleanManager mDeepCleanManager;
    private TmsUninstallAppTrash mUninstallResult;

    public TmsAppScanTask(Context context, DeepcleanManager deepcleanManage) {
        super(context);
        this.mDeepCleanManager = deepcleanManage;
    }

    protected void startWork(ScanParams p) {
        onPublishStart();
        if (p == null) {
            HwLog.e(TAG, "start work parma is null!");
        } else {
            SoftRubModel model = this.mDeepCleanManager.scanSoftRubbish((String) p.getCarry());
            if (model != null) {
                this.mUninstallResult = new TmsUninstallAppTrash(model);
            }
        }
        onPublishEnd();
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 5;
    }

    public List<Trash> getResult() {
        if (this.mUninstallResult == null) {
            return Collections.emptyList();
        }
        return HsmCollections.newArrayList(this.mUninstallResult);
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(8192));
    }

    public boolean isNormal() {
        return true;
    }
}
