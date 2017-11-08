package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.a.a;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.sdk.util.g;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/* compiled from: Unknown */
public class ParseItemManager {
    public static final String ADD_LAST_USE_TIME = "ALTER TABLE tb_regex ADD COLUMN last_use_time INTEGER DEFAULT '0'";
    public static final String CREATE_INDEX = "create index if not exists indx_s_m on tb_regex (scene_id,match_id)";
    public static final String CREATE_INDEX_SID = "create index if not exists indx_s on tb_regex (scene_id)";
    public static final String CREATE_TABLE = "create table  if not exists tb_regex (scene_id TEXT,match_id TEXT,regex_text TEXT,version_code TEXT,regex_type INTEGER  DEFAULT '0',last_use_time INTEGER  DEFAULT '0',state INTEGER  DEFAULT '0' )";
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_regex";
    public static final int INITTAG = -2;
    public static final String LAST_USE_TIME = "last_use_time";
    public static final String MATCH_ID = "match_id";
    public static final int NEEDDEL = -1;
    public static final int NORMAL = 0;
    public static final String REGEX_TEXT = "regex_text";
    public static final String REGEX_TYPE = "regex_type";
    public static final int REGEX_TYPE_AD = 2;
    public static final int REGEX_TYPE_SCENE = 1;
    public static final String SCENE_ID = "scene_id";
    public static final String STATE = "state";
    public static final String TABLE_NAME = "tb_regex";
    public static final String VERSION_CODE = "version_code";
    static ConcurrentHashMap<String, String> a = new ConcurrentHashMap();
    static ConcurrentHashMap<String, String> b = new ConcurrentHashMap(400);
    static long c = 0;
    private static boolean d = false;
    private static boolean e = false;
    public static boolean execNqSql = false;
    private static long f = 0;
    private static HashMap<Long, SQLiteDatabase> g = new HashMap();
    private static String h = null;
    private static boolean i = false;

    private static String a(String str) {
        try {
            return (String) a.remove(str);
        } catch (Throwable th) {
            return null;
        }
    }

    private static void a(int i) {
        try {
            a.a(TABLE_NAME, "state=? ", new String[]{new StringBuilder(ThemeUtil.SET_NULL_STR).toString()});
        } catch (Throwable th) {
        }
    }

    private static void a(String str, SQLiteDatabase sQLiteDatabase) {
        int i = 0;
        if (!StringUtils.isNull(str) && str.indexOf("values") != -1) {
            CharSequence substring = str.substring(0, str.indexOf("values"));
            String replace = str.replace(substring, "");
            String stringBuilder = new StringBuilder(String.valueOf(substring)).append(" values ").toString();
            String[] split = replace.replace("values", "").split("'\\),\\('");
            List arrayList = new ArrayList();
            if (split != null && split.length > 0) {
                for (int i2 = 0; i2 < split.length; i2++) {
                    if (!StringUtils.isNull(split[i2])) {
                        String trim = split[i2].trim();
                        if (trim.startsWith("(")) {
                            arrayList.add(new StringBuilder(String.valueOf(stringBuilder)).append(" ").append(trim).append("')").toString());
                        } else if (trim.endsWith(";")) {
                            arrayList.add(new StringBuilder(String.valueOf(stringBuilder)).append(" ('").append(trim).toString());
                        } else if (trim.endsWith(")")) {
                            arrayList.add(new StringBuilder(String.valueOf(stringBuilder)).append(" ('").append(trim).append("')").toString());
                        } else {
                            arrayList.add(new StringBuilder(String.valueOf(stringBuilder)).append(" ('").append(trim).append("')").toString());
                        }
                    }
                }
            }
            if (!arrayList.isEmpty()) {
                while (i < arrayList.size()) {
                    String str2 = (String) arrayList.get(i);
                    if (!StringUtils.isNull(str2)) {
                        try {
                            sQLiteDatabase.execSQL(str2);
                        } catch (Throwable th) {
                        }
                    }
                    i++;
                }
            }
        }
    }

    private static boolean a() {
        return !((System.currentTimeMillis() > (f + Constant.FIVE_MINUTES) ? 1 : (System.currentTimeMillis() == (f + Constant.FIVE_MINUTES) ? 0 : -1)) <= 0);
    }

    public static void appendUseMatchId(String str, String str2) {
        try {
            if (!StringUtils.isNull(str) && b.size() < 400) {
                b.put(str, str2);
            }
        } catch (Throwable th) {
        }
    }

    private static synchronized void b() {
        synchronized (ParseItemManager.class) {
            a.a((SQLiteDatabase) g.remove(Long.valueOf(Thread.currentThread().getId())));
        }
    }

    private static boolean c() {
        Context context = Constant.getContext();
        return context != null && context.getDatabasePath("bizport.db-corrupted").exists();
    }

    public static void cacheParsePatternString(String str, String str2, int i, SQLiteDatabase sQLiteDatabase) {
        try {
            if (!str.equals(h)) {
                a.clear();
                h = str;
                cn.com.xy.sms.sdk.a.a.h.execute(new m(str, i));
            }
        } catch (Throwable th) {
        }
    }

    public static void checkHasData() {
        boolean z = false;
        if (!((System.currentTimeMillis() > (f + Constant.FIVE_MINUTES) ? 1 : (System.currentTimeMillis() == (f + Constant.FIVE_MINUTES) ? 0 : -1)) <= 0)) {
            Context context = Constant.getContext();
            if (context != null && context.getDatabasePath("bizport.db-corrupted").exists()) {
                z = true;
            }
            if (z) {
                f = System.currentTimeMillis();
                try {
                    Context context2 = Constant.getContext();
                    context2.getDatabasePath("bizport.db-corrupted").delete();
                    context2.getDatabasePath("bizport.db-journalcorrupted").delete();
                } catch (Throwable th) {
                }
                g.a(Constant.getContext(), null);
                return;
            }
        }
        if (!e) {
            e = true;
            new k().start();
        }
    }

    public static void closeSpecialDatebase() {
        h = null;
        a.clear();
        b();
        updateUseTime();
    }

    private static void d() {
        try {
            Context context = Constant.getContext();
            context.getDatabasePath("bizport.db-corrupted").delete();
            context.getDatabasePath("bizport.db-journalcorrupted").delete();
        } catch (Throwable th) {
        }
    }

    public static void deleteAll() {
        try {
            a.a(TABLE_NAME, null, null);
        } catch (Throwable th) {
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void deleteRepeatData() {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            sQLiteDatabase = a.a();
            sQLiteDatabase.execSQL("DELETE FROM tb_regex WHERE state=-2 AND match_id IN (SELECT match_id FROM tb_regex GROUP BY match_id HAVING COUNT(match_id) > 1)");
            a.a(sQLiteDatabase);
        } catch (Throwable th) {
            Throwable th2 = th;
            SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
            Throwable th3 = th2;
            a.a(sQLiteDatabase2);
            throw th3;
        }
    }

    public static void deleteTimeOutMatchId(String str) {
        try {
            StringBuffer stringBuffer = new StringBuffer("last_use_time < ?");
            stringBuffer.append(" and scene_id = ? ");
            long currentTimeMillis = System.currentTimeMillis() - DexUtil.getUpdateCycleByType(32, 7776000000L);
            a.a(TABLE_NAME, stringBuffer.toString(), new String[]{String.valueOf(currentTimeMillis), str});
        } catch (Throwable th) {
        }
    }

    public static String getParsePatternString(String str, String str2, int i) {
        XyCursor xyCursor;
        Throwable th;
        XyCursor xyCursor2 = null;
        try {
            SQLiteDatabase patterDb = getPatterDb();
            if (i) {
                DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "getParsePatternString in mRunExeclSqlIng scenId:" + str + " matchId" + str2, null);
                return null;
            }
            String a = a(str2);
            if (a == null) {
                String str3 = TABLE_NAME;
                String[] strArr = new String[]{REGEX_TEXT};
                String[] strArr2 = new String[]{str, str2, String.valueOf(i)};
                Constant.getContext();
                XyCursor a2 = a.a(patterDb, false, str3, strArr, "scene_id = ? and  match_id = ? and regex_type = ?", strArr2, null, null, null, "1");
                if (a2 != null) {
                    if (a2.getCount() > 0) {
                        int columnIndex = a2.getColumnIndex(REGEX_TEXT);
                        appendUseMatchId(str2, str);
                        cacheParsePatternString(str, str2, i, patterDb);
                        if (a2.moveToNext()) {
                            String string = a2.getString(columnIndex);
                            if (a2 != null) {
                                XyCursor.closeCursor(a2, false);
                            }
                            return string;
                        }
                        if (a2 != null) {
                            XyCursor.closeCursor(a2, false);
                        }
                        return null;
                    }
                }
                try {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "not find PatternStr scenId" + str + " matchId:" + str2, null);
                    checkHasData();
                    if (a2 != null) {
                        XyCursor.closeCursor(a2, false);
                    }
                } catch (Throwable th2) {
                    th = th2;
                    xyCursor2 = a2;
                    if (xyCursor2 != null) {
                        XyCursor.closeCursor(xyCursor2, false);
                    }
                    throw th;
                }
                return null;
            }
            appendUseMatchId(str2, str);
            return a;
        } catch (Throwable th3) {
            th = th3;
            if (xyCursor2 != null) {
                XyCursor.closeCursor(xyCursor2, false);
            }
            throw th;
        }
    }

    public static synchronized SQLiteDatabase getPatterDb() {
        SQLiteDatabase sQLiteDatabase;
        synchronized (ParseItemManager.class) {
            long id = Thread.currentThread().getId();
            sQLiteDatabase = (SQLiteDatabase) g.get(Long.valueOf(id));
            if (sQLiteDatabase != null) {
                if (sQLiteDatabase.isOpen()) {
                }
            }
            sQLiteDatabase = a.a();
            g.put(Long.valueOf(id), sQLiteDatabase);
        }
        return sQLiteDatabase;
    }

    public static boolean isInitData() {
        boolean z = false;
        if (!d) {
            if (SysParamEntityManager.getLongParam("init_xiaoyuan_sdk", 0, Constant.getContext()) == 1) {
                z = true;
            }
            d = z;
        }
        return d;
    }

    public static String queryPubIdByPhoneNum(String str) {
        return cn.com.xy.sms.sdk.service.d.a.a(str);
    }

    public static boolean updateNeiQianSql(Context context) {
        try {
            if (new File(Constant.getNQSQL_PATH()).exists()) {
                execNqSql = false;
                return true;
            }
            execNqSql = false;
            return false;
        } catch (Throwable th) {
            return false;
        } finally {
            execNqSql = false;
        }
    }

    public static void updateParse(Context context) {
        String inidb_PATH = Constant.getInidb_PATH();
        if (f.a(inidb_PATH)) {
            try {
                updateStatue(0, -1);
                i = true;
                a.a(inidb_PATH, false);
                i = false;
                Constant.getContext();
                try {
                    a.a(TABLE_NAME, "state=? ", new String[]{new StringBuilder(ThemeUtil.SET_NULL_STR).toString()});
                } catch (Throwable th) {
                }
                f.d(inidb_PATH);
            } catch (Throwable th2) {
                updateStatue(-1, 0);
            } finally {
                i = false;
                a.a(inidb_PATH, true, null, null, null);
            }
        }
    }

    public static void updateStatue(int i, int i2) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(STATE, Integer.valueOf(i2));
            a.a(TABLE_NAME, contentValues, "state = ? ", new String[]{new StringBuilder(String.valueOf(i)).toString()});
        } catch (Throwable th) {
        }
    }

    public static void updateUseTime() {
        Object obj = null;
        if (b.size() > 0) {
            if (System.currentTimeMillis() - c >= 90000) {
                obj = 1;
            }
            if (obj != null) {
                cn.com.xy.sms.sdk.a.a.e.execute(new l());
            }
        }
    }
}
