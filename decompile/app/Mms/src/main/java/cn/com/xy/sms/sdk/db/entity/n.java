package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.database.Cursor;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.c;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class n {
    private static String a = "id";
    private static String b = "num";
    private static String c = "result";
    private static String d = "version";
    private static String e = "last_query_time";
    private static String f = "t9_flag";
    private static String g = "tb_number_info";
    private static String h = " DROP TABLE IF EXISTS tb_number_info";
    private static String i = "CREATE TABLE IF NOT EXISTS tb_number_info (id INTEGER PRIMARY KEY, num TEXT UNIQUE, result TEXT, version TEXT, t9_flag INTEGER DEFAULT 0, last_query_time INTEGER DEFAULT 0)";
    private static String j = "CREATE INDEX IDX_tb_number_info_last_query_time ON tb_number_info(last_query_time);";
    private static int k = 1;
    private static int l = 2;
    private static int m = 3;
    private static int n = 4;
    private static String o = "ALTER TABLE tb_number_info ADD COLUMN t9_flag INTEGER DEFAULT '0'";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(String str, int i) {
        XyCursor xyCursor = null;
        if (StringUtils.isNull(str)) {
            return -1;
        }
        try {
            xyCursor = c.a("tb_number_info", new String[]{"result"}, "num = ? ", new String[]{str});
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0 && xyCursor.moveToFirst()) {
                    new JSONObject(xyCursor.getString(xyCursor.getColumnIndex("result"))).put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, 1);
                    String[] strArr = new String[]{str};
                    int a = c.a("tb_number_info", BaseManager.getContentValues(null, "result", r2.toString()), "num = ? ", strArr);
                    XyCursor.closeCursor(xyCursor, true);
                    return a;
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
        return -1;
    }

    public static int a(String str, long j) {
        if (StringUtils.isNull(str)) {
            return -1;
        }
        try {
            return c.a("tb_number_info", BaseManager.getContentValues(null, "last_query_time", String.valueOf(j)), "num = ? ", new String[]{str});
        } catch (Throwable th) {
            return -1;
        }
    }

    private static int a(Set<String> set, int i) {
        XyCursor a;
        Throwable th;
        XyCursor xyCursor = null;
        if (set == null || set.size() == 0) {
            return -1;
        }
        try {
            int size = set.size();
            String[] strArr = new String[]{IccidInfoManager.NUM, "result"};
            a = c.a("tb_number_info", strArr, "num IN(" + C.a(size) + ")", (String[]) set.toArray(new String[size]));
            if (a != null) {
                try {
                    if (a.getCount() != 0) {
                        int i2 = 0;
                        while (a.moveToNext()) {
                            String string = a.getString(a.getColumnIndex("result"));
                            if (!StringUtils.isNull(string)) {
                                String string2 = a.getString(a.getColumnIndex(IccidInfoManager.NUM));
                                new JSONObject(string).put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, i);
                                i2 += c.a("tb_number_info", BaseManager.getContentValues(null, "result", r4.toString()), "num = ? ", new String[]{string2});
                            }
                        }
                        XyCursor.closeCursor(a, true);
                        return i2;
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
            return -1;
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    private static int a(Set<String> set, long j) {
        try {
            int size = set.size();
            return c.a("tb_number_info", BaseManager.getContentValues(null, "last_query_time", String.valueOf(j)), "num IN(" + C.a(size) + ")", (String[]) set.toArray(new String[size]));
        } catch (Throwable th) {
            return -1;
        }
    }

    public static long a(String str, JSONObject jSONObject, String str2, long j) {
        return a(str, jSONObject, str2, j, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long a(String str, JSONObject jSONObject, String str2, long j, int i) {
        if (StringUtils.isNull(str)) {
            return -1;
        }
        XyCursor xyCursor = null;
        try {
            xyCursor = c.a("tb_number_info", new String[]{"id", "result"}, "num = ? ", new String[]{str});
            String jSONObject2 = jSONObject != null ? jSONObject.toString() : "";
            ContentValues contentValues = BaseManager.getContentValues(null, IccidInfoManager.NUM, str, "result", jSONObject2, NumberInfo.VERSION_KEY, str2, "last_query_time", String.valueOf(j), "t9_flag", String.valueOf(i));
            long j2;
            if (xyCursor != null && xyCursor.getCount() > 0 && xyCursor.moveToFirst()) {
                Object obj;
                Object obj2;
                Object obj3;
                JSONObject jSONObject3;
                String string = xyCursor.getString(xyCursor.getColumnIndex("result"));
                if (jSONObject != null) {
                    obj = null;
                } else {
                    int i2 = 1;
                }
                if (obj == null) {
                    if (jSONObject.has(NumberInfo.USER_TAG_KEY)) {
                        obj2 = null;
                        obj3 = (StringUtils.isNull(string) && string.contains(NumberInfo.USER_TAG_KEY)) ? 1 : null;
                        obj3 = (obj2 == null || obj3 == null) ? null : 1;
                        if (obj3 == null) {
                            jSONObject2 = obj == null ? jSONObject.toString() : "";
                        } else {
                            if (obj != null) {
                                jSONObject = new JSONObject();
                            }
                            jSONObject3 = new JSONObject(string);
                            jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject3.optString(NumberInfo.USER_TAG_KEY));
                            jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject3.optInt(NumberInfo.USER_TAG_TYPE_KEY));
                            jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject3.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
                            jSONObject2 = jSONObject.toString();
                        }
                        contentValues.put("result", jSONObject2);
                        if (c.a("tb_number_info", contentValues, "num = ? ", new String[]{str}) <= 0) {
                            XyCursor.closeCursor(xyCursor, true);
                            return -1;
                        }
                        j2 = (long) xyCursor.getInt(xyCursor.getColumnIndex("id"));
                        XyCursor.closeCursor(xyCursor, true);
                        return j2;
                    }
                }
                obj2 = 1;
                if (!StringUtils.isNull(string)) {
                    if (obj2 == null) {
                        if (obj3 == null) {
                            if (obj != null) {
                                jSONObject = new JSONObject();
                            }
                            jSONObject3 = new JSONObject(string);
                            jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject3.optString(NumberInfo.USER_TAG_KEY));
                            jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject3.optInt(NumberInfo.USER_TAG_TYPE_KEY));
                            jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject3.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
                            jSONObject2 = jSONObject.toString();
                        } else if (obj == null) {
                        }
                        contentValues.put("result", jSONObject2);
                        if (c.a("tb_number_info", contentValues, "num = ? ", new String[]{str}) <= 0) {
                            j2 = (long) xyCursor.getInt(xyCursor.getColumnIndex("id"));
                            XyCursor.closeCursor(xyCursor, true);
                            return j2;
                        }
                        XyCursor.closeCursor(xyCursor, true);
                        return -1;
                    }
                    if (obj3 == null) {
                        if (obj != null) {
                            jSONObject = new JSONObject();
                        }
                        jSONObject3 = new JSONObject(string);
                        jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject3.optString(NumberInfo.USER_TAG_KEY));
                        jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject3.optInt(NumberInfo.USER_TAG_TYPE_KEY));
                        jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject3.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
                        jSONObject2 = jSONObject.toString();
                    } else if (obj == null) {
                    }
                    contentValues.put("result", jSONObject2);
                    if (c.a("tb_number_info", contentValues, "num = ? ", new String[]{str}) <= 0) {
                        XyCursor.closeCursor(xyCursor, true);
                        return -1;
                    }
                    j2 = (long) xyCursor.getInt(xyCursor.getColumnIndex("id"));
                    XyCursor.closeCursor(xyCursor, true);
                    return j2;
                }
                if (obj2 == null) {
                    if (obj3 == null) {
                        if (obj != null) {
                            jSONObject = new JSONObject();
                        }
                        jSONObject3 = new JSONObject(string);
                        jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject3.optString(NumberInfo.USER_TAG_KEY));
                        jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject3.optInt(NumberInfo.USER_TAG_TYPE_KEY));
                        jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject3.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
                        jSONObject2 = jSONObject.toString();
                    } else if (obj == null) {
                    }
                    contentValues.put("result", jSONObject2);
                    if (c.a("tb_number_info", contentValues, "num = ? ", new String[]{str}) <= 0) {
                        j2 = (long) xyCursor.getInt(xyCursor.getColumnIndex("id"));
                        XyCursor.closeCursor(xyCursor, true);
                        return j2;
                    }
                    XyCursor.closeCursor(xyCursor, true);
                    return -1;
                }
                if (obj3 == null) {
                    if (obj != null) {
                        jSONObject = new JSONObject();
                    }
                    jSONObject3 = new JSONObject(string);
                    jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject3.optString(NumberInfo.USER_TAG_KEY));
                    jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject3.optInt(NumberInfo.USER_TAG_TYPE_KEY));
                    jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject3.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
                    jSONObject2 = jSONObject.toString();
                } else if (obj == null) {
                }
                contentValues.put("result", jSONObject2);
                if (c.a("tb_number_info", contentValues, "num = ? ", new String[]{str}) <= 0) {
                    XyCursor.closeCursor(xyCursor, true);
                    return -1;
                }
                j2 = (long) xyCursor.getInt(xyCursor.getColumnIndex("id"));
                XyCursor.closeCursor(xyCursor, true);
                return j2;
            }
            j2 = c.a("tb_number_info", contentValues);
            XyCursor.closeCursor(xyCursor, true);
            return j2;
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
    }

    private static ContentValues a(JSONObject jSONObject, String str, String str2, int i, int i2, String str3, String str4) {
        jSONObject.put(NumberInfo.NUM_KEY, str);
        jSONObject.put(NumberInfo.USER_TAG_KEY, str2);
        jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, i);
        jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, 0);
        return BaseManager.getContentValues(null, IccidInfoManager.NUM, str, "result", jSONObject.toString(), NumberInfo.VERSION_KEY, str3, "last_query_time", str4);
    }

    public static Cursor a() {
        Cursor cursor = null;
        try {
            String str = "tb_number_info";
            String[] strArr = new String[]{"id", IccidInfoManager.NUM, "result"};
            XyCursor a = c.a(str, strArr, "t9_flag = ?", new String[]{"1"});
            if (a != null) {
                cursor = a.getCur();
            }
        } catch (Throwable th) {
        }
        return cursor;
    }

    private static NumberInfo a(String str, String str2) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        String str3;
        String[] strArr;
        if (StringUtils.isNull(null)) {
            str3 = "num = ?";
            strArr = new String[]{str};
        } else {
            str3 = "num = ? AND version = ?";
            strArr = new String[]{str, null};
        }
        List a = a(str3, strArr, 1);
        return (a != null && a.size() > 0) ? (NumberInfo) a.get(0) : null;
    }

    private static String a(JSONObject jSONObject, String str) {
        Object obj;
        Object obj2 = 1;
        if (jSONObject != null) {
            obj = null;
        } else {
            int i = 1;
        }
        Object obj3 = (obj == null && jSONObject.has(NumberInfo.USER_TAG_KEY)) ? null : 1;
        if (!StringUtils.isNull(str) && str.contains(NumberInfo.USER_TAG_KEY)) {
            int i2 = 1;
        } else {
            Object obj4 = null;
        }
        if (obj3 != null) {
            if (obj4 == null) {
            }
            if (obj2 != null) {
                return obj != null ? jSONObject.toString() : "";
            } else {
                if (obj != null) {
                    jSONObject = new JSONObject();
                }
                JSONObject jSONObject2 = new JSONObject(str);
                jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject2.optString(NumberInfo.USER_TAG_KEY));
                jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject2.optInt(NumberInfo.USER_TAG_TYPE_KEY));
                jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject2.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
                return jSONObject.toString();
            }
        }
        obj2 = null;
        if (obj2 != null) {
            if (obj != null) {
                jSONObject = new JSONObject();
            }
            JSONObject jSONObject22 = new JSONObject(str);
            jSONObject.put(NumberInfo.USER_TAG_KEY, jSONObject22.optString(NumberInfo.USER_TAG_KEY));
            jSONObject.put(NumberInfo.USER_TAG_TYPE_KEY, jSONObject22.optInt(NumberInfo.USER_TAG_TYPE_KEY));
            jSONObject.put(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY, jSONObject22.optString(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY));
            return jSONObject.toString();
        } else if (obj != null) {
        }
    }

    private static List<NumberInfo> a(String str, String[] strArr, int i) {
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor a;
        try {
            a = c.a(false, "tb_number_info", new String[]{"id", IccidInfoManager.NUM, "result", NumberInfo.VERSION_KEY, "last_query_time"}, str, strArr, null, null, null, String.valueOf(i));
            if (a != null) {
                try {
                    if (a.getCount() != 0) {
                        List<NumberInfo> arrayList = new ArrayList();
                        while (a.moveToNext()) {
                            NumberInfo numberInfo = new NumberInfo();
                            numberInfo.id = a.getInt(a.getColumnIndex("id"));
                            numberInfo.num = a.getString(a.getColumnIndex(IccidInfoManager.NUM));
                            numberInfo.result = a.getString(a.getColumnIndex("result"));
                            numberInfo.version = a.getString(a.getColumnIndex(NumberInfo.VERSION_KEY));
                            numberInfo.lastQueryTime = a.getLong(a.getColumnIndex("last_query_time"));
                            arrayList.add(numberInfo);
                        }
                        XyCursor.closeCursor(a, true);
                        return arrayList;
                    }
                } catch (Throwable th2) {
                    xyCursor = a;
                    th = th2;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(a, true);
            return null;
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    private static List<NumberInfo> a(List<String> list) {
        int size = list.size();
        return a("num IN(" + C.a(size) + ")", (String[]) list.toArray(new String[size]), Integer.MAX_VALUE);
    }

    public static Map<String, String[]> a(int i) {
        XyCursor a;
        Throwable th;
        XyCursor xyCursor = null;
        Map<String, String[]> linkedHashMap = new LinkedHashMap();
        try {
            a = c.a(false, "tb_number_info", new String[]{IccidInfoManager.NUM, "result"}, "result LIKE '%\"u\":0%' AND last_query_time>0 ", null, null, null, "last_query_time DESC", "20");
            if (a != null) {
                try {
                    if (a.getCount() != 0) {
                        while (a.moveToNext()) {
                            String string = a.getString(a.getColumnIndex(IccidInfoManager.NUM));
                            JSONObject jSONObject = new JSONObject(a.getString(a.getColumnIndex("result")));
                            linkedHashMap.put(string, new String[]{jSONObject.optString(NumberInfo.USER_TAG_KEY), jSONObject.optString(NumberInfo.USER_TAG_TYPE_KEY)});
                        }
                        XyCursor.closeCursor(a, true);
                        return linkedHashMap;
                    }
                } catch (Throwable th2) {
                    xyCursor = a;
                    th = th2;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(a, true);
            return null;
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static Map<String, String> a(int i, int i2) {
        String str;
        XyCursor xyCursor;
        XyCursor xyCursor2 = null;
        Map<String, String> linkedHashMap = new LinkedHashMap();
        switch (i) {
            case 1:
                try {
                    str = "result='' AND last_query_time > 0";
                    break;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    XyCursor.closeCursor(xyCursor2, true);
                    throw th2;
                }
            case 2:
                str = "result<>'' AND version='' AND last_query_time>0 AND last_query_time <= " + (System.currentTimeMillis() - DexUtil.getUpdateCycleByType(38, 86400000));
                break;
            case 3:
                str = "result<>'' AND version<>'' AND last_query_time>0 AND last_query_time <= " + (System.currentTimeMillis() - DexUtil.getUpdateCycleByType(39, Constant.month));
                break;
            default:
                XyCursor.closeCursor(null, true);
                return null;
        }
        xyCursor = c.a(false, "tb_number_info", new String[]{IccidInfoManager.NUM, NumberInfo.VERSION_KEY}, str, null, null, null, "last_query_time DESC", "20");
        if (xyCursor != null) {
            try {
                if (xyCursor.getCount() != 0) {
                    while (xyCursor.moveToNext()) {
                        linkedHashMap.put(xyCursor.getString(xyCursor.getColumnIndex(IccidInfoManager.NUM)), xyCursor.getString(xyCursor.getColumnIndex(NumberInfo.VERSION_KEY)));
                    }
                    XyCursor.closeCursor(xyCursor, true);
                    return linkedHashMap;
                }
            } catch (Throwable th3) {
                xyCursor2 = xyCursor;
                th2 = th3;
                XyCursor.closeCursor(xyCursor2, true);
                throw th2;
            }
        }
        XyCursor.closeCursor(xyCursor, true);
        return null;
    }

    private static JSONObject a(NumberInfo numberInfo, String str, int i) {
        if (numberInfo == null || StringUtils.isNull(str)) {
            return null;
        }
        JSONObject jSONObject = StringUtils.isNull(numberInfo.result) ? new JSONObject() : new JSONObject(numberInfo.result);
        return c.a("tb_number_info", a(jSONObject, numberInfo.num, str, i, 0, numberInfo.version, String.valueOf(numberInfo.lastQueryTime)), "num = ? ", new String[]{numberInfo.num}) <= 0 ? null : jSONObject;
    }

    public static JSONObject a(String str, String str2, int i) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        try {
            NumberInfo a = a(str, null);
            JSONObject jSONObject;
            if (a == null) {
                jSONObject = new JSONObject();
                return ((c.a("tb_number_info", a(jSONObject, str, str2, i, 0, "", String.valueOf(System.currentTimeMillis()))) > 0 ? 1 : (c.a("tb_number_info", a(jSONObject, str, str2, i, 0, "", String.valueOf(System.currentTimeMillis()))) == 0 ? 0 : -1)) <= 0 ? 1 : null) == null ? jSONObject : null;
            } else if (a == null || StringUtils.isNull(str2)) {
                return null;
            } else {
                jSONObject = StringUtils.isNull(a.result) ? new JSONObject() : new JSONObject(a.result);
                return c.a("tb_number_info", a(jSONObject, a.num, str2, i, 0, a.version, String.valueOf(a.lastQueryTime)), "num = ? ", new String[]{a.num}) <= 0 ? null : jSONObject;
            }
        } catch (Throwable th) {
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(String str) {
        XyCursor xyCursor = null;
        if (!StringUtils.isNull(str)) {
            try {
                xyCursor = c.a("tb_number_info", new String[]{"result"}, "num = ? ", new String[]{str});
                if (xyCursor != null) {
                    if (xyCursor.getCount() > 0 && xyCursor.moveToFirst()) {
                        a(new JSONObject(xyCursor.getString(xyCursor.getColumnIndex("result"))));
                        String[] strArr = new String[]{str};
                        c.a("tb_number_info", BaseManager.getContentValues(null, "result", r2.toString()), "num = ? ", strArr);
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
        }
    }

    public static void a(JSONObject jSONObject) {
        if (jSONObject != null) {
            jSONObject.remove(NumberInfo.USER_TAG_KEY);
            jSONObject.remove(NumberInfo.USER_TAG_TYPE_KEY);
            jSONObject.remove(NumberInfo.USER_TAG_UPLOAD_STATUS_KEY);
        }
    }

    public static JSONObject b(String str) {
        if (StringUtils.isNull(str)) {
            return null;
        }
        NumberInfo a = a(str, null);
        if (!(a == null || StringUtils.isNull(a.result))) {
            try {
                return new JSONObject(a.result);
            } catch (Throwable th) {
            }
        }
        return null;
    }

    private static JSONObject b(String str, String str2, int i) {
        int i2 = 0;
        JSONObject jSONObject = new JSONObject();
        if (c.a("tb_number_info", a(jSONObject, str, str2, i, 0, "", String.valueOf(System.currentTimeMillis()))) <= 0) {
            i2 = 1;
        }
        return i2 == 0 ? jSONObject : null;
    }

    public static boolean b(JSONObject jSONObject) {
        if (jSONObject == null) {
            return false;
        }
        boolean z = (StringUtils.isNull(jSONObject.optString(NumberInfo.NUM_KEY)) || StringUtils.isNull(jSONObject.optString(NumberInfo.VERSION_KEY))) ? false : true;
        return z || (!StringUtils.isNull(jSONObject.optString(NumberInfo.USER_TAG_KEY)));
    }
}
