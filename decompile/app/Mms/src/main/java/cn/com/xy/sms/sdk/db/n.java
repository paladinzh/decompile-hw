package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.LruCache;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.db.a.a;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class n {
    private static String a = "tb_phone_pubid";
    private static String b = "id";
    private static String c = "phonenum";
    private static String d = "publd";
    private static String e = "querytime";
    private static String f = "queryflag";
    private static String g = "create table  if not exists tb_phone_pubid(id INTEGER PRIMARY KEY AUTOINCREMENT,phonenum TEXT,publd TEXT,queryflag TEXT,querytime number(24))";
    private static String h = "DROP TABLE IF EXISTS tb_phone_pubid";
    private static String i = "create index if not exists indx_phone on tb_phone_pubid (phonenum)";
    private static LruCache<String, String> j = new LruCache(SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE);

    public static JSONArray a(long j, String str, boolean z) {
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        XyCursor xyCursor = null;
        JSONArray jSONArray = new JSONArray();
        String str2 = "querytime asc";
        String[] strArr = new String[]{"id", NetUtil.REQ_QUERY_NUM, "publd", "queryflag", "querytime"};
        SQLiteDatabase a;
        try {
            a = a.a();
            if (a != null) {
                if (z) {
                    xyCursor = a.a("tb_phone_pubid", strArr, "querytime <? and publd='' and queryflag =0", new String[]{String.valueOf(j)}, null, null, str2, str);
                } else {
                    try {
                        xyCursor = a.a("tb_phone_pubid", strArr, "querytime <? and publd!='' and queryflag =0", new String[]{String.valueOf(j)}, null, null, str2, str);
                    } catch (Throwable th2) {
                        th = th2;
                        XyCursor.closeCursor(xyCursor, true);
                        a.a(a);
                        throw th;
                    }
                }
                while (xyCursor.moveToNext()) {
                    jSONArray.put(StringUtils.getNoNullString(xyCursor.getString(1)));
                }
                if (z) {
                    a.execSQL("UPDATE tb_phone_pubid SET queryflag =1 WHERE id IN (SELECT id FROM tb_phone_pubid WHERE querytime < " + String.valueOf(j) + " and publd" + "='' and queryflag" + " =0  ORDER BY querytime asc  limit " + str + " )");
                } else {
                    a.execSQL("UPDATE tb_phone_pubid SET queryflag =1 WHERE id IN (SELECT id FROM tb_phone_pubid WHERE querytime < " + String.valueOf(j) + " and publd" + "!='' and queryflag" + " =0  ORDER BY querytime asc  limit " + str + " )");
                }
                XyCursor.closeCursor(xyCursor, true);
                a.a(a);
                return jSONArray;
            }
            XyCursor.closeCursor(null, true);
            a.a(a);
            return null;
        } catch (Throwable th3) {
            th = th3;
            a = null;
            XyCursor.closeCursor(xyCursor, true);
            a.a(a);
            throw th;
        }
    }

    public static JSONObject a(String str) {
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor a;
        try {
            a = a.a("tb_phone_pubid", new String[]{NetUtil.REQ_QUERY_NUM, "publd", "queryflag", "querytime"}, "phonenum =?", new String[]{str});
            if (a != null) {
                try {
                    if (a.moveToNext()) {
                        int columnIndex = a.getColumnIndex(NetUtil.REQ_QUERY_NUM);
                        int columnIndex2 = a.getColumnIndex("publd");
                        int columnIndex3 = a.getColumnIndex("querytime");
                        int columnIndex4 = a.getColumnIndex("queryflag");
                        String string = a.getString(columnIndex);
                        String string2 = a.getString(columnIndex2);
                        long j = a.getLong(columnIndex3);
                        JSONObject put = new JSONObject().put("phone", string).put("pubId", string2).put("querytime", j).put("queryflag", a.getString(columnIndex4));
                        XyCursor.closeCursor(a, true);
                        return put;
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = a;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(a, true);
            return null;
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static void a(String str, String str2) {
        try {
            j.put(str, str2);
        } catch (Exception e) {
        }
    }

    public static void a(boolean z) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("queryflag", Integer.valueOf(0));
        contentValues.put("querytime", Long.valueOf(System.currentTimeMillis()));
        try {
            a.a("tb_phone_pubid", contentValues, "queryflag=1", null);
        } catch (Exception e) {
        }
    }

    public static boolean a(HashMap<String, String> hashMap) {
        SQLiteDatabase a = a.a();
        if (a == null) {
            return false;
        }
        try {
            a.beginTransaction();
            ContentValues contentValues = new ContentValues();
            contentValues.put("PHONENUM", (String) hashMap.get("phone"));
            contentValues.put("PUBLD", (String) hashMap.get("pubId"));
            contentValues.put("QUERYTIME", (String) hashMap.get("querytime"));
            contentValues.put("QUERYFLAG", (String) hashMap.get("queryflag"));
            if (!(((long) a.update("tb_phone_pubid", contentValues, "phonenum=?", new String[]{(String) hashMap.get("phone")})) >= 1)) {
                a.insert("tb_phone_pubid", null, contentValues);
            }
            if (a != null) {
                try {
                    if (a.inTransaction()) {
                        a.setTransactionSuccessful();
                        a.endTransaction();
                    }
                    a.a(a);
                } catch (Throwable th) {
                }
            }
        } catch (Throwable th2) {
            try {
                new StringBuilder("ParsePhonePubIdManager.saveOrUpdate ").append(th2.getMessage());
            } finally {
                a.a(a);
            }
        }
        return false;
        a.a(a);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean a(JSONArray jSONArray) {
        SQLiteDatabase a = a.a();
        if (a != null) {
            a.beginTransaction();
            int i = 0;
            while (i < jSONArray.length()) {
                try {
                    JSONObject optJSONObject = jSONArray.optJSONObject(i);
                    if (!(optJSONObject == null || StringUtils.isNull(optJSONObject.optString("phone")))) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put("PHONENUM", optJSONObject.optString("phone"));
                        contentValues.put("PUBLD", optJSONObject.optString("pubId"));
                        contentValues.put("QUERYTIME", Long.valueOf(System.currentTimeMillis()));
                        contentValues.put("QUERYFLAG", "0");
                        if (!(((long) a.update("tb_phone_pubid", contentValues, "phonenum=?", new String[]{optJSONObject.optString("phone")})) >= 1)) {
                            a.insert("tb_phone_pubid", null, contentValues);
                        }
                    }
                    i++;
                } catch (Throwable th) {
                    if (a != null) {
                        try {
                            if (a.inTransaction()) {
                                a.setTransactionSuccessful();
                                a.endTransaction();
                            }
                            a.a(a);
                        } catch (Throwable th2) {
                            a.a(a);
                        }
                    }
                }
            }
            if (a != null) {
                try {
                    if (a.inTransaction()) {
                        a.setTransactionSuccessful();
                        a.endTransaction();
                    }
                    a.a(a);
                } catch (Throwable th3) {
                    a.a(a);
                }
            }
        }
        return false;
    }

    public static String b(String str) {
        return (String) j.get(str);
    }

    private static void b(JSONArray jSONArray) {
        if (jSONArray != null) {
            JSONObject jSONObject = new JSONObject();
            int i = 0;
            while (i < jSONArray.length()) {
                try {
                    JSONObject optJSONObject = jSONArray.optJSONObject(i);
                    if (optJSONObject != null) {
                        j.put(optJSONObject.optString("phone"), optJSONObject.optString("pubId"));
                    }
                    i++;
                } catch (Throwable th) {
                    return;
                }
            }
        }
    }
}
