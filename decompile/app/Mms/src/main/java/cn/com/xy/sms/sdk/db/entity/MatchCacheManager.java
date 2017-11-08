package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class MatchCacheManager {
    public static final String ADD_IS_FAVORITE = "ALTER TABLE tb_match_cache ADD COLUMN is_favorite INTEGER DEFAULT '0'";
    public static final String ADD_IS_MARK = "ALTER TABLE tb_match_cache ADD COLUMN is_mark INTEGER DEFAULT '0'";
    public static final String ADD_URLS_LASTTIME = "ALTER TABLE tb_match_cache ADD COLUMN urls_lasttime INTEGER DEFAULT '0'";
    public static final String ADD_URLS_RESULT = "ALTER TABLE tb_match_cache ADD COLUMN urls_result  TEXT";
    public static final String ADD_bubble_lasttime = "ALTER TABLE tb_match_cache ADD COLUMN bubble_lasttime INTEGER DEFAULT '0'";
    public static final String ADD_card_lasttime = "ALTER TABLE tb_match_cache ADD COLUMN card_lasttime INTEGER DEFAULT '0'";
    public static final String ADD_recognise_lasttime = "ALTER TABLE tb_match_cache ADD COLUMN recognise_lasttime INTEGER DEFAULT '0'";
    public static final String ADD_recognise_result = "ALTER TABLE tb_match_cache ADD COLUMN value_recognise_result  TEXT";
    public static final String ADD_session_lasttime = "ALTER TABLE tb_match_cache ADD COLUMN session_lasttime INTEGER DEFAULT '0'";
    public static final String ADD_url_valid_statu = "ALTER TABLE tb_match_cache ADD COLUMN url_valid_statu INTEGER DEFAULT '0'";
    public static final String ADD_urls = "ALTER TABLE tb_match_cache ADD COLUMN urls  TEXT";
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_match_cache";
    public static final String TABLE_NAME = "tb_match_cache";

    private static long a() {
        Object obj = null;
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), Constant.REPARSE_BUBBLE_CYCLE);
        long j = -1;
        if (!ThemeUtil.SET_NULL_STR.equals(stringParam)) {
            try {
                j = Long.parseLong(stringParam);
            } catch (Exception e) {
            }
        }
        if (j <= 0) {
            obj = 1;
        }
        return obj == null ? j : DexUtil.getUpdateCycleByType(14, 21600000);
    }

    private static Map<String, JSONObject> a(String[] strArr, int i, XyCursor xyCursor) {
        if (xyCursor != null) {
            try {
                Map<String, JSONObject> hashMap = new HashMap();
                while (xyCursor.moveToNext()) {
                    JSONObject jSONObject = new JSONObject();
                    for (int i2 = 0; i2 < strArr.length; i2++) {
                        jSONObject.put(strArr[i2], StringUtils.getNoNullString(xyCursor.getString(i2)));
                    }
                    a(jSONObject);
                    hashMap.put((String) JsonUtil.getValueFromJsonObject(jSONObject, strArr[0]), jSONObject);
                }
                return hashMap;
            } catch (Throwable th) {
            }
        }
        return null;
    }

    private static void a(String str, String str2, long j) {
        try {
            DBManager.update(TABLE_NAME, BaseManager.getContentValues(null, str2, String.valueOf(j)), new StringBuilder(String.valueOf(str)).append(" IS NULL OR ").append(str).append("=''").toString(), null);
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(JSONObject jSONObject) {
        long longValueFromJsonObject;
        String str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "bubble_result");
        try {
            longValueFromJsonObject = JsonUtil.getLongValueFromJsonObject(jSONObject, "bubble_lasttime");
            if (StringUtils.isNull(str)) {
                jSONObject.remove("bubble_result");
                long a = a();
                if (longValueFromJsonObject != 0) {
                }
                jSONObject.put("need_parse_bubble", "");
            } else {
                jSONObject.put("bubble_result", new JSONObject(str));
            }
        } catch (Throwable th) {
        }
        str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "session_reuslt");
        try {
            longValueFromJsonObject = JsonUtil.getLongValueFromJsonObject(jSONObject, "session_lasttime");
            if (StringUtils.isNull(str)) {
                jSONObject.remove("session_reuslt");
                a = a();
                if (longValueFromJsonObject != 0) {
                }
                jSONObject.put("need_parse_simple", "");
            } else {
                jSONObject.put("session_reuslt", new JSONArray(str));
            }
        } catch (Throwable th2) {
        }
        str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "value_recognise_result");
        try {
            longValueFromJsonObject = JsonUtil.getLongValueFromJsonObject(jSONObject, "recognise_lasttime");
            if (StringUtils.isNull(str)) {
                jSONObject.remove("value_recognise_result");
                a = a();
                if (longValueFromJsonObject != 0) {
                }
                jSONObject.put("need_parse_recognise", "");
                return;
            }
            jSONObject.put("value_recognise_result", new JSONObject(str));
        } catch (Throwable th3) {
        }
    }

    public static void deleteAll() {
        try {
            DBManager.delete(TABLE_NAME, null, null);
        } catch (Throwable th) {
        }
    }

    public static boolean deleteBubbleData(String str, String str2) {
        if (StringUtils.isNull(str)) {
            return false;
        }
        int i = -1;
        try {
            i = !StringUtils.isNull(str2) ? DBManager.delete(TABLE_NAME, " msg_id = ? and msg_num_md5 = ? ", new String[]{str, str2}) : DBManager.delete(TABLE_NAME, " msg_id = ? ", new String[]{str});
        } catch (Throwable th) {
        }
        return i > 0;
    }

    public static void deleteDataByMsgIds(Set<Integer> set) {
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        if (set != null && !set.isEmpty()) {
            synchronized (DBManager.dblock) {
                SQLiteDatabase sQLiteDatabase2;
                try {
                    sQLiteDatabase2 = DBManager.getSQLiteDatabase();
                    try {
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Integer num : set) {
                            stringBuffer.append(num + ",");
                        }
                        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                        sQLiteDatabase2.execSQL("DELETE FROM tb_match_cache WHERE msg_id IN (" + stringBuffer.toString() + ")");
                        DBManager.close(sQLiteDatabase2);
                    } catch (Throwable th2) {
                        th = th2;
                        DBManager.close(sQLiteDatabase2);
                        throw th;
                    }
                } catch (Throwable th3) {
                    Throwable th4 = th3;
                    sQLiteDatabase2 = null;
                    th = th4;
                    DBManager.close(sQLiteDatabase2);
                    throw th;
                }
            }
        }
    }

    public static int deleteDataByPhoneNum(String str) {
        if (!StringUtils.isNull(str)) {
            try {
                return DBManager.delete(TABLE_NAME, " phonenum = ? ", new String[]{str});
            } catch (Throwable th) {
            }
        }
        return -1;
    }

    public static void deleteMatchCache(String str, long j) {
        try {
            StringBuffer stringBuffer = new StringBuffer(" bubble_lasttime < ?");
            if (str.length() != 8) {
                stringBuffer.append(" and scene_id like '?%' ");
            } else {
                stringBuffer.append(" and scene_id = ? ");
            }
            DBManager.delete(TABLE_NAME, stringBuffer.toString(), new String[]{String.valueOf(j), str});
        } catch (Throwable th) {
        }
    }

    public static void deleteMatchCache(String str, String str2, long j) {
        try {
            StringBuffer stringBuffer = new StringBuffer(new StringBuilder(String.valueOf(str2)).append("  < ?").toString());
            if (StringUtils.isNull(str)) {
                stringBuffer.append(" and (scene_id is null or length(scene_id) = 0) ");
                DBManager.delete(TABLE_NAME, stringBuffer.toString(), new String[]{String.valueOf(j)});
            } else if (str.length() != 8) {
                stringBuffer.append(" and scene_id like '" + str + "%' ");
                DBManager.delete(TABLE_NAME, stringBuffer.toString(), new String[]{String.valueOf(j)});
            } else {
                stringBuffer.append(" and scene_id = ? ");
                DBManager.delete(TABLE_NAME, stringBuffer.toString(), new String[]{String.valueOf(j), str});
            }
        } catch (Throwable th) {
        }
    }

    public static String getCreateTableSql() {
        return "create table  if not exists tb_match_cache (  id INTEGER PRIMARY KEY, msg_num_md5 TEXT, phonenum TEXT, msg_id TEXT, scene_id TEXT, popup_window_result TEXT, bubble_result TEXT, session_reuslt TEXT, card_result TEXT, save_time INTEGER DEFAULT '0', value_recognise_result TEXT, bubble_lasttime integer default 0, session_lasttime integer default 0, card_lasttime integer default 0, recognise_lasttime integer default 0, EXTEND TEXT, url_valid_statu integer default 0, urls TEXT , urls_result TEXT, urls_lasttime integer default 0, is_favorite integer default 0, is_mark integer default 0)";
    }

    public static JSONObject getDataByParam(String str) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        String[] strArr = new String[]{"msg_id", "msg_num_md5", "bubble_result", "session_reuslt", "card_result", "bubble_lasttime", "session_lasttime", "card_lasttime", "save_time", "value_recognise_result", "recognise_lasttime", "urls_result", "urls_lasttime", Constant.IS_FAVORITE, "is_mark", NetUtil.REQ_QUERY_NUM};
        try {
            query = DBManager.query(TABLE_NAME, strArr, "msg_id=?", new String[]{str}, null, null, null, null);
            try {
                JSONObject loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr, query);
                if (loadSingleDataFromCursor != null) {
                    a(loadSingleDataFromCursor);
                }
                XyCursor.closeCursor(query, true);
                return loadSingleDataFromCursor;
            } catch (Throwable th2) {
                xyCursor = query;
                th = th2;
                XyCursor.closeCursor(xyCursor, true);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static String getMD5(String str, String str2) {
        try {
            String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
            if (!StringUtils.isNull(phoneNumberNo86)) {
                if (!StringUtils.isNull(str2)) {
                    return StringUtils.getMD5(phoneNumberNo86.trim() + str2.trim());
                }
            }
        } catch (Throwable th) {
        }
        return "";
    }

    public static synchronized long insertOrUpdate(ContentValues contentValues, int i) {
        XyCursor xyCursor;
        long j;
        XyCursor query;
        Throwable th;
        String str = null;
        synchronized (MatchCacheManager.class) {
            long j2 = -1;
            try {
                String str2 = (String) contentValues.get("msg_num_md5");
                if (StringUtils.isNull((String) contentValues.get("msg_id"))) {
                    xyCursor = null;
                    j = -1;
                } else {
                    String[] strArr = new String[]{(String) contentValues.get("msg_id")};
                    String[] strArr2 = new String[]{"msg_num_md5", "id"};
                    query = DBManager.query(TABLE_NAME, strArr2, " msg_id = ? ", strArr);
                    try {
                        JSONObject loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr2, query);
                        if (loadSingleDataFromCursor != null) {
                            String str3 = (String) JsonUtil.getValueFromJsonObject(loadSingleDataFromCursor, "msg_num_md5");
                            j2 = Long.valueOf((String) JsonUtil.getValueFromJsonObject(loadSingleDataFromCursor, "id")).longValue();
                            str = str3;
                        }
                        if (str != null && str.equals(str2)) {
                            DBManager.update(TABLE_NAME, contentValues, " msg_id = ? ", strArr);
                        } else {
                            if (str != null) {
                                DBManager.delete(TABLE_NAME, " msg_id = ? ", strArr);
                            }
                            j2 = DBManager.insert(TABLE_NAME, contentValues);
                        }
                        xyCursor = query;
                        j = j2;
                    } catch (Throwable th2) {
                        th = th2;
                        XyCursor.closeCursor(query, true);
                        throw th;
                    }
                }
                XyCursor.closeCursor(xyCursor, true);
            } catch (Throwable th3) {
                th = th3;
                query = null;
                XyCursor.closeCursor(query, true);
                throw th;
            }
        }
        return j;
    }

    public static Map<String, JSONObject> loadDataByParam(String str, String[] strArr) {
        Throwable th;
        XyCursor xyCursor = null;
        String[] strArr2 = new String[]{"msg_id", "msg_num_md5", "bubble_result", "session_reuslt", "card_result", "bubble_lasttime", "session_lasttime", "card_lasttime", "save_time", "value_recognise_result", "recognise_lasttime", "urls_result", "urls_lasttime", Constant.IS_FAVORITE, "is_mark", NetUtil.REQ_QUERY_NUM};
        XyCursor query;
        try {
            query = DBManager.query(TABLE_NAME, strArr2, str, strArr);
            try {
                Map<String, JSONObject> a = a(strArr2, 0, query);
                XyCursor.closeCursor(query, true);
                return a;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = query;
                th = th3;
                XyCursor.closeCursor(xyCursor, true);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static Map<String, JSONObject> loadDataByParam(String str, String[] strArr, String str2, String str3) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        String[] strArr2 = new String[]{"msg_id", "msg_num_md5", "bubble_result", "session_reuslt", "card_result", "bubble_lasttime", "session_lasttime", "card_lasttime", "save_time", "value_recognise_result", "recognise_lasttime", "urls_result", "urls_lasttime", Constant.IS_FAVORITE, "is_mark", NetUtil.REQ_QUERY_NUM};
        try {
            query = DBManager.query(TABLE_NAME, strArr2, str, strArr, null, null, str2, str3);
            try {
                Map<String, JSONObject> a = a(strArr2, 0, query);
                XyCursor.closeCursor(query, true);
                return a;
            } catch (Throwable th2) {
                xyCursor = query;
                th = th2;
                XyCursor.closeCursor(xyCursor, true);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static Map<String, JSONObject> loadDataByParam(String str, String[] strArr, String[] strArr2) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query(TABLE_NAME, strArr2, str, strArr);
            try {
                Map<String, JSONObject> a = a(strArr2, 0, query);
                XyCursor.closeCursor(query, true);
                return a;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = query;
                th = th3;
                XyCursor.closeCursor(xyCursor, true);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int queryDataCount(String str, String str2) {
        XyCursor xyCursor = null;
        try {
            xyCursor = DBManager.query(TABLE_NAME, new String[]{"id"}, "msg_num_md5 = ? AND msg_id = ?", new String[]{str, str2});
            if (xyCursor != null) {
                if (xyCursor.moveToNext()) {
                    int count = xyCursor.getCount();
                    XyCursor.closeCursor(xyCursor, true);
                    return count;
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
        return 0;
    }

    public static void removeUselessKey(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                String[] strArr = new String[]{NumberInfo.VERSION_KEY, "layoutName", Constant.KEY_ALLOW_VERCODE_MSG, "smsCenterNum", "channel", Constant.RECOGNIZE_LEVEL, "is_return", "viewPartParam", "simIndex"};
                for (int i = 0; i < 9; i++) {
                    String str = strArr[i];
                    if (jSONObject.has(str)) {
                        jSONObject.remove(str);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void resetLastParseTime(long j) {
        try {
            a("bubble_result", "bubble_lasttime", j);
            a("session_reuslt", "session_lasttime", j);
            a("card_result", "card_lasttime", j);
            a("value_recognise_result", "recognise_lasttime", j);
        } catch (Throwable th) {
        }
    }

    public static void resetRecognisedResult(String str) {
        if (!StringUtils.isNull(str)) {
            try {
                DBManager.update(TABLE_NAME, BaseManager.getContentValues(null, "msg_id", str, "value_recognise_result", "", "recognise_lasttime", "0"), " msg_id = ? ", new String[]{str});
            } catch (Throwable th) {
            }
        }
    }

    public static void updateCheckStatu(String str, int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("url_valid_statu", Integer.valueOf(i));
            DBManager.update(TABLE_NAME, contentValues, " urls = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static void updateCheckStatu(JSONArray jSONArray) {
        if (jSONArray != null) {
            try {
                if (jSONArray.length() > 0) {
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject optJSONObject = jSONArray.optJSONObject(i);
                        String optString = optJSONObject.optString("originURL");
                        int optInt = optJSONObject.optInt("validStatus");
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("url_valid_statu", Integer.valueOf(optInt));
                        DBManager.update(TABLE_NAME, contentValues, " urls = ? ", new String[]{optString});
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void updateMarkAndFavoriteAsy(String str, String str2, String str3, int i, int i2) {
        NetWebUtil.executeRunnable(new j(i, i2, str2, str3, str));
    }

    public static void updateMatchCacheManager(ContentValues contentValues) {
        if (contentValues != null) {
            NetWebUtil.executeRunnable(new i(contentValues));
        }
    }

    public static void updateMatchCacheManager(String str, String str2, String str3, JSONObject jSONObject, String str4) {
        updateMatchCacheManager(str, str2, str3, jSONObject, null, str4);
    }

    public static void updateMatchCacheManager(String str, String str2, String str3, JSONObject jSONObject, JSONArray jSONArray, String str4) {
        if (jSONObject == null || jSONObject.length() == 0) {
            if (jSONArray == null || jSONArray.length() == 0) {
                return;
            }
        }
        String md5 = getMD5(str, str4);
        if (!StringUtils.isNull(md5)) {
            String valueOf = String.valueOf(System.currentTimeMillis());
            ContentValues contentValues = new ContentValues();
            contentValues.put("msg_num_md5", md5);
            contentValues.put(NetUtil.REQ_QUERY_NUM, StringUtils.getPhoneNumberNo86(str));
            contentValues.put(ParseItemManager.SCENE_ID, str2);
            contentValues.put("msg_id", str3);
            contentValues.put("save_time", valueOf);
            if (jSONObject != null) {
                JSONObject jSONObject2 = new JSONObject(jSONObject.toString());
                removeUselessKey(jSONObject2);
                contentValues.put("bubble_result", jSONObject2.toString());
                contentValues.put("bubble_lasttime", valueOf);
            }
            if (jSONArray != null) {
                contentValues.put("session_reuslt", jSONArray.toString());
                contentValues.put("session_lasttime", valueOf);
            }
            updateMatchCacheManager(contentValues);
        }
    }
}
