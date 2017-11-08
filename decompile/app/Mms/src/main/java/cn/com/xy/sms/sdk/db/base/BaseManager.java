package cn.com.xy.sms.sdk.db.base;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class BaseManager {
    private static String a(String str, XyCursor xyCursor) {
        String str2 = null;
        if (xyCursor != null) {
            try {
                if (xyCursor.moveToNext()) {
                    str2 = xyCursor.getString(xyCursor.getColumnIndex(str));
                }
            } catch (Throwable th) {
            }
        }
        return str2;
    }

    public static ContentValues getContentValues(ContentValues contentValues, boolean z, JSONObject jSONObject, String... strArr) {
        if (jSONObject == null || strArr.length == 0) {
            return null;
        }
        if (contentValues == null) {
            contentValues = new ContentValues();
        }
        int length = strArr.length;
        for (int i = 0; i < length; i++) {
            String str = (String) JsonUtil.getValueFromJsonObject(jSONObject, strArr[i]);
            if (!z || !StringUtils.isNull(str)) {
                contentValues.put(strArr[i], JsonUtil.getValueFromJsonObject(jSONObject, strArr[i]).toString());
            }
        }
        return contentValues;
    }

    public static ContentValues getContentValues(ContentValues contentValues, boolean z, String... strArr) {
        if (strArr == null || strArr.length % 2 != 0) {
            return null;
        }
        if (contentValues == null) {
            contentValues = new ContentValues();
        }
        int length = strArr.length;
        for (int i = 0; i < length; i += 2) {
            if (z) {
                if (!StringUtils.isNull(strArr[i])) {
                    if (StringUtils.isNull(strArr[i + 1])) {
                    }
                }
            }
            contentValues.put(strArr[i], StringUtils.getNoNullString(strArr[i + 1]));
        }
        return contentValues;
    }

    public static ContentValues getContentValues(ContentValues contentValues, String... strArr) {
        return getContentValues(contentValues, false, strArr);
    }

    public static ContentValues getContentValues(ContentValues contentValues, String[] strArr, String[] strArr2, JSONObject jSONObject, boolean z) {
        int i = 0;
        if (jSONObject == null || strArr == null || strArr2 == null) {
            return null;
        }
        int length = strArr.length;
        int length2 = strArr2.length;
        if (length == 0 || length != length2) {
            return null;
        }
        if (contentValues == null) {
            contentValues = new ContentValues();
        }
        while (i < length) {
            if (z) {
                JsonUtil.putJsonToConV(contentValues, jSONObject, strArr[i], strArr2[i]);
            } else {
                contentValues.put(strArr[i], JsonUtil.getValueFromJsonObject(jSONObject, strArr2[i]).toString());
            }
            i++;
        }
        return contentValues;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean hasRecord(String str, String str2, String str3, String... strArr) {
        XyCursor xyCursor = null;
        try {
            xyCursor = DBManager.query(str, new String[]{str2}, str3, strArr);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    XyCursor.closeCursor(xyCursor, true);
                    return true;
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
        return false;
    }

    public static long insertOrUpdate(String str, String str2, String str3, String[] strArr, String... strArr2) {
        Throwable th;
        XyCursor xyCursor = null;
        long j = -1;
        XyCursor query;
        try {
            String[] strArr3 = new String[]{str2};
            query = DBManager.query(str, strArr3, str3, strArr);
            if (query != null) {
                if (query.getCount() > 0) {
                    j = (long) ((Integer) JsonUtil.getValueFromJsonObject(loadSingleDataFromCursor(strArr3, query), str2)).intValue();
                    XyCursor.closeCursor(query, true);
                    return j;
                }
            }
            try {
                j = DBManager.insert(str, getContentValues(null, strArr2));
                XyCursor.closeCursor(query, true);
            } catch (Throwable th2) {
                th = th2;
                XyCursor.closeCursor(query, true);
                throw th;
            }
        } catch (Throwable th3) {
            Throwable th4 = th3;
            query = null;
            th = th4;
            XyCursor.closeCursor(query, true);
            throw th;
        }
        return j;
    }

    public static JSONArray loadArrDataFromCursor(String[] strArr, XyCursor xyCursor) {
        if (xyCursor != null) {
            try {
                JSONArray jSONArray = new JSONArray();
                while (xyCursor.moveToNext()) {
                    JSONObject jSONObject = new JSONObject();
                    for (int i = 0; i < strArr.length; i++) {
                        jSONObject.put(strArr[i], StringUtils.getNoNullString(xyCursor.getString(i)));
                    }
                    jSONArray.put(jSONObject);
                }
                return jSONArray;
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public static Map<String, JSONObject> loadMapDataFromCursor(String[] strArr, int i, XyCursor xyCursor) {
        if (xyCursor != null) {
            try {
                Map<String, JSONObject> hashMap = new HashMap();
                while (xyCursor.moveToNext()) {
                    JSONObject jSONObject = new JSONObject();
                    for (int i2 = 0; i2 < strArr.length; i2++) {
                        jSONObject.put(strArr[i2], StringUtils.getNoNullString(xyCursor.getString(i2)));
                    }
                    hashMap.put((String) JsonUtil.getValueFromJsonObject(jSONObject, strArr[i]), jSONObject);
                }
                return hashMap;
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public static JSONObject loadSingleDataFromCursor(String[] strArr, XyCursor xyCursor) {
        JSONObject jSONObject = null;
        if (xyCursor != null) {
            try {
                if (xyCursor.moveToNext()) {
                    JSONObject jSONObject2 = new JSONObject();
                    for (int i = 0; i < strArr.length; i++) {
                        jSONObject2.put(strArr[i], StringUtils.getNoNullString(xyCursor.getString(i)));
                    }
                    jSONObject = jSONObject2;
                }
                return jSONObject;
            } catch (Throwable th) {
            }
        }
        return null;
    }

    public static long saveOrUpdate(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues, String str2, String[] strArr) {
        long j = 0;
        synchronized (DBManager.dblock) {
            try {
                if (((long) sQLiteDatabase.update(str, contentValues, str2, strArr)) == 0) {
                    j = sQLiteDatabase.insert(str, null, contentValues);
                }
            } catch (Throwable th) {
                r0 = new Exception();
            }
        }
        return j;
    }

    public static int update(String str, String str2, String[] strArr, String... strArr2) {
        int i = 0;
        try {
            i = DBManager.update(str, getContentValues(null, strArr2), str2, strArr);
        } catch (Throwable th) {
        }
        return i;
    }
}
