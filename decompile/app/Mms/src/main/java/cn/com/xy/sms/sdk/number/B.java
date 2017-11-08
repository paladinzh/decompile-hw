package cn.com.xy.sms.sdk.number;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Pair;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.c;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.E;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.sdk.util.t;
import cn.com.xy.sms.util.w;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class b {
    private static final String a = "duoqu_embed_number_version.txt";
    private static final String b = "0";
    private static final String c = "1";
    private static final Object d = new Object();
    private static boolean e = false;
    private static boolean f = false;
    private static String g = null;

    public static int a(String[] strArr, int i) {
        int i2 = 0;
        if (strArr.length >= 7) {
            try {
                i2 = Integer.parseInt(strArr[6]);
            } catch (Throwable th) {
            }
        }
        return i2;
    }

    private static JSONObject a(JSONObject jSONObject, String[] strArr) {
        if (strArr == null || strArr.length < 4) {
            return null;
        }
        try {
            Object obj = strArr[0];
            String str = strArr[1];
            if (str.equals("1")) {
                Object obj2;
                String str2 = strArr[2];
                if (strArr.length != 4) {
                    obj2 = null;
                } else {
                    int i = 1;
                }
                String c = c();
                int length = c.length();
                String str3 = null;
                String str4 = null;
                Object obj3 = null;
                String str5 = null;
                int i2 = 0;
                while (i2 < length) {
                    for (int i3 = 3; i3 < strArr.length; i3++) {
                        if (strArr[i3].length() != 0) {
                            str4 = strArr[i3].substring(0, 1);
                            str5 = strArr[i3].substring(1, 2);
                            str3 = strArr[i3].substring(2);
                            if (obj2 != null || str4.equals(c.substring(i2, i2 + 1))) {
                                obj3 = 1;
                                break;
                            }
                        }
                    }
                    if (obj3 != null) {
                        break;
                    }
                    i2++;
                }
                String str6 = str3;
                str3 = str5;
                str5 = str6;
                if (StringUtils.isNull(str4)) {
                    jSONObject.put(NumberInfo.SOURCE_KEY, str4);
                } else {
                    jSONObject.put(NumberInfo.SOURCE_KEY, str4);
                }
                jSONObject.put(NumberInfo.AUTH_KEY, !"1".equals(str3) ? 0 : 1);
                jSONObject.put("name", str5);
                jSONObject.put(NumberInfo.LOGO_KEY, !StringUtils.isNull(str2) ? str2 : "");
            } else {
                Object obj4 = strArr[2];
                Object obj5 = strArr[3];
                jSONObject.put("tag", obj4);
                jSONObject.put("amount", obj5);
            }
            jSONObject.put(NumberInfo.NUM_KEY, obj);
            jSONObject.put(NumberInfo.NUM_TYPE_KEY, str);
            jSONObject.put(NumberInfo.VERSION_KEY, cn.com.xy.sms.util.b.a());
            return jSONObject;
        } catch (Throwable th) {
            return null;
        }
    }

    public static void a(Context context) {
        synchronized (d) {
            if (e) {
                return;
            }
            e = true;
            E.a.execute(new c(context));
        }
    }

    static /* synthetic */ void a(InputStream inputStream) {
        Closeable lineNumberReader;
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            SQLiteStatement compileStatement;
            JSONObject jSONObject;
            Map numberTagTypeMap;
            SQLiteDatabase a = c.a();
            try {
                compileStatement = a.compileStatement(String.format("REPLACE INTO %s (%s,%s,%s,%s)VALUES(?,?,?,?)", new Object[]{"tb_number_info", IccidInfoManager.NUM, "result", NumberInfo.VERSION_KEY, "t9_flag"}));
                a.beginTransaction();
                jSONObject = new JSONObject();
                numberTagTypeMap = DexUtil.getNumberTagTypeMap(null);
                lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
            } catch (Throwable th2) {
                th = th2;
                lineNumberReader = null;
                sQLiteDatabase = a;
                if (sQLiteDatabase != null) {
                    try {
                        sQLiteDatabase.endTransaction();
                    } catch (Throwable th3) {
                    }
                }
                c.a(sQLiteDatabase);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
                throw th;
            }
            try {
                String a2 = cn.com.xy.sms.util.b.a();
                while (true) {
                    String readLine = lineNumberReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split("\t");
                    JSONObject a3 = cn.com.xy.sms.util.b.a(jSONObject, numberTagTypeMap, split, a2);
                    if (a3 != null) {
                        int a4 = a(split, 6);
                        compileStatement.bindString(1, split[0]);
                        compileStatement.bindString(2, a3.toString());
                        compileStatement.bindString(3, cn.com.xy.sms.util.b.a());
                        compileStatement.bindLong(4, (long) a4);
                        compileStatement.executeInsert();
                    }
                }
                a.setTransactionSuccessful();
                if (a != null) {
                    try {
                        a.endTransaction();
                    } catch (Throwable th4) {
                    }
                }
                c.a(a);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
            } catch (Throwable th5) {
                th = th5;
                sQLiteDatabase = a;
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.endTransaction();
                }
                c.a(sQLiteDatabase);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            lineNumberReader = null;
            if (sQLiteDatabase != null) {
                sQLiteDatabase.endTransaction();
            }
            c.a(sQLiteDatabase);
            f.a(lineNumberReader);
            f.a((Closeable) inputStream);
            throw th;
        }
    }

    public static boolean a() {
        return f;
    }

    private static String b() {
        return String.format("REPLACE INTO %s (%s,%s,%s,%s)VALUES(?,?,?,?)", new Object[]{"tb_number_info", IccidInfoManager.NUM, "result", NumberInfo.VERSION_KEY, "t9_flag"});
    }

    private static void b(InputStream inputStream) {
        Closeable lineNumberReader;
        Throwable th;
        SQLiteDatabase sQLiteDatabase = null;
        try {
            SQLiteStatement compileStatement;
            JSONObject jSONObject;
            Map numberTagTypeMap;
            SQLiteDatabase a = c.a();
            try {
                compileStatement = a.compileStatement(String.format("REPLACE INTO %s (%s,%s,%s,%s)VALUES(?,?,?,?)", new Object[]{"tb_number_info", IccidInfoManager.NUM, "result", NumberInfo.VERSION_KEY, "t9_flag"}));
                a.beginTransaction();
                jSONObject = new JSONObject();
                numberTagTypeMap = DexUtil.getNumberTagTypeMap(null);
                lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
            } catch (Throwable th2) {
                th = th2;
                lineNumberReader = null;
                sQLiteDatabase = a;
                if (sQLiteDatabase != null) {
                    try {
                        sQLiteDatabase.endTransaction();
                    } catch (Throwable th3) {
                    }
                }
                c.a(sQLiteDatabase);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
                throw th;
            }
            try {
                String a2 = cn.com.xy.sms.util.b.a();
                while (true) {
                    String readLine = lineNumberReader.readLine();
                    if (readLine == null) {
                        break;
                    }
                    String[] split = readLine.split("\t");
                    JSONObject a3 = cn.com.xy.sms.util.b.a(jSONObject, numberTagTypeMap, split, a2);
                    if (a3 != null) {
                        int a4 = a(split, 6);
                        compileStatement.bindString(1, split[0]);
                        compileStatement.bindString(2, a3.toString());
                        compileStatement.bindString(3, cn.com.xy.sms.util.b.a());
                        compileStatement.bindLong(4, (long) a4);
                        compileStatement.executeInsert();
                    }
                }
                a.setTransactionSuccessful();
                if (a != null) {
                    try {
                        a.endTransaction();
                    } catch (Throwable th4) {
                    }
                }
                c.a(a);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
            } catch (Throwable th5) {
                th = th5;
                sQLiteDatabase = a;
                if (sQLiteDatabase != null) {
                    sQLiteDatabase.endTransaction();
                }
                c.a(sQLiteDatabase);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            lineNumberReader = null;
            if (sQLiteDatabase != null) {
                sQLiteDatabase.endTransaction();
            }
            c.a(sQLiteDatabase);
            f.a(lineNumberReader);
            f.a((Closeable) inputStream);
            throw th;
        }
    }

    private static void b(String str) {
        SysParamEntityManager.setParam("init_embed_number", str);
    }

    static /* synthetic */ boolean b(Context context) {
        return SysParamEntityManager.getIntParam(context, "init_embed_number") == 1;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String c() {
        String stringBuilder;
        String str = null;
        if (g != null) {
            return g;
        }
        try {
            stringBuilder = new StringBuilder(String.valueOf(a.Qihu.toString())).append(a.DHB.toString()).append(a.Sogou.toString()).append(a.ChuBao.toString()).toString();
            try {
                Pair e = e();
                str = (String) e.first;
                Map map = (Map) e.second;
                if (str != null) {
                    if (map.size() > 0) {
                        str = t.a(str, w.b(), map);
                    }
                }
            } catch (Throwable th) {
                str = stringBuilder;
            }
        } catch (Throwable th2) {
        }
        g = stringBuilder;
        return stringBuilder;
        stringBuilder = str;
        g = stringBuilder;
        return stringBuilder;
    }

    private static boolean c(Context context) {
        return SysParamEntityManager.getIntParam(context, "init_embed_number") == 1;
    }

    private static String d() {
        return new StringBuilder(String.valueOf(a.Qihu.toString())).append(a.DHB.toString()).append(a.Sogou.toString()).append(a.ChuBao.toString()).toString();
    }

    private static Pair<String, Map<String, String>> e() {
        Object obj;
        Map hashMap = new HashMap();
        Object obj2 = null;
        try {
            Properties properties = new Properties();
            properties.load(Constant.getContext().getResources().getAssets().open("unionpriority.properties"));
            obj = null;
            for (Entry entry : properties.entrySet()) {
                String str = (String) entry.getKey();
                if (str.endsWith("_union")) {
                    try {
                        obj = (String) entry.getValue();
                    } catch (Throwable th) {
                        obj2 = obj;
                    }
                } else {
                    hashMap.put(str, (String) entry.getValue());
                }
            }
        } catch (Throwable th2) {
            obj = obj2;
            return new Pair(obj, hashMap);
        }
        return new Pair(obj, hashMap);
    }

    private static Properties f() {
        Properties properties = new Properties();
        properties.load(Constant.getContext().getResources().getAssets().open("unionpriority.properties"));
        return properties;
    }
}
