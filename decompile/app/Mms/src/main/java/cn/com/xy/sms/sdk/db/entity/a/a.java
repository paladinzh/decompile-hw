package cn.com.xy.sms.sdk.db.entity.a;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class a {
    private static final Map<String, cn.com.xy.sms.sdk.db.entity.a> a = new HashMap();

    public static cn.com.xy.sms.sdk.db.entity.a a(String str) {
        return (cn.com.xy.sms.sdk.db.entity.a) a.get(str);
    }

    public static void a(String str, cn.com.xy.sms.sdk.db.entity.a aVar) {
        a.put(str, aVar);
    }

    public static boolean a(cn.com.xy.sms.sdk.db.entity.a aVar) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IccidInfoManager.CNUM, aVar.b);
            if (!StringUtils.isNull(aVar.c)) {
                contentValues.put("areaCode", aVar.c);
            }
            if (!StringUtils.isNull(aVar.e)) {
                contentValues.put("city", aVar.e);
            }
            if (!StringUtils.isNull(aVar.f)) {
                contentValues.put(IccidInfoManager.OPERATOR, aVar.f);
            }
            contentValues.put("checkTime", Long.valueOf(aVar.g));
            long update = (long) DBManager.update("tb_centernum_location_info", contentValues, "cnum = ?", new String[]{String.valueOf(aVar.b)});
            if (!(update >= 1)) {
                update = DBManager.insert("tb_centernum_location_info", contentValues);
            }
            return !((update > 0 ? 1 : (update == 0 ? 0 : -1)) <= 0);
        } catch (Throwable th) {
            return false;
        }
    }

    private static ContentValues b(cn.com.xy.sms.sdk.db.entity.a aVar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(IccidInfoManager.CNUM, aVar.b);
        if (!StringUtils.isNull(aVar.c)) {
            contentValues.put("areaCode", aVar.c);
        }
        if (!StringUtils.isNull(aVar.e)) {
            contentValues.put("city", aVar.e);
        }
        if (!StringUtils.isNull(aVar.f)) {
            contentValues.put(IccidInfoManager.OPERATOR, aVar.f);
        }
        contentValues.put("checkTime", Long.valueOf(aVar.g));
        return contentValues;
    }

    public static cn.com.xy.sms.sdk.db.entity.a b(String str) {
        XyCursor xyCursor;
        Throwable th;
        cn.com.xy.sms.sdk.db.entity.a aVar = null;
        XyCursor query;
        try {
            query = DBManager.query("tb_centernum_location_info", new String[]{IccidInfoManager.CNUM, "areaCode", "city", IccidInfoManager.OPERATOR, "checkTime"}, "cnum = ? ", new String[]{new StringBuilder(String.valueOf(StringUtils.getSubString(str))).toString()});
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("areaCode");
                        int columnIndex2 = query.getColumnIndex("city");
                        int columnIndex3 = query.getColumnIndex(IccidInfoManager.OPERATOR);
                        int columnIndex4 = query.getColumnIndex("checkTime");
                        cn.com.xy.sms.sdk.db.entity.a aVar2 = null;
                        while (query.moveToNext()) {
                            try {
                                aVar = new cn.com.xy.sms.sdk.db.entity.a();
                                aVar.b = r3;
                                aVar.c = query.getString(columnIndex);
                                aVar.e = query.getString(columnIndex2);
                                aVar.f = query.getString(columnIndex3);
                                aVar.g = query.getLong(columnIndex4);
                                aVar2 = aVar;
                            } catch (Throwable th2) {
                                th = th2;
                            }
                        }
                        aVar = aVar2;
                    }
                } catch (Throwable th22) {
                    th = th22;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th3) {
            query = null;
            th = th3;
            XyCursor.closeCursor(query, true);
            throw th;
        }
        return aVar;
    }
}
