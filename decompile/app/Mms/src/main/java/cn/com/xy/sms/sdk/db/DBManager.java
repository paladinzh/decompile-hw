package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.o;
import cn.com.xy.sms.sdk.db.entity.p;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.Hashtable;

/* compiled from: Unknown */
public class DBManager {
    private static int a = 48;
    private static h b = null;
    private static int c = 1000;
    private static int d = 100;
    public static final String dataBaseName = "smssdk.db";
    public static Object dblock = new Object();
    private static Hashtable<SQLiteDatabase, Integer> e = new Hashtable();

    private static SQLiteDatabase a() {
        return getSQLiteDatabase();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static SQLiteDatabase a(Context context) {
        synchronized (e) {
            if (e.size() >= 10) {
                return null;
            } else if (context != null) {
                SQLiteDatabase readableDatabase = b(context).getReadableDatabase();
                if (readableDatabase != null) {
                    Integer num = (Integer) e.get(readableDatabase);
                    e.put(readableDatabase, num != null ? Integer.valueOf(num.intValue() + 1) : Integer.valueOf(1));
                    if (!readableDatabase.isOpen()) {
                        e.remove(readableDatabase);
                        return null;
                    }
                }
            } else {
                return null;
            }
        }
    }

    private static void a(SQLiteDatabase sQLiteDatabase, String str) {
        try {
            sQLiteDatabase.execSQL(str);
        } catch (Throwable th) {
        }
    }

    private static boolean a(SQLiteDatabase sQLiteDatabase) {
        if (sQLiteDatabase == null || !sQLiteDatabase.inTransaction()) {
            return false;
        }
        close(sQLiteDatabase);
        return true;
    }

    private static synchronized h b(Context context) {
        h hVar;
        synchronized (DBManager.class) {
            if (b == null) {
                b = new h(context, dataBaseName, null, 48);
            }
            hVar = b;
        }
        return hVar;
    }

    public static synchronized void close(SQLiteDatabase sQLiteDatabase) {
        synchronized (DBManager.class) {
            if (sQLiteDatabase != null) {
                try {
                    synchronized (e) {
                        if (sQLiteDatabase.isOpen()) {
                            Integer num = (Integer) e.get(sQLiteDatabase);
                            if (num != null) {
                                num = Integer.valueOf(num.intValue() - 1);
                                if (num.intValue() != 0) {
                                    e.put(sQLiteDatabase, num);
                                } else {
                                    e.remove(sQLiteDatabase);
                                    sQLiteDatabase.close();
                                }
                            }
                        } else {
                            e.remove(sQLiteDatabase);
                        }
                    }
                    if (e.size() == 0) {
                        return;
                    }
                } catch (Throwable th) {
                    new StringBuilder("DBManager close error: ").append(th.getMessage());
                    DexUtil.saveExceptionLog(th);
                }
            } else {
                return;
            }
        }
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void closeDB(File file, boolean z, LineNumberReader lineNumberReader, BufferedReader bufferedReader, SQLiteDatabase sQLiteDatabase) {
        if (z) {
            try {
                f.a(file);
            } catch (Throwable th) {
            }
        }
        if (lineNumberReader != null) {
            try {
                lineNumberReader.close();
            } catch (Throwable th2) {
            }
        }
        if (bufferedReader != null) {
            try {
                bufferedReader.close();
            } catch (Throwable th3) {
            }
        }
        if (sQLiteDatabase != null) {
            try {
                if (sQLiteDatabase.inTransaction()) {
                    sQLiteDatabase.setTransactionSuccessful();
                    sQLiteDatabase.endTransaction();
                }
                close(sQLiteDatabase);
            } catch (Throwable th4) {
            }
        }
    }

    public static void closeDB(String str, boolean z, LineNumberReader lineNumberReader, BufferedReader bufferedReader, SQLiteDatabase sQLiteDatabase) {
        File file = null;
        if (z) {
            try {
                file = new File(str);
            } catch (Throwable th) {
            }
        }
        closeDB(file, z, lineNumberReader, bufferedReader, sQLiteDatabase);
    }

    public static void createDb(SQLiteDatabase sQLiteDatabase) {
        try {
            sQLiteDatabase.execSQL(SysParamEntityManager.CREATE_TABLE);
            sQLiteDatabase.execSQL(IccidInfoManager.CREATE_TABLE);
            sQLiteDatabase.execSQL(cn.com.xy.sms.sdk.db.entity.a.f.a());
            sQLiteDatabase.execSQL(cn.com.xy.sms.sdk.db.entity.a.f.b());
            sQLiteDatabase.execSQL(cn.com.xy.sms.sdk.db.entity.a.f.c());
            sQLiteDatabase.execSQL(" create table  if not exists tb_centernum_location_info ( id INTEGER PRIMARY KEY, cnum TEXT not null unique, areaCode TEXT, city TEXT, checkTime long, operator TEXT )");
            sQLiteDatabase.execSQL("create table  if not exists tb_scene_config (scene_id TEXT,sceneType INTEGER DEFAULT '0',isCheck INTEGER DEFAULT '0',sceneVersion TEXT,isUse INTEGER DEFAULT '0',last_update_time INTEGER DEFAULT '0',useCount INTEGER DEFAULT '0')");
            sQLiteDatabase.execSQL("create table  if not exists tb_res_download (id INTEGER PRIMARY KEY,scene_id TEXT,url TEXT,status INTEGER,pos INTEGER,last_load_time INTEGER DEFAULT '0' )");
            sQLiteDatabase.execSQL("create table  if not exists tb_scenerule_config (id TEXT,sceneRuleVersion TEXT,scene_id TEXT,province TEXT,operator TEXT,expire_date TEXT,Func_call INTEGER,Func_acc_url INTEGER,Func_reply_sms INTEGER,Func_config TEXT,res_urls TEXT,s_version TEXT,Scene_page_config TEXT,sceneType INTEGER DEFAULT '-1',scene_rule_config TEXT,isdownload INTEGER DEFAULT '0',isuse INTEGER DEFAULT 0,last_update_time  INTEGER DEFAULT 0)");
            sQLiteDatabase.execSQL("create table  if not exists tb_jar_list (id INTEGER PRIMARY KEY,name TEXT,version TEXT,url TEXT,status INTEGER DEFAULT '0',update_time INTEGER DEFAULT '0',delaystart INTEGER DEFAULT '0',delayend INTEGER DEFAULT '0',count INTEGER DEFAULT '0',last_load_time INTEGER DEFAULT '0' ,is_use INTEGER DEFAULT '0' ,pver TEXT)");
            sQLiteDatabase.execSQL("create table  if not exists tb_count_scene (scene_id TEXT,count INT)");
            sQLiteDatabase.execSQL("create table  if not exists tb_popup_action_scene (scene_id TEXT, date TEXT, parse_times INTEGER DEFAULT '0', popup_times INTEGER DEFAULT '0' ) ");
            sQLiteDatabase.execSQL("create table  if not exists tb_menu_action (phone_num TEXT, date TEXT, company_num TEXT, function_mode TEXT, click_times INTEGER DEFAULT '0'  ) ");
            sQLiteDatabase.execSQL("create table  if not exists tb_button_action_scene (scene_id TEXT, date TEXT, action_type INTEGER DEFAULT '0', times INTEGER DEFAULT '0', action_code TEXT  ) ");
            sQLiteDatabase.execSQL(TrainManager.CREATE_TABLE);
            sQLiteDatabase.execSQL(AirManager.CREATE_TABLE);
            sQLiteDatabase.execSQL("create table  if not exists tb_menu_list (id INTEGER PRIMARY KEY,name TEXT,version TEXT,url TEXT,status INTEGER DEFAULT '0',update_time INTEGER DEFAULT '0',delaystart INTEGER DEFAULT '0',delayend INTEGER DEFAULT '0',count INTEGER DEFAULT '0',last_load_time INTEGER DEFAULT '0' )");
            sQLiteDatabase.execSQL(MatchCacheManager.getCreateTableSql());
            sQLiteDatabase.execSQL("create table  if not exists tb_update_task ( id INTEGER PRIMARY KEY,content TEXT,t_group TEXT,t_version long )");
            sQLiteDatabase.execSQL("create table  if not exists tb_xml_res_download (id INTEGER PRIMARY KEY,scene_id TEXT,url TEXT,status INTEGER,pos INTEGER,last_load_time INTEGER DEFAULT '0' ,sceneType INTEGER DEFAULT '0',insert_time INTEGER DEFAULT '0' )");
            sQLiteDatabase.execSQL(" create table  if not exists tb_resourse_queue ( id INTEGER PRIMARY KEY, res_type INTEGER, res_version INTEGER, res_url TEXT, down_statu INTEGER DEFAULT '0', temp_filename TEXT, down_failed_time LONG DEFAULT '0')");
            sQLiteDatabase.execSQL(PhoneSmsParseManager.getCreateTableSql());
            sQLiteDatabase.execSQL(" create table  if not exists tb_netquery_time (id INTEGER PRIMARY KEY,phone_num TEXT,area_code TEXT,request_time LONG DEFAULT 0,status INTEGER DEFAULT 0)");
            sQLiteDatabase.execSQL("CREATE TABLE  IF NOT EXISTS tb_num_name (id INTEGER PRIMARY KEY, num TEXT NOT NULL UNIQUE, name TEXT NOT NULL,cmd TEXT , ec TEXT , cnum TEXT,mark_time LONG DEFAULT 0,mark_cmd INTEGER DEFAULT 0,mark_ec INTEGER DEFAULT 0,last_name_time INTEGER DEFAULT 0,last_name_pubid INTEGER DEFAULT 0,last_cmd_time INTEGER DEFAULT 0,last_ec_time INTEGER DEFAULT 0,last_query_time INTEGER DEFAULT 0)");
            sQLiteDatabase.execSQL(" create table  if not exists tb_emergency_queue ( id INTEGER PRIMARY KEY, emVersion INTEGER, emContent TEXT )");
            sQLiteDatabase.execSQL(o.a());
            sQLiteDatabase.execSQL(p.a());
            sQLiteDatabase.execSQL("create table  if not exists tb_msg_url (  id INTEGER PRIMARY KEY, url TEXT, check_time integer default 0, check_statu integer default 0, third_check_statu integer default 0)");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS tb_shard_data (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, num TEXT NOT NULL, encode_content TEXT NOT NULL, content_sign TEXT NOT NULL, status INTEGER DEFAULT 0, msg_time INTEGER DEFAULT 0)");
            sQLiteDatabase.execSQL("create table  if not exists tb_jarsign(id INTEGER PRIMARY KEY,jarname TEXT unique,jarname_ver TEXT)");
            sQLiteDatabase.execSQL("create table  if not exists tb_phonenum_menu (id INTEGER PRIMARY KEY,queryKey TEXT unique,phoneNum TEXT,areaCode TEXT,jsonResult TEXT,status INTEGER DEFAULT '1',version TEXT,updateTime INTEGER DEFAULT '0')");
        } catch (Throwable th) {
        }
        try {
            sQLiteDatabase.execSQL("ALTER TABLE tb_public_info ADD COLUMN classifyCode TEXT");
        } catch (Throwable th2) {
        }
        try {
            sQLiteDatabase.execSQL("ALTER TABLE tb_scene_config ADD COLUMN isCheck INTEGER DEFAULT '0'");
        } catch (Throwable th3) {
        }
        try {
            sQLiteDatabase.execSQL("ALTER TABLE tb_scene_config ADD COLUMN useCount INTEGER DEFAULT '0'");
        } catch (Throwable th4) {
        }
        try {
            sQLiteDatabase.execSQL("ALTER TABLE tb_scene_config ADD COLUMN isUse INTEGER DEFAULT '0'");
        } catch (Throwable th5) {
        }
        try {
            sQLiteDatabase.execSQL("ALTER TABLE tb_jar_list ADD COLUMN is_use INTEGER DEFAULT '0'");
        } catch (Throwable th6) {
        }
        try {
            sQLiteDatabase.execSQL(TrainManager.ADD_STATION_LIST);
        } catch (Throwable th7) {
        }
        try {
            sQLiteDatabase.execSQL(TrainManager.ADD_DATA_TIME);
        } catch (Throwable th8) {
        }
        try {
            sQLiteDatabase.execSQL(MatchCacheManager.ADD_bubble_lasttime);
        } catch (Throwable th9) {
        }
        try {
            sQLiteDatabase.execSQL(MatchCacheManager.ADD_card_lasttime);
        } catch (Throwable th10) {
        }
        try {
            sQLiteDatabase.execSQL(MatchCacheManager.ADD_session_lasttime);
        } catch (Throwable th11) {
        }
        a(sQLiteDatabase, " ALTER TABLE tb_public_num_info ADD COLUMN lastloadtime LONG default 0");
        a(sQLiteDatabase, " ALTER TABLE tb_public_num_info ADD COLUMN isrulenum INTEGER default 0");
        a(sQLiteDatabase, "ALTER TABLE tb_netquery_time ADD COLUMN area_code TEXT");
        a(sQLiteDatabase, IccidInfoManager.ADD_USER_PROVINCES);
        a(sQLiteDatabase, IccidInfoManager.ADD_USER_AREACODE);
        a(sQLiteDatabase, IccidInfoManager.ADD_USER_OPERATOR);
        a(sQLiteDatabase, IccidInfoManager.ADD_SIM_INDEX);
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN cnum TEXT ");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN mark_time LONG DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN cmd TEXT ");
        a(sQLiteDatabase, " ALTER TABLE tb_public_info ADD COLUMN corpLevel INTEGER default 0");
        a(sQLiteDatabase, MatchCacheManager.ADD_urls);
        a(sQLiteDatabase, MatchCacheManager.ADD_url_valid_statu);
        a(sQLiteDatabase, "ALTER TABLE tb_jar_list ADD COLUMN pver TEXT ");
        a(sQLiteDatabase, "ALTER TABLE tb_button_action_scene ADD COLUMN action_code TEXT");
        try {
            sQLiteDatabase.execSQL(" UPDATE tb_button_action_scene SET action_code = action_type WHERE action_code = '' OR action_code IS NULL");
        } catch (Throwable th12) {
            th12.getMessage();
        }
        try {
            sQLiteDatabase.execSQL("CREATE TABLE  IF NOT EXISTS tb_sms_parse_recorder (phone_num TEXT, sms_num INTEGER DEFAULT 0, success_num INTEGER DEFAULT 0, date_time INTEGER DEFAULT 0, query_flag INTEGER DEFAULT 0)");
        } catch (Throwable th122) {
            th122.getMessage();
        }
        a(sQLiteDatabase, " ALTER TABLE tb_public_num_info ADD COLUMN isuse LONG default 0");
        a(sQLiteDatabase, "ALTER TABLE tb_public_info ADD COLUMN rid TEXT");
        a(sQLiteDatabase, " ALTER TABLE tb_public_info ADD COLUMN logoType TEXT");
        a(sQLiteDatabase, "ALTER TABLE tb_public_info ADD COLUMN scale INTEGER default 0");
        a(sQLiteDatabase, "ALTER TABLE tb_public_info ADD COLUMN backColor TEXT");
        a(sQLiteDatabase, "ALTER TABLE tb_public_info ADD COLUMN backColorEnd TEXT");
        a(sQLiteDatabase, MatchCacheManager.ADD_recognise_result);
        a(sQLiteDatabase, MatchCacheManager.ADD_recognise_lasttime);
        a(sQLiteDatabase, " ALTER TABLE tb_public_num_info ADD COLUMN nameType INTEGER default 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN mark_cmd INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN last_name_pubid INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN last_name_time INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN last_cmd_time INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN last_query_time INTEGER DEFAULT 0");
        a(sQLiteDatabase, TrainManager.ADD_DAY);
        sQLiteDatabase.execSQL(TrainManager.DROP_OLD_TABLE);
        a(sQLiteDatabase, "ALTER TABLE tb_netquery_time ADD COLUMN status INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN ec TEXT ");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN mark_ec INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_num_name ADD COLUMN last_ec_time INTEGER DEFAULT 0");
        a(sQLiteDatabase, "UPDATE tb_operator_parse_info SET msg=NULL");
        a(sQLiteDatabase, "ALTER TABLE tb_operator_parse_info ADD COLUMN numMsgMD5 TEXT");
        a(sQLiteDatabase, MatchCacheManager.ADD_URLS_RESULT);
        a(sQLiteDatabase, MatchCacheManager.ADD_URLS_LASTTIME);
        a(sQLiteDatabase, "ALTER TABLE tb_scene_config ADD COLUMN last_update_time INTEGER DEFAULT '0'");
        a(sQLiteDatabase, "ALTER TABLE tb_scenerule_config ADD COLUMN isuse INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_scenerule_config ADD COLUMN last_update_time INTEGER DEFAULT 0");
        a(sQLiteDatabase, "ALTER TABLE tb_scenerule_config ADD COLUMN scene_rule_config  TEXT");
        a(sQLiteDatabase, "create index if not exists scene_and_type_idx on tb_scenerule_config (scene_id,sceneType)");
        a(sQLiteDatabase, MatchCacheManager.ADD_IS_FAVORITE);
        a(sQLiteDatabase, MatchCacheManager.ADD_IS_MARK);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int delete(String str, String str2, String[] strArr) {
        int i;
        synchronized (dblock) {
            int delete;
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = getSQLiteDatabase();
                delete = sQLiteDatabase.delete(str, str2, strArr);
                close(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                close(sQLiteDatabase2);
                throw th3;
            }
            i = delete;
        }
        return i;
    }

    public static boolean excSql(File file, boolean z) {
        BufferedReader bufferedReader;
        BufferedReader bufferedReader2;
        SQLiteDatabase sQLiteDatabase;
        SQLiteDatabase sQLiteDatabase2;
        Throwable th;
        Throwable th2;
        LineNumberReader lineNumberReader = null;
        if (file == null || !file.exists()) {
            return false;
        }
        LineNumberReader lineNumberReader2;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            try {
                lineNumberReader2 = new LineNumberReader(bufferedReader);
            } catch (Throwable th3) {
                sQLiteDatabase2 = null;
                th = th3;
                lineNumberReader2 = null;
                closeDB(file, z, lineNumberReader2, bufferedReader, sQLiteDatabase2);
                throw th;
            }
            try {
                sQLiteDatabase2 = getSQLiteDatabase();
                try {
                    sQLiteDatabase2.beginTransaction();
                    while (true) {
                        String readLine = lineNumberReader2.readLine();
                        if (readLine == null) {
                            lineNumberReader2.close();
                            closeDB(file, z, lineNumberReader2, bufferedReader, sQLiteDatabase2);
                            return true;
                        } else if (!StringUtils.isNull(readLine)) {
                            sQLiteDatabase2.execSQL(readLine);
                        }
                    }
                } catch (Throwable th4) {
                    lineNumberReader = lineNumberReader2;
                    bufferedReader2 = bufferedReader;
                    sQLiteDatabase = sQLiteDatabase2;
                }
            } catch (Throwable th5) {
                th2 = th5;
                sQLiteDatabase2 = null;
                th = th2;
                closeDB(file, z, lineNumberReader2, bufferedReader, sQLiteDatabase2);
                throw th;
            }
        } catch (Throwable th32) {
            bufferedReader = null;
            sQLiteDatabase2 = null;
            th2 = th32;
            lineNumberReader2 = null;
            th = th2;
            closeDB(file, z, lineNumberReader2, bufferedReader, sQLiteDatabase2);
            throw th;
        }
    }

    public static boolean excSql(String str, boolean z) {
        return !StringUtils.isNull(str) ? excSql(new File(str), z) : false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void execSQL(String str) {
        if (!StringUtils.isNull(str)) {
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = getSQLiteDatabase();
                sQLiteDatabase.execSQL(str);
                close(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                close(sQLiteDatabase2);
                throw th3;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized SQLiteDatabase getSQLiteDatabase() {
        SQLiteDatabase sQLiteDatabase = null;
        synchronized (DBManager.class) {
            long currentTimeMillis = System.currentTimeMillis();
            while (sQLiteDatabase == null) {
                sQLiteDatabase = a(Constant.getContext());
                if (sQLiteDatabase == null) {
                    if ((System.currentTimeMillis() - currentTimeMillis >= ((long) c) ? 1 : null) != null) {
                        break;
                    }
                    try {
                        Thread.sleep((long) d);
                    } catch (InterruptedException e) {
                    }
                } else {
                    sQLiteDatabase.inTransaction();
                    return sQLiteDatabase;
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long insert(String str, ContentValues contentValues) {
        long j;
        SQLiteDatabase sQLiteDatabase = null;
        synchronized (dblock) {
            long insert;
            try {
                sQLiteDatabase = getSQLiteDatabase();
                insert = sQLiteDatabase.insert(str, null, contentValues);
                close(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                close(sQLiteDatabase2);
                throw th3;
            }
            j = insert;
        }
        return j;
    }

    public static XyCursor query(String str, String[] strArr, String str2, String[] strArr2) {
        return query(false, str, strArr, str2, strArr2, null, null, null, null);
    }

    public static XyCursor query(String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        XyCursor xyCursor;
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            SQLiteDatabase sQLiteDatabase2 = getSQLiteDatabase();
            try {
                if (a(sQLiteDatabase2)) {
                    return null;
                }
                xyCursor = new XyCursor(sQLiteDatabase2, sQLiteDatabase2.query(str, strArr, str2, strArr2, str3, str4, str5, str6), 0);
                return xyCursor;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                sQLiteDatabase = sQLiteDatabase2;
                th = th3;
                close(sQLiteDatabase);
                DexUtil.saveExceptionLog(th);
                xyCursor = null;
                return xyCursor;
            }
        } catch (Throwable th4) {
            th = th4;
            close(sQLiteDatabase);
            DexUtil.saveExceptionLog(th);
            xyCursor = null;
            return xyCursor;
        }
    }

    public static XyCursor query(boolean z, String str, String[] strArr, String str2, String[] strArr2, String str3, String str4, String str5, String str6) {
        XyCursor xyCursor;
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            SQLiteDatabase sQLiteDatabase2 = getSQLiteDatabase();
            if (sQLiteDatabase2 == null) {
                return null;
            }
            try {
                if (a(sQLiteDatabase2)) {
                    return null;
                }
                xyCursor = new XyCursor(sQLiteDatabase2, sQLiteDatabase2.query(z, str, strArr, str2, strArr2, str3, str4, str5, str6), 0);
                return xyCursor;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                sQLiteDatabase = sQLiteDatabase2;
                th = th3;
                close(sQLiteDatabase);
                DexUtil.saveExceptionLog(th);
                xyCursor = null;
                return xyCursor;
            }
        } catch (Throwable th4) {
            th = th4;
            close(sQLiteDatabase);
            DexUtil.saveExceptionLog(th);
            xyCursor = null;
            return xyCursor;
        }
    }

    public static XyCursor rawQuery(String str, String[] strArr) {
        XyCursor xyCursor = null;
        SQLiteDatabase sQLiteDatabase;
        try {
            sQLiteDatabase = getSQLiteDatabase();
            try {
                if (a(sQLiteDatabase)) {
                    return null;
                }
                xyCursor = new XyCursor(sQLiteDatabase, sQLiteDatabase.rawQuery(str, strArr), 0);
                return xyCursor;
            } catch (Throwable th) {
                close(sQLiteDatabase);
                return xyCursor;
            }
        } catch (Throwable th2) {
            sQLiteDatabase = null;
            close(sQLiteDatabase);
            return xyCursor;
        }
    }

    public static long saveOrUpdateTableData(String str, ContentValues contentValues, String str2, String[] strArr) {
        try {
            long update = (long) update(str, contentValues, str2, strArr);
            return ((update > 1 ? 1 : (update == 1 ? 0 : -1)) >= 0 ? 1 : null) == null ? insert(str, contentValues) : -update;
        } catch (Throwable th) {
            return 0;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int update(String str, ContentValues contentValues, String str2, String[] strArr) {
        int i;
        synchronized (dblock) {
            int update;
            SQLiteDatabase sQLiteDatabase = null;
            try {
                sQLiteDatabase = getSQLiteDatabase();
                update = sQLiteDatabase.update(str, contentValues, str2, strArr);
                close(sQLiteDatabase);
            } catch (Throwable th) {
                Throwable th2 = th;
                SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                Throwable th3 = th2;
                close(sQLiteDatabase2);
                throw th3;
            }
            i = update;
        }
        return i;
    }
}
