package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class o {
    private static String a = "tb_operator_cmd_info";
    private static String b = "id";
    private static String c = "phone";
    private static String d = "iccid";
    private static String e = "actions";
    private static String f = "updateInfoTime";
    private static String[] g = new String[]{"updateInfoTime", "actions"};

    public static long a(String str, String str2, String str3) {
        try {
            ContentValues contentValues = BaseManager.getContentValues(null, "phone", str, IccidInfoManager.ICCID, str2, "actions", str3, "updateInfoTime", String.valueOf(System.currentTimeMillis()));
            return DBManager.update("tb_operator_cmd_info", contentValues, "phone = ? or iccid = ?", new String[]{str, str2}) > 0 ? 0 : DBManager.insert("tb_operator_cmd_info", contentValues);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static String a() {
        return " create table  if not exists tb_operator_cmd_info (id  INTEGER PRIMARY KEY,phone  TEXT,iccid  TEXT,actions TEXT,updateInfoTime  long DEFAULT '0')";
    }

    public static JSONObject a(String str) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query("tb_operator_cmd_info", g, "phone = ?", new String[]{str}, null, null, null, null);
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(g, query);
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
