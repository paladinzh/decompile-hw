package com.huawei.systemmanager.netassistant.db.comm;

import android.content.ContentValues;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.rainbow.vaguerule.VagueRegConst;
import java.util.ArrayList;
import java.util.List;

public final class InsertSqlUtils {
    private InsertSqlUtils() {
    }

    public static List<DBSqlBean> createInsertSql(String tableName, ContentValues[] values) {
        List<DBSqlBean> dbSqlBeans = new ArrayList();
        if (tableName == null || values == null || values.length == 0) {
            return dbSqlBeans;
        }
        for (int i = 0; i < values.length; i++) {
            DBSqlBean bean = new DBSqlBean();
            bean.setSql(createInsertSqlStatement(tableName, values[i]));
            bean.setBindArgs(createInsertValues(values[i]));
            dbSqlBeans.add(bean);
        }
        return dbSqlBeans;
    }

    private static String createInsertSqlStatement(String tableName, ContentValues values) {
        if (tableName == null || values == null) {
            return null;
        }
        int len = values.size();
        if (len == 0) {
            return null;
        }
        StringBuilder sqlStatement = new StringBuilder();
        sqlStatement.append("INSERT INTO ").append(tableName).append("(");
        int i = 0;
        for (String column : values.keySet()) {
            if (i != 0) {
                sqlStatement.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
            sqlStatement.append(column);
            i++;
        }
        sqlStatement.append(") VALUES ( ");
        for (int j = 0; j < len; j++) {
            if (j != 0) {
                sqlStatement.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            }
            sqlStatement.append(VagueRegConst.REG_ONE_CHAR);
        }
        sqlStatement.append(");");
        return sqlStatement.toString();
    }

    private static List<Object> createInsertValues(ContentValues values) {
        if (values == null) {
            return null;
        }
        List<Object> bingValues = new ArrayList();
        for (String asString : values.keySet()) {
            bingValues.add(values.getAsString(asString));
        }
        return bingValues;
    }
}
