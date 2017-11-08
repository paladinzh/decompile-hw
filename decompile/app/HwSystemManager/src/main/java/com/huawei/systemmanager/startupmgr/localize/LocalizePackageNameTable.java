package com.huawei.systemmanager.startupmgr.localize;

import com.huawei.systemmanager.comm.database.IBasicColumns;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColLimit;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColType;
import com.huawei.systemmanager.comm.database.ITableInfo;

public class LocalizePackageNameTable implements IBasicColumns, ITableInfo {
    private static String[][] COLUMN_DEFINES = null;
    public static final String COL_LOCALIZE_NAME = "localizeName";
    public static final String COL_PACKAGE_NAME = "packageName";
    private static String[] INDEX_DEFINES = new String[]{"packageName"};
    public static final String TABLE_NAME = "localize_package";

    static {
        r0 = new String[3][];
        r0[0] = new String[]{"_id", ColLimit.COL_PRIMARY_LIMIT};
        r0[1] = new String[]{"packageName", ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[2] = new String[]{COL_LOCALIZE_NAME, ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        COLUMN_DEFINES = r0;
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public String[][] getColumnDefines() {
        return (String[][]) COLUMN_DEFINES.clone();
    }

    public String[] getIndexCols() {
        return (String[]) INDEX_DEFINES.clone();
    }
}
