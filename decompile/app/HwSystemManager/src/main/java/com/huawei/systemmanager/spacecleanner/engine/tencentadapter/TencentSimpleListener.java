package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import tmsdk.fg.module.deepclean.RubbishEntity;
import tmsdk.fg.module.deepclean.ScanProcessListener;

public class TencentSimpleListener implements ScanProcessListener {
    public static final ScanProcessListener sEmptyScanListener = new TencentSimpleListener();

    public void onCleanCancel() {
    }

    public void onCleanError(int arg0) {
    }

    public void onCleanFinish() {
    }

    public void onCleanProcessChange(long arg0, int arg1) {
    }

    public void onCleanStart() {
    }

    public void onRubbishFound(RubbishEntity arg0) {
    }

    public void onScanCanceled() {
    }

    public void onScanError(int arg0) {
    }

    public void onScanFinished() {
    }

    public void onScanProcessChange(int arg0, String arg1) {
    }

    public void onScanStarted() {
    }
}
