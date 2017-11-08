package cn.com.xy.sms.sdk.db.entity.a;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.XyCursor;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.m;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;
import com.google.android.gms.common.Scopes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class f {
    private static String a = "tb_public_info";
    private static String b = "tb_public_menu_info";
    private static String c = "tb_public_num_info";
    private static String d = " DROP TABLE IF EXISTS tb_public_info";
    private static String e = " DROP TABLE IF EXISTS tb_public_menu_info";
    private static String f = " DROP TABLE IF EXISTS tb_public_num_info";
    private static String g = "ALTER TABLE tb_public_info ADD COLUMN classifyCode TEXT";
    private static String h = " ALTER TABLE tb_public_info ADD COLUMN corpLevel INTEGER default 0";
    private static String i = " ALTER TABLE tb_public_num_info ADD COLUMN lastloadtime LONG default 0";
    private static String j = " ALTER TABLE tb_public_num_info ADD COLUMN isrulenum INTEGER default 0";
    private static String k = " ALTER TABLE tb_public_num_info ADD COLUMN isuse LONG default 0";
    private static String l = " ALTER TABLE tb_public_num_info ADD COLUMN nameType INTEGER default 0";
    private static String m = "ALTER TABLE tb_public_info ADD COLUMN rid TEXT";
    private static String n = " ALTER TABLE tb_public_info ADD COLUMN logoType TEXT";
    private static String o = "ALTER TABLE tb_public_info ADD COLUMN scale INTEGER default 0";
    private static String p = "ALTER TABLE tb_public_info ADD COLUMN backColor TEXT";
    private static String q = "ALTER TABLE tb_public_info ADD COLUMN backColorEnd TEXT";
    private static Handler r = new Handler(Looper.getMainLooper());
    private static int s = 1;
    private static String t = "queryTraffic";
    private static String u = "queryCharge";
    private static String v = "selectSimCard";

    public static int a(String str, String str2) {
        try {
            if (StringUtils.isNull(str2)) {
                str2 = "CN";
            }
            int c = c(str, str2);
            if (c == -1 && !"CN".equalsIgnoreCase(str2)) {
                c = c(str, "CN");
            }
            return c;
        } catch (Throwable th) {
            return -1;
        }
    }

    private static int a(HashMap<String, String> hashMap) {
        return hashMap != null ? i((String) hashMap.get("extend")) : -1;
    }

    private static int a(String[] strArr) {
        if (strArr == null || strArr.length == 0) {
            return -2;
        }
        try {
            if (strArr.length > 4 && !StringUtils.isNull(strArr[4])) {
                try {
                    int parseInt = Integer.parseInt(strArr[4]);
                    if (parseInt != -2) {
                        return parseInt;
                    }
                } catch (Throwable th) {
                }
            }
            if (strArr.length > 2) {
                if (!StringUtils.isNull(strArr[2])) {
                    try {
                        return Integer.parseInt(strArr[2]);
                    } catch (Throwable th2) {
                    }
                }
            }
        } catch (Throwable th3) {
        }
        return -2;
    }

    public static String a() {
        return " create table  if not exists tb_public_info (id INTEGER PRIMARY KEY, pubId INTEGER not null unique, pubName TEXT not null, pubType TEXT, classifyCode TEXT, weiXin TEXT, weiBoName TEXT, weiBoUrl TEXT, introduce TEXT, address TEXT, faxNum TEXT, webSite TEXT, moveWebSite TEXT, versionCode TEXT, email TEXT, parentPubId int, slogan TEXT, rectLogoName TEXT, circleLogoName TEXT, extend TEXT, hasmenu int, loadMenuTime long, updateInfoTime long default 0, corpLevel INTEGER default 0, rid TEXT, logoType  TEXT, scale INTEGER default 0, backColor TEXT, backColorEnd TEXT)";
    }

    private static String a(int i, int i2, String str) {
        if (StringUtils.isNull(str) || "CN".equalsIgnoreCase(str)) {
            return "CN";
        }
        if (i2 < 0) {
            return "CN";
        }
        IccidInfo queryIccidInfo = IccidInfoManager.queryIccidInfo(null, i2);
        if (queryIccidInfo == null) {
            return "CN";
        }
        String valueOf = String.valueOf(i);
        String str2 = queryIccidInfo.userAreacode;
        String operatorNum = IccidLocationUtil.getOperatorNum(null, queryIccidInfo.userOperator);
        if (StringUtils.isNull(str2)) {
            str2 = queryIccidInfo.areaCode;
            String operatorNum2 = IccidLocationUtil.getOperatorNum(null, queryIccidInfo.operator);
            if (str.equalsIgnoreCase(str2) && valueOf.equals(operatorNum2)) {
                return str;
            }
        } else if (str.equalsIgnoreCase(str2) && valueOf.equals(operatorNum)) {
            return str;
        }
        return "CN";
    }

    private static String a(int i, String str, String str2) {
        int a = a(IccidLocationUtil.getIccidInfoArr(str));
        if (a == -2) {
            a = IccidLocationUtil.getOperatorByICCID(str);
        }
        return (a != -2 && a == i) ? str2 : "CN";
    }

    private static String a(int i, String str, String str2, int i2) {
        if ("CN".equalsIgnoreCase(str2)) {
            return str2;
        }
        try {
            String str3;
            String str4;
            if (StringUtils.isNull(str) && i2 < 0) {
                HashMap iccidAreaCodeMap = IccidLocationUtil.getIccidAreaCodeMap();
                if (iccidAreaCodeMap == null || iccidAreaCodeMap.isEmpty()) {
                    return "CN";
                }
                for (Entry entry : iccidAreaCodeMap.entrySet()) {
                    String[] strArr = (String[]) entry.getValue();
                    int a = a(strArr);
                    if (a != -2 && a == i) {
                        str3 = strArr[3];
                        if (!StringUtils.isNull(str3)) {
                            return new StringBuilder(String.valueOf(str3)).append("_").append((String) entry.getKey()).toString();
                        }
                        str4 = strArr[0];
                        if (!StringUtils.isNull(str4)) {
                            return new StringBuilder(String.valueOf(str4)).append("_").append((String) entry.getKey()).toString();
                        }
                    }
                }
                return "CN";
            } else if (!StringUtils.isNull(str) || i2 < 0) {
                int a2 = a(IccidLocationUtil.getIccidInfoArr(str));
                if (a2 == -2) {
                    a2 = IccidLocationUtil.getOperatorByICCID(str);
                }
                return (a2 != -2 && a2 == i) ? str2 : "CN";
            } else if (StringUtils.isNull(str2) || "CN".equalsIgnoreCase(str2)) {
                return "CN";
            } else {
                if (i2 < 0) {
                    return "CN";
                }
                IccidInfo queryIccidInfo = IccidInfoManager.queryIccidInfo(null, i2);
                if (queryIccidInfo == null) {
                    return "CN";
                }
                str4 = String.valueOf(i);
                String str5 = queryIccidInfo.userAreacode;
                str3 = IccidLocationUtil.getOperatorNum(null, queryIccidInfo.userOperator);
                if (StringUtils.isNull(str5)) {
                    str5 = queryIccidInfo.areaCode;
                    String operatorNum = IccidLocationUtil.getOperatorNum(null, queryIccidInfo.operator);
                    if (str2.equalsIgnoreCase(str5) && str4.equals(operatorNum)) {
                        return str2;
                    }
                } else if (str2.equalsIgnoreCase(str5) && str4.equals(str3)) {
                    return str2;
                }
                return "CN";
            }
        } catch (Throwable th) {
            return "CN";
        }
    }

    public static String a(String str, Map<String, String> map) {
        JSONArray jSONArray;
        if (StringUtils.isNull(str) || !str.contains("action_data") || map == null || map.isEmpty()) {
            return null;
        }
        JSONArray jSONArray2;
        try {
            jSONArray2 = new JSONArray(str);
            try {
                JSONObject jSONObject = jSONArray2.getJSONObject(0);
                jSONObject.put("name", "业务办理");
                JSONArray jSONArray3 = jSONObject.getJSONArray("secondmenu");
                Iterator it = map.entrySet().iterator();
                if (it.hasNext()) {
                    JSONObject jSONObject2 = new JSONObject((String) ((Entry) it.next()).getValue());
                    if (jSONObject2.optJSONObject("queryTraffic") != null) {
                        jSONArray3.put(JsonUtil.getJsonObject("name", jSONObject2.optJSONObject("queryTraffic").getString("name"), NumberInfo.TYPE_KEY, "selectSimCard", "actionType", "queryTraffic"));
                    }
                    if (jSONObject2.optJSONObject("queryCharge") != null) {
                        jSONArray3.put(JsonUtil.getJsonObject("name", jSONObject2.optJSONObject("queryCharge").getString("name"), NumberInfo.TYPE_KEY, "selectSimCard", "actionType", "queryCharge"));
                    }
                }
            } catch (Throwable th) {
                jSONArray = jSONArray2;
                jSONArray2 = jSONArray;
                return jSONArray2 != null ? null : jSONArray2.toString();
            }
        } catch (Throwable th2) {
            Object obj = null;
            jSONArray2 = jSONArray;
            if (jSONArray2 != null) {
            }
        }
        if (jSONArray2 != null) {
        }
    }

    public static String a(String str, JSONObject jSONObject) {
        if (StringUtils.isNull(str)) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        if (!str.startsWith("WEB_")) {
            str = str.toLowerCase();
        }
        if ("reply_sms".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"send_code\":\"" + jSONObject.optString("sms") + "\",");
            stringBuffer.append("\"phone\":\"" + jSONObject.optString("sendTo") + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("send_sms".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"send_code\":\"" + jSONObject.optString("sms") + "\",");
            stringBuffer.append("\"phone\":\"" + jSONObject.optString("sendTo") + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("access_url".equalsIgnoreCase(str) || "open_url".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"url\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("down_url".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"url\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("download".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"url\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"appName\":\"" + jSONObject.optString("appName") + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"extend\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("weibo_url".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"url\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("call_phone".equalsIgnoreCase(str) || "call".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"phoneNum\":\"" + jSONObject.optString("phoneNum") + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("map_site".equalsIgnoreCase(str) || "open_map".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"address\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("open_map_list".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"address\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("repayment".equalsIgnoreCase(str) || "zfb_repayment".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"appName\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"appDownUrl\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("recharge".equalsIgnoreCase(str) || "zfb_recharge".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"sp\":\"" + jSONObject.optString("sp") + "\",");
            stringBuffer.append("\"appName\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"appDownUrl\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("open_app".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"appName\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"appDownUrl\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("open_app_url".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"appName\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"appDownUrl\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else if ("WEB_TRAFFIC_ORDER".equalsIgnoreCase(str)) {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"sp\":\"" + jSONObject.optString("sp") + "\",");
            stringBuffer.append("\"appName\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"appDownUrl\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        } else {
            stringBuffer.append("{");
            stringBuffer.append("\"type\":\"" + str + "\",");
            stringBuffer.append("\"extend\":\"" + jSONObject.optString("extend") + "\",");
            stringBuffer.append("\"url\":\"" + jSONObject.optString(Constant.URLS) + "\",");
            stringBuffer.append("\"menuName\":\"" + jSONObject.optString("menuName") + "\",");
            stringBuffer.append("\"publicId\":\"" + jSONObject.optString("pubId") + "\",");
            stringBuffer.append("\"extendVal\":\"" + jSONObject.optString("extendVal") + "\"");
            stringBuffer.append("}");
        }
        return StringUtils.encode(stringBuffer.toString());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HashMap<String, String> a(SQLiteDatabase sQLiteDatabase, String str, String str2, String str3, int i) {
        int i2;
        Cursor cursor;
        Object obj;
        HashMap<String, String> hashMap;
        Throwable th;
        String str4;
        Object obj2;
        HashMap<String, String> hashMap2;
        String str5;
        int i3 = -1;
        String str6 = null;
        String str7 = null;
        int i4 = 0;
        Object obj3 = null;
        HashMap<String, String> hashMap3 = null;
        Cursor rawQuery;
        String substring;
        try {
            int indexOf;
            rawQuery = sQLiteDatabase.rawQuery(str2, null);
            if (rawQuery != null) {
                try {
                    if (rawQuery.getCount() > 0) {
                        while (rawQuery.moveToNext()) {
                            int i5;
                            int i6;
                            String string;
                            String string2;
                            try {
                                i2 = rawQuery.getInt(rawQuery.getColumnIndex("minLen"));
                                i5 = rawQuery.getInt(rawQuery.getColumnIndex("maxLen"));
                                i6 = rawQuery.getInt(rawQuery.getColumnIndex("len"));
                                string = rawQuery.getString(rawQuery.getColumnIndex("ntype"));
                                string2 = rawQuery.getString(rawQuery.getColumnIndex(IccidInfoManager.NUM));
                            } catch (Throwable th2) {
                                th = th2;
                            }
                            HashMap<String, String> hashMap4;
                            try {
                                String string3 = rawQuery.getString(rawQuery.getColumnIndex("areaCode"));
                                indexOf = string2.indexOf("*");
                                if (indexOf >= 0) {
                                    String substring2 = string2.substring(0, indexOf);
                                    substring = string2.substring(indexOf + 1);
                                    if (str.startsWith(substring2) && str.endsWith(substring)) {
                                        indexOf = substring.length() + substring2.length();
                                        int length = str.length();
                                        if (i6 <= 0) {
                                            if (i5 > 0) {
                                                if (length <= i5 + indexOf) {
                                                }
                                            }
                                            if (i2 > 0) {
                                                if (length >= indexOf + i2) {
                                                }
                                            }
                                        } else if (indexOf + i6 == length) {
                                        }
                                        if ("sj".equals(string)) {
                                            if (StringUtils.sj(str)) {
                                            }
                                        }
                                        obj3 = 1;
                                        if (obj3 != null) {
                                            if (string3.indexOf(str3) == -1) {
                                                indexOf = rawQuery.getInt(rawQuery.getColumnIndex("pubId"));
                                                str6 = rawQuery.getString(rawQuery.getColumnIndex("purpose"));
                                                str7 = rawQuery.getString(rawQuery.getColumnIndex("extend"));
                                                i4 = rawQuery.getInt(rawQuery.getColumnIndex("nameType"));
                                                a(indexOf, new StringBuilder(String.valueOf(str3)).append(";").toString(), str, i, str6, str7, i4);
                                                hashMap4 = new HashMap();
                                                hashMap4.put("pubId", String.valueOf(indexOf));
                                                hashMap4.put("purpose", str6);
                                                hashMap4.put("areaCode", string3);
                                                hashMap4.put("extend", str7);
                                                hashMap4.put("nameType", String.valueOf(i4));
                                                hashMap4.put(IccidInfoManager.NUM, string2);
                                                DBManager.closeCursor(rawQuery);
                                                return hashMap4;
                                            } else if (string3.indexOf("CN") != -1) {
                                                indexOf = rawQuery.getInt(rawQuery.getColumnIndex("pubId"));
                                                str6 = rawQuery.getString(rawQuery.getColumnIndex("purpose"));
                                                str7 = rawQuery.getString(rawQuery.getColumnIndex("extend"));
                                                i4 = rawQuery.getInt(rawQuery.getColumnIndex("nameType"));
                                                i3 = indexOf;
                                                obj3 = string2;
                                            }
                                        }
                                        obj3 = string2;
                                    }
                                }
                                obj3 = null;
                                if (obj3 != null) {
                                    if (string3.indexOf(str3) == -1) {
                                        indexOf = rawQuery.getInt(rawQuery.getColumnIndex("pubId"));
                                        str6 = rawQuery.getString(rawQuery.getColumnIndex("purpose"));
                                        str7 = rawQuery.getString(rawQuery.getColumnIndex("extend"));
                                        i4 = rawQuery.getInt(rawQuery.getColumnIndex("nameType"));
                                        a(indexOf, new StringBuilder(String.valueOf(str3)).append(";").toString(), str, i, str6, str7, i4);
                                        hashMap4 = new HashMap();
                                        hashMap4.put("pubId", String.valueOf(indexOf));
                                        hashMap4.put("purpose", str6);
                                        hashMap4.put("areaCode", string3);
                                        hashMap4.put("extend", str7);
                                        hashMap4.put("nameType", String.valueOf(i4));
                                        hashMap4.put(IccidInfoManager.NUM, string2);
                                        DBManager.closeCursor(rawQuery);
                                        return hashMap4;
                                    } else if (string3.indexOf("CN") != -1) {
                                        indexOf = rawQuery.getInt(rawQuery.getColumnIndex("pubId"));
                                        str6 = rawQuery.getString(rawQuery.getColumnIndex("purpose"));
                                        str7 = rawQuery.getString(rawQuery.getColumnIndex("extend"));
                                        i4 = rawQuery.getInt(rawQuery.getColumnIndex("nameType"));
                                        i3 = indexOf;
                                        obj3 = string2;
                                    }
                                }
                                obj3 = string2;
                            } catch (Throwable th22) {
                                th = th22;
                            }
                        }
                        obj2 = obj3;
                        indexOf = i3;
                        if (indexOf > 0) {
                            a(indexOf, "CN;", str, i, str6, str7, i4);
                        }
                        DBManager.closeCursor(rawQuery);
                        str4 = str7;
                        i2 = indexOf;
                        obj3 = str6;
                        if (i2 == -1) {
                            return hashMap3;
                        }
                        hashMap2 = new HashMap();
                        hashMap2.put("pubId", String.valueOf(i2));
                        str5 = "purpose";
                        if (obj3 == null) {
                            obj3 = "";
                        }
                        hashMap2.put(str5, obj3);
                        hashMap2.put("areaCode", "CN");
                        if (str4 != null) {
                        }
                        hashMap2.put("extend", str4 != null ? "" : str4);
                        hashMap2.put("nameType", String.valueOf(i4));
                        hashMap2.put(IccidInfoManager.NUM, obj2);
                        return hashMap2;
                    }
                } catch (Throwable th222) {
                    th = th222;
                }
            }
            obj2 = null;
            indexOf = -1;
            if (indexOf > 0) {
                a(indexOf, "CN;", str, i, str6, str7, i4);
            }
            DBManager.closeCursor(rawQuery);
            str4 = str7;
            i2 = indexOf;
            obj3 = str6;
        } catch (Throwable th3) {
            th = th3;
            rawQuery = null;
            DBManager.closeCursor(rawQuery);
            throw th;
        }
        if (i2 == -1) {
            return hashMap3;
        }
        hashMap2 = new HashMap();
        hashMap2.put("pubId", String.valueOf(i2));
        str5 = "purpose";
        if (obj3 == null) {
            obj3 = "";
        }
        hashMap2.put(str5, obj3);
        hashMap2.put("areaCode", "CN");
        if (str4 != null) {
        }
        hashMap2.put("extend", str4 != null ? "" : str4);
        hashMap2.put("nameType", String.valueOf(i4));
        hashMap2.put(IccidInfoManager.NUM, obj2);
        return hashMap2;
    }

    public static HashMap<String, JSONObject> a(Set<String> set) {
        XyCursor rawQuery;
        Throwable th;
        if (set == null || set.isEmpty()) {
            return null;
        }
        HashMap<String, JSONObject> hashMap = new HashMap();
        XyCursor xyCursor = null;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            for (String valueOf : set) {
                stringBuffer.append(new StringBuilder(String.valueOf(valueOf)).append(",").toString());
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            rawQuery = DBManager.rawQuery("SELECT pubId,pubName,rectLogoName,classifyCode,circleLogoName,logoType,scale, backColor, backColorEnd from tb_public_info where pubId in  (" + stringBuffer.toString() + ")", null);
            if (rawQuery != null) {
                try {
                    if (rawQuery.getCount() > 0) {
                        int columnIndex = rawQuery.getColumnIndex("pubId");
                        int columnIndex2 = rawQuery.getColumnIndex("pubName");
                        int columnIndex3 = rawQuery.getColumnIndex("rectLogoName");
                        int columnIndex4 = rawQuery.getColumnIndex("circleLogoName");
                        int columnIndex5 = rawQuery.getColumnIndex("classifyCode");
                        int columnIndex6 = rawQuery.getColumnIndex("logoType");
                        int columnIndex7 = rawQuery.getColumnIndex("scale");
                        int columnIndex8 = rawQuery.getColumnIndex("backColor");
                        int columnIndex9 = rawQuery.getColumnIndex("backColorEnd");
                        while (rawQuery.moveToNext()) {
                            JSONObject jSONObject = new JSONObject();
                            String valueOf2 = String.valueOf(rawQuery.getInt(columnIndex));
                            jSONObject.put("id", valueOf2);
                            jSONObject.put("name", rawQuery.getString(columnIndex2));
                            jSONObject.put("classifyCode", rawQuery.getString(columnIndex5));
                            jSONObject.put(NumberInfo.LOGO_KEY, rawQuery.getString(columnIndex3));
                            jSONObject.put("logoc", rawQuery.getString(columnIndex4));
                            jSONObject.put("logoType", rawQuery.getString(columnIndex6));
                            jSONObject.put("scale", rawQuery.getString(columnIndex7));
                            jSONObject.put("backColor", rawQuery.getString(columnIndex8));
                            jSONObject.put("backColorEnd", rawQuery.getString(columnIndex9));
                            hashMap.put(valueOf2, jSONObject);
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = rawQuery;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(rawQuery, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return hashMap;
    }

    private static HashMap<String, String[]> a(Set<String> set, int i) {
        XyCursor rawQuery;
        Throwable th;
        if (set == null || set.isEmpty()) {
            return null;
        }
        HashMap<String, String[]> hashMap = new HashMap();
        XyCursor xyCursor = null;
        try {
            StringBuffer stringBuffer = new StringBuffer();
            for (String str : set) {
                if (StringUtils.isNumber(str)) {
                    stringBuffer.append(new StringBuilder(String.valueOf(str)).append(",").toString());
                }
            }
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
            Set hashSet = new HashSet();
            String str2 = IccidLocationUtil.getAreaCodeByDefaultCard();
            if (StringUtils.isNull(str2)) {
                str2 = "CN";
            }
            String str3 = str2;
            rawQuery = DBManager.rawQuery("SELECT pubId, num, purpose, areaCode from tb_public_num_info where num in  (" + stringBuffer.toString() + ") and ptype = 1", null);
            if (rawQuery != null) {
                try {
                    if (rawQuery.getCount() > 0) {
                        int columnIndex = rawQuery.getColumnIndex("pubId");
                        int columnIndex2 = rawQuery.getColumnIndex(IccidInfoManager.NUM);
                        int columnIndex3 = rawQuery.getColumnIndex("purpose");
                        int columnIndex4 = rawQuery.getColumnIndex("areaCode");
                        while (rawQuery.moveToNext()) {
                            String string = rawQuery.getString(columnIndex2);
                            String string2 = rawQuery.getString(columnIndex);
                            String string3 = rawQuery.getString(columnIndex3);
                            String string4 = rawQuery.getString(columnIndex4);
                            if (!hashSet.contains(string) || string4.contains(str3)) {
                                hashMap.remove(string);
                                hashMap.put(string, new String[]{string2, string3});
                                hashSet.add(string);
                            }
                        }
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = rawQuery;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(rawQuery, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return hashMap;
    }

    private static List<String> a(XyCursor xyCursor) {
        if (xyCursor == null || xyCursor.getCount() == 0) {
            return null;
        }
        try {
            List<String> arrayList = new ArrayList();
            while (xyCursor.moveToNext()) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put(IccidInfoManager.NUM, xyCursor.getString(xyCursor.getColumnIndex(IccidInfoManager.NUM)));
                jSONObject.put(NumberInfo.VERSION_KEY, xyCursor.getString(xyCursor.getColumnIndex("versionCode")));
                jSONObject.put("pubId", xyCursor.getString(xyCursor.getColumnIndex("pubId")));
                jSONObject.put("name", xyCursor.getString(xyCursor.getColumnIndex("name")));
                jSONObject.put("cmd", xyCursor.getString(xyCursor.getColumnIndex("cmd")));
                jSONObject.put("ec", xyCursor.getString(xyCursor.getColumnIndex("ec")));
                jSONObject.put("nameType", xyCursor.getInt(xyCursor.getColumnIndex("nameType")));
                jSONObject.put("markTime", xyCursor.getInt(xyCursor.getColumnIndex("mark_time")));
                jSONObject.put("markCmd", xyCursor.getInt(xyCursor.getColumnIndex("mark_cmd")));
                jSONObject.put("markEC", xyCursor.getInt(xyCursor.getColumnIndex("mark_ec")));
                arrayList.add(jSONObject.toString());
            }
            return arrayList;
        } catch (Exception e) {
            return null;
        }
    }

    public static JSONArray a(String str, String str2, int i, boolean z, String str3, String str4, int i2, SdkCallBack sdkCallBack) {
        if (z && "CN".equals(str2)) {
            return null;
        }
        try {
            HashMap c = c(str, str2, i);
            if (c == null) {
                a(str2, str3, str, str4, i, false, sdkCallBack);
                return null;
            } else if (!b(new JSONObject(c))) {
                return null;
            } else {
                String a;
                HashMap c2;
                String str5;
                JSONObject a2;
                String str6;
                String str7;
                JSONArray a3;
                String str8 = (String) c.get("areaCode");
                if ("CN".equals(str8) && !c.containsKey("ruleMatch")) {
                    if (!StringUtils.isNull(str2)) {
                        a(str2, str3, str, str4, i, true, new g(sdkCallBack, c));
                    }
                    if (z) {
                        return null;
                    }
                }
                boolean equals = "true".equals(SysParamEntityManager.getStringParam(Constant.getContext(), Constant.COMPARE_PUBNUM_OPERATOR));
                if (!((IccidLocationUtil.getIccidAreaCodeMap().size() <= 0 ? null : 1) == null || !equals || "CN".equals(str8))) {
                    int i3 = c != null ? i((String) c.get("extend")) : -1;
                    if (i3 != -1) {
                        a = a(i3, str3, str2, i2);
                        if (a != null && a.startsWith("CN")) {
                            c2 = c(str, a, i);
                            if (c2 != null && c2.containsKey("pubId")) {
                                str5 = null;
                                a = (String) c2.get("pubId");
                                a2 = a(Integer.parseInt(a));
                                str6 = "";
                                str7 = "";
                                if (a2 != null) {
                                    str7 = a2.optString("versionCode");
                                    str6 = a2.optString("rid");
                                }
                                a3 = a(a, str, str7, str6);
                                if (a3 != null) {
                                    a = new StringBuilder(String.valueOf(a)).append("_").append(str7).toString();
                                    i.a(new k(12, ParseItemManager.STATE, "16"));
                                    i.a(new k(11, "phoneNum", String.valueOf(str), "companyNum", a, "functionMode", Constant.MENU_SHOW_TIMES));
                                    if (!StringUtils.isNull(str5) && a3.length() > 0) {
                                        a3.getJSONObject(0).put(IccidInfoManager.ICCID, str5);
                                    }
                                }
                                return a3;
                            }
                            a("CN", str3, str, str4, i, false, sdkCallBack);
                            return null;
                        } else if (a != null && a.contains("_")) {
                            String[] split = a.split("_");
                            if (split.length > 1) {
                                str5 = split[1];
                                c2 = c;
                                a = (String) c2.get("pubId");
                                a2 = a(Integer.parseInt(a));
                                str6 = "";
                                str7 = "";
                                if (a2 != null) {
                                    str7 = a2.optString("versionCode");
                                    str6 = a2.optString("rid");
                                }
                                a3 = a(a, str, str7, str6);
                                if (a3 != null) {
                                    a = new StringBuilder(String.valueOf(a)).append("_").append(str7).toString();
                                    i.a(new k(12, ParseItemManager.STATE, "16"));
                                    i.a(new k(11, "phoneNum", String.valueOf(str), "companyNum", a, "functionMode", Constant.MENU_SHOW_TIMES));
                                    a3.getJSONObject(0).put(IccidInfoManager.ICCID, str5);
                                }
                                return a3;
                            }
                        }
                    }
                }
                c2 = c;
                str5 = null;
                a = (String) c2.get("pubId");
                a2 = a(Integer.parseInt(a));
                str6 = "";
                str7 = "";
                if (a2 != null) {
                    str7 = a2.optString("versionCode");
                    str6 = a2.optString("rid");
                }
                a3 = a(a, str, str7, str6);
                if (a3 != null) {
                    a = new StringBuilder(String.valueOf(a)).append("_").append(str7).toString();
                    i.a(new k(12, ParseItemManager.STATE, "16"));
                    i.a(new k(11, "phoneNum", String.valueOf(str), "companyNum", a, "functionMode", Constant.MENU_SHOW_TIMES));
                    a3.getJSONObject(0).put(IccidInfoManager.ICCID, str5);
                }
                return a3;
            }
        } catch (Throwable th) {
            return null;
        }
    }

    private static JSONArray a(String str, String str2, String str3, String str4) {
        XyCursor xyCursor;
        Throwable th;
        String stringBuilder = new StringBuilder(String.valueOf(str)).append("_").append(str3).toString();
        XyCursor query;
        try {
            query = DBManager.query("tb_public_menu_info", new String[]{"menuCode", "menuName", "menuType", "actionData"}, "pubId = ? ", new String[]{String.valueOf(str)}, null, null, " length(menuCode) ", null);
            if (query != null) {
                try {
                    HashMap hashMap = new HashMap();
                    String str5 = "name";
                    String str6 = "menuCode";
                    String str7 = NumberInfo.TYPE_KEY;
                    String str8 = "action_data";
                    String str9 = "secondmenu";
                    JSONArray jSONArray = new JSONArray();
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("uiType", "MENU");
                    jSONObject.put("phoneNum", str2);
                    jSONObject.put("companyNum", stringBuilder);
                    jSONObject.put("rid", str4);
                    HashSet hashSet = new HashSet();
                    while (query.moveToNext()) {
                        String string = query.getString(0);
                        if (!hashSet.contains(string)) {
                            hashSet.add(string);
                            String string2 = query.getString(1);
                            String string3 = query.getString(2);
                            String string4 = query.getString(3);
                            jSONObject.put(str6, string);
                            if (string4 != null) {
                                jSONObject.put("menu_item_action_data", string4);
                                string4 = jSONObject.toString();
                            }
                            if (string.length() == 2) {
                                Object jsonObject;
                                if ("menu".equalsIgnoreCase(string3)) {
                                    jsonObject = JsonUtil.getJsonObject(str5, string2, str7, string3);
                                    jsonObject.put(str9, new JSONArray());
                                    hashMap.put(string, jsonObject);
                                } else {
                                    jsonObject = JsonUtil.getJsonObject(str5, string2, str7, string3, str8, string4);
                                }
                                if (jsonObject != null) {
                                    jSONArray.put(jsonObject);
                                }
                            } else if (string.length() == 4) {
                                ((JSONObject) hashMap.get(string.substring(0, 2))).optJSONArray(str9).put(JsonUtil.getJsonObject(str5, string2, str7, string3, str8, string4));
                            }
                        }
                    }
                    hashMap.clear();
                    hashSet.clear();
                    XyCursor.closeCursor(query, true);
                    return jSONArray;
                } catch (Throwable th2) {
                    th = th2;
                }
            } else {
                XyCursor.closeCursor(query, true);
                return null;
            }
        } catch (Throwable th3) {
            th = th3;
            query = null;
            XyCursor.closeCursor(query, true);
            throw th;
        }
    }

    private static JSONObject a(int i) {
        XyCursor query;
        JSONObject loadSingleDataFromCursor;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            String[] strArr = new String[]{"id", "pubId", "pubName", "pubType", "classifyCode", "weiXin", "weiBoName", "weiBoUrl", "introduce", "address", "faxNum", "webSite", "versionCode", Scopes.EMAIL, "parentPubId", "slogan", "rectLogoName", "circleLogoName", "extend", "hasmenu", "loadMenuTime", "updateInfoTime", "moveWebSite", "corpLevel", "rid", "logoType", "scale", "backColor", "backColorEnd"};
            query = DBManager.query("tb_public_info", strArr, "pubId = ? ", new String[]{new StringBuilder(String.valueOf(i)).toString()});
            try {
                loadSingleDataFromCursor = BaseManager.loadSingleDataFromCursor(strArr, query);
                XyCursor.closeCursor(query, true);
            } catch (Throwable th2) {
                Throwable th3 = th2;
                xyCursor = query;
                th = th3;
                XyCursor.closeCursor(xyCursor, true);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return loadSingleDataFromCursor;
    }

    public static JSONObject a(String str, String str2, int i) {
        JSONObject jSONObject;
        try {
            HashMap c = c(str, str2, i);
            if (c == null) {
                return null;
            }
            int intValue = Integer.valueOf((String) c.get("pubId")).intValue();
            if (intValue == -1) {
                jSONObject = null;
            } else {
                jSONObject = a(intValue);
                if (jSONObject == null) {
                    return null;
                }
                try {
                    jSONObject.put("purpose", c.get("purpose"));
                    jSONObject.put("rid", c.get("rid"));
                    jSONObject.put("logoType", c.get("logoType"));
                    jSONObject.put("extend", c.get("extend"));
                    jSONObject.put("nameType", c.get("nameType"));
                    jSONObject.put(IccidInfoManager.NUM, str);
                    if (!(StringUtils.isNull(jSONObject.optString("pubName")) || jSONObject == null)) {
                        try {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("loadMenuTime", Long.valueOf(System.currentTimeMillis()));
                            DBManager.update("tb_public_info", contentValues, "pubId = ?", new String[]{jSONObject.optString("pubId")});
                        } catch (Throwable th) {
                        }
                    }
                } catch (Throwable th2) {
                }
            }
            return jSONObject;
        } catch (Throwable th3) {
            jSONObject = null;
        }
    }

    private static void a(int i, String str, String str2, int i2, String str3, String str4, int i3) {
        NetUtil.executeRunnable(new i(i, str, str2, i2, str3, str4, i3));
    }

    public static void a(String str) {
        try {
            if (!StringUtils.isNull(str)) {
                List arrayList = new ArrayList();
                arrayList.add(str);
                a(arrayList);
            }
        } catch (Throwable th) {
        }
    }

    private static void a(String str, String str2, String str3, String str4, int i, boolean z, SdkCallBack sdkCallBack) {
        try {
            a.b.execute(new h(str3, str4, str, str2, String.valueOf(i), sdkCallBack, z));
        } catch (Throwable th) {
        }
    }

    public static void a(List<String> list) {
        if (list != null) {
            try {
                if (list.size() > 0) {
                    Map hashMap = new HashMap();
                    hashMap.put("nums", list);
                    DuoquUtils.getSdkDoAction().onEventCallback(10, hashMap);
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(JSONObject jSONObject) {
        if (jSONObject != null) {
            String optString = jSONObject.optString("corpLevel");
            String[] strArr = new String[56];
            strArr[0] = "pubId";
            strArr[1] = jSONObject.optString("pubId");
            strArr[2] = "pubName";
            strArr[3] = jSONObject.optString("pubName");
            strArr[4] = "pubType";
            strArr[5] = jSONObject.optString("pubType");
            strArr[6] = "classifyCode";
            strArr[7] = jSONObject.optString("classifyCode");
            strArr[8] = "weiXin";
            strArr[9] = jSONObject.optString("weiXin");
            strArr[10] = "weiBoName";
            strArr[11] = jSONObject.optString("weiBoName");
            strArr[12] = "weiBoUrl";
            strArr[13] = jSONObject.optString("weiBoUrl");
            strArr[14] = "introduce";
            strArr[15] = jSONObject.optString("introduce");
            strArr[16] = "address";
            strArr[17] = jSONObject.optString("address");
            strArr[18] = "faxNum";
            strArr[19] = jSONObject.optString("faxNum");
            strArr[20] = "webSite";
            strArr[21] = jSONObject.optString("webSite");
            strArr[22] = "versionCode";
            strArr[23] = jSONObject.optString("versionCode");
            strArr[24] = Scopes.EMAIL;
            strArr[25] = jSONObject.optString(Scopes.EMAIL);
            strArr[26] = "parentPubId";
            strArr[27] = jSONObject.optString("parentPubId");
            strArr[28] = "slogan";
            strArr[29] = jSONObject.optString("slogan");
            strArr[30] = "rectLogoName";
            strArr[31] = jSONObject.optString("rectLogoName");
            strArr[32] = "circleLogoName";
            strArr[33] = jSONObject.optString("circleLogoName");
            strArr[34] = "extend";
            strArr[35] = jSONObject.optString("extend");
            strArr[36] = "hasMenu";
            strArr[37] = jSONObject.optString("hasMenu");
            strArr[38] = "loadMenuTime";
            strArr[39] = jSONObject.optString("loadMenuTime");
            strArr[40] = "moveWebSite";
            strArr[41] = jSONObject.optString("moveWebSite");
            strArr[42] = "corpLevel";
            if (StringUtils.isNull(optString)) {
                optString = "0";
            }
            strArr[43] = optString;
            strArr[44] = "updateInfoTime";
            strArr[45] = String.valueOf(System.currentTimeMillis());
            strArr[46] = "rid";
            strArr[47] = jSONObject.optString("rid");
            strArr[48] = "logoType";
            strArr[49] = jSONObject.optString("logoType");
            strArr[50] = "scale";
            strArr[51] = jSONObject.optString("scale", "0");
            strArr[52] = "backColor";
            strArr[53] = jSONObject.optString("backColor");
            strArr[54] = "backColorEnd";
            strArr[55] = jSONObject.optString("backColorEnd");
            ContentValues contentValues = BaseManager.getContentValues(null, strArr);
            if ((((long) DBManager.update("tb_public_info", contentValues, "pubId = ?", new String[]{jSONObject.optString("pubId")})) >= 1 ? 1 : null) == null) {
                DBManager.insert("tb_public_info", contentValues);
            }
            try {
                DBManager.delete("tb_public_num_info", " pubId =? ", new String[]{jSONObject.optString("pubId")});
            } catch (Throwable th) {
            }
            try {
                int i;
                JSONArray optJSONArray = jSONObject.optJSONArray("pubNumInfolist");
                if (optJSONArray != null && optJSONArray.length() > 0) {
                    int length = optJSONArray.length();
                    for (i = 0; i < length; i++) {
                        JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                        if (optJSONObject != null) {
                            try {
                                h(optJSONObject);
                                String optString2 = optJSONObject.optString(IccidInfoManager.NUM);
                                String optString3 = optJSONObject.optString("nameType", "0");
                                if ((DBManager.insert("tb_public_num_info", BaseManager.getContentValues(null, "pubId", optJSONObject.optString("pubId"), IccidInfoManager.NUM, optString2, "main", optJSONObject.optString("main"), "communication", optJSONObject.optString("communication"), "purpose", optJSONObject.optString("purpose"), "areaCode", optJSONObject.optString("areaCode"), "extend", optJSONObject.optString("extend"), "ptype", optJSONObject.optString(NumberInfo.TYPE_KEY), "isfull", optJSONObject.optString("isfull"), "minLen", optJSONObject.optString("minLen"), "maxLen", optJSONObject.optString("maxLen"), "len", optJSONObject.optString("len"), "ntype", optJSONObject.optString("ntype"), "nameType", optJSONObject.optString("nameType", "0"), "lastloadtime", String.valueOf(System.currentTimeMillis()))) <= 0 ? 1 : null) == null && "1".equals(optString3)) {
                                    c.a(optString2, optJSONObject.optInt("pubId"));
                                }
                                String optString4 = optJSONObject.optString("areaCode");
                                m.a(optString2, !StringUtils.isNull(optString4) ? optString4.split(";")[0] : "", 1);
                            } catch (Throwable th2) {
                            }
                        }
                    }
                }
                try {
                    DBManager.delete("tb_public_menu_info", "pubId = ?", new String[]{jSONObject.optString("pubId")});
                } catch (Throwable th3) {
                }
                JSONArray optJSONArray2 = jSONObject.optJSONArray("pubMenuInfolist");
                if (optJSONArray2 != null) {
                    if (optJSONArray2.length() > 0) {
                        int length2 = optJSONArray2.length();
                        for (i = 0; i < length2; i++) {
                            JSONObject optJSONObject2 = optJSONArray2.optJSONObject(i);
                            if (optJSONObject2 != null) {
                                try {
                                    JSONArray optJSONArray3 = optJSONObject2.optJSONArray("secondmenu");
                                    if (optJSONArray3 != null && optJSONArray3.length() > 0) {
                                        f(optJSONObject2);
                                        int length3 = optJSONArray3.length();
                                        for (int i2 = 0; i2 < length3; i2++) {
                                            f(optJSONArray3.getJSONObject(i2));
                                        }
                                    } else {
                                        f(optJSONObject2);
                                    }
                                } catch (Throwable th4) {
                                }
                            }
                        }
                    }
                }
            } catch (Throwable th5) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean a(String str, int i) {
        b a = c.a(str, false);
        return (a == null || a.h == 0 || i == a.h) ? false : true;
    }

    private static boolean a(String str, String str2, int i, int i2, int i3, String str3) {
        int indexOf = str2.indexOf("*");
        if (indexOf < 0) {
            return false;
        }
        String substring = str2.substring(0, indexOf);
        String substring2 = str2.substring(indexOf + 1);
        if (!str.startsWith(substring) || !str.endsWith(substring2)) {
            return false;
        }
        indexOf = substring2.length() + substring.length();
        int length = str.length();
        if (i <= 0) {
            if (i2 > 0 && length > indexOf + i2) {
                return false;
            }
            if (i3 > 0 && length < indexOf + i3) {
                return false;
            }
        } else if (indexOf + i != length) {
            return false;
        }
        return !"sj".equals(str3) || StringUtils.sj(str);
    }

    public static String b() {
        return "create table  if not exists tb_public_menu_info (id INTEGER PRIMARY KEY, menuCode text not null, pubId INTEGER, menuName text not null, menuType text not null, sendTo text, sp text , menuDesc text , sms text, url text, phoneNum text, actionData text, extend text)";
    }

    private static String b(int i) {
        HashMap iccidAreaCodeMap = IccidLocationUtil.getIccidAreaCodeMap();
        if (iccidAreaCodeMap == null || iccidAreaCodeMap.isEmpty()) {
            return "CN";
        }
        for (Entry entry : iccidAreaCodeMap.entrySet()) {
            String[] strArr = (String[]) entry.getValue();
            int a = a(strArr);
            if (a != -2 && a == i) {
                String str = strArr[3];
                if (!StringUtils.isNull(str)) {
                    return new StringBuilder(String.valueOf(str)).append("_").append((String) entry.getKey()).toString();
                }
                String str2 = strArr[0];
                if (!StringUtils.isNull(str2)) {
                    return new StringBuilder(String.valueOf(str2)).append("_").append((String) entry.getKey()).toString();
                }
            }
        }
        return "CN";
    }

    public static ArrayList<String> b(String str) {
        XyCursor rawQuery;
        Throwable th;
        XyCursor xyCursor = null;
        try {
            ArrayList<String> arrayList = new ArrayList();
            rawQuery = DBManager.rawQuery("select DISTINCT versionCode, pubId from tb_public_info where  pubId  in ( " + str + " )", null);
            if (rawQuery != null) {
                try {
                    if (rawQuery.getCount() > 0) {
                        while (rawQuery.moveToNext()) {
                            String string = rawQuery.getString(rawQuery.getColumnIndex("pubId"));
                            String string2 = rawQuery.getString(rawQuery.getColumnIndex("versionCode"));
                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put(IccidInfoManager.NUM, string);
                            jSONObject.put(NumberInfo.VERSION_KEY, string2);
                            arrayList.add(jSONObject.toString());
                        }
                        XyCursor.closeCursor(rawQuery, true);
                        return arrayList;
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = rawQuery;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(rawQuery, true);
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
        return null;
    }

    private static HashMap<String, String> b(String str, String str2, int i) {
        SQLiteDatabase sQLiteDatabase;
        Throwable th;
        SQLiteDatabase sQLiteDatabase2 = null;
        try {
            sQLiteDatabase = DBManager.getSQLiteDatabase();
            try {
                Object obj = "SELECT pubId,minLen,maxLen,len,ntype,num,areaCode,purpose,extend,nameType from tb_public_num_info where num like '%*' and '" + str + "' like  substr(num,1,length(num)-1) || '%'  and ptype = '" + i + "'";
                if ("CN".equals(str2)) {
                    obj = new StringBuilder(String.valueOf(obj)).append(" and areaCode = 'CN;'").toString();
                }
                HashMap<String, String> a = a(sQLiteDatabase, str, new StringBuilder(String.valueOf(obj)).append(" order by lastloadtime desc, length(num) desc ").toString(), str2, i);
                DBManager.close(sQLiteDatabase);
                return a;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                sQLiteDatabase2 = sQLiteDatabase;
                th = th3;
                DBManager.close(sQLiteDatabase2);
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            DBManager.close(sQLiteDatabase2);
            throw th;
        }
    }

    public static HashMap<String, String[]> b(Set<String> set) {
        return a((Set) set, 1);
    }

    private static void b(String str, String str2) {
        r.postDelayed(new j(str2, str), 180000);
    }

    public static void b(List<String> list) {
        if (list != null) {
            try {
                if (!list.isEmpty()) {
                    String optString;
                    StringBuffer stringBuffer = new StringBuffer();
                    for (String optString2 : list) {
                        optString2 = new JSONObject(optString2).optString("pubId");
                        if (!StringUtils.isNull(optString2)) {
                            stringBuffer.append("," + optString2);
                        }
                    }
                    if (stringBuffer.length() > 0) {
                        optString2 = stringBuffer.substring(1);
                        stringBuffer.setLength(0);
                        DBManager.execSQL("UPDATE tb_public_info SET updateInfoTime = " + System.currentTimeMillis() + " WHERE pubId IN (" + optString2 + ")");
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static boolean b(JSONObject jSONObject) {
        if (jSONObject == null) {
            return false;
        }
        if (!c(jSONObject.optInt("nameType"))) {
            return true;
        }
        String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(jSONObject.optString(IccidInfoManager.NUM));
        int optInt = jSONObject.optInt("pubId");
        b a = c.a(phoneNumberNo86, false);
        boolean z = (a == null || a.h == 0 || optInt == a.h) ? false : true;
        return !z;
    }

    public static int c(String str) {
        Throwable th;
        XyCursor xyCursor = null;
        if (StringUtils.isNull(str)) {
            return -1;
        }
        XyCursor rawQuery;
        try {
            rawQuery = DBManager.rawQuery("SELECT extend FROM tb_public_num_info WHERE num = ? LIMIT 1", new String[]{str});
            if (rawQuery != null) {
                try {
                    if (rawQuery.getCount() > 0) {
                        String string;
                        if (rawQuery.moveToNext()) {
                            string = rawQuery.getString(0);
                        }
                        int i = i(string);
                        XyCursor.closeCursor(rawQuery, true);
                        return i;
                    }
                } catch (Throwable th2) {
                    Throwable th3 = th2;
                    xyCursor = rawQuery;
                    th = th3;
                    XyCursor.closeCursor(xyCursor, true);
                    throw th;
                }
            }
            XyCursor.closeCursor(rawQuery, true);
            return -1;
        } catch (Throwable th4) {
            th = th4;
            XyCursor.closeCursor(xyCursor, true);
            throw th;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static int c(String str, String str2) {
        XyCursor xyCursor = null;
        try {
            xyCursor = DBManager.rawQuery("SELECT DISTINCT nameType FROM tb_public_num_info WHERE num =? AND areaCode LIKE ?", new String[]{str, "%" + str2 + "%"});
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0 && xyCursor.moveToNext()) {
                    int i = xyCursor.getInt(0);
                    XyCursor.closeCursor(xyCursor, true);
                    return i;
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
        return -1;
    }

    public static String c() {
        return "create table  if not exists tb_public_num_info (id INTEGER PRIMARY KEY, pubId INTEGER not null, num text not null, purpose text , areaCode text not null, ptype int default 1, main INTEGER default 0, communication INTEGER default 0, isfull INTEGER default 0, minLen INTEGER default 0, maxLen INTEGER default 0, len INTEGER default 0, ntype text, extend text, lastloadtime LONG default 0, isuse LONG default 0, isrulenum INTEGER default 0, nameType INTEGER default 0)";
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static HashMap<String, String> c(String str, String str2, int i) {
        HashMap<String, String> hashMap;
        XyCursor xyCursor = null;
        int i2 = -1;
        String str3 = null;
        String str4 = null;
        String str5 = null;
        int i3 = 0;
        HashMap<String, String> hashMap2 = null;
        Object obj = null;
        try {
            xyCursor = DBManager.rawQuery("SELECT pubId,purpose,extend,nameType from tb_public_num_info where num = '" + str + "' and ptype = '" + i + "' and areaCode LIKE '%" + str2 + "%'", null);
            if (xyCursor != null) {
                if (xyCursor.getCount() > 0) {
                    if (xyCursor.moveToNext()) {
                        i2 = xyCursor.getInt(xyCursor.getColumnIndex("pubId"));
                        str3 = xyCursor.getString(xyCursor.getColumnIndex("purpose"));
                        str5 = xyCursor.getString(xyCursor.getColumnIndex("extend"));
                        i3 = xyCursor.getInt(xyCursor.getColumnIndex("nameType"));
                        str4 = str2;
                    }
                    if (i2 > 0 && r6 == null) {
                        r.postDelayed(new j(str4, str), 180000);
                    }
                    if (hashMap2 == null || r6 == null) {
                        XyCursor.closeCursor(xyCursor, true);
                        if (i2 >= 0) {
                            return null;
                        }
                        hashMap = new HashMap();
                        hashMap.put("pubId", String.valueOf(i2));
                        if (!StringUtils.isNull(str3)) {
                            hashMap.put("purpose", str3);
                        }
                        if (!StringUtils.isNull(str4)) {
                            hashMap.put("areaCode", str4);
                        }
                        if (!StringUtils.isNull(str5)) {
                            hashMap.put("extend", str5);
                        }
                        hashMap.put("nameType", String.valueOf(i3));
                        hashMap.put(IccidInfoManager.NUM, str);
                        return hashMap;
                    }
                    hashMap2.put("ruleMatch", "");
                    XyCursor.closeCursor(xyCursor, true);
                    return hashMap2;
                }
            }
            XyCursor.closeCursor(xyCursor, true);
            if ("CN".equalsIgnoreCase(str2)) {
                hashMap2 = b(str, str2, i);
            } else {
                xyCursor = DBManager.rawQuery("SELECT pubId,purpose,extend,nameType from tb_public_num_info where num = '" + str + "' and ptype = '" + i + "' and areaCode LIKE '%CN%'", null);
                if (xyCursor != null && xyCursor.getCount() > 0) {
                    if (xyCursor.moveToNext()) {
                        i2 = xyCursor.getInt(xyCursor.getColumnIndex("pubId"));
                        str3 = xyCursor.getString(xyCursor.getColumnIndex("purpose"));
                        str5 = xyCursor.getString(xyCursor.getColumnIndex("extend"));
                        i3 = xyCursor.getInt(xyCursor.getColumnIndex("nameType"));
                        str4 = "CN";
                    }
                    r.postDelayed(new j(str4, str), 180000);
                    if (hashMap2 == null) {
                        hashMap2.put("ruleMatch", "");
                        XyCursor.closeCursor(xyCursor, true);
                        return hashMap2;
                    }
                    XyCursor.closeCursor(xyCursor, true);
                    if (i2 >= 0) {
                        return null;
                    }
                    hashMap = new HashMap();
                    hashMap.put("pubId", String.valueOf(i2));
                    if (StringUtils.isNull(str3)) {
                        hashMap.put("purpose", str3);
                    }
                    if (StringUtils.isNull(str4)) {
                        hashMap.put("areaCode", str4);
                    }
                    if (StringUtils.isNull(str5)) {
                        hashMap.put("extend", str5);
                    }
                    hashMap.put("nameType", String.valueOf(i3));
                    hashMap.put(IccidInfoManager.NUM, str);
                    return hashMap;
                }
                hashMap2 = b(str, str2, i);
            }
            obj = 1;
            r.postDelayed(new j(str4, str), 180000);
            if (hashMap2 == null) {
                hashMap2.put("ruleMatch", "");
                XyCursor.closeCursor(xyCursor, true);
                return hashMap2;
            }
            XyCursor.closeCursor(xyCursor, true);
        } catch (Throwable th) {
            i3 = th;
            boolean z = true;
            XyCursor.closeCursor(xyCursor, true);
        }
        if (i2 >= 0) {
            return null;
        }
        hashMap = new HashMap();
        hashMap.put("pubId", String.valueOf(i2));
        if (StringUtils.isNull(str3)) {
            hashMap.put("purpose", str3);
        }
        if (StringUtils.isNull(str4)) {
            hashMap.put("areaCode", str4);
        }
        if (StringUtils.isNull(str5)) {
            hashMap.put("extend", str5);
        }
        hashMap.put("nameType", String.valueOf(i3));
        hashMap.put(IccidInfoManager.NUM, str);
        return hashMap;
    }

    private static boolean c(int i) {
        return i == 1;
    }

    public static boolean c(JSONObject jSONObject) {
        if (jSONObject == null || !c(jSONObject.optInt("nameType"))) {
            return false;
        }
        b a = c.a(StringUtils.getPhoneNumberNo86(jSONObject.optString(IccidInfoManager.NUM)), false);
        if (a == null || a.h == 0) {
            return false;
        }
        if (!(!(((System.currentTimeMillis() - a.k) > DexUtil.getUpdateCycleByType(34, 86400000) ? 1 : ((System.currentTimeMillis() - a.k) == DexUtil.getUpdateCycleByType(34, 86400000) ? 0 : -1)) <= 0)) || a.h == jSONObject.optInt("pubId")) {
            return false;
        }
        c.a(jSONObject.optString(IccidInfoManager.NUM), System.currentTimeMillis(), 1);
        return true;
    }

    public static int d(String str) {
        int i = -1;
        if (StringUtils.isNull(str)) {
            return i;
        }
        try {
            i = Integer.parseInt(str);
        } catch (Throwable th) {
        }
        return i;
    }

    public static List<String> d() {
        XyCursor rawQuery;
        List<String> list = null;
        long updateCycleByType = DexUtil.getUpdateCycleByType(1, Constant.month);
        try {
            rawQuery = DBManager.rawQuery("SELECT DISTINCT pni.num, pi.pubId, pi.versionCode, pni.nameType, nn.name, nn.cmd, nn.ec, nn.mark_time, nn.mark_cmd, nn.mark_ec FROM tb_public_num_info pni JOIN tb_public_info pi ON pi.pubId = pni.pubId LEFT JOIN tb_num_name nn ON nn.num = pni.num WHERE pni.isuse = 1 AND pni.pubId IN (SELECT pi.pubId FROM tb_public_num_info pni JOIN tb_public_info pi ON pi.pubId = pni.pubId AND pi.updateInfoTime < ? AND pni.isuse = 1 GROUP BY pi.pubId LIMIT 10)", new String[]{String.valueOf(System.currentTimeMillis() - updateCycleByType)});
            try {
                list = a(rawQuery);
            } catch (Throwable th) {
            }
        } catch (Throwable th2) {
            Object obj = list;
        }
        XyCursor.closeCursor(rawQuery, true);
        return list;
    }

    private static void d(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                ContentValues contentValues = new ContentValues();
                contentValues.put("loadMenuTime", Long.valueOf(System.currentTimeMillis()));
                DBManager.update("tb_public_info", contentValues, "pubId = ?", new String[]{jSONObject.optString("pubId")});
            } catch (Throwable th) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static String e(String str) {
        if (StringUtils.isNull(str) || !str.contains("action_data")) {
            return null;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            JSONArray jSONArray = new JSONArray(str);
            int length = jSONArray.length();
            Object obj = null;
            for (int i = 0; i < length; i++) {
                JSONObject jSONObject2 = jSONArray.getJSONObject(i);
                if (jSONObject2.has("secondmenu")) {
                    JSONArray jSONArray2 = jSONObject2.getJSONArray("secondmenu");
                    if (jSONArray2.length() != 0) {
                        int length2 = jSONArray2.length();
                        for (int i2 = 0; i2 < length2; i2++) {
                            JSONObject jSONObject3 = jSONArray2.getJSONObject(i2);
                            String string = jSONObject3.getString("name");
                            if (!string.contains("查") || !string.contains("流量")) {
                                if (string.contains("查")) {
                                }
                                if (string.contains("查")) {
                                    if (!string.contains("余额")) {
                                    }
                                    jSONObject.put("queryCharge", jSONObject3);
                                }
                                if (jSONObject.has("queryTraffic") && jSONObject.has("queryCharge")) {
                                    obj = 1;
                                    break;
                                }
                            }
                            jSONObject.put("queryTraffic", jSONObject3);
                        }
                        if (obj != null) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
        } catch (Throwable th) {
        }
        return jSONObject.length() != 0 ? jSONObject.toString() : null;
    }

    public static void e() {
        try {
            DBManager.delete("tb_public_num_info", "isrulenum = 1", null);
        } catch (Throwable th) {
        }
    }

    private static void e(JSONObject jSONObject) {
        if (jSONObject != null) {
            try {
                JSONArray optJSONArray = jSONObject.optJSONArray("secondmenu");
                if (optJSONArray != null && optJSONArray.length() > 0) {
                    f(jSONObject);
                    int length = optJSONArray.length();
                    for (int i = 0; i < length; i++) {
                        f(optJSONArray.getJSONObject(i));
                    }
                    return;
                }
                f(jSONObject);
            } catch (Throwable th) {
            }
        }
    }

    private static void f(String str) {
        try {
            DBManager.delete("tb_public_menu_info", "pubId = ?", new String[]{str});
        } catch (Throwable th) {
        }
    }

    private static void f(JSONObject jSONObject) {
        Object obj = 1;
        if (jSONObject != null) {
            try {
                ContentValues contentValues = BaseManager.getContentValues(null, "menuCode", jSONObject.optString("menuCode"), "pubId", jSONObject.optString("pubId"), "menuName", jSONObject.optString("name"), "menuType", jSONObject.optString(NumberInfo.TYPE_KEY), "extend", jSONObject.optString("extend"), "actionData", jSONObject.optString("action_data"));
                if (((long) DBManager.update("tb_public_menu_info", contentValues, "pubId = ? and menuCode = ?", new String[]{jSONObject.optString("pubId"), jSONObject.optString("menuCode")})) < 1) {
                    obj = null;
                }
                if (obj == null) {
                    DBManager.insert("tb_public_menu_info", contentValues);
                }
            } catch (Throwable th) {
            }
        }
    }

    private static String g(String str) {
        return !StringUtils.isNull(str) ? str.split(";")[0] : "";
    }

    private static void g(JSONObject jSONObject) {
        Object obj = 1;
        if (jSONObject != null) {
            try {
                for (String str : jSONObject.optString("areaCode").split(";")) {
                    if (!StringUtils.isNull(str)) {
                        DBManager.delete("tb_public_num_info", "  ptype = ? and num = ? and areaCode like '%" + str + "%'  and pubId !=? ", new String[]{String.valueOf(jSONObject.optString(NumberInfo.TYPE_KEY)), jSONObject.optString(IccidInfoManager.NUM), jSONObject.optString("pubId")});
                    }
                }
            } catch (Throwable th) {
            }
            try {
                String optString = jSONObject.optString(IccidInfoManager.NUM);
                String optString2 = jSONObject.optString("nameType", "0");
                if (DBManager.insert("tb_public_num_info", BaseManager.getContentValues(null, "pubId", jSONObject.optString("pubId"), IccidInfoManager.NUM, optString, "main", jSONObject.optString("main"), "communication", jSONObject.optString("communication"), "purpose", jSONObject.optString("purpose"), "areaCode", jSONObject.optString("areaCode"), "extend", jSONObject.optString("extend"), "ptype", jSONObject.optString(NumberInfo.TYPE_KEY), "isfull", jSONObject.optString("isfull"), "minLen", jSONObject.optString("minLen"), "maxLen", jSONObject.optString("maxLen"), "len", jSONObject.optString("len"), "ntype", jSONObject.optString("ntype"), "nameType", jSONObject.optString("nameType", "0"), "lastloadtime", String.valueOf(System.currentTimeMillis()))) > 0) {
                    obj = null;
                }
                if (obj == null && "1".equals(optString2)) {
                    c.a(optString, jSONObject.optInt("pubId"));
                }
                String optString3 = jSONObject.optString("areaCode");
                m.a(optString, !StringUtils.isNull(optString3) ? optString3.split(";")[0] : "", 1);
            } catch (Throwable th2) {
            }
        }
    }

    private static void h(String str) {
        try {
            DBManager.delete("tb_public_num_info", " pubId =? ", new String[]{str});
        } catch (Throwable th) {
        }
    }

    private static void h(JSONObject jSONObject) {
        try {
            for (String str : jSONObject.optString("areaCode").split(";")) {
                if (!StringUtils.isNull(str)) {
                    DBManager.delete("tb_public_num_info", "  ptype = ? and num = ? and areaCode like '%" + str + "%'  and pubId !=? ", new String[]{String.valueOf(jSONObject.optString(NumberInfo.TYPE_KEY)), jSONObject.optString(IccidInfoManager.NUM), jSONObject.optString("pubId")});
                }
            }
        } catch (Throwable th) {
        }
    }

    private static int i(String str) {
        if (str == null || str.indexOf("{") == -1) {
            return -1;
        }
        try {
            return new JSONObject(str).getInt("sp");
        } catch (Throwable th) {
            return -1;
        }
    }
}
