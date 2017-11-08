package cn.com.xy.sms.sdk.db.entity;

import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* compiled from: Unknown */
public final class d {
    private static String a = "scene_id";
    private static String b = "count";
    private static String c = "tb_count_scene";
    private static String d = " DROP TABLE IF EXISTS tb_count_scene";
    private static String e = "create table  if not exists tb_count_scene (scene_id TEXT,count INT)";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long a(HashMap<String, String> hashMap) {
        XyCursor xyCursor = null;
        long j = -1;
        try {
            if (!StringUtils.isNull((String) hashMap.get("titleNo"))) {
                xyCursor = DBManager.query("tb_count_scene", new String[]{ParseItemManager.SCENE_ID, "count"}, "scene_id = ? ", new String[]{(String) hashMap.get("titleNo")});
            }
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    xyCursor.moveToNext();
                    int i = xyCursor.getInt(xyCursor.getColumnIndex("count")) + 1;
                    DBManager.update("tb_count_scene", BaseManager.getContentValues(null, ParseItemManager.SCENE_ID, r0, "count", String.valueOf(i)), "scene_id = ? ", new String[]{r0});
                    XyCursor.closeCursor(xyCursor, true);
                    return 0;
                }
            }
            j = DBManager.insert("tb_count_scene", BaseManager.getContentValues(null, ParseItemManager.SCENE_ID, r0, "count", "1"));
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            XyCursor.closeCursor(xyCursor, true);
        }
        return j;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<A> a() {
        XyCursor xyCursor = null;
        List<A> arrayList = new ArrayList();
        try {
            xyCursor = DBManager.query("tb_count_scene", new String[]{ParseItemManager.SCENE_ID, "count"}, null, null);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    int columnIndex = xyCursor.getColumnIndex(ParseItemManager.SCENE_ID);
                    int columnIndex2 = xyCursor.getColumnIndex("count");
                    while (xyCursor.moveToNext()) {
                        A a = new A();
                        a.a = xyCursor.getString(columnIndex);
                        a.c = xyCursor.getInt(columnIndex2);
                        arrayList.add(a);
                    }
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
        return arrayList;
    }

    private static void a(String str) {
        try {
            DBManager.delete("tb_count_scene", "scene_id = ?", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static void b() {
        try {
            DBManager.delete("tb_count_scene", null, null);
        } catch (Throwable th) {
        }
    }
}
