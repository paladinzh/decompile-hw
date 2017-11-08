package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import com.google.android.collect.Maps;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwAppCustomMgr;
import com.huawei.systemmanager.spacecleanner.engine.trash.Trash;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import tmsdk.fg.module.deepclean.DeepcleanManager;
import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.ScanProcessListener;

public class TencentScanTask extends Task {
    public static final String TAG = "TencentScanTask";
    private final DeepcleanManager mDeepCleanManager;
    private TencentSimpleListener mTencentListener = new TencentSimpleListener() {
        public void onScanStarted() {
            HwLog.i(TencentScanTask.TAG, "onScanStarted");
            TencentScanTask.this.onPublishStart();
        }

        public void onScanError(int error) {
            HwLog.i(TencentScanTask.TAG, "onScanError, error=" + error);
        }

        public void onScanProcessChange(int i, String s) {
            TencentScanTask.this.onPublishProgress(i, s);
        }

        public void onScanCanceled() {
            HwLog.i(TencentScanTask.TAG, "onScanCanceled callback called");
            TencentScanTask.this.setCanceled(true);
        }

        public void onRubbishFound(RubbishEntity rubbish) {
        }

        public void onScanFinished() {
            HwLog.i(TencentScanTask.TAG, "onScanFinished callback called");
            for (RubbishEntity rubbish : TencentScanTask.this.mDeepCleanManager.getmRubbishEntityManager().getRubbishes()) {
                if (!TencentScanTask.this.checkIsCanceled()) {
                    String pkg = rubbish.getPackageName();
                    boolean exist = HsmPackageManager.getInstance().packageExists(pkg, 0);
                    switch (rubbish.getRubbishType()) {
                        case 0:
                            if (!exist) {
                                HwLog.e(TencentScanTask.TAG, "App not installed, but in TYPE_APK_CUSTOM_DATA, pkg = " + pkg);
                                break;
                            } else {
                                TencentScanTask.this.dealRubbishResult(16384, rubbish);
                                break;
                            }
                        case 1:
                        case 2:
                            break;
                        case 4:
                            if (!exist) {
                                TencentScanTask.this.dealRubbishResult(8192, rubbish);
                                break;
                            } else {
                                HwLog.e(TencentScanTask.TAG, "App installed, but in INDEX_UNINSTALL_RETAIL, pkg = " + pkg);
                                break;
                            }
                        default:
                            break;
                    }
                }
                return;
            }
            for (Entry<String, TencenAppGroup> entry : TencentScanTask.this.trashMap.entrySet()) {
                TencentScanTask.this.onPublishItemUpdate((Trash) entry.getValue());
            }
            for (Entry<String, TencenTopVideoAppGroup> entry2 : TencentScanTask.this.trashVideoMap.entrySet()) {
                TencentScanTask.this.onPublishItemUpdate((Trash) entry2.getValue());
            }
            TencentScanTask.this.onPublishEnd();
        }
    };
    Map<String, TencenAppGroup> trashMap = Maps.newHashMap();
    Map<String, TencenTopVideoAppGroup> trashVideoMap = Maps.newHashMap();

    public TencentScanTask(Context ctx, DeepcleanManager deepcleanManager) {
        super(ctx);
        this.mDeepCleanManager = deepcleanManager;
    }

    public void cancel() {
        if (isEnd()) {
            HwLog.i(TAG, "cancel called, but this task is already end");
            return;
        }
        HwLog.i(TAG, "cancel called, set canceled!");
        setCanceled(true);
        this.mDeepCleanManager.cancelScan();
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 4;
    }

    protected int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_trash_tencent_scan);
    }

    protected void startWork(ScanParams params) {
        if (!this.mDeepCleanManager.startScan(3)) {
            HwLog.e(TAG, "start scan failed!");
        }
    }

    ScanProcessListener getTencentListener() {
        return this.mTencentListener;
    }

    private boolean checkIsCanceled() {
        if (!isCanceled()) {
            return false;
        }
        HwLog.i(TAG, "onScanFinished callback called, but task is canceled");
        onPublishEnd();
        return true;
    }

    private void dealRubbishResult(int type, RubbishEntity rubbish) {
        if (rubbish == null) {
            HwLog.i(TAG, "dealRubbishResult rubbish is null");
            return;
        }
        Set<String> appSet = ((HwAppCustomMgr) getParams().getCarry()).getHwCustomPkgs();
        String pkg = rubbish.getPackageName();
        boolean suggest = rubbish.isSuggest();
        String appName = rubbish.getAppName();
        if (type == 16384 && appSet.contains(pkg)) {
            HwLog.i(TAG, "This is hw app, ignore its data, pkg:" + pkg);
            return;
        }
        HwLog.d(TAG, "modelName:" + rubbish.getAppName() + ", pkg:" + pkg + ",isSuggest:" + suggest);
        TencentAppTrash trash;
        String pkgName;
        if (TopVideoFilter.checkIsTopVideo(pkg)) {
            trash = TencentAppTrash.create(rubbish, 65536, this.mParams);
            pkgName = trash.getPackageName();
            TencenTopVideoAppGroup appTopVideoTrash = (TencenTopVideoAppGroup) this.trashVideoMap.get(pkgName);
            if (appTopVideoTrash == null) {
                appTopVideoTrash = new TencenTopVideoAppGroup(65536, pkgName, appName, false);
                this.trashVideoMap.put(pkgName, appTopVideoTrash);
            }
            appTopVideoTrash.addChild(trash);
        } else {
            trash = TencentAppTrash.create(rubbish, type, this.mParams);
            pkgName = trash.getPackageName();
            TencenAppGroup appTrash = (TencenAppGroup) this.trashMap.get(pkgName);
            if (appTrash == null) {
                appTrash = new TencenAppGroup(type, pkgName, appName, false);
                this.trashMap.put(pkgName, appTrash);
            }
            appTrash.addChild(trash);
        }
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(16384), Integer.valueOf(8192), Integer.valueOf(65536));
    }

    public boolean isNormal() {
        return true;
    }
}
