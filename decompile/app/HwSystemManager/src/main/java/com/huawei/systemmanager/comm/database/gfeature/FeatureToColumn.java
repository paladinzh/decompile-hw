package com.huawei.systemmanager.comm.database.gfeature;

import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;

public class FeatureToColumn {
    private String mColName;
    private String mDftColValue;
    private String mFeatureName;

    public FeatureToColumn(String colName, String featureName, String dftColValue) {
        this.mColName = colName;
        this.mFeatureName = featureName;
        this.mDftColValue = dftColValue;
    }

    public FeatureToColumn(String colName, String featureName) {
        this(colName, featureName, "null");
    }

    public String getColName() {
        return this.mColName;
    }

    public String getFeatureName() {
        return this.mFeatureName;
    }

    public String getDftColValue() {
        return this.mDftColValue;
    }

    public void appendAbsViewSelection(StringBuffer strBuf, String colFeatureView) {
        strBuf.append(SqlMarker.COMMA_SEPARATE).append("ifnull (");
        strBuf.append(colFeatureView);
        strBuf.append(".").append(GFeatureTable.COL_FEATURE_VALUE);
        strBuf.append(SqlMarker.COMMA_SEPARATE).append(getDftColValue()).append(")");
        strBuf.append(" ").append(getColName());
    }

    public void appendAbsViewLeftJoin(StringBuffer strBuf, String pkgListViewName, String colFeatureView) {
        strBuf.append(" ").append(" LEFT JOIN ").append(colFeatureView);
        strBuf.append(" ON ").append(pkgListViewName).append(".").append("packageName");
        strBuf.append(" = ");
        strBuf.append(colFeatureView).append(".").append("packageName");
    }
}
