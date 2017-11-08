package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class l {
    private static String a = "tb_msg_url";
    private static String b = " DROP TABLE IF EXISTS tb_msg_url";
    private static int c = 0;
    private static int d = -1;
    private static int e = 1;
    private static int f = 2;
    private static String g = "_ARR_";
    private static String h = "url";
    private static String i = "check_time";
    private static String j = "check_statu";
    private static String k = "third_check_statu";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(String str, boolean z) {
        String str2;
        XyCursor xyCursor = null;
        if (z) {
            str2 = "third_check_statu";
        } else {
            try {
                str2 = "check_statu";
            } catch (Throwable th) {
                Throwable th2 = th;
                XyCursor xyCursor2 = xyCursor;
                Throwable th3 = th2;
                XyCursor.closeCursor(xyCursor2, true);
                throw th3;
            }
        }
        xyCursor = DBManager.query("tb_msg_url", new String[]{str2}, "url = ? ", new String[]{str});
        if (xyCursor != null) {
            if (xyCursor.moveToNext()) {
                int i = xyCursor.getInt(xyCursor.getColumnIndex(str2));
                XyCursor.closeCursor(xyCursor, true);
                return i;
            }
        }
        XyCursor.closeCursor(xyCursor, true);
        return 0;
    }

    public static long a(String str, int i) {
        try {
            ContentValues contentValues = BaseManager.getContentValues(null, Constant.URLS, str, "third_check_statu", new StringBuilder(String.valueOf(i)).toString(), "check_time", String.valueOf(System.currentTimeMillis()));
            return DBManager.update("tb_msg_url", contentValues, "url = ? ", new String[]{str}) > 0 ? 0 : DBManager.insert("tb_msg_url", contentValues);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static String a() {
        return "create table  if not exists tb_msg_url (  id INTEGER PRIMARY KEY, url TEXT, check_time integer default 0, check_statu integer default 0, third_check_statu integer default 0)";
    }

    public static HashMap<String, Object> a(String[] strArr, boolean z) {
        int i = 0;
        HashMap<String, Object> hashMap = new HashMap();
        int i2 = Integer.MAX_VALUE;
        int i3 = 0;
        while (i < strArr.length) {
            int a = a(strArr[i], z);
            if (a == 0) {
                boolean z2 = true;
            }
            if (a <= i2) {
                i2 = a;
            }
            if (i2 == -1) {
                break;
            }
            i++;
        }
        hashMap.put("statu", Integer.valueOf(i2));
        if (i3 != 0) {
            hashMap.put("hasNotCheck", Boolean.valueOf(true));
        }
        return hashMap;
    }

    public static void a(String str) {
        if (!StringUtils.isNull(str)) {
            try {
                String[] split = str.split("_ARR_");
                if (split != null) {
                    for (String b : split) {
                        b(b, 0);
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(JSONArray jSONArray) {
        if (jSONArray != null && jSONArray.length() > 0) {
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject optJSONObject = jSONArray.optJSONObject(i);
                b(optJSONObject.optString("originURL"), optJSONObject.optInt("validStatus"));
            }
        }
    }

    public static long b(String str, int i) {
        try {
            ContentValues contentValues = BaseManager.getContentValues(null, Constant.URLS, str, "check_statu", new StringBuilder(String.valueOf(i)).toString(), "check_time", String.valueOf(System.currentTimeMillis()));
            if (i != 0) {
                return DBManager.update("tb_msg_url", contentValues, "url = ? ", new String[]{str}) > 0 ? 0 : DBManager.insert("tb_msg_url", contentValues);
            } else {
                return DBManager.update("tb_msg_url", contentValues, "url = ? and check_statu = ?", new String[]{str, "0"}) <= 0 ? DBManager.insert("tb_msg_url", contentValues) : 0;
            }
        } catch (Throwable th) {
            return -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static JSONArray b() {
        XyCursor xyCursor = null;
        JSONArray jSONArray = new JSONArray();
        long currentTimeMillis = System.currentTimeMillis() - DexUtil.getUpdateCycleByType(28, Constant.month);
        try {
            xyCursor = DBManager.query("tb_msg_url", new String[]{Constant.URLS}, "check_time < ? ", new String[]{new StringBuilder(String.valueOf(currentTimeMillis)).toString()});
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    int columnIndex = xyCursor.getColumnIndex(Constant.URLS);
                    while (xyCursor.moveToNext()) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put(Constant.URLS, xyCursor.getString(columnIndex));
                        jSONArray.put(jSONObject);
                    }
                }
            }
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            XyCursor.closeCursor(xyCursor, true);
        }
        return jSONArray;
    }
}
