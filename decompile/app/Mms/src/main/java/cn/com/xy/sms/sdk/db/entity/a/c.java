package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.C;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.l;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/* compiled from: Unknown */
public final class c {
    private static String A = "ALTER TABLE tb_num_name ADD COLUMN ec TEXT ";
    private static String B = "ALTER TABLE tb_num_name ADD COLUMN mark_ec INTEGER DEFAULT 0";
    private static String C = "ALTER TABLE tb_num_name ADD COLUMN last_ec_time INTEGER DEFAULT 0";
    private static String a = ";&XY_PIX&;";
    private static String b = "id";
    private static String c = "num";
    private static String d = "name";
    private static String e = "cnum";
    private static String f = "cmd";
    private static String g = "mark_time";
    private static String h = "ec";
    private static String i = "mark_ec";
    private static String j = "last_ec_time";
    private static String k = "mark_cmd";
    private static String l = "last_name_pubid";
    private static String m = "last_name_time";
    private static String n = "last_cmd_time";
    private static String o = "last_query_time";
    private static String p = "ResetPubId";
    private static final String q = "tb_num_name";
    private static String r = " DROP TABLE IF EXISTS tb_num_name";
    private static String s = "ALTER TABLE tb_num_name ADD COLUMN cnum TEXT ";
    private static String t = "ALTER TABLE tb_num_name ADD COLUMN mark_time LONG DEFAULT 0";
    private static String u = "ALTER TABLE tb_num_name ADD COLUMN cmd TEXT ";
    private static String v = "ALTER TABLE tb_num_name ADD COLUMN mark_cmd INTEGER DEFAULT 0";
    private static String w = "ALTER TABLE tb_num_name ADD COLUMN last_name_pubid INTEGER DEFAULT 0";
    private static String x = "ALTER TABLE tb_num_name ADD COLUMN last_name_time INTEGER DEFAULT 0";
    private static String y = "ALTER TABLE tb_num_name ADD COLUMN last_cmd_time INTEGER DEFAULT 0";
    private static String z = "ALTER TABLE tb_num_name ADD COLUMN last_query_time INTEGER DEFAULT 0";

    private static long a(String str, String str2, String str3, int i, long j) {
        if (StringUtils.isNull(str) || StringUtils.isNull(str2)) {
            return -1;
        }
        try {
            b a = a(str, false);
            String str4;
            if (a == null) {
                try {
                    if (!StringUtils.isNull(str)) {
                        str4 = " phone_num = ? ";
                        if (!StringUtils.isNull(null)) {
                            str4 = new StringBuilder(String.valueOf(str4)).append(" and area_code = '").append(null).append("'").toString();
                        }
                        DBManager.delete("tb_netquery_time", str4, new String[]{str});
                    }
                } catch (Throwable th) {
                }
                return a(IccidInfoManager.NUM, str, "name", str2, "last_name_time", String.valueOf(j), IccidInfoManager.CNUM, str3, "mark_time", "1", "last_name_pubid", ThemeUtil.SET_NULL_STR);
            }
            String[] a2 = a(a.c, a.i, str2, j, ";");
            if (a2 == null) {
                return -1;
            }
            String str5 = a2[0];
            String str6 = a2[1];
            String valueOf = String.valueOf(a.k);
            str4 = String.valueOf(a.h);
            if (!(StringUtils.isNull(str5) || str5.equals(a.c))) {
                valueOf = "0";
            }
            if (a2.length == 3 && "ResetPubId".equals(a2[2])) {
                str4 = ThemeUtil.SET_NULL_STR;
            }
            return a(str, "name", str5, "last_name_time", str6, "last_query_time", valueOf, "last_name_pubid", str4, IccidInfoManager.CNUM, str3, "mark_time", "1");
        } catch (Throwable th2) {
            return -1;
        }
    }

    private static long a(String str, String... strArr) {
        return (long) DBManager.update(q, BaseManager.getContentValues(null, strArr), "num = ? ", new String[]{str});
    }

    private static long a(String... strArr) {
        return DBManager.insert(q, BaseManager.getContentValues(null, strArr));
    }

    public static b a(String str) {
        return b() ? a(str, true) : null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static b a(String str, boolean z) {
        try {
            StringBuffer stringBuffer = new StringBuffer(IccidInfoManager.NUM);
            stringBuffer.append(" = ? ");
            if (z) {
                stringBuffer.append(" AND (");
                stringBuffer.append("mark_time");
                stringBuffer.append(" = 1 OR ");
                stringBuffer.append("mark_cmd");
                stringBuffer.append(" = 1 OR ");
                stringBuffer.append("mark_ec");
                stringBuffer.append(" = 1) ");
            }
            List a = a(stringBuffer.toString(), new String[]{str}, 1);
            if (a != null) {
                if (a.size() > 0) {
                    b bVar = (b) a.get(0);
                    XyCursor.closeCursor(null, true);
                    return bVar;
                }
            }
            XyCursor.closeCursor(null, true);
            return null;
        } catch (Throwable th) {
            XyCursor.closeCursor(null, true);
        }
    }

    public static String a() {
        return "CREATE TABLE  IF NOT EXISTS tb_num_name (id INTEGER PRIMARY KEY, num TEXT NOT NULL UNIQUE, name TEXT NOT NULL,cmd TEXT , ec TEXT , cnum TEXT,mark_time LONG DEFAULT 0,mark_cmd INTEGER DEFAULT 0,mark_ec INTEGER DEFAULT 0,last_name_time INTEGER DEFAULT 0,last_name_pubid INTEGER DEFAULT 0,last_cmd_time INTEGER DEFAULT 0,last_ec_time INTEGER DEFAULT 0,last_query_time INTEGER DEFAULT 0)";
    }

    private static String a(b bVar) {
        return (bVar == null || StringUtils.isNull(bVar.c)) ? null : b(bVar.c.split(";"));
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String a(String str, String str2, String[] strArr) {
        XyCursor xyCursor = null;
        if (StringUtils.isNull(str)) {
            return "";
        }
        try {
            xyCursor = DBManager.query(q, new String[]{str}, str2, strArr);
            if (xyCursor != null) {
                if (xyCursor.moveToFirst()) {
                    String string = xyCursor.getString(0);
                    XyCursor.closeCursor(xyCursor, true);
                    return string;
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
        return "";
    }

    private static String a(List<String> list, String str, String str2) {
        if (list == null || StringUtils.isNull(str)) {
            return "";
        }
        int size = list.size();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            String str3 = (String) list.get(i);
            if (!str3.equals(str)) {
                if (stringBuilder.length() > 0 && !StringUtils.isNull(str2)) {
                    stringBuilder.append(str2);
                }
                stringBuilder.append(str3);
            }
        }
        return stringBuilder.toString();
    }

    public static List<b> a(String str, String[] strArr, int i) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        if (i <= 0) {
            return null;
        }
        try {
            query = DBManager.query(q, new String[]{"id", IccidInfoManager.NUM, "name", "cmd", "ec", IccidInfoManager.CNUM, "mark_time", "mark_cmd", "mark_ec", "last_name_pubid", "last_name_time", "last_cmd_time", "last_ec_time", "last_query_time"}, str, strArr, null, null, null, String.valueOf(i));
            if (query != null) {
                try {
                    if (query.getCount() > 0) {
                        List<b> arrayList = new ArrayList();
                        while (query.moveToNext()) {
                            b bVar = new b();
                            query.getInt(query.getColumnIndex("id"));
                            bVar.b = query.getString(query.getColumnIndex(IccidInfoManager.NUM));
                            bVar.c = query.getString(query.getColumnIndex("name"));
                            bVar.e = query.getString(query.getColumnIndex("cmd"));
                            bVar.d = query.getString(query.getColumnIndex(IccidInfoManager.CNUM));
                            bVar.f = query.getLong(query.getColumnIndex("mark_time"));
                            bVar.g = query.getInt(query.getColumnIndex("mark_cmd"));
                            bVar.h = query.getInt(query.getColumnIndex("last_name_pubid"));
                            bVar.i = query.getLong(query.getColumnIndex("last_name_time"));
                            bVar.j = query.getLong(query.getColumnIndex("last_cmd_time"));
                            bVar.k = query.getLong(query.getColumnIndex("last_query_time"));
                            bVar.l = query.getString(query.getColumnIndex("ec"));
                            bVar.m = query.getInt(query.getColumnIndex("mark_ec"));
                            bVar.n = query.getLong(query.getColumnIndex("last_ec_time"));
                            arrayList.add(bVar);
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

    public static List<b> a(List<String> list) {
        if (!b() || list == null || list.size() <= 0) {
            return null;
        }
        int size = list.size();
        StringBuffer stringBuffer = new StringBuffer(IccidInfoManager.NUM);
        stringBuffer.append(" IN(");
        stringBuffer.append(C.a(size));
        stringBuffer.append(")");
        if (!StringUtils.isNull(null)) {
            stringBuffer.append(null);
        }
        return a(stringBuffer.toString(), (String[]) list.toArray(new String[size]), Integer.MAX_VALUE);
    }

    private static List<b> a(List<String> list, String str) {
        if (list == null || list.size() <= 0) {
            return null;
        }
        int size = list.size();
        StringBuffer stringBuffer = new StringBuffer(IccidInfoManager.NUM);
        stringBuffer.append(" IN(");
        stringBuffer.append(C.a(size));
        stringBuffer.append(")");
        if (!StringUtils.isNull(null)) {
            stringBuffer.append(null);
        }
        return a(stringBuffer.toString(), (String[]) list.toArray(new String[size]), Integer.MAX_VALUE);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(int i, String str, String[] strArr, int i2) {
        boolean b = b();
        String valueOf = String.valueOf(i);
        String[] strArr2 = null;
        switch (i2) {
            case 1:
                if (b) {
                    strArr2 = new String[2];
                    strArr2[0] = "mark_time";
                }
                break;
            case 2:
                strArr2 = new String[2];
                strArr2[0] = "mark_cmd";
                strArr2[1] = valueOf;
                break;
            case 3:
                strArr2 = new String[2];
                strArr2[0] = "mark_ec";
                strArr2[1] = valueOf;
                break;
            default:
                if (!b) {
                    strArr2 = new String[]{"mark_cmd", valueOf, "mark_ec", valueOf};
                    break;
                } else {
                    strArr2 = new String[]{"mark_time", valueOf, "mark_cmd", valueOf, "mark_ec", valueOf};
                    break;
                }
        }
        strArr2[1] = valueOf;
        if (strArr2 != null) {
            a(str, strArr, strArr2);
        }
    }

    public static void a(String str, int i) {
        a("num = ?", new String[]{str}, "last_name_pubid", String.valueOf(i));
    }

    public static void a(String str, int i, int i2) {
        if (!StringUtils.isNull(str)) {
            a(0, "num = ?", new String[]{str}, 0);
        }
    }

    private static void a(String str, long j) {
        Object obj = 1;
        long currentTimeMillis = System.currentTimeMillis() - j;
        if ((currentTimeMillis < 0 ? 1 : null) == null) {
            if (currentTimeMillis < Constant.MINUTE) {
                obj = null;
            }
            if (obj == null) {
                a.b.execute(new d(str));
            }
        }
    }

    public static void a(String str, long j, int i) {
        a("num = ?", new String[]{str}, "last_query_time", String.valueOf(j), "mark_time", "1");
    }

    private static void a(String str, String[] strArr, String... strArr2) {
        if (strArr2 != null) {
            try {
                if (strArr2.length != 0) {
                    DBManager.update(q, BaseManager.getContentValues(null, strArr2), str, strArr);
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(HashMap<String, String> hashMap) {
        try {
            String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) hashMap.get(IccidInfoManager.NUM));
            String str = (String) hashMap.get("msg");
            String str2 = (String) hashMap.get(IccidInfoManager.CNUM);
            long parseLong = Long.parseLong((String) hashMap.get("smsTime"));
            String[] corpAndEc = DexUtil.getCorpAndEc(str);
            str = corpAndEc[0];
            String str3 = corpAndEc[1];
            a(phoneNumberNo86, str, str2, 1, parseLong);
            if (StringUtils.allValuesIsNotNull(phoneNumberNo86, str3)) {
                a(phoneNumberNo86, str2, "ec", str3, "mark_ec", "1", "last_ec_time", parseLong, ";");
            }
            long currentTimeMillis = System.currentTimeMillis() - parseLong;
            if ((currentTimeMillis < 0 ? 1 : null) == null) {
                if ((currentTimeMillis >= Constant.MINUTE ? 1 : null) == null) {
                    a.b.execute(new d(phoneNumberNo86));
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void a(List<String> list, int i, int i2) {
        if (!list.isEmpty()) {
            a(0, "num IN (" + C.a(list.size()) + ")", (String[]) list.toArray(new String[list.size()]), 0);
        }
    }

    public static boolean a(long j) {
        return !(((System.currentTimeMillis() - j) > DexUtil.getUpdateCycleByType(34, 86400000) ? 1 : ((System.currentTimeMillis() - j) == DexUtil.getUpdateCycleByType(34, 86400000) ? 0 : -1)) <= 0);
    }

    private static boolean a(String str, String str2, String str3, long j) {
        if (!StringUtils.allValuesIsNotNull(str, str2)) {
            return false;
        }
        return a(str, str3, "ec", str2, "mark_ec", "1", "last_ec_time", j, ";");
    }

    private static boolean a(String str, String str2, String str3, String str4, String str5, String str6, String str7, long j, String str8) {
        if (!StringUtils.allValuesIsNotNull(str, str3, str4, str5, str6, str7, str8)) {
            return false;
        }
        try {
            b a = a(str, false);
            if (a == null) {
                return ((a(IccidInfoManager.NUM, str, str3, str4, IccidInfoManager.CNUM, str2, str5, str6, str7, String.valueOf(j), "name", "") > 0 ? 1 : (a(IccidInfoManager.NUM, str, str3, str4, IccidInfoManager.CNUM, str2, str5, str6, str7, String.valueOf(j), "name", "") == 0 ? 0 : -1)) <= 0 ? 1 : null) == null;
            } else {
                String str9;
                long j2;
                if ("cmd".equals(str3)) {
                    str9 = a.e;
                    j2 = a.j;
                } else {
                    str9 = a.l;
                    j2 = a.n;
                }
                String[] a2 = a(str9, j2, str4, j, str8);
                if (a2 == null) {
                    return false;
                }
                String str10 = a2[0];
                str9 = a2[1];
                return ((a(str, str3, str10, str7, str9, IccidInfoManager.CNUM, str2, str5, str6) > 0 ? 1 : (a(str, str3, str10, str7, str9, IccidInfoManager.CNUM, str2, str5, str6) == 0 ? 0 : -1)) <= 0 ? 1 : null) == null;
            }
        } catch (Throwable th) {
            return false;
        }
    }

    private static String[] a(String str, long j, String str2, long j2, String str3) {
        if (StringUtils.isNull(str)) {
            return new String[]{str2, String.valueOf(j2)};
        } else if (StringUtils.isNull(str2)) {
            return new String[]{str, String.valueOf(j)};
        } else if (str2.equals(str) && j2 != j) {
            return new String[]{str2, String.valueOf(j2)};
        } else {
            List asList = Arrays.asList(str.split(str3));
            int size = asList.size();
            Object obj = ((j2 > j ? 1 : (j2 == j ? 0 : -1)) < 0 ? 1 : null) == null ? 1 : null;
            Object obj2 = size >= 5 ? null : 1;
            if (obj == null) {
                if (obj2 == null || asList.contains(str2)) {
                    return null;
                }
            }
            if (obj == null || !str2.equals(asList.get(size - 1))) {
                String[] strArr;
                if (obj == null) {
                    strArr = new String[]{new StringBuilder(String.valueOf(str2)).append(str3).append(str).toString(), String.valueOf(j)};
                } else {
                    int i;
                    String str4;
                    boolean contains = asList.contains(str2);
                    if (obj2 == null && !contains) {
                        strArr = new String[3];
                        strArr[0] = str2;
                        strArr[1] = String.valueOf(j2);
                        i = 2;
                        str4 = "ResetPubId";
                    } else {
                        if (contains) {
                            String str5;
                            if (asList == null || StringUtils.isNull(str2)) {
                                str5 = "";
                            } else {
                                size = asList.size();
                                StringBuilder stringBuilder = new StringBuilder();
                                for (i = 0; i < size; i++) {
                                    str5 = (String) asList.get(i);
                                    if (!str5.equals(str2)) {
                                        if (stringBuilder.length() > 0 && !StringUtils.isNull(str3)) {
                                            stringBuilder.append(str3);
                                        }
                                        stringBuilder.append(str5);
                                    }
                                }
                                str5 = stringBuilder.toString();
                            }
                            str = str5;
                        }
                        strArr = new String[3];
                        strArr[0] = new StringBuilder(String.valueOf(r6)).append(str3).append(str2).toString();
                        strArr[1] = String.valueOf(j2);
                        i = 2;
                        str4 = "ResetPubId";
                    }
                    strArr[i] = str4;
                }
                return strArr;
            } else if (j2 == j) {
                return null;
            } else {
                return new String[]{str, String.valueOf(j2)};
            }
        }
    }

    public static String b(String str) {
        String a = !StringUtils.isNull(str) ? a("name", " num = ? ", new String[]{str}) : "";
        return !StringUtils.isNull(a) ? b(a.split(";")) : null;
    }

    private static String b(String str, boolean z) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        return a("name", " num = ? ", new String[]{str});
    }

    private static String b(String[] strArr) {
        return (strArr == null || strArr.length == 0) ? null : strArr[strArr.length - 1];
    }

    public static void b(HashMap<String, String> hashMap) {
        try {
            String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) hashMap.get(IccidInfoManager.NUM));
            String cmd = DexUtil.getCmd(phoneNumberNo86, (String) hashMap.get("msg"));
            if (!StringUtils.isNull(cmd)) {
                String str = (String) hashMap.get(IccidInfoManager.CNUM);
                long parseLong = Long.parseLong((String) hashMap.get("smsTime"));
                if (StringUtils.allValuesIsNotNull(phoneNumberNo86, cmd.trim())) {
                    a(phoneNumberNo86, str, "cmd", cmd.trim(), "mark_cmd", "1", "last_cmd_time", parseLong, ";&XY_PIX&;");
                }
            }
        } catch (Exception e) {
        }
    }

    public static boolean b() {
        boolean z = false;
        String[] strArr = new String[]{"HUAWEICARD"};
        for (int i = 0; i <= 0; i++) {
            if (strArr[0].equals(l.b)) {
                break;
            }
        }
        z = true;
        return SysParamEntityManager.getBooleanParam(Constant.getContext(), "num_name_power", z);
    }

    private static boolean b(String str, String str2, String str3, int i, long j) {
        if (!StringUtils.allValuesIsNotNull(str, str2)) {
            return false;
        }
        return a(str, str3, "cmd", str2, "mark_cmd", "1", "last_cmd_time", j, ";&XY_PIX&;");
    }

    private static String c(String str, boolean z) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        return a("cmd", !z ? " num = ? " : " num = ? AND mark_cmd = 1 ", new String[]{str});
    }

    public static void c(HashMap<String, String> hashMap) {
        try {
            if ("SAMCLASSFIYVwIDAQAB".equals(KeyManager.channel)) {
                a.b.execute(new e(hashMap));
            }
        } catch (Throwable th) {
        }
    }
}
