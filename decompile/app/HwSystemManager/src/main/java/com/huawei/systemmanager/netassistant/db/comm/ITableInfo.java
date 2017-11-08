package com.huawei.systemmanager.netassistant.db.comm;

public abstract class ITableInfo<T> {
    public abstract ITableInfo clear();

    public abstract ITableInfo get();

    public abstract ITableInfo save(T... tArr);
}
