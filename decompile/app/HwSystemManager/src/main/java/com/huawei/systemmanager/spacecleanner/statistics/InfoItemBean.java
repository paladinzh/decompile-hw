package com.huawei.systemmanager.spacecleanner.statistics;

public class InfoItemBean {
    private String mKey;
    private String mValue;

    public String getValue() {
        return this.mValue;
    }

    public String getKey() {
        return this.mKey;
    }

    public InfoItemBean(String key, String value) {
        this.mKey = key;
        this.mValue = value;
    }

    public String toString() {
        return this.mKey + ":" + this.mValue;
    }
}
