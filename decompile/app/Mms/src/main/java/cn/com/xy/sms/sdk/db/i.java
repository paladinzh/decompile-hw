package cn.com.xy.sms.sdk.db;

import android.content.ContentValues;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.k;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.G;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.sdk.util.f;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/* compiled from: Unknown */
public final class i {
    private static int a = 0;
    private static int b = 1;
    private static String c = "id";
    private static String d = "name";
    private static String e = "version";
    private static String f = "url";
    private static String g = "status";
    private static String h = "last_load_time";
    private static String i = "update_time";
    private static String j = "delaystart";
    private static String k = "delayend";
    private static String l = "count";
    private static String m = "tb_menu_list";
    private static String n = " DROP TABLE IF EXISTS tb_menu_list";
    private static String o = "create table  if not exists tb_menu_list (id INTEGER PRIMARY KEY,name TEXT,version TEXT,url TEXT,status INTEGER DEFAULT '0',update_time INTEGER DEFAULT '0',delaystart INTEGER DEFAULT '0',delayend INTEGER DEFAULT '0',count INTEGER DEFAULT '0',last_load_time INTEGER DEFAULT '0' )";

    public static void a() {
        boolean z = false;
        k c = c();
        if (c != null) {
            if (System.currentTimeMillis() <= c.e + DexUtil.getUpdateCycleByType(5, Constant.weekTime)) {
                z = true;
            }
            if (!z) {
                a(c, null, true, null);
            } else if (SysParamEntityManager.getIntParam(Constant.getContext(), Constant.AUTO_UPDATE_DATA) == 0 && NetUtil.checkAccessNetWork(1)) {
                b(c);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void a(k kVar) {
        String str = "duoqu_nqsql.zip";
        try {
            if (StringUtils.isNull(kVar.d)) {
                try {
                    f.c(Constant.getFilePath() + str);
                    return;
                } catch (Throwable th) {
                    return;
                }
            }
            String str2 = "menu.sql";
            if (f.f(kVar.d, Constant.getFilePath(), str) == -1) {
                try {
                    f.c(Constant.getFilePath() + str);
                } catch (Throwable th2) {
                }
            } else if (!XyUtil.upZipFile(Constant.getFilePath() + str, Constant.getINITSQL_PATH())) {
                try {
                    f.c(Constant.getFilePath() + str);
                } catch (Throwable th3) {
                }
            } else if (f.a(Constant.getINITSQL_PATH() + str2)) {
                b();
                str2 = kVar.b;
                ContentValues contentValues = new ContentValues();
                contentValues.put("last_load_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
                contentValues.put("status", new StringBuilder("1").toString());
                DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str2});
                try {
                    f.c(Constant.getFilePath() + str);
                } catch (Throwable th4) {
                }
            } else {
                try {
                    f.c(Constant.getFilePath() + str);
                } catch (Throwable th5) {
                }
            }
        } catch (Throwable th6) {
        }
    }

    public static void a(k kVar, XyCallBack xyCallBack, boolean z, Map<String, String> map) {
        try {
            XyCallBack jVar = new j(z, map, kVar, xyCallBack);
            if (NetUtil.checkAccessNetWork((Map) map)) {
                String str = kVar.c;
                String str2 = kVar.b;
                str = j.a(str, kVar.f, kVar.j);
                if (!StringUtils.isNull(str)) {
                    NetUtil.executeHttpPublicRequest("", str, jVar, NetUtil.getPubNumServiceUrl() + NetUtil.UpdatePublicInfoRequest, map);
                }
            }
            String str3 = kVar.b;
            int i = kVar.j;
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("count", Integer.valueOf(i + 1));
                DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str3});
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
        }
    }

    public static void a(String str) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
            DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    private static void a(String str, int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("last_load_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
            contentValues.put("status", new StringBuilder("1").toString());
            DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static void a(String str, long j, long j2) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("update_time", new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
            contentValues.put("delaystart", new StringBuilder(String.valueOf(j)).toString());
            contentValues.put("delayend", new StringBuilder(String.valueOf(j2)).toString());
            DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static void a(String str, String str2, String str3, long j, int i, long j2, long j3) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NumberInfo.VERSION_KEY, str2);
            contentValues.put(Constant.URLS, str3);
            contentValues.put("status", Integer.valueOf(i));
            contentValues.put("update_time", new StringBuilder(String.valueOf(j)).toString());
            contentValues.put("delaystart", new StringBuilder(String.valueOf(j2)).toString());
            contentValues.put("delayend", new StringBuilder(String.valueOf(j3)).toString());
            DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static void a(Map<String, String> map, XyCallBack xyCallBack) {
        if (NetUtil.checkAccessNetWork((Map) map)) {
            k c = c();
            if (c != null) {
                if (!StringUtils.isNull(c.d) && c.f == 0) {
                    XyUtil.doXycallBack(xyCallBack, "1");
                    return;
                }
                a(c, xyCallBack, false, map);
            }
            return;
        }
        XyUtil.doXycallBack(xyCallBack, ThemeUtil.SET_NULL_STR);
    }

    public static k b(String str) {
        try {
            Document stringConvertXML = StringUtils.stringConvertXML(str, "");
            if (stringConvertXML == null) {
                return null;
            }
            long parseLong;
            k kVar = new k();
            String str2 = "";
            Element documentElement = stringConvertXML.getDocumentElement();
            NodeList elementsByTagName = documentElement.getElementsByTagName("PublicInfoVersion");
            if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                str2 = G.a(elementsByTagName.item(0));
                if (StringUtils.isNull(str2)) {
                    str2 = "";
                }
            }
            kVar.c = str2;
            str2 = "";
            elementsByTagName = documentElement.getElementsByTagName("downLoadUrl");
            if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                str2 = G.a(elementsByTagName.item(0));
                if (StringUtils.isNull(str2)) {
                    str2 = "";
                }
            }
            kVar.d = str2;
            NodeList elementsByTagName2 = documentElement.getElementsByTagName("delaystart");
            if (elementsByTagName2 != null && elementsByTagName2.getLength() > 0) {
                str2 = G.a(elementsByTagName2.item(0));
                if (!StringUtils.isNull(str2)) {
                    try {
                        parseLong = Long.parseLong(str2);
                    } catch (Throwable th) {
                    }
                    kVar.h = parseLong;
                    elementsByTagName2 = documentElement.getElementsByTagName("delayend");
                    if (elementsByTagName2 != null) {
                        if (elementsByTagName2.getLength() > 0) {
                            str2 = G.a(elementsByTagName2.item(0));
                            if (!StringUtils.isNull(str2)) {
                                try {
                                    parseLong = Long.parseLong(str2);
                                } catch (Throwable th2) {
                                }
                                if ((parseLong <= 0 ? 1 : null) == null) {
                                    parseLong = 86400000;
                                }
                                kVar.i = parseLong;
                                return kVar;
                            }
                        }
                    }
                    parseLong = 0;
                    if (parseLong <= 0) {
                    }
                    if ((parseLong <= 0 ? 1 : null) == null) {
                        parseLong = 86400000;
                    }
                    kVar.i = parseLong;
                    return kVar;
                }
            }
            parseLong = 0;
            kVar.h = parseLong;
            elementsByTagName2 = documentElement.getElementsByTagName("delayend");
            if (elementsByTagName2 != null) {
                if (elementsByTagName2.getLength() > 0) {
                    str2 = G.a(elementsByTagName2.item(0));
                    if (StringUtils.isNull(str2)) {
                        parseLong = Long.parseLong(str2);
                        if (parseLong <= 0) {
                        }
                        if ((parseLong <= 0 ? 1 : null) == null) {
                            parseLong = 86400000;
                        }
                        kVar.i = parseLong;
                        return kVar;
                    }
                }
            }
            parseLong = 0;
            if (parseLong <= 0) {
            }
            if ((parseLong <= 0 ? 1 : null) == null) {
                parseLong = 86400000;
            }
            kVar.i = parseLong;
            return kVar;
        } catch (Throwable th3) {
            return null;
        }
    }

    public static void b() {
        try {
            d("pubInfo");
            d("pubNum");
            d("pubMenu");
            ParseItemManager.updateNeiQianSql(Constant.getContext());
        } catch (Throwable th) {
        }
    }

    public static void b(k kVar) {
        Object obj = 1;
        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (kVar.f == 0) {
                if ((kVar.h > currentTimeMillis ? 1 : null) == null) {
                    if (kVar.i >= currentTimeMillis) {
                        obj = null;
                    }
                    if (obj == null) {
                        a(kVar);
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    private static void b(String str, int i) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put("count", Integer.valueOf(i + 1));
            DBManager.update("tb_menu_list", contentValues, "name = ? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    public static k c() {
        Throwable th;
        XyCursor xyCursor = null;
        XyCursor query;
        try {
            query = DBManager.query("tb_menu_list", new String[]{"id", "name", NumberInfo.VERSION_KEY, Constant.URLS, "status", "last_load_time", "update_time", "delaystart", "delayend", "count"}, null, null);
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        int columnIndex = query.getColumnIndex("id");
                        int columnIndex2 = query.getColumnIndex("name");
                        int columnIndex3 = query.getColumnIndex(NumberInfo.VERSION_KEY);
                        int columnIndex4 = query.getColumnIndex(Constant.URLS);
                        int columnIndex5 = query.getColumnIndex("status");
                        int columnIndex6 = query.getColumnIndex("last_load_time");
                        int columnIndex7 = query.getColumnIndex("update_time");
                        int columnIndex8 = query.getColumnIndex("delaystart");
                        int columnIndex9 = query.getColumnIndex("delayend");
                        int columnIndex10 = query.getColumnIndex("count");
                        if (query.moveToNext()) {
                            k kVar = new k();
                            query.getLong(columnIndex);
                            kVar.b = query.getString(columnIndex2);
                            kVar.c = query.getString(columnIndex3);
                            kVar.d = query.getString(columnIndex4);
                            kVar.f = query.getInt(columnIndex5);
                            query.getLong(columnIndex6);
                            kVar.e = query.getLong(columnIndex7);
                            kVar.h = query.getLong(columnIndex8);
                            kVar.i = query.getLong(columnIndex9);
                            kVar.j = query.getInt(columnIndex10);
                            XyCursor.closeCursor(query, true);
                            return kVar;
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = query;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(query, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void c(String str) {
        XyCursor xyCursor = null;
        synchronized (i.class) {
            try {
                xyCursor = DBManager.query("tb_menu_list", new String[]{Constant.URLS, NumberInfo.VERSION_KEY}, "name = ? ", new String[]{str});
                if (xyCursor != null) {
                    if (xyCursor.getCount() > 0) {
                        XyCursor.closeCursor(xyCursor, true);
                    }
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put("name", str);
                contentValues.put(NumberInfo.VERSION_KEY, ThemeUtil.SET_NULL_STR);
                DBManager.insert("tb_menu_list", contentValues);
                XyCursor.closeCursor(xyCursor, true);
            } catch (Throwable th) {
                Throwable th2 = th;
                XyCursor xyCursor2 = xyCursor;
                Throwable th3 = th2;
                XyCursor.closeCursor(xyCursor2, true);
                throw th3;
            }
        }
    }

    public static boolean c(k kVar) {
        if (kVar != null) {
            try {
                if (!StringUtils.isNull(kVar.d) && kVar.f == 0) {
                    return true;
                }
            } catch (Throwable th) {
            }
        }
        return false;
    }

    private static void d(k kVar) {
        boolean z = false;
        if (System.currentTimeMillis() <= kVar.e + DexUtil.getUpdateCycleByType(5, Constant.weekTime)) {
            z = true;
        }
        if (z) {
            if (SysParamEntityManager.getIntParam(Constant.getContext(), Constant.AUTO_UPDATE_DATA) == 0 && NetUtil.checkAccessNetWork(1)) {
                b(kVar);
            }
            return;
        }
        a(kVar, null, true, null);
    }

    private static void d(String str) {
        String str2 = "";
        if (!StringUtils.isNull(str)) {
            if (str.equalsIgnoreCase("pubInfo")) {
                str2 = "tb_public_info";
            } else if (str.equalsIgnoreCase("pubNum")) {
                str2 = "tb_public_num_info";
            } else if (str.equalsIgnoreCase("pubMenu")) {
                str2 = "tb_public_menu_info";
            }
            if (!StringUtils.isNull(str2)) {
                try {
                    DBManager.delete(str2, null, null);
                } catch (Throwable th) {
                }
            }
        }
    }
}
