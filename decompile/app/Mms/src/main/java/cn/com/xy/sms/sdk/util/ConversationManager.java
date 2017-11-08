package cn.com.xy.sms.sdk.util;

import android.content.ContentValues;
import android.content.Context;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.e;
import cn.com.xy.sms.sdk.dex.DexUtil;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ConversationManager {
    public static int delete(String str, String str2, String[] strArr) {
        try {
            return e.a(str, str2, strArr);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static void deleteLog(String str, String str2, JSONObject jSONObject) {
        DexUtil.deleteLog(str, str2, jSONObject);
    }

    public static void execSQL(String str) {
        e.a(str);
    }

    public static void execSQLSyn(String str) {
        e.b(str);
    }

    public static long insert(String str, ContentValues contentValues) {
        try {
            return e.a(str, contentValues);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static void insertLog(String str, String str2, JSONObject jSONObject) {
        DexUtil.insertLog(str, str2, jSONObject);
    }

    public static XyCursor query(String str, String[] strArr, String str2, String[] strArr2) {
        try {
            return e.a(str, strArr, str2, strArr2);
        } catch (Throwable th) {
            return null;
        }
    }

    public static XyCursor query(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return e.a(str, strArr, str2, strArr2, str3, str4, str5, str6);
        } catch (Throwable th) {
            return null;
        }
    }

    public static XyCursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return e.a(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONObject queryConversationMsg(Context context, String str, JSONObject jSONObject, Map map) {
        return DexUtil.queryConversationMsg(context, str, jSONObject, map);
    }

    public static XyCursor queryDBManager(String str, String[] strArr, String str2, String[] strArr2) {
        try {
            return DBManager.query(str, strArr, str2, strArr2);
        } catch (Throwable th) {
            return null;
        }
    }

    public static XyCursor queryDBManager(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return DBManager.query(str, strArr, str2, strArr2, str3, str4, str5, str6);
        } catch (Throwable th) {
            return null;
        }
    }

    public static XyCursor queryDBManager(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        try {
            return DBManager.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6);
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONArray queryLog(String str, String str2, String[] strArr, JSONObject jSONObject, String str3, String str4) {
        return DexUtil.queryLog(str, str2, strArr, jSONObject, str3, str4);
    }

    public static XyCursor rawQuery(String str, String[] strArr) {
        try {
            return e.a(str, strArr);
        } catch (Throwable th) {
            return null;
        }
    }

    public static XyCursor rawQueryDBManager(String str, String[] strArr) {
        try {
            return DBManager.rawQuery(str, strArr);
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONArray rawQueryJSONArray(String str) {
        return e.c(str);
    }

    public static void saveLogIn(String str, String str2, String str3, Object... objArr) {
        DexUtil.saveLogIn(str, str2, str3, objArr);
    }

    public static void saveLogOut(String str, String str2, String str3, Object... objArr) {
        DexUtil.saveLogOut(str, str2, str3, objArr);
    }

    public static int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        try {
            return e.a(str, contentValues, str2, strArr);
        } catch (Throwable th) {
            return -1;
        }
    }
}
