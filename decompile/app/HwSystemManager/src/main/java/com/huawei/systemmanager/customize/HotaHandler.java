package com.huawei.systemmanager.customize;

import com.huawei.systemmanager.util.HwLog;

public class HotaHandler extends HotaUpgradeHandler {
    private static final String TAG = "HotaHandler";

    public HotaHandler(String fileName) {
        super(fileName);
    }

    public void onFileUpdated() {
        HwLog.i(TAG, "onFileUpdated " + this.mTargetFile);
        switch (this.mChangeType) {
            case 1:
                onConfigCreated();
                return;
            case 2:
                onConfigDeleted();
                return;
            case 3:
                onConfigUpdated();
                return;
            default:
                return;
        }
    }

    protected void onConfigDeleted() {
    }

    protected void onConfigCreated() {
    }

    protected void onConfigUpdated() {
    }
}
