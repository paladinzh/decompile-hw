package com.huawei.systemmanager.rainbow.db.base;

import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.database.gfeature.FeatureToColumn;
import com.huawei.systemmanager.rainbow.db.featureview.FeatureViewConst;

public class CommonFeatureColumn {
    private String mDefaultValueColName;
    private String mFeatureColumnName;
    private String mFeatureName;
    private String mOuterViewName;

    public CommonFeatureColumn(String permissionColumnName, String permissionName, String outerViewName, String defaultValueColName) {
        this.mFeatureColumnName = permissionColumnName;
        this.mFeatureName = permissionName;
        this.mOuterViewName = outerViewName;
        this.mDefaultValueColName = defaultValueColName;
    }

    public void appendVagueCommonString(StringBuffer buf) {
        buf.append("CASE " + this.mFeatureColumnName + " WHEN " + "\"0\"" + " THEN " + "\"0\"" + " WHEN " + FeatureViewConst.HTTPS_COMMON_REMIND + " THEN " + FeatureViewConst.HTTPS_COMMON_REMIND + " ELSE " + "\"1\"" + " END " + " AS " + this.mDefaultValueColName);
    }

    public String genCommonViewSqlSentence(String srcViewName) {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS " + this.mOuterViewName).append(" AS SELECT DISTINCT ").append("packageName AS packageName").append(SqlMarker.COMMA_SEPARATE).append(this.mFeatureColumnName + " AS " + this.mDefaultValueColName).append(" FROM " + srcViewName + " WHERE " + this.mFeatureColumnName + " IS NOT NULL ").append(SqlMarker.SQL_END);
        return strBuf.toString();
    }

    public FeatureToColumn createFeatureToColumn() {
        return new FeatureToColumn(this.mFeatureColumnName, this.mFeatureName);
    }
}
