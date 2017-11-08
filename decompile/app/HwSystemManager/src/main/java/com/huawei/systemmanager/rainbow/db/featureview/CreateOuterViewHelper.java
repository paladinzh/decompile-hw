package com.huawei.systemmanager.rainbow.db.featureview;

import android.database.sqlite.SQLiteDatabase;
import com.huawei.systemmanager.comm.database.DbOpWrapper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.rainbow.db.base.CloudConst;
import com.huawei.systemmanager.rainbow.db.base.CommonFeatureColumn;
import com.huawei.systemmanager.rainbow.db.base.PermissionColumn;
import java.util.List;

public class CreateOuterViewHelper {
    private static CreateOuterViewHelper mCreateViewHelper;

    public static synchronized CreateOuterViewHelper getInstance() {
        CreateOuterViewHelper createOuterViewHelper;
        synchronized (CreateOuterViewHelper.class) {
            if (mCreateViewHelper == null) {
                mCreateViewHelper = new CreateOuterViewHelper();
            }
            createOuterViewHelper = mCreateViewHelper;
        }
        return createOuterViewHelper;
    }

    private String getPermissionCodeString(List<PermissionColumn> permissionColumnList) {
        StringBuffer stringPermissionCode = new StringBuffer();
        int length = permissionColumnList.size();
        int index = 0;
        while (index < length - 1) {
            ((PermissionColumn) permissionColumnList.get(index)).appendPermissionCodeString(stringPermissionCode);
            stringPermissionCode.append("+");
            index++;
        }
        ((PermissionColumn) permissionColumnList.get(index)).appendPermissionCodeString(stringPermissionCode);
        stringPermissionCode.append(" AS permissionCode");
        return stringPermissionCode.toString();
    }

    private String getPermissionCfgString(List<PermissionColumn> permissionColumnList) {
        StringBuffer stringPermissionCfg = new StringBuffer();
        int length = permissionColumnList.size();
        int index = 0;
        while (index < length - 1) {
            ((PermissionColumn) permissionColumnList.get(index)).appendPermissionCfgString(stringPermissionCfg);
            stringPermissionCfg.append("+");
            index++;
        }
        ((PermissionColumn) permissionColumnList.get(index)).appendPermissionCfgString(stringPermissionCfg);
        stringPermissionCfg.append(" AS permissionCfg");
        return stringPermissionCfg.toString();
    }

    private String getPermissionTrustString() {
        StringBuffer stringTrust = new StringBuffer();
        stringTrust.append("CASE PERMISSION_TRUST_COL WHEN \"1\" THEN \"true\" ELSE \"false\" END ");
        stringTrust.append(" AS trust ");
        return stringTrust.toString();
    }

    private String getVagueCommonString(List<CommonFeatureColumn> commonColumnList) {
        StringBuffer stringCommon = new StringBuffer();
        int length = commonColumnList.size();
        int index = 0;
        while (index < length - 1) {
            ((CommonFeatureColumn) commonColumnList.get(index)).appendVagueCommonString(stringCommon);
            stringCommon.append(SqlMarker.COMMA_SEPARATE);
            index++;
        }
        ((CommonFeatureColumn) commonColumnList.get(index)).appendVagueCommonString(stringCommon);
        return stringCommon.toString();
    }

    private String genPermissionViewSqlSentence(String desViewName, String srcViewName) {
        List<PermissionColumn> permissionColumnList = CloudConst.getPermissionColumnList();
        if (permissionColumnList.isEmpty()) {
            return "";
        }
        String stringPermissionCode = getPermissionCodeString(permissionColumnList);
        String stringPermissionCfg = getPermissionCfgString(permissionColumnList);
        String stringTrust = getPermissionTrustString();
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS " + desViewName).append(" AS SELECT DISTINCT ").append("packageName AS packageName").append(SqlMarker.COMMA_SEPARATE).append(stringPermissionCode).append(SqlMarker.COMMA_SEPARATE).append(stringPermissionCfg).append(SqlMarker.COMMA_SEPARATE).append(stringTrust).append(" FROM " + srcViewName + " ").append(SqlMarker.SQL_END);
        return strBuf.toString();
    }

    private String genVaguePermissionViewSqlSentence(String desViewName, String srcViewName) {
        List<PermissionColumn> permissionColumnList = CloudConst.getPermissionColumnList();
        if (permissionColumnList.isEmpty()) {
            return "";
        }
        List<CommonFeatureColumn> commonColumnList = CloudConst.getCommonFeatureColumnList();
        if (commonColumnList.isEmpty()) {
            return "";
        }
        String stringPermissionCode = getPermissionCodeString(permissionColumnList);
        String stringPermissionCfg = getPermissionCfgString(permissionColumnList);
        String stringTrust = getPermissionTrustString();
        String stringNetwork = getNetworkString();
        String stringCommon = getVagueCommonString(commonColumnList);
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS " + desViewName).append(" AS SELECT DISTINCT ").append("packageName AS packageName").append(SqlMarker.COMMA_SEPARATE).append(stringPermissionCode).append(SqlMarker.COMMA_SEPARATE).append(stringPermissionCfg).append(SqlMarker.COMMA_SEPARATE).append(stringTrust).append(SqlMarker.COMMA_SEPARATE).append(stringNetwork).append(SqlMarker.COMMA_SEPARATE).append(stringCommon).append(" FROM " + srcViewName + " ").append(SqlMarker.SQL_END);
        return strBuf.toString();
    }

    public void genPermissionView(SQLiteDatabase db, String desViewName, String srcViewName) {
        DbOpWrapper.runSingleSqlSentence(db, genPermissionViewSqlSentence(desViewName, srcViewName));
    }

    public void genVaguePermissionView(SQLiteDatabase db, String desViewName, String srcViewName) {
        DbOpWrapper.runSingleSqlSentence(db, genVaguePermissionViewSqlSentence(desViewName, srcViewName));
    }

    public void genCommonFeatureView(SQLiteDatabase db, String srcViewName) {
        List<CommonFeatureColumn> commonColumnList = CloudConst.getCommonFeatureColumnList();
        if (!commonColumnList.isEmpty()) {
            for (CommonFeatureColumn currentFeature : commonColumnList) {
                DbOpWrapper.runSingleSqlSentence(db, genCommonViewSqlSentence(currentFeature, srcViewName));
            }
        }
    }

    private String genCommonViewSqlSentence(CommonFeatureColumn currentFeature, String srcViewName) {
        return currentFeature.genCommonViewSqlSentence(srcViewName);
    }

    public void genNetworkFeatureView(SQLiteDatabase db, String desViewName, String srcViewName) {
        String stringNetwork = getNetworkString();
        StringBuffer strBuf = new StringBuffer();
        strBuf.append("CREATE VIEW IF NOT EXISTS " + desViewName).append(" AS SELECT DISTINCT ").append("packageName AS packageName").append(SqlMarker.COMMA_SEPARATE).append(stringNetwork).append(" FROM " + srcViewName + " ").append(SqlMarker.SQL_END);
        DbOpWrapper.runSingleSqlSentence(db, strBuf.toString());
    }

    private String getNetworkString() {
        StringBuffer stringNetwork = new StringBuffer();
        stringNetwork.append("CASE NETWORK_DATA_COL WHEN \"1\" THEN \"false\" ELSE \"true\" END ");
        stringNetwork.append(" AS netDataPermission, ");
        stringNetwork.append("CASE NETWORK_WIFI_COL WHEN \"1\" THEN \"false\" ELSE \"true\" END ");
        stringNetwork.append(" AS netWifiPermission ");
        return stringNetwork.toString();
    }
}
