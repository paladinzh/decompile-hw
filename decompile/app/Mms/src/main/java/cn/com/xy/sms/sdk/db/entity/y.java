package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.queue.a.a;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class y {
    private static String a = "tb_resourse_queue";
    private static String b = " DROP TABLE IF EXISTS tb_resourse_queue";

    public static String a() {
        return " create table  if not exists tb_resourse_queue ( id INTEGER PRIMARY KEY, res_type INTEGER, res_version INTEGER, res_url TEXT, down_statu INTEGER DEFAULT '0', temp_filename TEXT, down_failed_time LONG DEFAULT '0')";
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static JSONArray a(int i) {
        XyCursor query;
        JSONArray jSONArray;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            JSONArray jSONArray2;
            query = DBManager.query("tb_resourse_queue", new String[]{"id", "res_type", "res_version", "res_url", "down_statu", "temp_filename", "down_failed_time"}, "res_type = ? and down_statu = ? ", new String[]{new StringBuilder(String.valueOf(i)).toString(), "0"}, null, null, "res_version asc", null);
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("id");
                        int columnIndex2 = query.getColumnIndex("res_type");
                        int columnIndex3 = query.getColumnIndex("res_version");
                        int columnIndex4 = query.getColumnIndex("res_url");
                        int columnIndex5 = query.getColumnIndex("down_statu");
                        int columnIndex6 = query.getColumnIndex("temp_filename");
                        int columnIndex7 = query.getColumnIndex("down_failed_time");
                        jSONArray = new JSONArray();
                        while (query.moveToNext()) {
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("id", query.getInt(columnIndex));
                            jSONObject.put("res_type", query.getInt(columnIndex2));
                            jSONObject.put("res_version", query.getInt(columnIndex3));
                            jSONObject.put("res_url", query.getString(columnIndex4));
                            jSONObject.put("down_statu", query.getInt(columnIndex5));
                            jSONObject.put("temp_filename", query.getString(columnIndex6));
                            jSONObject.put("down_failed_time", query.getLong(columnIndex7));
                            jSONArray.put(jSONObject);
                        }
                        jSONArray2 = jSONArray;
                    }
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            XyCursor.closeCursor(query, true);
            return jSONArray2;
        } catch (Throwable th3) {
            th = th3;
            query = null;
            XyCursor.closeCursor(query, true);
            throw th;
        }
    }

    public static void a(Integer num, boolean z, String str) {
        try {
            ContentValues contentValues = new ContentValues();
            if (z) {
                contentValues.put("res_url", "");
                contentValues.put("down_failed_time", "0");
                contentValues.put("down_statu", "1");
                contentValues.put("temp_filename", str);
            } else {
                contentValues.put("down_failed_time", Long.valueOf(System.currentTimeMillis()));
                contentValues.put("down_statu", "0");
                contentValues.put("temp_filename", str);
            }
            DBManager.update("tb_resourse_queue", contentValues, "id = ? ", new String[]{String.valueOf(num)});
        } catch (Throwable th) {
        }
    }

    public static void a(JSONArray jSONArray) {
        XyCursor xyCursor;
        XyCursor xyCursor2;
        Throwable th;
        XyCursor xyCursor3 = null;
        if (jSONArray != null) {
            if (jSONArray.length() > 0) {
                int length = jSONArray.length();
                int i = 0;
                while (i < length) {
                    String str;
                    JSONObject jSONObject = jSONArray.getJSONObject(i);
                    String str2 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "del_history");
                    if (!(StringUtils.isNull(str2) || !str2.equals("1") || jSONObject == null)) {
                        try {
                            DBManager.delete("tb_resourse_queue", "res_type = ? and res_version < ?", new String[]{new StringBuilder(String.valueOf((String) JsonUtil.getValueFromJsonObject(jSONObject, "res_type"))).toString(), new StringBuilder(String.valueOf((String) JsonUtil.getValueFromJsonObject(jSONObject, "res_version"))).toString()});
                            a.a(str2, str);
                        } catch (Throwable th2) {
                        }
                    }
                    try {
                        str2 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_type");
                        str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_version");
                        if (jSONObject == null) {
                            xyCursor = null;
                        } else {
                            xyCursor = DBManager.query("tb_resourse_queue", new String[]{new StringBuilder(String.valueOf(str2)).toString(), new StringBuilder(String.valueOf(str)).toString()}, "res_type = ? and res_version = ? ", new String[]{new StringBuilder(String.valueOf(str2)).toString(), new StringBuilder(String.valueOf(str)).toString()});
                            try {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("res_type", str2);
                                contentValues.put("res_version", str);
                                contentValues.put("res_url", (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_url"));
                                if (xyCursor != null) {
                                    if (xyCursor.getCount() > 0) {
                                        DBManager.update("tb_resourse_queue", contentValues, "res_type = ? and res_version = ? ", new String[]{new StringBuilder(String.valueOf(str2)).toString(), new StringBuilder(String.valueOf(str)).toString()});
                                    }
                                }
                                DBManager.insert("tb_resourse_queue", contentValues);
                            } catch (Throwable th3) {
                                th = th3;
                                xyCursor3 = xyCursor;
                            }
                        }
                    } catch (Throwable th4) {
                        th = th4;
                    }
                    try {
                        XyCursor.closeCursor(xyCursor, true);
                        i++;
                    } catch (Throwable th5) {
                    }
                }
                return;
            }
        }
        return;
        XyCursor.closeCursor(xyCursor3, true);
        throw th;
    }

    private static void a(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                DBManager.delete("tb_resourse_queue", "res_type = ? and res_version < ?", new String[]{new StringBuilder(String.valueOf((String) JsonUtil.getValueFromJsonObject(jSONObject, "res_type"))).toString(), new StringBuilder(String.valueOf((String) JsonUtil.getValueFromJsonObject(jSONObject, "res_version"))).toString()});
                a.a(r0, r1);
            } catch (Throwable th) {
            }
        }
    }

    public static void a(boolean z, String str) {
        int i = 0;
        try {
            ContentValues contentValues = new ContentValues();
            if (z) {
                i = 2;
            }
            contentValues.put("down_statu", Integer.valueOf(i));
            DBManager.update("tb_resourse_queue", contentValues, "temp_filename = ?", new String[]{str});
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String b(int i) {
        XyCursor xyCursor = null;
        String str = "";
        try {
            String[] strArr = new String[]{"res_version"};
            xyCursor = DBManager.query("tb_resourse_queue", strArr, "res_type = " + i + " ORDER BY res_version desc LIMIT 1 ", null);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    while (xyCursor.moveToNext()) {
                        str = xyCursor.getString(0);
                    }
                }
            }
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            str = th;
            XyCursor.closeCursor(xyCursor, true);
        }
        return str;
    }

    private static void b(JSONObject jSONObject) {
        XyCursor xyCursor;
        Throwable th;
        XyCursor xyCursor2 = null;
        try {
            XyCursor xyCursor3;
            String str = (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_type");
            String str2 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_version");
            if (jSONObject == null) {
                xyCursor3 = null;
            } else {
                xyCursor3 = DBManager.query("tb_resourse_queue", new String[]{new StringBuilder(String.valueOf(str)).toString(), new StringBuilder(String.valueOf(str2)).toString()}, "res_type = ? and res_version = ? ", new String[]{new StringBuilder(String.valueOf(str)).toString(), new StringBuilder(String.valueOf(str2)).toString()});
                try {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("res_type", str);
                    contentValues.put("res_version", str2);
                    contentValues.put("res_url", (String) JsonUtil.getValueFromJsonObject(jSONObject, "res_url"));
                    if (xyCursor3 != null) {
                        if (xyCursor3.getCount() > 0) {
                            DBManager.update("tb_resourse_queue", contentValues, "res_type = ? and res_version = ? ", new String[]{new StringBuilder(String.valueOf(str)).toString(), new StringBuilder(String.valueOf(str2)).toString()});
                        }
                    }
                    DBManager.insert("tb_resourse_queue", contentValues);
                } catch (Throwable th2) {
                    th = th2;
                    xyCursor2 = xyCursor3;
                    XyCursor.closeCursor(xyCursor2, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(xyCursor3, true);
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor2, true);
            throw th;
        }
    }
}
