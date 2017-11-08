package com.huawei.systemmanager.comm.database.gfeature;

import com.huawei.systemmanager.comm.database.IBasicColumns;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColLimit;
import com.huawei.systemmanager.comm.database.IDatabaseConst.ColType;
import com.huawei.systemmanager.comm.database.ITableInfo;

public class GFeatureTable implements IBasicColumns, ITableInfo {
    private static String[][] COLUMN_DEFINES = null;
    public static final String COL_FEATURE_NAME = "featureName";
    public static final String COL_FEATURE_VALUE = "featureValue";
    public static final String COL_PACKAGE_NAME = "packageName";
    private static final String DUMMY_TABLE_NAME = "DUMMY_TABLE_NAME";
    private static String[] INDEX_DEFINES = new String[]{"packageName", COL_FEATURE_NAME};

    static {
        r0 = new String[4][];
        r0[0] = new String[]{"_id", ColLimit.COL_PRIMARY_LIMIT};
        r0[1] = new String[]{"packageName", ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[2] = new String[]{COL_FEATURE_NAME, ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        r0[3] = new String[]{COL_FEATURE_VALUE, ColType.COL_TYPE_TXT, ColLimit.COL_LIMIT_NOT_NULL};
        COLUMN_DEFINES = r0;
    }

    public String getTableName() {
        return DUMMY_TABLE_NAME;
    }

    public String[][] getColumnDefines() {
        return COLUMN_DEFINES;
    }

    public String[] getIndexCols() {
        return INDEX_DEFINES;
    }
}
