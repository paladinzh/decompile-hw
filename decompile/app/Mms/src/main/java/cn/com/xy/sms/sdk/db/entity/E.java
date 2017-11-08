package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class e {
    private static String a = "tb_emergency_queue";

    public static String a() {
        return " create table  if not exists tb_emergency_queue ( id INTEGER PRIMARY KEY, emVersion INTEGER, emContent TEXT )";
    }

    public static void a(JSONArray jSONArray) {
        XyCursor xyCursor = null;
        if (jSONArray != null) {
            if (jSONArray.length() > 0) {
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject optJSONObject = jSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        try {
                            String str = (String) JsonUtil.getValueFromJsonObject(optJSONObject, "emContent");
                            String str2 = (String) JsonUtil.getValueFromJsonObject(optJSONObject, "emVersion");
                            if (!StringUtils.isNull(str2)) {
                                if (!StringUtils.isNull(str)) {
                                    XyCursor query = DBManager.query("tb_emergency_queue", new String[]{"emVersion"}, "emVersion = ?", new String[]{new StringBuilder(String.valueOf(str2)).toString()});
                                    try {
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put("emVersion", str2);
                                        contentValues.put("emContent", str);
                                        if (query != null) {
                                            if (query.getCount() > 0) {
                                                DBManager.update("tb_emergency_queue", contentValues, "emVersion = ? ", new String[]{new StringBuilder(String.valueOf(str2)).toString()});
                                                XyCursor.closeCursor(query, true);
                                            }
                                        }
                                        DBManager.insert("tb_emergency_queue", contentValues);
                                        XyCursor.closeCursor(query, true);
                                    } catch (Throwable th) {
                                        Throwable th2 = th;
                                        xyCursor = query;
                                    }
                                }
                            }
                            XyCursor.closeCursor(null, true);
                        } catch (Throwable th3) {
                            return;
                        }
                    }
                    XyCursor.closeCursor(null, true);
                }
                return;
            }
        }
        return;
        XyCursor.closeCursor(xyCursor, true);
        throw th2;
    }

    public static void a(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                String optString = jSONObject.optString("emVersion");
                if (StringUtils.isNull(optString)) {
                    optString = "";
                }
                DBManager.delete("tb_emergency_queue", "emVersion = ?", new String[]{optString});
            } catch (Throwable th) {
            }
        }
    }

    public static JSONObject b() {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            String[] strArr = new String[]{"id", "emVersion", "emContent"};
            query = DBManager.query("tb_emergency_queue", strArr, null, null, null, null, " emVersion asc", "1");
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr, query);
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void b(JSONObject jSONObject) {
        XyCursor xyCursor = null;
        if (jSONObject != null) {
            try {
                String str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "emContent");
                String str2 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "emVersion");
                if (!StringUtils.isNull(str2)) {
                    if (!StringUtils.isNull(str)) {
                        xyCursor = DBManager.query("tb_emergency_queue", new String[]{"emVersion"}, "emVersion = ?", new String[]{new StringBuilder(String.valueOf(str2)).toString()});
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("emVersion", str2);
                        contentValues.put("emContent", str);
                        if (xyCursor != null) {
                            if (xyCursor.getCount() > 0) {
                                DBManager.update("tb_emergency_queue", contentValues, "emVersion = ? ", new String[]{new StringBuilder(String.valueOf(str2)).toString()});
                                XyCursor.closeCursor(xyCursor, true);
                                return;
                            }
                        }
                        DBManager.insert("tb_emergency_queue", contentValues);
                        XyCursor.closeCursor(xyCursor, true);
                        return;
                    }
                }
                XyCursor.closeCursor(xyCursor, true);
            } catch (Throwable th) {
                XyCursor.closeCursor(xyCursor, true);
            }
        } else {
            XyCursor.closeCursor(xyCursor, true);
        }
    }
}
