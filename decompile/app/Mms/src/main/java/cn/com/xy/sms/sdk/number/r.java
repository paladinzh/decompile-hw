package cn.com.xy.sms.sdk.number;

import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.c;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.service.number.k;
import cn.com.xy.sms.sdk.util.E;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.f;
import cn.com.xy.sms.sdk.util.o;
import cn.com.xy.sms.util.b;
import java.io.Closeable;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class r {
    private static final String a = "status";
    private static final String b = "url";
    private static final String c = "dataDigest";
    private static final String d = "dataVersion";
    private static final int e = 204;
    private static final int f = 200;
    private static final int g = 500;
    private static final int h = 1;
    private static final int i = 2;
    private static final int j = 3;
    private static String k = null;

    public static void a(XyCallBack xyCallBack) {
        E.b.execute(new s(xyCallBack));
    }

    static /* synthetic */ void a(XyCallBack xyCallBack, int i, String str) {
        new StringBuilder("upgrade callback:").append(i).append("/").append(str);
        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(i), str);
    }

    static /* synthetic */ void a(XyCallBack xyCallBack, JSONObject jSONObject) {
        String optString = jSONObject.optString("url");
        String optString2 = jSONObject.optString(c);
        k = jSONObject.optString(d);
        E.b.execute(new t(optString2, optString, xyCallBack));
    }

    static /* synthetic */ void a(InputStream inputStream) {
        Throwable th;
        SQLiteDatabase sQLiteDatabase;
        Closeable closeable = null;
        SQLiteDatabase a;
        Closeable lineNumberReader;
        try {
            a = c.a();
            try {
                JSONObject jSONObject = new JSONObject();
                Map numberTagTypeMap = DexUtil.getNumberTagTypeMap(null);
                lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
                while (true) {
                    try {
                        String readLine = lineNumberReader.readLine();
                        if (readLine != null) {
                            int indexOf = readLine.indexOf("\t");
                            int parseInt = Integer.parseInt(readLine.substring(0, indexOf));
                            String[] split = readLine.substring(indexOf + 1).split("\t");
                            if (3 != parseInt) {
                                JSONObject a2 = b.a(jSONObject, numberTagTypeMap, split, k);
                                if (a2 != null) {
                                    int a3 = b.a(split, 6);
                                    if (2 == parseInt) {
                                        c.a("tb_number_info", BaseManager.getContentValues(null, IccidInfoManager.NUM, split[0], "result", a2.toString(), NumberInfo.VERSION_KEY, k, "t9_flag", String.valueOf(a3)), "num=?", new String[]{split[0]});
                                    } else if (1 == parseInt) {
                                        n.a(split[0], a2, k, 0, a3);
                                    }
                                } else {
                                    continue;
                                }
                            } else {
                                c.a("tb_number_info", "num=?", new String[]{split[0]});
                            }
                        } else {
                            c.a(a);
                            f.a(lineNumberReader);
                            f.a((Closeable) inputStream);
                            return;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                lineNumberReader = null;
                c.a(a);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            lineNumberReader = null;
            a = null;
            c.a(a);
            f.a(lineNumberReader);
            f.a((Closeable) inputStream);
            throw th;
        }
    }

    private static String b(String str) {
        String str2 = null;
        File file = new File(str);
        if (file.exists()) {
            try {
                str2 = o.a(file);
            } catch (Throwable th) {
                th.getMessage();
            }
        }
        return str2;
    }

    static /* synthetic */ void b(XyCallBack xyCallBack) {
        Map hashMap = new HashMap();
        hashMap.put(NumberInfo.VERSION_KEY, b.a());
        k.a(hashMap, null, new u(xyCallBack));
    }

    private static void b(XyCallBack xyCallBack, int i, String str) {
        new StringBuilder("upgrade callback:").append(i).append("/").append(str);
        XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(i), str);
    }

    private static void b(XyCallBack xyCallBack, JSONObject jSONObject) {
        String optString = jSONObject.optString("url");
        String optString2 = jSONObject.optString(c);
        k = jSONObject.optString(d);
        E.b.execute(new t(optString2, optString, xyCallBack));
    }

    private static void b(InputStream inputStream) {
        SQLiteDatabase a;
        Closeable lineNumberReader;
        Throwable th;
        SQLiteDatabase sQLiteDatabase;
        Closeable closeable = null;
        try {
            a = c.a();
            try {
                JSONObject jSONObject = new JSONObject();
                Map numberTagTypeMap = DexUtil.getNumberTagTypeMap(null);
                lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
                while (true) {
                    try {
                        String readLine = lineNumberReader.readLine();
                        if (readLine != null) {
                            int indexOf = readLine.indexOf("\t");
                            int parseInt = Integer.parseInt(readLine.substring(0, indexOf));
                            String[] split = readLine.substring(indexOf + 1).split("\t");
                            if (3 != parseInt) {
                                JSONObject a2 = b.a(jSONObject, numberTagTypeMap, split, k);
                                if (a2 != null) {
                                    int a3 = b.a(split, 6);
                                    if (2 == parseInt) {
                                        c.a("tb_number_info", BaseManager.getContentValues(null, IccidInfoManager.NUM, split[0], "result", a2.toString(), NumberInfo.VERSION_KEY, k, "t9_flag", String.valueOf(a3)), "num=?", new String[]{split[0]});
                                    } else if (1 == parseInt) {
                                        n.a(split[0], a2, k, 0, a3);
                                    }
                                } else {
                                    continue;
                                }
                            } else {
                                c.a("tb_number_info", "num=?", new String[]{split[0]});
                            }
                        } else {
                            c.a(a);
                            f.a(lineNumberReader);
                            f.a((Closeable) inputStream);
                            return;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                lineNumberReader = null;
                c.a(a);
                f.a(lineNumberReader);
                f.a((Closeable) inputStream);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            lineNumberReader = null;
            a = null;
            c.a(a);
            f.a(lineNumberReader);
            f.a((Closeable) inputStream);
            throw th;
        }
    }

    private static void c(XyCallBack xyCallBack) {
        Map hashMap = new HashMap();
        hashMap.put(NumberInfo.VERSION_KEY, b.a());
        k.a(hashMap, null, new u(xyCallBack));
    }
}
