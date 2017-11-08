package com.huawei.systemmanager.customize;

public abstract class HotaUpgradeHandler {
    protected int mChangeType = CustomizeManager.getInstance().checkConfigFileChange(this.mTargetFile);
    protected String mTargetFile;

    public abstract void onFileUpdated();

    public HotaUpgradeHandler(String fileName) {
        this.mTargetFile = fileName;
    }

    public void handleHotaUpgradeIfNeeded() {
        if (this.mChangeType == 0) {
            CustomizeManager.getInstance().finishConfigFileChange(this.mTargetFile);
        } else if (this.mChangeType != 4) {
            onFileUpdated();
            CustomizeManager.getInstance().finishConfigFileChange(this.mTargetFile);
        }
    }
}
