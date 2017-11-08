package com.huawei.systemmanager.preventmode.util;

public interface IPreventDataChange {

    public static class DefaultImpl implements IPreventDataChange {
        public void onZenModeChange() {
        }

        public void onZenModeConfigChange() {
        }
    }

    void onZenModeChange();

    void onZenModeConfigChange();
}
