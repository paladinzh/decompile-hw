package com.huawei.systemmanager.spacecleanner.setting;

public class CacheCleanSetting extends SpaceSwitchSetting {
    public CacheCleanSetting(String key) {
        super(key);
    }

    public void doSwitchOn() {
        schduleAutoCleanCache();
        schduleAutoCheckCache();
    }

    public void doSwitchOff() {
        cancelAutoCleanCache();
        cancelAutoCheckCache();
    }

    public void doAction() {
    }

    public void doCheck() {
    }

    private void schduleAutoCheckCache() {
    }

    private void schduleAutoCleanCache() {
    }

    private void cancelAutoCheckCache() {
    }

    private void cancelAutoCleanCache() {
    }
}
