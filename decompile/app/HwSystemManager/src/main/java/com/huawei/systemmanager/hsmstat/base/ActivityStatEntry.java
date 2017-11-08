package com.huawei.systemmanager.hsmstat.base;

public class ActivityStatEntry extends StatEntry {
    public int acAction;

    public ActivityStatEntry(int acAction, String key, String value) {
        super(key, value);
        this.acAction = acAction;
    }
}
