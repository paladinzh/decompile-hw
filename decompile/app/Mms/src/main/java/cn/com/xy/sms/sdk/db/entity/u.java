package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.util.DateUtils;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/* compiled from: Unknown */
public final class u {
    private static String a = "scene_id";
    private static String b = "date";
    private static String c = "parse_times";
    private static String d = "popup_times";
    private static String e = "tb_popup_action_scene";
    private static String f = " DROP TABLE IF EXISTS tb_popup_action_scene";
    private static String g = "create table  if not exists tb_popup_action_scene (scene_id TEXT, date TEXT, parse_times INTEGER DEFAULT '0', popup_times INTEGER DEFAULT '0' ) ";

    public static long a(HashMap<String, String> hashMap) {
        t tVar = null;
        String str = (String) hashMap.get("titleNo");
        long insert;
        try {
            if (!StringUtils.isNull(str)) {
                tVar = d(str);
            }
            if (tVar == null) {
                tVar = new t();
                tVar.a = str;
                tVar.b = DateUtils.getCurrentTimeString("yyyyMMdd");
                tVar.c = 1;
                insert = DBManager.insert("tb_popup_action_scene", a(tVar));
                DuoquUtils.getSdkDoAction().statisticAction(str, Constant.ACTION_PARSE, null);
                return insert;
            }
            tVar.c++;
            DBManager.update("tb_popup_action_scene", a(tVar), "scene_id = ? and date = ? ", new String[]{tVar.a, tVar.b});
            return 0;
        } catch (Throwable th) {
            insert = -1;
        }
    }

    private static ContentValues a(t tVar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ParseItemManager.SCENE_ID, tVar.a);
        contentValues.put("date", tVar.b);
        contentValues.put("parse_times", Integer.valueOf(tVar.c));
        contentValues.put("popup_times", Integer.valueOf(tVar.d));
        return contentValues;
    }

    public static List<t> a(String str) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        List<t> arrayList = new ArrayList();
        try {
            query = DBManager.query("tb_popup_action_scene", new String[]{"date"}, "date < ? ", new String[]{str}, "date", null, null, null);
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("date");
                        while (query.moveToNext()) {
                            t tVar = new t();
                            tVar.b = query.getString(columnIndex);
                            arrayList.add(tVar);
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
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return arrayList;
    }

    private static void a() {
        try {
            DBManager.delete("tb_popup_action_scene", null, null);
        } catch (Throwable th) {
        }
    }

    public static long b(HashMap<String, String> hashMap) {
        t tVar = null;
        String str = (String) hashMap.get("titleNo");
        long insert;
        try {
            if (!StringUtils.isNull(str)) {
                tVar = d(str);
            }
            if (tVar == null) {
                tVar = new t();
                tVar.a = str;
                tVar.b = DateUtils.getCurrentTimeString("yyyyMMdd");
                tVar.d = 1;
                insert = DBManager.insert("tb_popup_action_scene", a(tVar));
                return insert;
            }
            tVar.d++;
            DBManager.update("tb_popup_action_scene", a(tVar), "scene_id = ? and date = ? ", new String[]{tVar.a, tVar.b});
            return 0;
        } catch (Throwable th) {
            insert = -1;
        }
    }

    public static List<t> b(String str) {
        Throwable th;
        XyCursor xyCursor = null;
        List<t> arrayList = new ArrayList();
        XyCursor query;
        try {
            query = DBManager.query("tb_popup_action_scene", new String[]{ParseItemManager.SCENE_ID, "date", "parse_times", "popup_times"}, "date = ? ", new String[]{str}, null, null, null, null);
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex(ParseItemManager.SCENE_ID);
                        int columnIndex2 = query.getColumnIndex("date");
                        int columnIndex3 = query.getColumnIndex("parse_times");
                        int columnIndex4 = query.getColumnIndex("popup_times");
                        while (query.moveToNext()) {
                            t tVar = new t();
                            tVar.a = query.getString(columnIndex);
                            tVar.b = query.getString(columnIndex2);
                            tVar.c = query.getInt(columnIndex3);
                            tVar.d = query.getInt(columnIndex4);
                            arrayList.add(tVar);
                        }
                    }
                } catch (Throwable th2) {
                    xyCursor = query;
                    th = th2;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th3) {
            th = th3;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return arrayList;
    }

    public static void c(String str) {
        try {
            DBManager.delete("tb_popup_action_scene", "date < ?", new String[]{str});
        } catch (Throwable th) {
        }
    }

    private static t d(String str) {
        t tVar;
        Throwable th;
        t tVar2;
        XyCursor xyCursor = null;
        String currentTimeString = DateUtils.getCurrentTimeString("yyyyMMdd");
        XyCursor query;
        try {
            query = DBManager.query("tb_popup_action_scene", new String[]{ParseItemManager.SCENE_ID, "date", "parse_times", "popup_times"}, "scene_id = ? and date = ? ", new String[]{str, currentTimeString}, null, null, null, "1");
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex(ParseItemManager.SCENE_ID);
                        int columnIndex2 = query.getColumnIndex("date");
                        int columnIndex3 = query.getColumnIndex("parse_times");
                        int columnIndex4 = query.getColumnIndex("popup_times");
                        t tVar3 = null;
                        while (query.moveToNext()) {
                            try {
                                tVar = new t();
                            } catch (Throwable th2) {
                                th = th2;
                            }
                            try {
                                tVar.a = query.getString(columnIndex);
                                tVar.b = query.getString(columnIndex2);
                                tVar.c = query.getInt(columnIndex3);
                                tVar.d = query.getInt(columnIndex4);
                                tVar3 = tVar;
                            } catch (Throwable th22) {
                                th = th22;
                            }
                        }
                        XyCursor.closeCursor(query, true);
                        return tVar3;
                    }
                } catch (Throwable th222) {
                    th = th222;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th3) {
            th = th3;
            query = null;
            XyCursor.closeCursor(query, true);
            throw th;
        }
        return tVar2;
    }
}
