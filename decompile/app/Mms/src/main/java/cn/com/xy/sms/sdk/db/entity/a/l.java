package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.C;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.util.m;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public final class l {
    private static String a = "id";
    private static String b = "num";
    private static String c = "encode_content";
    private static String d = "content_sign";
    private static String e = "status";
    private static String f = "msg_time";
    private static final String g = "tb_shard_data";
    private static String h = " DROP TABLE IF EXISTS tb_shard_data";
    private static String i = "CREATE TABLE IF NOT EXISTS tb_shard_data (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, num TEXT NOT NULL, encode_content TEXT NOT NULL, content_sign TEXT NOT NULL, status INTEGER DEFAULT 0, msg_time INTEGER DEFAULT 0)";

    private static long a(String str, m mVar) {
        try {
            return (long) DBManager.update(g, BaseManager.getContentValues(null, "status", mVar.toString()), "content_sign=? ", new String[]{str});
        } catch (Throwable th) {
            return -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long a(String str, String str2, String str3, String str4) {
        XyCursor xyCursor = null;
        if (StringUtils.isNull(str) || StringUtils.isNull(str2) || StringUtils.isNull(str3)) {
            return -1;
        }
        try {
            xyCursor = DBManager.query(g, new String[]{str}, "num=? AND encode_content=? ", new String[]{str, str2});
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    XyCursor.closeCursor(xyCursor, true);
                    return 0;
                }
            }
            long insert = DBManager.insert(g, BaseManager.getContentValues(null, IccidInfoManager.NUM, str, "encode_content", str2, "content_sign", str3, "msg_time", str4));
            XyCursor.closeCursor(xyCursor, true);
            return insert;
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
    }

    public static long a(List<String> list, m mVar) {
        try {
            int size = list.size();
            String str = "content_sign IN(" + C.a(size) + ")";
            String[] strArr = (String[]) list.toArray(new String[size]);
            return (long) DBManager.update(g, BaseManager.getContentValues(null, "status", mVar.toString()), str, strArr);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static k a(String str) {
        List a = a("content_sign=? ", new String[]{str}, 1);
        return (a != null && a.size() > 0) ? (k) a.get(0) : null;
    }

    public static List<k> a(String str, m mVar, int i) {
        if (mVar == m.ALL) {
            return a("num=? ", new String[]{str}, i);
        }
        return a("num=? AND status=? ", new String[]{str, mVar.toString()}, i);
    }

    public static List<k> a(String str, String[] strArr, int i) {
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor query;
        try {
            query = DBManager.query(false, g, new String[]{"id", IccidInfoManager.NUM, "encode_content", "content_sign", "status", "msg_time"}, str, strArr, null, null, null, String.valueOf(i));
            if (query != null) {
                try {
                    if (query.getCount() != 0) {
                        List<k> arrayList = new ArrayList();
                        while (query.moveToNext()) {
                            k kVar = new k();
                            query.getInt(query.getColumnIndex("id"));
                            query.getString(query.getColumnIndex(IccidInfoManager.NUM));
                            kVar.c = query.getString(query.getColumnIndex("encode_content"));
                            kVar.d = query.getString(query.getColumnIndex("content_sign"));
                            query.getInt(query.getColumnIndex("status"));
                            query.getLong(query.getColumnIndex("msg_time"));
                            arrayList.add(kVar);
                        }
                        XyCursor.closeCursor(query, true);
                        return arrayList;
                    }
                } catch (Throwable th2) {
                    xyCursor = query;
                    th = th2;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
            return null;
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    public static void a(long j) {
        try {
            DBManager.delete(g, "msg_time<=?", new String[]{String.valueOf(j)});
        } catch (Throwable th) {
        }
    }

    public static void a(Map<String, String> map) {
        if (map != null && !map.isEmpty()) {
            try {
                String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) map.get(IccidInfoManager.NUM));
                String str = (String) map.get("msg");
                String str2 = (String) map.get("smsTime");
                if (!StringUtils.isNull(phoneNumberNo86) && !StringUtils.isNull(str) && !StringUtils.isNull(str2)) {
                    str = DexUtil.multiReplace(str.trim());
                    if (!StringUtils.isNull(str)) {
                        String encode = StringUtils.encode(str);
                        if (!StringUtils.isNull(encode)) {
                            a(phoneNumberNo86, encode, m.a(str), str2);
                        }
                    }
                }
            } catch (Throwable th) {
            }
        }
    }
}
