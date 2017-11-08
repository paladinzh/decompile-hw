package com.huawei.systemmanager.spacecleanner.engine;

import android.content.Context;
import com.huawei.systemmanager.sdk.tmsdk.TMSEngineFeature;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine;
import com.huawei.systemmanager.spacecleanner.engine.base.ITrashEngine.IUpdateListener;
import com.huawei.systemmanager.spacecleanner.engine.base.SpaceConst;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwadapter.HwTrashEngine;
import com.huawei.systemmanager.spacecleanner.engine.tencentadapter.TencentTrashEngine;
import com.huawei.systemmanager.util.HwLog;

public class TrashEngineImpl implements ITrashEngine {
    private static final String TAG = "TrashEngineImpl";
    private final Context mContext;
    private ITrashEngine mHwEngine;
    private ITrashEngine mTencentEngine = null;

    public TrashEngineImpl(Context context) {
        this.mContext = context;
        this.mHwEngine = new HwTrashEngine(context);
        if (TMSEngineFeature.isSupportTMS()) {
            this.mTencentEngine = new TencentTrashEngine(context);
            if (!this.mTencentEngine.init()) {
                this.mTencentEngine = null;
                return;
            }
            return;
        }
        HwLog.i(TAG, "TMS is not support for spacecleaner!");
    }

    public Task getScanner(ScanParams p) {
        Task task = null;
        Task task2 = null;
        int type = p.getType();
        switch (type) {
            case 0:
            case 3:
            case 4:
                Context context = this.mContext;
                Task[] taskArr = new Task[2];
                if (this.mTencentEngine != null) {
                    task = this.mTencentEngine.getScanner(p);
                }
                taskArr[0] = task;
                taskArr[1] = this.mHwEngine.getScanner(p);
                task2 = new TotalScanTask(context, taskArr);
                break;
            case 1:
                if (this.mTencentEngine != null) {
                    task2 = this.mTencentEngine.getScanner(p);
                    break;
                }
                task2 = null;
                break;
            case 2:
                if (this.mHwEngine != null) {
                    task2 = new TotalScanTask(this.mContext, this.mHwEngine.getScanner(p));
                    break;
                }
                task2 = null;
                break;
            default:
                HwLog.e(TAG, "getScanner unknow scan param, type" + type);
                break;
        }
        if (task2 != null) {
            task2.setExecutor(SpaceConst.sExecutor);
        }
        return task2;
    }

    public void destory() {
        this.mHwEngine.destory();
        if (this.mTencentEngine != null) {
            this.mTencentEngine.destory();
        }
    }

    public void update(IUpdateListener updateListener) {
        this.mHwEngine.update(updateListener);
        if (this.mTencentEngine != null) {
            this.mTencentEngine.update(updateListener);
        }
    }

    public boolean init() {
        return true;
    }
}
