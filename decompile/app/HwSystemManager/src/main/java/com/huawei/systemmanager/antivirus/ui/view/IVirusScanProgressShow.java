package com.huawei.systemmanager.antivirus.ui.view;

public interface IVirusScanProgressShow {

    public enum ScanStatus {
        DANGER,
        RISK,
        SAFE
    }

    void cancel();

    void finish(ScanStatus scanStatus);

    void initView();

    void play();

    void show(String str);
}
