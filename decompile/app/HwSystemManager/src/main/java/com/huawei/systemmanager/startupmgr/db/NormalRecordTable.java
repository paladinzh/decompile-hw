package com.huawei.systemmanager.startupmgr.db;

import com.huawei.systemmanager.comm.database.IBasicColumns;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColLimit;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColType;
import com.huawei.systemmanager.comm.database.ITableInfo;

public class NormalRecordTable implements IBasicColumns, ITableInfo {
    private static String[][] COLUMN_DEFINES = null;
    public static final String COL_PACKAGE_NAME = "packageName";
    public static final String COL_STARTUP_RESULT = "startupResult";
    public static final String COL_TIME_OF_DAY = "timeOfDay";
    public static final String COL_TIME_OF_LAST_EXACT = "timeOfLastExact";
    public static final String COL_TOTAL_COUNT = "totalCount";
    private static String[] INDEX_DEFINES = new String[]{"packageName", "startupResult", "timeOfDay"};
    public static final String TABLE_NAME = "normal_record";

    static {
        r0 = new String[6][];
        r0[0] = new String[]{"_id", ColLimit.COL_PRIMARY_LIMIT};
        r0[1] = new String[]{"packageName", ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[2] = new String[]{"startupResult", ColType.COL_TYPE_INT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[3] = new String[]{"timeOfDay", ColType.COL_TYPE_INT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[4] = new String[]{"timeOfLastExact", ColType.COL_TYPE_INT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[5] = new String[]{"totalCount", ColType.COL_TYPE_INT, ColLimit.COL_LIMIT_NOT_NULL};
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
