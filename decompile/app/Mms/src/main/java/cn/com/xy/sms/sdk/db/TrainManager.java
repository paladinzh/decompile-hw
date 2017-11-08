package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DateUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.io.LineNumberReader;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class TrainManager {
    public static final String ADD_DATA_TIME = "ALTER TABLE tb_train6 ADD COLUMN data_time LONG default 0 ";
    public static final String ADD_DAY = "ALTER TABLE tb_train6 ADD COLUMN day TEXT ";
    public static final String ADD_STATION_LIST = "ALTER TABLE tb_train6 ADD COLUMN station_list TEXT ";
    public static final String CREATE_TABLE = "create table  if not exists tb_train6 (id INTEGER PRIMARY KEY,train_num TEXT not null,start_city TEXT,end_city TEXT,train_type INTEGER default 0,start_time TEXT,end_time TEXT,mileage TEXT,station_list TEXT,duration TEXT,day TEXT,data_time LONG default 0)";
    public static final String DATA_TIME = "data_time";
    public static final String DAY = "day";
    public static final String DROP_OLD_TABLE = " DROP TABLE IF EXISTS tb_train ";
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_train6";
    public static final String DURATION = "duration";
    public static final String END_CITY = "end_city";
    public static final String END_TIME = "end_time";
    public static final String ID = "id";
    public static final String MILEAGE = "mileage";
    public static final String START_CITY = "start_city";
    public static final String START_TIME = "start_time";
    public static final String STATION_LIST = "station_list";
    public static final String TABLE_NAME = "tb_train6";
    public static final String TRAIN_NUM = "train_num";
    public static final String TRAIN_TYPE = "train_type";

    private static JSONObject b(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                if (jSONObject.has("stations")) {
                    JSONObject jSONObject2 = new JSONObject();
                    JSONArray jSONArray = jSONObject.getJSONArray("stations");
                    JSONArray jSONArray2 = new JSONArray();
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jSONObject3 = new JSONObject();
                        JSONObject jSONObject4 = jSONArray.getJSONObject(i);
                        jSONObject3.put("name", jSONObject4.optString("cz"));
                        jSONObject3.put("spt", jSONObject4.optString("tcsj"));
                        jSONObject3.put("stt", jSONObject4.optString("kcsj"));
                        jSONObject3.put(DAY, jSONObject4.optString("rq"));
                        jSONObject3.put("travel_time", jSONObject4.optString("qjls"));
                        jSONObject3.put("cx", jSONObject4.optString("cx"));
                        jSONObject3.put("tcTime", jSONObject4.optLong("tcTime"));
                        jSONObject3.put("kcTime", jSONObject4.optLong("kcTime"));
                        jSONObject3.put("lsMills", jSONObject4.optLong("lsMills"));
                        jSONObject3.put("stayMills", jSONObject4.optLong("stayMills"));
                        jSONArray2.put(jSONObject3);
                    }
                    jSONObject2.put(STATION_LIST, jSONArray2);
                    jSONObject2.put(TRAIN_NUM, jSONObject.optString("cc"));
                    jSONObject2.put("start_city", jSONObject.optString("sfz"));
                    jSONObject2.put("end_city", jSONObject.optString("zdz"));
                    jSONObject2.put(TRAIN_TYPE, jSONObject.optString("lclx"));
                    jSONObject2.put("start_time", jSONObject.optString("fs"));
                    jSONObject2.put("end_time", jSONObject.optString("ds"));
                    jSONObject2.put(MILEAGE, jSONObject.optString("lc"));
                    jSONObject2.put(DURATION, jSONObject.optString("ls"));
                    jSONObject2.put(DAY, jSONObject.optString(DAY));
                    jSONObject2.put(DATA_TIME, System.currentTimeMillis());
                    return jSONObject2;
                }
            } catch (Throwable th) {
                return null;
            }
        }
        return null;
    }

    public static void checkDataOnline(XyCallBack xyCallBack, String str, String str2, String str3, String str4, String str5) {
        checkDataOnline(xyCallBack, str, str2, str3, str4, str5, null);
    }

    public static void checkDataOnline(XyCallBack xyCallBack, String str, String str2, String str3, String str4, String str5, Map map) {
        try {
            NetUtil.requestTokenIfNeed("");
            p pVar = new p(xyCallBack, str, str2, str3, str4, str5);
            if (NetUtil.isEnhance()) {
                String str6 = (map != null && map.containsKey("ft")) ? (String) map.get("ft") : null;
                str6 = j.b(str2.replace("次", ""), str3, str4, str6);
                if (StringUtils.isNull(str6)) {
                    XyUtil.doXycallBackResult(xyCallBack, str);
                    return;
                } else {
                    NetUtil.executePubNumServiceHttpRequest(str6, "990005", pVar, "", true, false, NetUtil.REQ_QUERY_CHECI, false);
                    return;
                }
            }
            XyUtil.doXycallBackResult(xyCallBack, str);
        } catch (Throwable th) {
        }
    }

    public static boolean checkUpdateData() {
        new o().start();
        return true;
    }

    public static void deleteExpireTrainInfo() {
        try {
            DBManager.delete(TABLE_NAME, "day < ? ", new String[]{DateUtils.getTimeString(Constant.PATTERN, System.currentTimeMillis() - DexUtil.getUpdateCycleByType(37, Constant.month))});
        } catch (Throwable th) {
        }
    }

    public static ContentValues getContentValues(JSONObject jSONObject) {
        String[] strArr = new String[]{TRAIN_NUM, "start_city", "end_city", TRAIN_TYPE, "start_time", "end_time", MILEAGE, STATION_LIST, DURATION, DAY, DATA_TIME};
        return BaseManager.getContentValues(null, strArr, strArr, jSONObject, false);
    }

    public static String getEndCity(String str) {
        String[] strArr = null;
        try {
            if (!StringUtils.isNull(str)) {
                strArr = str.split("/");
            }
            if (strArr != null && strArr.length > 0) {
                return queryEndCity(strArr[0]);
            }
        } catch (Throwable th) {
        }
        return "";
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void importTrainData(Context context) {
        SQLiteDatabase sQLiteDatabase = null;
        String str = Constant.getDRAWBLE_PATH() + "train_data.txt";
        LineNumberReader lineNumberReader = null;
        try {
            int channelType = ViewUtil.getChannelType();
            lineNumberReader = XyUtil.getLineByCompressFile(str);
            sQLiteDatabase = DBManager.getSQLiteDatabase();
            sQLiteDatabase.beginTransaction();
            String readLine = lineNumberReader.readLine();
            ContentValues contentValues = new ContentValues();
            String readLine2;
            String[] split;
            if (channelType != 6 && channelType != 7) {
                String[] split2 = readLine.split("=");
                while (true) {
                    readLine2 = lineNumberReader.readLine();
                    if (readLine2 == null) {
                        break;
                    }
                    split = readLine2.split(" ");
                    contentValues.put(TRAIN_NUM, split[0]);
                    if (split.length >= 2) {
                        contentValues.put("end_city", split2[Integer.valueOf(split[1]).intValue()]);
                    }
                    if ((((long) sQLiteDatabase.update(TABLE_NAME, contentValues, "train_num=?", new String[]{split[0]})) >= 1 ? 1 : null) == null) {
                        sQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                    }
                }
            } else {
                while (true) {
                    readLine2 = lineNumberReader.readLine();
                    if (readLine2 == null) {
                        break;
                    }
                    split = readLine2.split("\t");
                    if (split.length >= 5) {
                        contentValues.put(TRAIN_NUM, split[0]);
                        contentValues.put("end_city", split[1]);
                        contentValues.put("start_time", split[2]);
                        contentValues.put("end_time", split[3]);
                        if (!StringUtils.isNull(split[4])) {
                            contentValues.put(STATION_LIST, split[4]);
                        }
                        if ((((long) sQLiteDatabase.update(TABLE_NAME, contentValues, "train_num=?", new String[]{split[0]})) >= 1 ? 1 : null) == null) {
                            sQLiteDatabase.insert(TABLE_NAME, null, contentValues);
                        }
                    }
                }
            }
            DBManager.closeDB(str, true, lineNumberReader, null, sQLiteDatabase);
        } catch (Throwable th) {
            Throwable th2 = th;
            SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
            LineNumberReader lineNumberReader2 = lineNumberReader;
            Throwable th3 = th2;
            DBManager.closeDB(str, true, lineNumberReader2, null, sQLiteDatabase2);
            throw th3;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String queryEndCity(String str) {
        XyCursor xyCursor = null;
        try {
            String replaceFirst = str.replaceFirst("次", "");
            xyCursor = DBManager.query(TABLE_NAME, new String[]{"end_city"}, "train_num = ?", new String[]{new StringBuilder(String.valueOf(replaceFirst)).toString()});
            if (xyCursor != null) {
                if (xyCursor.moveToNext()) {
                    replaceFirst = xyCursor.getString(xyCursor.getColumnIndex("end_city"));
                    XyCursor.closeCursor(xyCursor, true);
                    return replaceFirst;
                }
            }
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
        return "";
    }

    public static JSONObject queryTrainInfo(String str, String str2) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            String replaceFirst = str.replaceFirst("次", "");
            String[] strArr = new String[]{"id", TRAIN_NUM, "start_city", "end_city", TRAIN_TYPE, "start_time", "end_time", MILEAGE, STATION_LIST, DURATION, DAY, DATA_TIME};
            query = DBManager.query(TABLE_NAME, strArr, "train_num = ? AND day = ? ", new String[]{replaceFirst, str2});
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr, query);
                XyCursor.closeCursor(query, true);
                deleteExpireTrainInfo();
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = query;
                th = th3;
                XyCursor.closeCursor(xyCursor, true);
                deleteExpireTrainInfo();
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            deleteExpireTrainInfo();
            throw th;
        }
        return loadSingleDataFromCursor;
    }
}
