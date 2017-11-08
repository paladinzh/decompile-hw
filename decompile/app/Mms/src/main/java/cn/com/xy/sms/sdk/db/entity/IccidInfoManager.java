package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
public class IccidInfoManager {
    public static final String ADD_SIM_INDEX = "ALTER TABLE tb_phone_info ADD COLUMN sim_index INTEGER DEFAULT -1 ";
    public static final String ADD_USER_AREACODE = "ALTER TABLE tb_phone_info ADD COLUMN user_areacode TEXT ";
    public static final String ADD_USER_OPERATOR = "ALTER TABLE tb_phone_info ADD COLUMN user_operator TEXT ";
    public static final String ADD_USER_PROVINCES = "ALTER TABLE tb_phone_info ADD COLUMN user_provinces TEXT ";
    public static final String AREACODE = "areacode";
    public static final String CITY = "city";
    public static final String CNUM = "cnum";
    public static final String CREATE_TABLE = "create table  if not exists tb_phone_info (id INTEGER PRIMARY KEY,iccid TEXT ,city TEXT,provinces TEXT,operator TEXT,areacode TEXT,ispost INTEGER DEFAULT 0,num TEXT,cnum TEXT,updateTime LONG,deft  INTEGER DEFAULT 0,net_updateTime LONG DEFAULT 0,user_provinces TEXT,user_areacode TEXT,user_operator TEXT,sim_index INTEGER DEFAULT -1)";
    public static final String DEFT = "deft";
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_phone_info";
    public static final String ICCID = "iccid";
    public static final String ID = "id";
    public static final String ISPOST = "ispost";
    public static final String NET_UPDATE_TIME = "net_updateTime";
    public static final String NUM = "num";
    public static final String OPERATOR = "operator";
    public static final String PROVINCES = "provinces";
    public static final String SIM_INDEX = "sim_index";
    public static final String TABLE_NAME = "tb_phone_info";
    public static final String UPDATE_TIME = "updateTime";
    public static final String USER_AREACODE = "user_areacode";
    public static final String USER_OPERATOR = "user_operator";
    public static final String USER_PROVINCES = "user_provinces";

    private static long a(SQLiteDatabase sQLiteDatabase, String str, int i, String str2, String str3, String str4) {
        Object obj = 1;
        try {
            long update;
            ContentValues contentValues = new ContentValues();
            contentValues.put(ICCID, str);
            contentValues.put(SIM_INDEX, Integer.valueOf(i));
            if (!StringUtils.isNull(str2)) {
                contentValues.put(USER_PROVINCES, str2.trim());
                contentValues.put(UPDATE_TIME, Long.valueOf(System.currentTimeMillis()));
            }
            if (StringUtils.isNull(str3)) {
                contentValues.put(USER_AREACODE, getProviceCode(str2));
            } else {
                contentValues.put(USER_AREACODE, str3.trim());
            }
            if (!StringUtils.isNull(str4)) {
                contentValues.put(USER_OPERATOR, str4);
            }
            if (StringUtils.isNull(str)) {
                update = (long) sQLiteDatabase.update(TABLE_NAME, contentValues, "sim_index = " + i + " AND (iccid" + " IS NULL OR iccid" + "='' )", null);
            } else {
                update = (long) sQLiteDatabase.update(TABLE_NAME, contentValues, "iccid = ?", new String[]{str});
                if ((update >= 1 ? 1 : null) == null) {
                    sQLiteDatabase.execSQL("UPDATE tb_phone_info SET sim_index= -1 WHERE sim_index=" + i + " AND iccid" + " IS NOT NULL");
                }
            }
            if (update < 1) {
                obj = null;
            }
            if (obj == null) {
                update = sQLiteDatabase.insert(TABLE_NAME, null, contentValues);
            }
            return update;
        } catch (Throwable th) {
            return -1;
        }
    }

    private static boolean a(String str, String str2) {
        return (str == null || str2 == null || str.indexOf(str2) == -1) ? false : true;
    }

    public static int deleteIccidInfo(String str, int i) {
        String str2 = (str != null && str.length() > 0) ? "iccid='" + str + "'" : "(iccid IS NULL OR iccid='' ) AND sim_index='" + i + "'";
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_PROVINCES, "");
            contentValues.put(USER_AREACODE, "");
            contentValues.put(USER_OPERATOR, "");
            return DBManager.update(TABLE_NAME, contentValues, str2, null);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static String getHead(IccidInfo iccidInfo) {
        return (iccidInfo == null || iccidInfo.isPost != 0 || StringUtils.isNull(iccidInfo.iccid) || StringUtils.isNull(iccidInfo.num) || StringUtils.isNull(iccidInfo.cnum)) ? "" : iccidInfo.num + ";" + iccidInfo.cnum + ";" + iccidInfo.iccid;
    }

    public static String getProviceCode(String str) {
        r4 = new String[34][];
        r4[0] = new String[]{"北京", "BJ"};
        r4[1] = new String[]{"上海", "SH"};
        r4[2] = new String[]{"天津", "TJ"};
        r4[3] = new String[]{"重庆", "CQ"};
        r4[4] = new String[]{"黑龙江", "HL"};
        r4[5] = new String[]{"吉林", "JL"};
        r4[6] = new String[]{"辽宁", "LN"};
        r4[7] = new String[]{"新疆", "XJ"};
        r4[8] = new String[]{"西藏", "XZ"};
        r4[9] = new String[]{"内蒙古", "NM"};
        r4[10] = new String[]{"甘肃", "GS"};
        r4[11] = new String[]{"青海", "QH"};
        r4[12] = new String[]{"陕西", "XA"};
        r4[13] = new String[]{"宁夏", "NX"};
        r4[14] = new String[]{"山西", "SX"};
        r4[15] = new String[]{"山东", "SD"};
        r4[16] = new String[]{"安徽", "AW"};
        r4[17] = new String[]{"河南", "HN"};
        r4[18] = new String[]{"河北", "HB"};
        r4[19] = new String[]{"浙江", "ZJ"};
        r4[20] = new String[]{"江苏", "JS"};
        r4[21] = new String[]{"湖南", "CS"};
        r4[22] = new String[]{"湖北", "WH"};
        r4[23] = new String[]{"贵州", "GZ"};
        r4[24] = new String[]{"四川", "SC"};
        r4[25] = new String[]{"江西", "JX"};
        r4[26] = new String[]{"云南", "YN"};
        r4[27] = new String[]{"广东", "GD"};
        r4[28] = new String[]{"广西", "GX"};
        r4[29] = new String[]{"福建", "FJ"};
        r4[30] = new String[]{"海南", "HK"};
        r4[31] = new String[]{"香港", "XG"};
        r4[32] = new String[]{"澳门", "OM"};
        r4[33] = new String[]{"台湾", "TW"};
        for (int i = 0; i < 34; i++) {
            String str2 = r4[i][0];
            int i2 = (str == null || str2 == null || str.indexOf(str2) == -1) ? 0 : 1;
            if (i2 != 0) {
                return r4[i][1];
            }
        }
        return null;
    }

    public static long insertIccid(String str, boolean z, String str2, String str3, String str4, String str5, Context context) {
        long j = -1;
        try {
            ContentValues contentValues;
            long update;
            IccidInfo queryIccidInfo;
            ContentValues contentValues2 = new ContentValues();
            contentValues2.put(ICCID, str);
            if (!StringUtils.isNull(str2)) {
                if (str2.indexOf(";") == -1) {
                    if (!StringUtils.isNull(str2)) {
                        contentValues2.put(PROVINCES, str2.trim());
                        contentValues2.put(UPDATE_TIME, Long.valueOf(System.currentTimeMillis()));
                    }
                    if (StringUtils.isNull(str3)) {
                        contentValues2.put(AREACODE, getProviceCode(str2));
                    } else {
                        contentValues2.put(AREACODE, str3.trim());
                    }
                    if (!StringUtils.isNull(str4)) {
                        contentValues2.put("city", str4.trim());
                    }
                    if (!StringUtils.isNull(str5)) {
                        contentValues2.put(OPERATOR, str5);
                    }
                    if (z) {
                        contentValues = new ContentValues();
                        contentValues.put(DEFT, Integer.valueOf(0));
                        DBManager.update(TABLE_NAME, contentValues, null, null);
                    }
                    contentValues2.put(DEFT, Integer.valueOf(z ? 0 : 1));
                    update = (long) DBManager.update(TABLE_NAME, contentValues2, "iccid = ?", new String[]{str});
                    if ((update < 1 ? 1 : null) != null) {
                        try {
                            contentValues2.put(SIM_INDEX, Integer.valueOf(-1));
                            update = DBManager.insert(TABLE_NAME, contentValues2);
                            IccidLocationUtil.putIccidAreaCodeToCache(str, contentValues2.getAsString(AREACODE), str5, null, null, -1, 0);
                            return update;
                        } catch (Throwable th) {
                            j = update;
                        }
                    } else {
                        queryIccidInfo = queryIccidInfo(str, Constant.getContext());
                        if (queryIccidInfo != null) {
                            return update;
                        }
                        IccidLocationUtil.putIccidAreaCodeToCache(str, queryIccidInfo.areaCode, queryIccidInfo.operator, queryIccidInfo.userAreacode, queryIccidInfo.userOperator, queryIccidInfo.simIndex, queryIccidInfo.deft);
                        return update;
                    }
                }
            }
            if (!StringUtils.isNull(str2)) {
                String[] split = str2.split(";");
                if (split.length > 0) {
                    contentValues2.put(PROVINCES, split[0]);
                    contentValues2.put(UPDATE_TIME, Long.valueOf(System.currentTimeMillis()));
                    contentValues2.put(AREACODE, getProviceCode(split[0]));
                }
                if (split.length >= 2) {
                    contentValues2.put("city", split[1]);
                }
            }
            if (z) {
                contentValues = new ContentValues();
                contentValues.put(DEFT, Integer.valueOf(0));
                DBManager.update(TABLE_NAME, contentValues, null, null);
            }
            if (z) {
            }
            contentValues2.put(DEFT, Integer.valueOf(z ? 0 : 1));
            update = (long) DBManager.update(TABLE_NAME, contentValues2, "iccid = ?", new String[]{str});
            if (update < 1) {
            }
            if ((update < 1 ? 1 : null) != null) {
                queryIccidInfo = queryIccidInfo(str, Constant.getContext());
                if (queryIccidInfo != null) {
                    return update;
                }
                IccidLocationUtil.putIccidAreaCodeToCache(str, queryIccidInfo.areaCode, queryIccidInfo.operator, queryIccidInfo.userAreacode, queryIccidInfo.userOperator, queryIccidInfo.simIndex, queryIccidInfo.deft);
                return update;
            }
            contentValues2.put(SIM_INDEX, Integer.valueOf(-1));
            update = DBManager.insert(TABLE_NAME, contentValues2);
            IccidLocationUtil.putIccidAreaCodeToCache(str, contentValues2.getAsString(AREACODE), str5, null, null, -1, 0);
            return update;
        } catch (Throwable th2) {
            return j;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean insertOrUpdateIccid(String str, int i, String str2, String str3, String str4, boolean z) {
        SQLiteDatabase sQLiteDatabase = null;
        if (i < 0 || StringUtils.isNull(str2) || StringUtils.isNull(str3) || StringUtils.isNull(str4)) {
            return false;
        }
        try {
            sQLiteDatabase = DBManager.getSQLiteDatabase();
            sQLiteDatabase.beginTransaction();
            if (a(sQLiteDatabase, str, i, str2, str3, str4) >= 1) {
                sQLiteDatabase.setTransactionSuccessful();
                if (sQLiteDatabase != null) {
                    try {
                        if (sQLiteDatabase.inTransaction()) {
                            sQLiteDatabase.endTransaction();
                        }
                        DBManager.close(sQLiteDatabase);
                    } catch (Throwable th) {
                        DBManager.close(sQLiteDatabase);
                    }
                }
                return true;
            }
            if (sQLiteDatabase != null) {
                try {
                    if (sQLiteDatabase.inTransaction()) {
                        sQLiteDatabase.endTransaction();
                    }
                    DBManager.close(sQLiteDatabase);
                } catch (Throwable th2) {
                    DBManager.close(sQLiteDatabase);
                }
            }
            return false;
        } catch (Throwable th3) {
            Throwable th4 = th3;
            SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
            Throwable th5 = th4;
            if (sQLiteDatabase2 != null) {
                try {
                    if (sQLiteDatabase2.inTransaction()) {
                        sQLiteDatabase2.endTransaction();
                    }
                    DBManager.close(sQLiteDatabase2);
                } catch (Throwable th6) {
                    DBManager.close(sQLiteDatabase2);
                }
            }
            throw th5;
        }
    }

    public static IccidInfo queryDeftIccidInfo(Context context) {
        return queryIccidInfo(null, context);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static IccidInfo queryIccidInfo(String str, int i) {
        XyCursor query;
        XyCursor xyCursor;
        Throwable th;
        IccidInfo iccidInfo = null;
        try {
            String str2 = StringUtils.isNull(str) ? i < 0 ? "deft=1" : "(iccid IS NULL OR iccid='' ) AND sim_index='" + i + "'" : "iccid='" + str + "'";
            query = DBManager.query(TABLE_NAME, new String[]{"id", "city", PROVINCES, UPDATE_TIME, OPERATOR, ISPOST, NUM, CNUM, NET_UPDATE_TIME, AREACODE, ICCID, DEFT, USER_PROVINCES, USER_AREACODE, USER_OPERATOR, SIM_INDEX}, str2, null);
            if (query != null) {
                IccidInfo iccidInfo2;
                try {
                    if (query.moveToNext()) {
                        iccidInfo2 = new IccidInfo();
                        iccidInfo2.id = query.getInt(query.getColumnIndex("id"));
                        iccidInfo2.city = query.getString(query.getColumnIndex("city"));
                        iccidInfo2.operator = query.getString(query.getColumnIndex(OPERATOR));
                        iccidInfo2.areaCode = query.getString(query.getColumnIndex(AREACODE));
                        iccidInfo2.provinces = query.getString(query.getColumnIndex(PROVINCES));
                        iccidInfo2.updateTime = query.getLong(query.getColumnIndex(UPDATE_TIME));
                        iccidInfo2.iccid = query.getString(query.getColumnIndex(ICCID));
                        iccidInfo2.isPost = query.getInt(query.getColumnIndex(ISPOST));
                        iccidInfo2.num = query.getString(query.getColumnIndex(NUM));
                        iccidInfo2.cnum = query.getString(query.getColumnIndex(CNUM));
                        iccidInfo2.netUpdateTime = query.getLong(query.getColumnIndex(NET_UPDATE_TIME));
                        iccidInfo2.deft = query.getInt(query.getColumnIndex(DEFT));
                        iccidInfo2.userProvinces = query.getString(query.getColumnIndex(USER_PROVINCES));
                        iccidInfo2.userAreacode = query.getString(query.getColumnIndex(USER_AREACODE));
                        iccidInfo2.userOperator = query.getString(query.getColumnIndex(USER_OPERATOR));
                        iccidInfo2.simIndex = query.getInt(query.getColumnIndex(SIM_INDEX));
                        iccidInfo = iccidInfo2;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    XyCursor.closeCursor(query, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th3) {
            query = null;
            th = th3;
            XyCursor.closeCursor(query, true);
            throw th;
        }
        return iccidInfo;
    }

    public static IccidInfo queryIccidInfo(String str, Context context) {
        return queryIccidInfo(str, -1);
    }

    public static void updateIccidCnum(String str, String str2, String str3, Context context) {
        try {
            if (!(StringUtils.isNull(str) || StringUtils.isNull(str2) || StringUtils.isNull(str3))) {
                if (str3.equals("10086") || str3.equals("10010") || str3.equals("10000")) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(NUM, str3);
                    contentValues.put(CNUM, str2);
                    DBManager.update(TABLE_NAME, contentValues, "iccid = ? and ispost = 0", new String[]{str});
                }
            }
        } catch (Throwable th) {
        }
    }
}
