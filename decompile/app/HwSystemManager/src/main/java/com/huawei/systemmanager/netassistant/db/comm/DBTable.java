package com.huawei.systemmanager.netassistant.db.comm;

import android.net.Uri;

public abstract class DBTable {
    public abstract String getAuthority();

    public abstract String getPrimaryColumn();

    public abstract String getTableCreateCmd();

    public abstract String getTableDropCmd();

    public abstract String getTableName();

    public Uri getUri() {
        return Uri.parse("content://" + getAuthority() + "/" + getTableName());
    }
}
