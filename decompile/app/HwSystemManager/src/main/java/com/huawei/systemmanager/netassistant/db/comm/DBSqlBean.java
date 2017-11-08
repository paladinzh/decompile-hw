package com.huawei.systemmanager.netassistant.db.comm;

import java.util.ArrayList;
import java.util.List;

public class DBSqlBean {
    private List<Object> mBindArgs = new ArrayList();
    private String mSql;

    public DBSqlBean(String sql) {
        this.mSql = sql;
    }

    public DBSqlBean(String sql, List<Object> bindArgs) {
        this.mSql = sql;
        this.mBindArgs = bindArgs;
    }

    public String getSql() {
        return this.mSql;
    }

    public void setSql(String sql) {
        this.mSql = sql;
    }

    public List<Object> getBindArgs() {
        return this.mBindArgs;
    }

    public void setBindArgs(List<Object> bindArgs) {
        this.mBindArgs = bindArgs;
    }
}
