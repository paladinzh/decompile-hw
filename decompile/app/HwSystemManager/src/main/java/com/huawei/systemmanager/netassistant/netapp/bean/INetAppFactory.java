package com.huawei.systemmanager.netassistant.netapp.bean;

public interface INetAppFactory {
    AbsNetAppInfo create(String str, int i);

    int getType();
}
