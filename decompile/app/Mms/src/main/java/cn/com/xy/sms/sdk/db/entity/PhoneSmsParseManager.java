package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class PhoneSmsParseManager {
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_phone_bubble_cache";
    public static final String TABLE_NAME = "tb_phone_bubble_cache";

    public static void addInsertQueue(String str, long j, String str2, String str3, String str4) {
        saveOrUpdateObject(str, j, str2, str3, str4);
    }

    public static JSONObject findObjectByPhone(String str) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            String[] strArr = new String[]{"id", "phone", "minReceiveTime", "maxReceiveTime", "useBubbleViews", "useBubbleLogoName", "extend"};
            query = DBManager.query(TABLE_NAME, strArr, "phone = ?", new String[]{str});
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr, query);
                XyCursor.closeCursor(query, true);
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = query;
                th = th3;
                XyCursor.closeCursor(xyCursor, true);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return loadSingleDataFromCursor;
    }

    public static String getCreateTableSql() {
        return "create table  if not exists tb_phone_bubble_cache (  id INTEGER PRIMARY KEY, phone TEXT UNIQUE, minReceiveTime LONG default 0, maxReceiveTime LONG default 0, useBubbleViews TEXT, useBubbleLogoName TEXT, extend TEXT)";
    }

    public static void reSetAllDataTime(String str, long j, String str2, String str3, String str4) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("minReceiveTime", Long.valueOf(System.currentTimeMillis()));
        contentValues.put("maxReceiveTime", Integer.valueOf(0));
        try {
            DBManager.update(TABLE_NAME, contentValues, null, null);
        } catch (Throwable th) {
        }
    }

    public static long saveOrUpdateObject(String str, long j, String str2, String str3, String str4) {
        int i = 1;
        int i2 = 0;
        try {
            JSONObject findObjectByPhone = findObjectByPhone(str);
            String string;
            JSONArray jSONArray;
            if (findObjectByPhone != null) {
                ContentValues contentValues = new ContentValues();
                if ((j <= 0 ? 1 : 0) == 0) {
                    if ((j >= Long.valueOf(findObjectByPhone.getString("maxReceiveTime")).longValue() ? 1 : 0) == 0) {
                        contentValues.put("maxReceiveTime", Long.valueOf(j));
                    } else {
                        if (j > Long.valueOf(findObjectByPhone.getString("minReceiveTime")).longValue()) {
                            i = 0;
                        }
                        if (i == 0) {
                            contentValues.put("minReceiveTime", Long.valueOf(j));
                        }
                    }
                }
                if (!StringUtils.isNull(str3)) {
                    contentValues.put("useBubbleLogoName", str3);
                }
                if (!StringUtils.isNull(str4)) {
                    contentValues.put("extend", str4);
                }
                if (!StringUtils.isNull(str2)) {
                    string = findObjectByPhone.getString("useBubbleViews");
                    if (StringUtils.isNull(string)) {
                        new JSONArray().put(0, str2);
                    } else {
                        jSONArray = new JSONArray(string);
                        i = jSONArray.length();
                        while (i2 < i) {
                            if (str2.equals(jSONArray.getString(i2))) {
                                break;
                            }
                            i2++;
                        }
                        if (i2 == i) {
                            jSONArray.put(i, str2);
                            contentValues.put("useBubbleViews", jSONArray.toString());
                        }
                    }
                }
                if (contentValues.size() <= 0) {
                    return -1;
                }
                DBManager.update(TABLE_NAME, contentValues, "phone=?", new String[]{str});
                return Long.valueOf(findObjectByPhone.getString("id")).longValue();
            }
            String valueOf;
            jSONArray = new JSONArray();
            if (!StringUtils.isNull(str2)) {
                jSONArray.put(0, str2);
            }
            if (j > 0) {
                i = 0;
            }
            if (i == 0) {
                valueOf = String.valueOf(j);
                string = valueOf;
            } else {
                string = String.valueOf(System.currentTimeMillis() + 2147483647L);
                valueOf = "0";
            }
            ContentValues contentValues2 = BaseManager.getContentValues(null, true, "phone", str, "minReceiveTime", valueOf, "maxReceiveTime", string, "useBubbleViews", jSONArray.toString(), "useBubbleLogoName", str3, "extend", str4);
            return contentValues2 != null ? DBManager.insert(TABLE_NAME, contentValues2) : -1;
        } catch (Throwable th) {
            return -1;
        }
    }
}
