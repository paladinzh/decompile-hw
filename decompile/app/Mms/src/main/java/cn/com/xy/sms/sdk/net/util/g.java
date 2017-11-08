package cn.com.xy.sms.sdk.net.util;

import android.database.sqlite.SQLiteDatabase;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.util.SceneconfigUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.Iterator;

/* compiled from: Unknown */
public final class g {
    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void a(e eVar) {
        SQLiteDatabase sQLiteDatabase = null;
        if (eVar != null) {
            try {
                String str;
                String b;
                switch (eVar.a()) {
                    case 0:
                        return;
                    case 1:
                        return;
                    case 2:
                        return;
                    case 3:
                        return;
                    case 4:
                        return;
                    case 5:
                        f.e();
                        IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
                        String[] c = eVar.c();
                        if (queryDeftIccidInfo != null) {
                            if (c == null || c.length <= 0) {
                                cn.com.xy.sms.sdk.service.e.g.b(queryDeftIccidInfo.areaCode, queryDeftIccidInfo.iccid);
                                break;
                            }
                            str = queryDeftIccidInfo.areaCode;
                            String str2 = queryDeftIccidInfo.iccid;
                            cn.com.xy.sms.sdk.service.e.g.a(c);
                            break;
                        } else if (c != null && c.length > 0) {
                            cn.com.xy.sms.sdk.service.e.g.a(c);
                            break;
                        } else {
                            cn.com.xy.sms.sdk.service.e.g.b("", "");
                            return;
                        }
                        break;
                    case 6:
                        SceneconfigUtil.updateData();
                        return;
                    case 7:
                        return;
                    case 8:
                        return;
                    case 9:
                        return;
                    case 10:
                        String[] c2 = eVar.c();
                        if (c2 != null && c2.length > 0) {
                            for (String b2 : c2) {
                                if (!StringUtils.isNull(b2)) {
                                    MatchCacheManager.deleteMatchCache(b2, System.currentTimeMillis());
                                }
                            }
                            return;
                        }
                    case 11:
                        try {
                            b2 = eVar.b();
                            if (!StringUtils.isNull(b2)) {
                                str = b2.toLowerCase();
                                if (str.indexOf("update ") < 0) {
                                    if (str.indexOf("drop ") < 0) {
                                        sQLiteDatabase = DBManager.getSQLiteDatabase();
                                        sQLiteDatabase.execSQL(b2);
                                        DBManager.close(sQLiteDatabase);
                                        break;
                                    }
                                }
                                DBManager.close(null);
                                break;
                            }
                            DBManager.close(null);
                            break;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            SQLiteDatabase sQLiteDatabase2 = sQLiteDatabase;
                            Throwable th3 = th2;
                            DBManager.close(sQLiteDatabase2);
                            throw th3;
                        }
                }
            } catch (Throwable th4) {
            }
        }
    }

    public static void a(String str) {
        try {
            Iterator it = CommandAnalyzer.a(str).iterator();
        } catch (Throwable th) {
        }
        while (it.hasNext()) {
            e eVar = (e) it.next();
            if (eVar != null) {
                switch (eVar.a()) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 7:
                    case 8:
                    case 9:
                        break;
                    case 5:
                        f.e();
                        IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
                        String[] c = eVar.c();
                        if (queryDeftIccidInfo != null) {
                            if (c == null || c.length <= 0) {
                                cn.com.xy.sms.sdk.service.e.g.b(queryDeftIccidInfo.areaCode, queryDeftIccidInfo.iccid);
                                break;
                            }
                            String str2 = queryDeftIccidInfo.areaCode;
                            String str3 = queryDeftIccidInfo.iccid;
                            cn.com.xy.sms.sdk.service.e.g.a(c);
                            break;
                        } else if (c == null || c.length <= 0) {
                            cn.com.xy.sms.sdk.service.e.g.b("", "");
                            break;
                        } else {
                            cn.com.xy.sms.sdk.service.e.g.a(c);
                            break;
                        }
                        break;
                    case 6:
                        SceneconfigUtil.updateData();
                        break;
                    case 10:
                        String[] c2 = eVar.c();
                        if (c2 != null && c2.length > 0) {
                            for (String str4 : c2) {
                                if (!StringUtils.isNull(str4)) {
                                    MatchCacheManager.deleteMatchCache(str4, System.currentTimeMillis());
                                }
                            }
                            break;
                        }
                    case 11:
                        d(eVar);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static void b(e eVar) {
        f.e();
        IccidInfo queryDeftIccidInfo = IccidInfoManager.queryDeftIccidInfo(Constant.getContext());
        String[] c = eVar.c();
        if (queryDeftIccidInfo == null) {
            if (c != null && c.length > 0) {
                cn.com.xy.sms.sdk.service.e.g.a(c);
            } else {
                cn.com.xy.sms.sdk.service.e.g.b("", "");
            }
        } else if (c != null && c.length > 0) {
            String str = queryDeftIccidInfo.areaCode;
            String str2 = queryDeftIccidInfo.iccid;
            cn.com.xy.sms.sdk.service.e.g.a(c);
        } else {
            cn.com.xy.sms.sdk.service.e.g.b(queryDeftIccidInfo.areaCode, queryDeftIccidInfo.iccid);
        }
    }

    private static void c(e eVar) {
        String[] c = eVar.c();
        if (c != null && c.length > 0) {
            for (String str : c) {
                if (!StringUtils.isNull(str)) {
                    MatchCacheManager.deleteMatchCache(str, System.currentTimeMillis());
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void d(e eVar) {
        SQLiteDatabase sQLiteDatabase = null;
        try {
            String b = eVar.b();
            if (StringUtils.isNull(b)) {
                DBManager.close(sQLiteDatabase);
                return;
            }
            String toLowerCase = b.toLowerCase();
            if (toLowerCase.indexOf("update ") < 0) {
                if (toLowerCase.indexOf("drop ") < 0) {
                    sQLiteDatabase = DBManager.getSQLiteDatabase();
                    sQLiteDatabase.execSQL(b);
                    DBManager.close(sQLiteDatabase);
                    return;
                }
            }
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
