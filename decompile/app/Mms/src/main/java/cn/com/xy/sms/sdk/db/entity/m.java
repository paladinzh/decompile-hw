package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class m {
    private static String a = "tb_netquery_time";
    private static String b = " DROP TABLE IF EXISTS tb_netquery_time";
    private static String c = "id";
    private static String d = "phone_num";
    private static String e = "area_code";
    private static String f = "request_time";
    private static String g = "status";
    private static String h = "ALTER TABLE tb_netquery_time ADD COLUMN area_code TEXT";
    private static String i = "ALTER TABLE tb_netquery_time ADD COLUMN status INTEGER DEFAULT 0";

    public static long a(String str, String str2, int i) {
        return a(str, str2, i, System.currentTimeMillis());
    }

    public static long a(String str, String str2, int i, long j) {
        try {
            ContentValues contentValues = BaseManager.getContentValues(null, "area_code", str2, "phone_num", str, "request_time", String.valueOf(j), "status", String.valueOf(i));
            return DBManager.update("tb_netquery_time", contentValues, "phone_num = ? and area_code = ?", new String[]{str, str2}) > 0 ? 0 : DBManager.insert("tb_netquery_time", contentValues);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static String a() {
        return " create table  if not exists tb_netquery_time (id INTEGER PRIMARY KEY,phone_num TEXT,area_code TEXT,request_time LONG DEFAULT 0,status INTEGER DEFAULT 0)";
    }

    public static List<String> a(String str, long j) {
        return a(str, j, 0);
    }

    public static List<String> a(String str, long j, int i) {
        XyCursor rawQuery;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            List arrayList = new ArrayList();
            arrayList.add(String.valueOf(j));
            String str2 = "SELECT DISTINCT tbA.phone_num,tbB.name,tbB.cmd,tbB.ec,tbB.mark_time,tbB.mark_cmd,tbB.mark_ec FROM tb_netquery_time tbA LEFT JOIN tb_num_name tbB ON tbB.num=tbA.phone_num WHERE tbA.request_time<? AND tbA.request_time>0" + " AND tbA.status=?";
            arrayList.add("0");
            if (!StringUtils.isNull(str)) {
                str2 = new StringBuilder(String.valueOf(str2)).append(" AND tbA.area_code=?").toString();
                arrayList.add(str);
            }
            rawQuery = DBManager.rawQuery(str2, (String[]) arrayList.toArray(new String[arrayList.size()]));
            if (rawQuery != null) {
                try {
                    List<String> arrayList2 = new ArrayList();
                    while (rawQuery.moveToNext()) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put(IccidInfoManager.NUM, rawQuery.getString(0));
                        jSONObject.put(NumberInfo.VERSION_KEY, ThemeUtil.SET_NULL_STR);
                        jSONObject.put("name", rawQuery.getString(rawQuery.getColumnIndex("name")));
                        jSONObject.put("cmd", rawQuery.getString(rawQuery.getColumnIndex("cmd")));
                        jSONObject.put("ec", rawQuery.getString(rawQuery.getColumnIndex("ec")));
                        jSONObject.put("markTime", rawQuery.getInt(rawQuery.getColumnIndex("mark_time")));
                        jSONObject.put("markCmd", rawQuery.getInt(rawQuery.getColumnIndex("mark_cmd")));
                        jSONObject.put("markEC", rawQuery.getInt(rawQuery.getColumnIndex("mark_ec")));
                        arrayList2.add(jSONObject.toString());
                    }
                    XyCursor.closeCursor(rawQuery, true);
                    return arrayList2;
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = rawQuery;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(rawQuery, true);
            return null;
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static void a(long j) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("request_time", Long.valueOf(System.currentTimeMillis()));
            DBManager.update("tb_netquery_time", contentValues, "request_time < ? and request_time > 0 AND status = 0", new String[]{String.valueOf(j)});
        } catch (Throwable th) {
        }
    }

    public static boolean a(String str, String str2) {
        try {
            if (XyUtil.checkNetWork(Constant.getContext(), 2) == -1) {
                return false;
            }
            JSONObject c = c(str, str2);
            if (c == null) {
                a(str, str2, 0);
                return true;
            }
            if (System.currentTimeMillis() <= Long.parseLong(c.getString("request_time")) + DexUtil.getUpdateCycleByType(1, Constant.month)) {
                return false;
            }
            a(str, str2, 0);
            return true;
        } catch (Throwable th) {
            return true;
        }
    }

    public static void b(String str, String str2) {
        try {
            if (!StringUtils.isNull(str)) {
                String str3 = " phone_num = ? ";
                if (!StringUtils.isNull(null)) {
                    str3 = new StringBuilder(String.valueOf(str3)).append(" and area_code = '").append(null).append("'").toString();
                }
                DBManager.delete("tb_netquery_time", str3, new String[]{str});
            }
        } catch (Throwable th) {
        }
    }

    private static JSONObject c(String str, String str2) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            String[] strArr = new String[]{"id", "phone_num", "request_time"};
            query = DBManager.query("tb_netquery_time", strArr, "phone_num = ? and area_code = ?", new String[]{str, str2}, null, null, null, "1");
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr, query);
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
