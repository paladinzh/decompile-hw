package com.huawei.systemmanager.comm.database;

public interface ITableInfo {
    String[][] getColumnDefines();

    String[] getIndexCols();

    String getTableName();
}
