package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.b.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.amap.api.services.district.DistrictSearchQuery;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class z {
    private static String a = "id";
    private static String b = "sceneRuleVersion";
    private static String c = "scene_id";
    private static String d = "province";
    private static String e = "operator";
    private static String f = "expire_date";
    private static String g = "Func_call";
    private static String h = "Func_acc_url";
    private static String i = "Func_reply_sms";
    private static String j = "Func_config";
    private static String k = "res_urls";
    private static String l = "s_version";
    private static String m = "Scene_page_config";
    private static String n = "isdownload";
    private static String o = "tb_scenerule_config";
    private static String p = "sceneType";
    private static String q = "isuse";
    private static String r = "last_update_time";
    private static String s = "scene_rule_config";
    private static String t = " DROP TABLE IF EXISTS tb_scenerule_config";
    private static String u = "create table  if not exists tb_scenerule_config (id TEXT,sceneRuleVersion TEXT,scene_id TEXT,province TEXT,operator TEXT,expire_date TEXT,Func_call INTEGER,Func_acc_url INTEGER,Func_reply_sms INTEGER,Func_config TEXT,res_urls TEXT,s_version TEXT,Scene_page_config TEXT,sceneType INTEGER DEFAULT '-1',scene_rule_config TEXT,isdownload INTEGER DEFAULT '0',isuse INTEGER DEFAULT 0,last_update_time  INTEGER DEFAULT 0)";
    private static String v = "ALTER TABLE tb_scenerule_config ADD COLUMN isdownload INTEGER DEFAULT '0'";
    private static String w = "ALTER TABLE tb_scenerule_config ADD COLUMN isuse INTEGER DEFAULT 0";
    private static String x = "ALTER TABLE tb_scenerule_config ADD COLUMN last_update_time INTEGER DEFAULT 0";
    private static String y = "ALTER TABLE tb_scenerule_config ADD COLUMN scene_rule_config  TEXT";
    private static String z = "create index if not exists scene_and_type_idx on tb_scenerule_config (scene_id,sceneType)";

    public static int a(int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("Func_call", Integer.valueOf(i));
            return DBManager.update("tb_scenerule_config", contentValues, null, null);
        } catch (Throwable th) {
            return -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int a(String str) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = DBManager.getSQLiteDatabase();
            sQLiteDatabase.execSQL("update  tb_scenerule_config set isuse = 1 WHERE scene_id = '" + str + "'");
            DBManager.close(sQLiteDatabase);
        } catch (Throwable th) {
            Throwable th2 = th;
            SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
            Throwable th3 = th2;
            DBManager.close(sQLiteDatabase2);
            throw th3;
        }
        return 0;
    }

    public static int a(List<SceneRule> list) {
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        SQLiteDatabase sQLiteDatabase2 = null;
        if (list != null) {
            try {
                if (!list.isEmpty()) {
                    sQLiteDatabase = DBManager.getSQLiteDatabase();
                    try {
                        sQLiteDatabase.beginTransaction();
                        String str = "update tb_scenerule_config  set last_update_time = " + System.currentTimeMillis() + " WHERE id" + " = ?";
                        Iterator it = list.iterator();
                        while (it.hasNext()) {
                            sQLiteDatabase.execSQL(str, new String[]{((SceneRule) it.next()).id});
                        }
                        if (sQLiteDatabase != null) {
                            try {
                                sQLiteDatabase.setTransactionSuccessful();
                                sQLiteDatabase.endTransaction();
                            } catch (Throwable th2) {
                            }
                        }
                        DBManager.close(sQLiteDatabase);
                    } catch (Throwable th3) {
                        th = th3;
                        if (sQLiteDatabase != null) {
                            try {
                                sQLiteDatabase.setTransactionSuccessful();
                                sQLiteDatabase.endTransaction();
                            } catch (Throwable th4) {
                            }
                        }
                        DBManager.close(sQLiteDatabase);
                        throw th;
                    }
                    return 0;
                }
            } catch (Throwable th5) {
                Throwable th6 = th5;
                sQLiteDatabase = null;
                th = th6;
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                }
                DBManager.close(sQLiteDatabase);
                throw th;
            }
        }
        DBManager.close(null);
        return 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<SceneRule> a(int i, long j) {
        Object obj;
        List<SceneRule> arrayList = new ArrayList();
        XyCursor xyCursor = null;
        if (i != 1) {
            try {
                obj = "sceneType != 1";
            } catch (Throwable th) {
                Throwable th2 = th;
                XyCursor xyCursor2 = xyCursor;
                Throwable th3 = th2;
                XyCursor.closeCursor(xyCursor2, true);
                throw th3;
            }
        }
        obj = "sceneType = " + i;
        long currentTimeMillis = System.currentTimeMillis() - j;
        String stringBuilder = new StringBuilder(String.valueOf(new StringBuilder(String.valueOf(obj)).append(" and isuse").append(" = 1").toString())).append(" and last_update_time").append(" < ").append(currentTimeMillis).toString();
        xyCursor = DBManager.query("tb_scenerule_config", new String[]{"id", ParseItemManager.SCENE_ID, DistrictSearchQuery.KEYWORDS_PROVINCE, IccidInfoManager.OPERATOR, "expire_date", "Func_call", "Func_acc_url", "Func_reply_sms", "Func_config", "res_urls", "s_version", "Scene_page_config", "isdownload", "sceneRuleVersion"}, stringBuilder, null);
        if (xyCursor != null) {
            if (xyCursor.getCount() > 0) {
                int columnIndex = xyCursor.getColumnIndex("id");
                int columnIndex2 = xyCursor.getColumnIndex(ParseItemManager.SCENE_ID);
                int columnIndex3 = xyCursor.getColumnIndex(DistrictSearchQuery.KEYWORDS_PROVINCE);
                int columnIndex4 = xyCursor.getColumnIndex(IccidInfoManager.OPERATOR);
                int columnIndex5 = xyCursor.getColumnIndex("expire_date");
                int columnIndex6 = xyCursor.getColumnIndex("Func_call");
                int columnIndex7 = xyCursor.getColumnIndex("Func_acc_url");
                int columnIndex8 = xyCursor.getColumnIndex("Func_reply_sms");
                int columnIndex9 = xyCursor.getColumnIndex("Func_config");
                int columnIndex10 = xyCursor.getColumnIndex("res_urls");
                int columnIndex11 = xyCursor.getColumnIndex("s_version");
                int columnIndex12 = xyCursor.getColumnIndex("Scene_page_config");
                int columnIndex13 = xyCursor.getColumnIndex("isdownload");
                int columnIndex14 = xyCursor.getColumnIndex("sceneRuleVersion");
                while (xyCursor.moveToNext()) {
                    SceneRule sceneRule = new SceneRule();
                    sceneRule.id = xyCursor.getString(columnIndex);
                    sceneRule.scene_id = xyCursor.getString(columnIndex2);
                    sceneRule.province = xyCursor.getString(columnIndex3);
                    sceneRule.operator = xyCursor.getString(columnIndex4);
                    sceneRule.expire_date = xyCursor.getString(columnIndex5);
                    sceneRule.Func_call = xyCursor.getInt(columnIndex6);
                    sceneRule.Func_acc_url = xyCursor.getInt(columnIndex7);
                    sceneRule.Func_reply_sms = xyCursor.getInt(columnIndex8);
                    sceneRule.Func_config = xyCursor.getString(columnIndex9);
                    sceneRule.res_urls = xyCursor.getString(columnIndex10);
                    sceneRule.s_version = xyCursor.getString(columnIndex11);
                    sceneRule.Scene_page_config = xyCursor.getString(columnIndex12);
                    sceneRule.isDownload = xyCursor.getInt(columnIndex13);
                    sceneRule.sceneruleVersion = xyCursor.getString(columnIndex14);
                    arrayList.add(sceneRule);
                }
            }
        }
        XyCursor.closeCursor(xyCursor, true);
        return arrayList;
    }

    public static List<SceneRule> a(String str, int i) {
        return a(str, i, false);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<SceneRule> a(String str, int i, boolean z) {
        int columnIndex;
        int columnIndex2;
        int columnIndex3;
        int columnIndex4;
        int columnIndex5;
        int columnIndex6;
        int columnIndex7;
        int columnIndex8;
        int columnIndex9;
        int columnIndex10;
        int columnIndex11;
        int columnIndex12;
        int columnIndex13;
        int columnIndex14;
        int columnIndex15;
        List<SceneRule> arrayList = new ArrayList();
        XyCursor xyCursor = null;
        if (z) {
            if (str.length() == 8) {
                Object obj = "scene_id LIKE '" + str.substring(0, 2) + "%" + str.substring(5, 8) + "' ";
                xyCursor = DBManager.query("tb_scenerule_config", new String[]{"id", ParseItemManager.SCENE_ID, DistrictSearchQuery.KEYWORDS_PROVINCE, IccidInfoManager.OPERATOR, "expire_date", "Func_call", "Func_acc_url", "Func_reply_sms", "Func_config", "res_urls", "s_version", "Scene_page_config", "isdownload", "sceneRuleVersion", "scene_rule_config"}, i == 1 ? new StringBuilder(String.valueOf(obj)).append(" and sceneType").append(" != 1").toString() : new StringBuilder(String.valueOf(obj)).append(" and sceneType").append(" = ").append(i).toString(), null);
                if (xyCursor != null) {
                    if (xyCursor.getCount() > 0) {
                        columnIndex = xyCursor.getColumnIndex("id");
                        columnIndex2 = xyCursor.getColumnIndex(ParseItemManager.SCENE_ID);
                        columnIndex3 = xyCursor.getColumnIndex(DistrictSearchQuery.KEYWORDS_PROVINCE);
                        columnIndex4 = xyCursor.getColumnIndex(IccidInfoManager.OPERATOR);
                        columnIndex5 = xyCursor.getColumnIndex("expire_date");
                        columnIndex6 = xyCursor.getColumnIndex("Func_call");
                        columnIndex7 = xyCursor.getColumnIndex("Func_acc_url");
                        columnIndex8 = xyCursor.getColumnIndex("Func_reply_sms");
                        columnIndex9 = xyCursor.getColumnIndex("Func_config");
                        columnIndex10 = xyCursor.getColumnIndex("res_urls");
                        columnIndex11 = xyCursor.getColumnIndex("s_version");
                        columnIndex12 = xyCursor.getColumnIndex("Scene_page_config");
                        columnIndex13 = xyCursor.getColumnIndex("isdownload");
                        columnIndex14 = xyCursor.getColumnIndex("sceneRuleVersion");
                        columnIndex15 = xyCursor.getColumnIndex("scene_rule_config");
                        while (xyCursor.moveToNext()) {
                            SceneRule sceneRule = new SceneRule();
                            sceneRule.id = xyCursor.getString(columnIndex);
                            sceneRule.scene_id = xyCursor.getString(columnIndex2);
                            sceneRule.province = xyCursor.getString(columnIndex3);
                            sceneRule.operator = xyCursor.getString(columnIndex4);
                            sceneRule.expire_date = xyCursor.getString(columnIndex5);
                            sceneRule.Func_call = xyCursor.getInt(columnIndex6);
                            sceneRule.Func_acc_url = xyCursor.getInt(columnIndex7);
                            sceneRule.Func_reply_sms = xyCursor.getInt(columnIndex8);
                            sceneRule.Func_config = xyCursor.getString(columnIndex9);
                            sceneRule.res_urls = xyCursor.getString(columnIndex10);
                            sceneRule.s_version = xyCursor.getString(columnIndex11);
                            sceneRule.Scene_page_config = xyCursor.getString(columnIndex12);
                            sceneRule.isDownload = xyCursor.getInt(columnIndex13);
                            sceneRule.sceneruleVersion = xyCursor.getString(columnIndex14);
                            sceneRule.mSceneRuleConfig = xyCursor.getString(columnIndex15);
                            arrayList.add(sceneRule);
                        }
                    }
                }
                XyCursor.closeCursor(xyCursor, true);
                return arrayList;
            }
        }
        try {
            obj = "scene_id = '" + str + "' ";
            if (i == 1) {
            }
            xyCursor = DBManager.query("tb_scenerule_config", new String[]{"id", ParseItemManager.SCENE_ID, DistrictSearchQuery.KEYWORDS_PROVINCE, IccidInfoManager.OPERATOR, "expire_date", "Func_call", "Func_acc_url", "Func_reply_sms", "Func_config", "res_urls", "s_version", "Scene_page_config", "isdownload", "sceneRuleVersion", "scene_rule_config"}, i == 1 ? new StringBuilder(String.valueOf(obj)).append(" and sceneType").append(" != 1").toString() : new StringBuilder(String.valueOf(obj)).append(" and sceneType").append(" = ").append(i).toString(), null);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    columnIndex = xyCursor.getColumnIndex("id");
                    columnIndex2 = xyCursor.getColumnIndex(ParseItemManager.SCENE_ID);
                    columnIndex3 = xyCursor.getColumnIndex(DistrictSearchQuery.KEYWORDS_PROVINCE);
                    columnIndex4 = xyCursor.getColumnIndex(IccidInfoManager.OPERATOR);
                    columnIndex5 = xyCursor.getColumnIndex("expire_date");
                    columnIndex6 = xyCursor.getColumnIndex("Func_call");
                    columnIndex7 = xyCursor.getColumnIndex("Func_acc_url");
                    columnIndex8 = xyCursor.getColumnIndex("Func_reply_sms");
                    columnIndex9 = xyCursor.getColumnIndex("Func_config");
                    columnIndex10 = xyCursor.getColumnIndex("res_urls");
                    columnIndex11 = xyCursor.getColumnIndex("s_version");
                    columnIndex12 = xyCursor.getColumnIndex("Scene_page_config");
                    columnIndex13 = xyCursor.getColumnIndex("isdownload");
                    columnIndex14 = xyCursor.getColumnIndex("sceneRuleVersion");
                    columnIndex15 = xyCursor.getColumnIndex("scene_rule_config");
                    while (xyCursor.moveToNext()) {
                        SceneRule sceneRule2 = new SceneRule();
                        sceneRule2.id = xyCursor.getString(columnIndex);
                        sceneRule2.scene_id = xyCursor.getString(columnIndex2);
                        sceneRule2.province = xyCursor.getString(columnIndex3);
                        sceneRule2.operator = xyCursor.getString(columnIndex4);
                        sceneRule2.expire_date = xyCursor.getString(columnIndex5);
                        sceneRule2.Func_call = xyCursor.getInt(columnIndex6);
                        sceneRule2.Func_acc_url = xyCursor.getInt(columnIndex7);
                        sceneRule2.Func_reply_sms = xyCursor.getInt(columnIndex8);
                        sceneRule2.Func_config = xyCursor.getString(columnIndex9);
                        sceneRule2.res_urls = xyCursor.getString(columnIndex10);
                        sceneRule2.s_version = xyCursor.getString(columnIndex11);
                        sceneRule2.Scene_page_config = xyCursor.getString(columnIndex12);
                        sceneRule2.isDownload = xyCursor.getInt(columnIndex13);
                        sceneRule2.sceneruleVersion = xyCursor.getString(columnIndex14);
                        sceneRule2.mSceneRuleConfig = xyCursor.getString(columnIndex15);
                        arrayList.add(sceneRule2);
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

    public static void a() {
        try {
            DBManager.delete("tb_scenerule_config", null, null);
        } catch (Throwable th) {
        }
    }

    public static void a(SceneRule sceneRule, int i) {
        if (sceneRule != null) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("isdownload", Integer.valueOf(1));
                DBManager.update("tb_scenerule_config", contentValues, "id = ? ", new String[]{sceneRule.id});
            } catch (Throwable th) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long b(SceneRule sceneRule, int i) {
        XyCursor xyCursor = null;
        long j = -1;
        ContentValues c = c(sceneRule, i);
        try {
            if (!StringUtils.isNull(sceneRule.id)) {
                xyCursor = DBManager.query("tb_scenerule_config", new String[]{"id", "sceneRuleVersion"}, "id = ? ", new String[]{sceneRule.id});
            }
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    j = (long) DBManager.update("tb_scenerule_config", c, "id=? ", new String[]{sceneRule.id});
                    XyCursor.closeCursor(xyCursor, true);
                    return j;
                }
            }
            j = DBManager.insert("tb_scenerule_config", c);
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            Throwable th2 = th;
            XyCursor xyCursor2 = xyCursor;
            Throwable th3 = th2;
            XyCursor.closeCursor(xyCursor2, true);
            throw th3;
        }
        return j;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void b() {
        synchronized (DBManager.dblock) {
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = DBManager.getSQLiteDatabase();
                sQLiteDatabase.execSQL("DELETE FROM tb_scenerule_config WHERE FUNC_CALL=10 AND scene_id IN (SELECT scene_id FROM tb_scenerule_config WHERE 1=1 GROUP BY scene_id HAVING COUNT(scene_id) > 1)");
                DBManager.close(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                DBManager.close(sQLiteDatabase2);
                throw th3;
            }
        }
    }

    public static void b(String str, int i) {
        String str2;
        if (i != 1) {
            try {
                str2 = "scene_id=? and sceneType != 1";
            } catch (Throwable th) {
                return;
            }
        }
        str2 = "scene_id=? and sceneType = " + i;
        DBManager.delete("tb_scenerule_config", str2, new String[]{str});
    }

    private static ContentValues c(SceneRule sceneRule, int i) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", sceneRule.id);
        contentValues.put("sceneRuleVersion", sceneRule.sceneruleVersion);
        contentValues.put(ParseItemManager.SCENE_ID, sceneRule.scene_id);
        contentValues.put(DistrictSearchQuery.KEYWORDS_PROVINCE, sceneRule.province);
        contentValues.put(IccidInfoManager.OPERATOR, sceneRule.operator);
        contentValues.put("expire_date", sceneRule.expire_date);
        contentValues.put("Func_call", Integer.valueOf(sceneRule.Func_call));
        contentValues.put("Func_acc_url", Integer.valueOf(sceneRule.Func_acc_url));
        contentValues.put("Func_reply_sms", Integer.valueOf(sceneRule.Func_reply_sms));
        contentValues.put("Func_config", sceneRule.Func_config);
        contentValues.put("res_urls", sceneRule.res_urls);
        contentValues.put("s_version", sceneRule.s_version);
        contentValues.put("Scene_page_config", sceneRule.Scene_page_config);
        contentValues.put("isdownload", Integer.valueOf(sceneRule.isDownload));
        contentValues.put("sceneType", Integer.valueOf(i));
        try {
            Constant.getContext();
            Map a = a.a(sceneRule.scene_id, sceneRule.Scene_page_config, sceneRule.Func_config);
            if (!(a == null || a.isEmpty())) {
                contentValues.put("scene_rule_config", new JSONObject(a).toString());
                contentValues.put("Func_config", "");
                contentValues.put("Scene_page_config", "");
            }
        } catch (Throwable th) {
        }
        return contentValues;
    }
}
