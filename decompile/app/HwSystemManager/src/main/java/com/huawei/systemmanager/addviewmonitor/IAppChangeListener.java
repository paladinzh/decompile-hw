package com.huawei.systemmanager.addviewmonitor;

public interface IAppChangeListener {
    void onPackageAdded(String str, AddViewAppInfo addViewAppInfo);

    void onPackageRemoved(String str);
}
