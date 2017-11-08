package com.huawei.systemmanager.rainbow.db.base;

import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;

public class PermissionColumn {
    public String mPermissionColumnName;
    public String mPermissionName;
    public int mPermissionType;

    public PermissionColumn(String permissionColumnName, String permissionName, int permissionType) {
        this.mPermissionColumnName = permissionColumnName;
        this.mPermissionName = permissionName;
        this.mPermissionType = permissionType;
    }

    public void appendPermissionCodeString(StringBuffer buf) {
        buf.append("CASE " + this.mPermissionColumnName + " WHEN " + "\"0\"" + " THEN " + this.mPermissionType + " WHEN " + "\"1\"" + " THEN " + this.mPermissionType + " ELSE " + "0" + " END ");
    }

    public void appendPermissionCfgString(StringBuffer buf) {
        buf.append("CASE " + this.mPermissionColumnName + " WHEN " + "\"1\"" + " THEN " + this.mPermissionType + " ELSE " + "0" + " END ");
    }

    public FeatureToColumn genFeatureToColunm() {
        return new FeatureToColumn(this.mPermissionColumnName, this.mPermissionName);
    }
}
