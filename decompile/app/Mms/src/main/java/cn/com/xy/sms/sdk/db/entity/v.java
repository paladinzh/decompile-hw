package cn.com.xy.sms.sdk.db.entity;

import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.DateUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class v {
    private static String a = "scene_id";
    private static String b = "date";
    private static String c = "action_type";
    private static String d = "action_code";
    private static String e = "times";
    private static String f = "tb_button_action_scene";
    private static String g = " DROP TABLE IF EXISTS tb_button_action_scene";
    private static String h = "create table  if not exists tb_button_action_scene (scene_id TEXT, date TEXT, action_type INTEGER DEFAULT '0', times INTEGER DEFAULT '0', action_code TEXT  ) ";
    private static String i = "ALTER TABLE tb_button_action_scene ADD COLUMN action_code TEXT";
    private static String j = " UPDATE tb_button_action_scene SET action_code = action_type WHERE action_code = '' OR action_code IS NULL";
    private static String[] k = new String[]{ParseItemManager.SCENE_ID, "date", "action_type", "times", "action_code"};

    public static long a(HashMap<String, String> hashMap) {
        JSONObject jSONObject = null;
        String str = (String) hashMap.get("titleNo");
        String str2 = (String) hashMap.get(NumberInfo.TYPE_KEY);
        str2 = StringUtils.isNull(str2) ? ThemeUtil.SET_NULL_STR : str2.trim();
        long insert;
        try {
            if (!StringUtils.isNull(str)) {
                if (!ThemeUtil.SET_NULL_STR.endsWith(str2)) {
                    jSONObject = b(str, str2);
                }
            }
            if (jSONObject == null) {
                String currentTimeString = DateUtils.getCurrentTimeString("yyyyMMdd");
                insert = DBManager.insert("tb_button_action_scene", BaseManager.getContentValues(null, ParseItemManager.SCENE_ID, str, "date", currentTimeString, "action_code", str2, "times", "1"));
                return insert;
            }
            jSONObject.put("times", String.valueOf(Integer.parseInt(jSONObject.getString("times")) + 1));
            DBManager.update("tb_button_action_scene", BaseManager.getContentValues(null, false, jSONObject, k), "scene_id = ? and date = ? and action_type = ? and action_code = ? ", new String[]{jSONObject.getString(ParseItemManager.SCENE_ID), jSONObject.getString("date"), jSONObject.getString("action_type"), jSONObject.getString("action_code")});
            return 0;
        } catch (Throwable th) {
            insert = -1;
        }
    }

    public static JSONArray a(String str, String str2) {
        JSONArray loadArrDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor query;
        try {
            query = DBManager.query("tb_button_action_scene", k, "scene_id = ? and date = ? ", new String[]{str, str2}, null, null, null, null);
            try {
                loadArrDataFromCursor = BaseManager.loadArrDataFromCursor(k, query);
                XyCursor.closeCursor(query, true);
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
        return loadArrDataFromCursor;
    }

    public static void a() {
        try {
            DBManager.delete("tb_button_action_scene", null, null);
        } catch (Throwable th) {
        }
    }

    public static void a(String str) {
        try {
            DBManager.delete("tb_button_action_scene", "date < ?", new String[]{str});
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Map<String, String> b() {
        Map<String, String> hashMap;
        XyCursor xyCursor;
        Throwable th;
        Map<String, String> map = null;
        XyCursor query;
        try {
            query = DBManager.query("tb_button_action_scene", k, null, null, null, null, "action_code", null);
            if (query != null) {
                try {
                    hashMap = new HashMap();
                    while (query.moveToNext()) {
                        String string = query.getString(0);
                        String twoDigitType = StringUtils.getTwoDigitType(query.getString(4));
                        String twoDigitType2 = StringUtils.getTwoDigitType(query.getString(3));
                        if (hashMap.containsKey(string)) {
                            hashMap.put(string, new StringBuilder(String.valueOf((String) hashMap.get(string))).append(twoDigitType).append(twoDigitType2).toString());
                        } else {
                            hashMap.put(string, twoDigitType2);
                        }
                    }
                    XyCursor.closeCursor(query, true);
                } catch (Throwable th2) {
                    th = th2;
                }
                return hashMap;
            }
            XyCursor.closeCursor(query, true);
            return null;
        } catch (Throwable th3) {
            th = th3;
            query = null;
            XyCursor.closeCursor(query, true);
            throw th;
        }
    }

    private static JSONObject b(String str, String str2) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        String currentTimeString = DateUtils.getCurrentTimeString("yyyyMMdd");
        try {
            query = DBManager.query("tb_button_action_scene", k, "scene_id = ? and date = ? and (action_type = ? or action_code = ? )", new String[]{str, currentTimeString, str2, str2}, null, null, null, "1");
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(k, query);
                XyCursor.closeCursor(query, true);
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
        return loadSingleDataFromCursor;
    }
}
