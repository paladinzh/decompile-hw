package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.Arrays;
import java.util.List;

/* compiled from: Unknown */
public final class x {
    private static int a = 0;
    private static int b = 1;
    private static String c = "id";
    private static String d = "scene_id";
    private static String e = "url";
    private static String f = "status";
    private static String g = "pos";
    private static String h = "last_load_time";
    private static String i = "tb_res_download";
    private static String j = " DROP TABLE IF EXISTS tb_res_download";
    private static String k = "create table  if not exists tb_res_download (id INTEGER PRIMARY KEY,scene_id TEXT,url TEXT,status INTEGER,pos INTEGER,last_load_time INTEGER DEFAULT '0' )";
    private static String l = "ALTER TABLE tb_res_download ADD COLUMN last_load_time INTEGER DEFAULT '0'";

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static w a(w wVar) {
        XyCursor xyCursor = null;
        boolean z = false;
        try {
            xyCursor = DBManager.query("tb_res_download", new String[]{"id", ParseItemManager.SCENE_ID, Constant.URLS, "status", "pos"}, "url = ? ", new String[]{wVar.c});
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    int columnIndex = xyCursor.getColumnIndex("id");
                    int columnIndex2 = xyCursor.getColumnIndex(ParseItemManager.SCENE_ID);
                    int columnIndex3 = xyCursor.getColumnIndex("status");
                    int columnIndex4 = xyCursor.getColumnIndex("pos");
                    if (xyCursor.moveToNext()) {
                        long j = xyCursor.getLong(columnIndex);
                        String string = xyCursor.getString(columnIndex2);
                        columnIndex2 = xyCursor.getInt(columnIndex3);
                        columnIndex3 = xyCursor.getInt(columnIndex4);
                        wVar.a = j;
                        wVar.b = string;
                        wVar.d = columnIndex2;
                        wVar.e = columnIndex3;
                        XyCursor.closeCursor(xyCursor, true);
                        return wVar;
                    }
                    XyCursor.closeCursor(xyCursor, true);
                    return wVar;
                }
            }
            long insert = DBManager.insert("tb_res_download", c(wVar));
            if (insert <= -1) {
                z = true;
            }
            if (!z) {
                wVar.a = insert;
                XyCursor.closeCursor(xyCursor, true);
                return wVar;
            }
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
        return wVar;
    }

    public static List<String> a(String str) {
        try {
            if (!StringUtils.isNull(str)) {
                return Arrays.asList(str.split(";"));
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public static void a() {
        try {
            DBManager.delete("tb_res_download", null, null);
        } catch (Throwable th) {
        }
    }

    public static void a(long j, int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("status", Integer.valueOf(1));
            DBManager.update("tb_res_download", contentValues, "id = ? ", new String[]{new StringBuilder(String.valueOf(j)).toString()});
        } catch (Throwable th) {
        }
    }

    public static void a(w wVar, long j) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("last_load_time", new StringBuilder(String.valueOf(j)).toString());
            DBManager.update("tb_res_download", contentValues, "id = ? ", new String[]{new StringBuilder(String.valueOf(wVar.a)).toString()});
        } catch (Throwable th) {
        }
    }

    public static w b() {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query("tb_res_download", new String[]{"id", ParseItemManager.SCENE_ID, Constant.URLS, "status", "pos"}, "status= ? and last_load_time < ?", new String[]{"0", new StringBuilder(String.valueOf(System.currentTimeMillis() - 21600000)).toString()}, null, null, "id asc", "1");
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("id");
                        int columnIndex2 = query.getColumnIndex(ParseItemManager.SCENE_ID);
                        int columnIndex3 = query.getColumnIndex("status");
                        int columnIndex4 = query.getColumnIndex("pos");
                        int columnIndex5 = query.getColumnIndex(Constant.URLS);
                        if (query.moveToNext()) {
                            long j = query.getLong(columnIndex);
                            String string = query.getString(columnIndex2);
                            columnIndex2 = query.getInt(columnIndex3);
                            columnIndex3 = query.getInt(columnIndex4);
                            String string2 = query.getString(columnIndex5);
                            w wVar = new w();
                            wVar.a = j;
                            wVar.b = string;
                            wVar.d = columnIndex2;
                            wVar.e = columnIndex3;
                            wVar.c = string2;
                            d(wVar);
                            XyCursor.closeCursor(query, true);
                            return wVar;
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
        return null;
    }

    private static void b(w wVar) {
        try {
            DBManager.insert("tb_res_download", c(wVar));
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean b(String str) {
        XyCursor xyCursor = null;
        if (StringUtils.isNull(str)) {
            return false;
        }
        try {
            xyCursor = DBManager.query("tb_res_download", new String[]{"id", Constant.URLS, "status"}, "url = ? and status = ?", new String[]{new StringBuilder(String.valueOf(str)).toString(), "1"});
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    XyCursor.closeCursor(xyCursor, true);
                    return true;
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
        return false;
    }

    private static ContentValues c(w wVar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ParseItemManager.SCENE_ID, wVar.b);
        contentValues.put(Constant.URLS, wVar.c);
        contentValues.put("status", Integer.valueOf(wVar.d));
        contentValues.put("pos", Integer.valueOf(wVar.e));
        contentValues.put("last_load_time", Integer.valueOf(0));
        return contentValues;
    }

    public static w c(String str) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query("tb_res_download", new String[]{"id", ParseItemManager.SCENE_ID, Constant.URLS, "status", "pos"}, "status= ? and url =? ", new String[]{"0", str}, null, null, "id asc", "1");
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("id");
                        int columnIndex2 = query.getColumnIndex(ParseItemManager.SCENE_ID);
                        int columnIndex3 = query.getColumnIndex("status");
                        int columnIndex4 = query.getColumnIndex("pos");
                        int columnIndex5 = query.getColumnIndex(Constant.URLS);
                        if (query.moveToNext()) {
                            long j = query.getLong(columnIndex);
                            String string = query.getString(columnIndex2);
                            columnIndex2 = query.getInt(columnIndex3);
                            columnIndex3 = query.getInt(columnIndex4);
                            String string2 = query.getString(columnIndex5);
                            w wVar = new w();
                            wVar.a = j;
                            wVar.b = string;
                            wVar.d = columnIndex2;
                            wVar.e = columnIndex3;
                            wVar.c = string2;
                            d(wVar);
                            XyCursor.closeCursor(query, true);
                            return wVar;
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
        return null;
    }

    private static void d(w wVar) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("last_load_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
            DBManager.update("tb_res_download", contentValues, "id = ? ", new String[]{new StringBuilder(String.valueOf(wVar.a)).toString()});
        } catch (Throwable th) {
        }
    }
}
