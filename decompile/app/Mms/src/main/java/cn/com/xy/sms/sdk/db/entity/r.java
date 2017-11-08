package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.util.LruCache;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class r {
    private static String a = "id";
    private static String b = "phoneNum";
    private static String c = "areaCode";
    private static String d = "jsonResult";
    private static String e = "updateTime";
    private static String f = "status";
    private static String g = "version";
    private static String h = "queryKey";
    private static String i = "tb_phonenum_menu";
    private static String j = " DROP TABLE IF EXISTS tb_phonenum_menu";
    private static String k = "create table  if not exists tb_phonenum_menu (id INTEGER PRIMARY KEY,queryKey TEXT unique,phoneNum TEXT,areaCode TEXT,jsonResult TEXT,status INTEGER DEFAULT '1',version TEXT,updateTime INTEGER DEFAULT '0')";
    private static String l = "%s:%s";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long a(String str, String str2, JSONObject jSONObject, long j) {
        long j2 = -1;
        XyCursor xyCursor = null;
        if (jSONObject == null || StringUtils.isNull(str)) {
            return j2;
        }
        if (StringUtils.isNull(str2)) {
            str2 = "CN";
        }
        String format = String.format("%s:%s", new Object[]{str, str2});
        int optInt = jSONObject.optInt("status");
        String optString = jSONObject.optString(NumberInfo.VERSION_KEY);
        ContentValues contentValues = new ContentValues();
        contentValues.put("phoneNum", str);
        contentValues.put("status", Integer.valueOf(optInt));
        contentValues.put("areaCode", str2);
        contentValues.put("jsonResult", jSONObject.toString());
        contentValues.put(NumberInfo.VERSION_KEY, optString);
        contentValues.put("queryKey", format);
        contentValues.put(IccidInfoManager.UPDATE_TIME, Long.valueOf(j));
        contentValues.put("status", Integer.valueOf(optInt));
        try {
            String str3 = "queryKey = ?";
            String[] strArr = new String[]{format};
            xyCursor = DBManager.query("tb_phonenum_menu", null, str3, strArr);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    j2 = (long) DBManager.update("tb_phonenum_menu", contentValues, str3, strArr);
                    XyCursor.closeCursor(xyCursor, true);
                    return j2;
                }
            }
            j2 = DBManager.insert("tb_phonenum_menu", contentValues);
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
        return j2;
    }

    public static long a(List<q> list, long j) {
        if (list != null) {
            if (j > 0) {
                long update;
                ContentValues contentValues = new ContentValues();
                contentValues.put(IccidInfoManager.UPDATE_TIME, Long.valueOf(j));
                int size = list.size();
                String[] strArr = new String[size];
                for (int i = 0; i < size; i++) {
                    q qVar = (q) list.get(i);
                    strArr[i] = String.format("%s:%s", new Object[]{qVar.a, qVar.b});
                }
                try {
                    update = (long) DBManager.update("tb_phonenum_menu", contentValues, "queryKey IN (" + C.a(size) + ")", strArr);
                } catch (Throwable th) {
                    update = th;
                    update = -1;
                    return update;
                } finally {
                    XyCursor.closeCursor(null, true);
                }
                return update;
            }
        }
        return -1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static q a(String str, String str2) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        if (StringUtils.isNull(str)) {
            return null;
        }
        q qVar;
        if (StringUtils.isNull(str2)) {
            str2 = "CN";
        }
        try {
            String format = String.format("%s:%s", new Object[]{str, str2});
            String[] strArr = new String[]{format};
            query = DBManager.query("tb_phonenum_menu", null, "queryKey = ?", strArr);
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        q qVar2 = new q();
                        qVar2.b = str2;
                        qVar2.a = str;
                        int columnIndex = query.getColumnIndex("jsonResult");
                        int columnIndex2 = query.getColumnIndex("status");
                        if (query.moveToNext()) {
                            query.getInt(columnIndex2);
                            qVar2.c = query.getString(columnIndex);
                        }
                        qVar = qVar2;
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = query;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return qVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<q> a(long j, int i) {
        XyCursor xyCursor = null;
        List<q> arrayList = new ArrayList();
        try {
            String[] strArr = new String[]{"phoneNum", "areaCode", NumberInfo.VERSION_KEY};
            String str = "tb_phonenum_menu";
            xyCursor = DBManager.query(str, strArr, "updateTime < ? and status = ?", new String[]{String.valueOf(j), String.valueOf(i)});
            if (xyCursor != null) {
                int columnIndex = xyCursor.getColumnIndex("phoneNum");
                int columnIndex2 = xyCursor.getColumnIndex("areaCode");
                int columnIndex3 = xyCursor.getColumnIndex(NumberInfo.VERSION_KEY);
                while (xyCursor.moveToNext()) {
                    q qVar = new q();
                    qVar.a = xyCursor.getString(columnIndex);
                    qVar.b = xyCursor.getString(columnIndex2);
                    qVar.d = xyCursor.getString(columnIndex3);
                    arrayList.add(qVar);
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
        return arrayList;
    }

    public static Map<String, JSONObject> a(List<String> list, String str, LruCache<String, JSONObject> lruCache) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        Map<String, JSONObject> hashMap = new HashMap();
        if (list == null || list.size() == 0) {
            return hashMap;
        }
        if (StringUtils.isNull(str)) {
            str = "CN";
        }
        try {
            int i;
            int size = list.size();
            String[] strArr = new String[size];
            for (i = 0; i < size; i++) {
                strArr[i] = String.format("%s:%s", new Object[]{StringUtils.getPhoneNumberNo86((String) list.get(i)), str});
            }
            String str2 = "tb_phonenum_menu";
            query = DBManager.query(str2, null, "queryKey IN (" + C.a(size) + ")", strArr);
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("jsonResult");
                        i = query.getColumnIndex("queryKey");
                        while (query.moveToNext()) {
                            String string = query.getString(i);
                            JSONObject jSONObject = new JSONObject(query.getString(columnIndex));
                            hashMap.put(string, jSONObject);
                            if (lruCache != null) {
                                lruCache.put(string, jSONObject);
                            }
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = query;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return hashMap;
    }
}
