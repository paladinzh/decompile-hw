package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import java.util.HashMap;

/* compiled from: Unknown */
public final class f {
    public static long a(String str, String str2) {
        long j = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("jarname", str);
            contentValues.put("jarname_ver", str2);
            String str3 = "tb_jarsign";
            if (DBManager.update(str3, contentValues, "jarname=?", new String[]{str}) <= 0) {
                j = DBManager.insert(str3, contentValues);
            }
        } catch (Throwable th) {
        }
        return j;
    }

    public static String a() {
        return "create table  if not exists tb_jarsign(id INTEGER PRIMARY KEY,jarname TEXT unique,jarname_ver TEXT)";
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static HashMap<String, Boolean> b() {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        HashMap<String, Boolean> hashMap;
        try {
            query = DBManager.query("tb_jarsign", new String[]{"jarname", "jarname_ver"}, null, null);
            if (query != null) {
                try {
                    hashMap = new HashMap();
                    while (query.moveToNext()) {
                        hashMap.put(query.getString(0) + query.getString(1), Boolean.valueOf(true));
                    }
                    XyCursor.closeCursor(query, true);
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = query;
                    th = th3;
                }
                return hashMap;
            }
            XyCursor.closeCursor(query, true);
            return null;
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }
}
