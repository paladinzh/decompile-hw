package cn.com.xy.sms.sdk.db.entity;

import android.content.ContentValues;
import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.f;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class SysParamEntityManager {
    public static final String CREATE_TABLE = "create table  if not exists tb_sdk_param (id int primary key,p_key TEXT,p_value TEXT,pextend_value TEXT)";
    public static final String DROP_TABLE = " DROP TABLE IF EXISTS tb_sdk_param";
    public static final String ID = "id";
    public static final String PEXTENDVALUE = "pextend_value";
    public static final String PKEY = "p_key";
    public static final String PVALUE = "p_value";
    public static final String TABLE_NAME = "tb_sdk_param";
    public static HashMap<String, Object> cacheMap = new HashMap();

    public static void clearOldData(String str) {
        try {
            String queryValueParamKey = queryValueParamKey(Constant.getContext(), Constant.CHANNEL);
            if (!(StringUtils.isNull(queryValueParamKey) || StringUtils.isNull(str) || queryValueParamKey.equals(str))) {
                clearOldData(true);
            }
        } catch (Throwable th) {
        }
    }

    public static void clearOldData(boolean z) {
        if (z) {
            try {
                DBManager.delete("tb_scene_config", null, null);
            } catch (Throwable th) {
            }
            try {
                DBManager.delete("tb_scenerule_config", null, null);
            } catch (Throwable th2) {
            }
            try {
                DBManager.delete("tb_res_download", null, null);
            } catch (Throwable th3) {
            }
            try {
                DBManager.delete("tb_xml_res_download", null, null);
            } catch (Throwable th4) {
                return;
            }
            MatchCacheManager.deleteAll();
        }
        h.e();
        f.d(Constant.getPARSE_PATH());
        f.a(Constant.getContext().getDir("outdex", 0));
        ParseItemManager.deleteAll();
    }

    public static void deleteOldFile() {
        try {
            String stringBuilder = new StringBuilder(String.valueOf(Constant.getContext().getFilesDir().getPath())).append(File.separator).append("parse").append(File.separator).toString();
            if (f.a(stringBuilder)) {
                f.a(stringBuilder, "ParseUtilCasual_", ".jar", null);
                f.a(stringBuilder, "ParseUtilEC_", ".jar", null);
                f.a(stringBuilder, "ParseUtilFinanceL_", ".jar", null);
                f.a(stringBuilder, "ParseUtilFinanceM_", ".jar", null);
                f.a(stringBuilder, "ParseUtilFinanceS_", ".jar", null);
                f.a(stringBuilder, "ParseUtilLife_", ".jar", null);
                f.a(stringBuilder, "ParseUtilMove_", ".jar", null);
                f.a(stringBuilder, "ParseUtilTelecom_", ".jar", null);
                f.a(stringBuilder, "ParseUtilTravel_", ".jar", null);
                f.a(stringBuilder, "ParseUtilUnicom_", ".jar", null);
            }
            if (f.a(Constant.getContext().getDir("outdex", 0).getPath())) {
                f.b("ParseUtilCasual_", ".dex", null);
                f.b("ParseUtilEC_", ".dex", null);
                f.b("ParseUtilFinanceL_", ".dex", null);
                f.b("ParseUtilFinanceM_", ".dex", null);
                f.b("ParseUtilFinanceS_", ".dex", null);
                f.b("ParseUtilLife_", ".dex", null);
                f.b("ParseUtilMove_", ".dex", null);
                f.b("ParseUtilTelecom_", ".dex", null);
                f.b("ParseUtilTravel_", ".dex", null);
                f.b("ParseUtilUnicom_", ".dex", null);
            }
        } catch (Throwable th) {
        }
    }

    public static boolean getBooleanParam(Context context, String str) {
        return getBooleanParam(context, str, false);
    }

    public static boolean getBooleanParam(Context context, String str, boolean z) {
        boolean z2 = false;
        Object obj = cacheMap.get(str);
        if (obj != null) {
            try {
                return Boolean.parseBoolean(obj.toString());
            } catch (Throwable th) {
            }
        } else {
            String queryValueParamKey = queryValueParamKey(context, str);
            if (queryValueParamKey == null) {
                cacheMap.put(str, Boolean.valueOf(z));
                z2 = z;
            } else if (!"false".equalsIgnoreCase(queryValueParamKey)) {
                z2 = true;
            }
            cacheMap.put(str, Boolean.valueOf(z2));
            return z2;
        }
    }

    public static int getIntParam(Context context, String str) {
        Object obj = cacheMap.get(str);
        if (obj != null) {
            try {
                return Integer.parseInt((String) obj);
            } catch (Throwable th) {
            }
        } else {
            int intValue;
            String queryValueParamKey = queryValueParamKey(context, str);
            if (queryValueParamKey != null) {
                cacheMap.put(str, queryValueParamKey);
                intValue = Integer.valueOf(queryValueParamKey).intValue();
                return intValue;
            }
            intValue = -1;
            return intValue;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static long getLongParam(String str, long j, Context context) {
        XyCursor xyCursor = null;
        try {
            xyCursor = DBManager.query(TABLE_NAME, new String[]{PVALUE}, "p_key=?", new String[]{str});
            if (xyCursor != null) {
                if (xyCursor.moveToNext()) {
                    String string = xyCursor.getString(xyCursor.getColumnIndex(PVALUE));
                    if (!StringUtils.isNull(string)) {
                        long longValue = Long.valueOf(string).longValue();
                        XyCursor.closeCursor(xyCursor, true);
                        return longValue;
                    }
                    XyCursor.closeCursor(xyCursor, true);
                    return j;
                }
            }
            if (context != null) {
                setParam(str, new StringBuilder(String.valueOf(j)).toString());
                XyCursor.closeCursor(xyCursor, true);
                return j;
            }
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

    public static String getStringParam(Context context, String str) {
        Object obj = cacheMap.get(str);
        String str2;
        if (obj != null) {
            try {
                return (String) obj;
            } catch (Throwable th) {
                str2 = null;
            }
        } else {
            str2 = queryValueParamKey(context, str);
            if (str2 != null) {
                try {
                    cacheMap.put(str, str2);
                } catch (Throwable th2) {
                }
            }
            return str2;
        }
    }

    public static void initParams(Context context, String str, String str2, boolean z, boolean z2, Map<String, String> map) {
        clearOldData(str);
        String queryValueParamKey = queryValueParamKey(context, "smartsms_enhance");
        if (queryValueParamKey == null) {
            if (map != null) {
                queryValueParamKey = (String) map.get("smartsms_enhance");
            }
            if (queryValueParamKey == null) {
                queryValueParamKey = "true";
            }
            insertOrUpdateKeyValue(context, "smartsms_enhance", queryValueParamKey, null);
            cacheMap.put("smartsms_enhance", queryValueParamKey);
        }
        queryValueParamKey = queryValueParamKey(context, Constant.SUPPORT_NETWORK_TYPE);
        if (queryValueParamKey == null) {
            if (map != null) {
                queryValueParamKey = (String) map.get(Constant.SUPPORT_NETWORK_TYPE);
            }
            if (queryValueParamKey == null) {
                queryValueParamKey = "1";
            }
            insertOrUpdateKeyValue(context, Constant.SUPPORT_NETWORK_TYPE, queryValueParamKey, null);
            cacheMap.put(Constant.SUPPORT_NETWORK_TYPE, queryValueParamKey);
        }
        queryValueParamKey = queryValueParamKey(context, Constant.ONLINE_UPDATE_SDK_PERIOD);
        if (queryValueParamKey == null) {
            if (map != null) {
                queryValueParamKey = (String) map.get(Constant.ONLINE_UPDATE_SDK_PERIOD);
            }
            if (queryValueParamKey == null) {
                queryValueParamKey = "1";
            }
            insertOrUpdateKeyValue(context, Constant.ONLINE_UPDATE_SDK_PERIOD, queryValueParamKey, null);
            cacheMap.put(Constant.ONLINE_UPDATE_SDK_PERIOD, queryValueParamKey);
        }
        insertOrUpdateKeyValue(context, Constant.PRELOADENABLE, new StringBuilder(String.valueOf(z)).toString(), null);
        insertOrUpdateKeyValue(context, Constant.SMSLOCATEENABLE, new StringBuilder(String.valueOf(z2)).toString(), null);
        insertOrUpdateKeyValue(context, Constant.CHANNEL, new StringBuilder(String.valueOf(str)).toString(), null);
        cacheMap.put(Constant.PRELOADENABLE, Boolean.valueOf(z));
        cacheMap.put(Constant.SMSLOCATEENABLE, Boolean.valueOf(z2));
        cacheMap.put(Constant.CHANNEL, str);
        String str3 = null;
        queryValueParamKey = null;
        String str4 = null;
        String str5 = null;
        String str6 = null;
        String str7 = null;
        String str8 = null;
        String str9 = null;
        Object obj = null;
        String str10 = null;
        String str11 = null;
        String str12 = null;
        String str13 = null;
        String str14 = null;
        String str15 = null;
        String str16 = null;
        String str17 = null;
        if (map != null) {
            queryValueParamKey = (String) map.get("SIM_ICCID_2");
            str3 = (String) map.get("SMS_LOCATE_2");
            if (queryValueParamKey != null) {
                IccidInfoManager.insertIccid(queryValueParamKey, false, str3, "", "", "", context);
                i.a(new k(1, "simIccid", queryValueParamKey, "smsLocate", str3));
            }
            queryValueParamKey = (String) map.get(Constant.CUSTOM_LOCATION_SERVER_URL);
            str4 = (String) map.get(Constant.CUSTOM_PUBLIC_SERVER_URL);
            str3 = (String) map.get(Constant.CUSTOM_PUBINFO_SERVER_URL);
            str5 = (String) map.get(Constant.CUSTOM_SDK_SERVER_URL);
            str6 = (String) map.get(Constant.ONLINE_UPDATE_SDK);
            str15 = (String) map.get(Constant.QUERY_ONLINE);
            str7 = (String) map.get(Constant.SMS_LOCATE);
            str8 = (String) map.get(Constant.RECOGNIZE_LEVEL);
            str9 = (String) map.get(Constant.OPEN_POPUP_DRAG);
            String str18 = (String) map.get(Constant.AUTO_UPDATE_DATA);
            cacheMap.put(Constant.SECRETKEY, (String) map.get(Constant.SECRETKEY));
            str14 = (String) map.get(Constant.POPUP_BG_TYPE);
            str11 = (String) map.get(Constant.SCENE_CENSUS_ONLINE);
            str10 = (String) map.get(Constant.CUSTOM_SDK_RES_DOWNLAOD_URL);
            str12 = (String) map.get(Constant.SUPPORT_NETWORK_TYPE_MAJOR);
            str13 = (String) map.get(Constant.ONLINE_UPDATE_RES_PERIOD);
            str16 = (String) map.get(Constant.REPARSE_BUBBLE_CYCLE);
            str17 = (String) map.get(Constant.COMPARE_PUBNUM_OPERATOR);
            cacheMap.put(Constant.RSAPRVKEY, (String) map.get(Constant.RSAPRVKEY));
            String str19 = str10;
            str10 = str14;
            str14 = str11;
            str11 = str18;
            str18 = str9;
            str9 = str8;
            str8 = str15;
            str15 = str19;
            String str20 = str6;
            str6 = str5;
            str5 = str3;
            str3 = str7;
            str7 = str20;
        }
        if (queryValueParamKey == null) {
            queryValueParamKey = "";
        }
        insertOrUpdateKeyValue(context, Constant.CUSTOM_LOCATION_SERVER_URL, queryValueParamKey, null);
        cacheMap.put(Constant.CUSTOM_LOCATION_SERVER_URL, queryValueParamKey);
        queryValueParamKey = str4 != null ? str4 : "";
        if (!StringUtils.isNull(queryValueParamKey)) {
            if (NetUtil.isUseHttps()) {
                NetUtil.PUBINFO_SERVER_URL_HTTPS = queryValueParamKey;
            } else {
                NetUtil.serverUrl2 = queryValueParamKey;
            }
        }
        str4 = str5 != null ? str5 : "";
        if (!StringUtils.isNull(str4)) {
            NetUtil.publicInfoServerUrl = str4;
        }
        insertOrUpdateKeyValue(context, Constant.CUSTOM_PUBINFO_SERVER_URL, str4, null);
        cacheMap.put(Constant.CUSTOM_PUBINFO_SERVER_URL, str4);
        insertOrUpdateKeyValue(context, Constant.CUSTOM_PUBLIC_SERVER_URL, queryValueParamKey, null);
        cacheMap.put(Constant.CUSTOM_PUBLIC_SERVER_URL, queryValueParamKey);
        queryValueParamKey = str6 != null ? str6 : "";
        insertOrUpdateKeyValue(context, Constant.CUSTOM_SDK_SERVER_URL, queryValueParamKey, null);
        cacheMap.put(Constant.CUSTOM_SDK_SERVER_URL, queryValueParamKey);
        if (!StringUtils.isNull(queryValueParamKey)) {
            if (NetUtil.isUseHttps()) {
                NetUtil.POPUP_SERVER_URL_HTTPS = queryValueParamKey;
            } else {
                NetUtil.serverUrl = queryValueParamKey;
            }
        }
        queryValueParamKey = str15 != null ? str15 : "";
        insertOrUpdateKeyValue(context, Constant.CUSTOM_SDK_RES_DOWNLAOD_URL, queryValueParamKey, null);
        cacheMap.put(Constant.CUSTOM_SDK_RES_DOWNLAOD_URL, queryValueParamKey);
        if (!StringUtils.isNull(queryValueParamKey)) {
            NetUtil.prex = queryValueParamKey;
        }
        queryValueParamKey = str7 != null ? str7 : "1";
        insertOrUpdateKeyValue(context, Constant.ONLINE_UPDATE_SDK, queryValueParamKey, null);
        cacheMap.put(Constant.ONLINE_UPDATE_SDK, queryValueParamKey);
        queryValueParamKey = str8 != null ? str8 : "1";
        insertOrUpdateKeyValue(context, Constant.QUERY_ONLINE, queryValueParamKey, null);
        cacheMap.put(Constant.QUERY_ONLINE, queryValueParamKey);
        queryValueParamKey = str14 != null ? str14 : "0";
        insertOrUpdateKeyValue(context, Constant.SCENE_CENSUS_ONLINE, queryValueParamKey, null);
        cacheMap.put(Constant.SCENE_CENSUS_ONLINE, queryValueParamKey);
        queryValueParamKey = str9 != null ? str9 : NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR;
        insertOrUpdateKeyValue(context, Constant.RECOGNIZE_LEVEL, queryValueParamKey, null);
        cacheMap.put(Constant.RECOGNIZE_LEVEL, queryValueParamKey);
        cacheMap.put(Constant.OPEN_POPUP_DRAG, obj != null ? obj : "0");
        queryValueParamKey = str11 != null ? str11 : "0";
        insertOrUpdateKeyValue(context, Constant.AUTO_UPDATE_DATA, queryValueParamKey, null);
        cacheMap.put(Constant.AUTO_UPDATE_DATA, queryValueParamKey);
        queryValueParamKey = str10 != null ? str10 : "1";
        insertOrUpdateKeyValue(context, Constant.POPUP_BG_TYPE, queryValueParamKey, null);
        cacheMap.put(Constant.POPUP_BG_TYPE, queryValueParamKey);
        queryValueParamKey = str12 != null ? str12 : "2";
        insertOrUpdateKeyValue(context, Constant.SUPPORT_NETWORK_TYPE_MAJOR, queryValueParamKey, null);
        cacheMap.put(Constant.SUPPORT_NETWORK_TYPE_MAJOR, queryValueParamKey);
        queryValueParamKey = str13 != null ? str13 : "2";
        insertOrUpdateKeyValue(context, Constant.ONLINE_UPDATE_RES_PERIOD, queryValueParamKey, null);
        cacheMap.put(Constant.ONLINE_UPDATE_RES_PERIOD, queryValueParamKey);
        queryValueParamKey = str16 != null ? str16 : ThemeUtil.SET_NULL_STR;
        insertOrUpdateKeyValue(context, Constant.REPARSE_BUBBLE_CYCLE, queryValueParamKey, null);
        cacheMap.put(Constant.REPARSE_BUBBLE_CYCLE, queryValueParamKey);
        queryValueParamKey = str17 != null ? str17 : "true";
        insertOrUpdateKeyValue(context, Constant.COMPARE_PUBNUM_OPERATOR, queryValueParamKey, null);
        cacheMap.put(Constant.COMPARE_PUBNUM_OPERATOR, queryValueParamKey);
        if (!StringUtils.isNull(str2)) {
            IccidInfoManager.insertIccid(str2, true, str3, "", "", "", context);
            i.a(new k(1, "simIccid", str2, "smsLocate", str3));
        }
        insertOrUpdateKeyValue(context, Constant.APPVERSION, NetUtil.APPVERSION, null);
        if (map != null) {
            queryValueParamKey = (String) map.get(Constant.CUSTOM_LOGO_DOWN_URL);
            if (!StringUtils.isNull(queryValueParamKey)) {
                NetUtil.BIZPORT_DOWN_URL = queryValueParamKey;
            }
        }
        if (map != null) {
            queryValueParamKey = (String) map.get(Constant.CONFIG_NOTIFY_TIMEMS);
            if (StringUtils.isNull(queryValueParamKey)) {
                queryValueParamKey = "600000";
            }
            insertOrUpdateKeyValue(context, Constant.CONFIG_NOTIFY_TIMEMS, queryValueParamKey, null);
            cacheMap.put(Constant.CONFIG_NOTIFY_TIMEMS, queryValueParamKey);
        }
    }

    public static long insertOrUpdateKeyValue(Context context, String str, String str2, String str3) {
        if (StringUtils.isNull(str2) && StringUtils.isNull(str3)) {
            return -1;
        }
        int insert;
        String queryValueParamKey = queryValueParamKey(context, str);
        ContentValues contentValues = new ContentValues();
        contentValues.put(PKEY, str);
        contentValues.put(PVALUE, str2);
        contentValues.put(PEXTENDVALUE, str3);
        if (queryValueParamKey == null) {
            insert = (int) DBManager.insert(TABLE_NAME, contentValues);
        } else {
            insert = DBManager.update(TABLE_NAME, contentValues, "p_key=?", new String[]{str});
        }
        return (long) insert;
    }

    public static Map<String, String> queryKeyValue(String str, String[] strArr, int i) {
        XyCursor query;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            query = DBManager.query(false, TABLE_NAME, new String[]{PKEY, PVALUE}, str, strArr, null, null, null, String.valueOf(i));
            if (query != null) {
                try {
                    if (query.getCount() != 0) {
                        Map<String, String> hashMap = new HashMap();
                        while (query.moveToNext()) {
                            hashMap.put(query.getString(query.getColumnIndex(PKEY)), query.getString(query.getColumnIndex(PVALUE)));
                        }
                        XyCursor.closeCursor(query, true);
                        return hashMap;
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

    public static Map<String, String> queryKeyValueByPextend(String str) {
        return queryKeyValue("pextend_value=?", new String[]{str}, Integer.MAX_VALUE);
    }

    public static String queryValueParamKey(Context context, String str) {
        Map queryKeyValue = queryKeyValue("p_key=?", new String[]{str}, 1);
        return (queryKeyValue != null && queryKeyValue.containsKey(str)) ? (String) queryKeyValue.get(str) : null;
    }

    public static long setParam(String str, String str2) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(PKEY, str);
            contentValues.put(PVALUE, str2);
            int update = DBManager.update(TABLE_NAME, contentValues, "p_key=?", new String[]{str});
            cacheMap.put(str, str2);
            if (update <= 0) {
                return DBManager.insert(TABLE_NAME, contentValues);
            }
        } catch (Throwable th) {
        }
        return 0;
    }
}
