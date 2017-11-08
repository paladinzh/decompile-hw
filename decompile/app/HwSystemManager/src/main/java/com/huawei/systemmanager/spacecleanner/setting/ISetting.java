package com.huawei.systemmanager.spacecleanner.setting;

public interface ISetting<T> {
    void doAction();

    void doSettingChanged(T t);

    String getKey();

    T getValue();

    void onBackup(String str);

    void setValue(T t);
}
