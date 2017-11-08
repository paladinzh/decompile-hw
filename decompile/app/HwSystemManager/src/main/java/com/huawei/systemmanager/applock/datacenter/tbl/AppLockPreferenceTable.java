package com.huawei.systemmanager.applock.datacenter.tbl;

import com.huawei.systemmanager.comm.database.IBasicColumns;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColLimit;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColType;
import com.huawei.systemmanager.comm.database.ITableInfo;

public class AppLockPreferenceTable implements IBasicColumns, ITableInfo {
    private static String[][] COLUMN_DEFINES = null;
    public static final String COL_PREF_BACKUP = "prefbackup";
    public static final String COL_PREF_KEY = "prefkey";
    public static final String COL_PREF_VALUE = "prefvalue";
    private static String[] INDEX_DEFINES = new String[]{COL_PREF_KEY};
    public static final String TABLE_NAME = "applockpreference";
    public static final int VAL_PREF_BACKUP = 1;
    public static final int VAL_PREF_NOT_BACKUP = 0;

    static {
        r0 = new String[4][];
        r0[0] = new String[]{"_id", ColLimit.COL_PRIMARY_LIMIT};
        r0[1] = new String[]{COL_PREF_KEY, ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[2] = new String[]{COL_PREF_VALUE, ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[3] = new String[]{COL_PREF_BACKUP, ColType.COL_TYPE_INT, ColLimit.COL_LIMIT_NOT_NULL};
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
