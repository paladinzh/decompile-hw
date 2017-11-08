package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class p {
    private static String a = "tb_operator_parse_info";
    private static String b = "id";
    private static String c = "phone";
    private static String d = "msg";
    private static String e = "result";
    private static String f = "updateInfoTime";
    private static String g = "numMsgMD5";
    private static String[] h = new String[]{"updateInfoTime", "result"};
    private static String i = "UPDATE tb_operator_parse_info SET msg=NULL";
    private static String j = "ALTER TABLE tb_operator_parse_info ADD COLUMN numMsgMD5 TEXT";

    public static long a(String str, String str2, String str3) {
        long j = -1;
        if (!StringUtils.allValuesIsNotNull(str, str2)) {
            return -1;
        }
        try {
            String md5 = StringUtils.getMD5(new StringBuilder(String.valueOf(str)).append(str2).toString());
            ContentValues contentValues = BaseManager.getContentValues(null, "phone", str, "numMsgMD5", md5, "result", str3, "updateInfoTime", String.valueOf(System.currentTimeMillis()));
            j = DBManager.update("tb_operator_parse_info", contentValues, "phone = ? and numMsgMD5 = ?", new String[]{str, md5}) > 0 ? 0 : DBManager.insert("tb_operator_parse_info", contentValues);
        } catch (Throwable th) {
        }
        return j;
    }

    public static String a() {
        return " create table if not exists tb_operator_parse_info (id INTEGER PRIMARY KEY,phone TEXT,msg TEXT,numMsgMD5 TEXT,result TEXT,updateInfoTime  long DEFAULT '0')";
    }

    public static JSONObject a(String str, String str2) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            String md5 = StringUtils.getMD5(new StringBuilder(String.valueOf(str)).append(str2).toString());
            query = DBManager.query("tb_operator_parse_info", h, "phone = ? and numMsgMD5 = ? ", new String[]{str, md5}, null, null, null, null);
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(h, query);
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
