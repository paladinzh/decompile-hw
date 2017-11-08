package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model;

interface INumUnit<T> {
    long getComputableNum();

    float getNum();

    T getUnit();

    T[] getUnitContain();

    void setPackage(float f, T t);
}
