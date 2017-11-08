package cn.com.xy.sms.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.service.f.a;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseCardManager {
    public static String SMS_DATE_TIME = "sms_date_time";
    private static String a = "yunyingshang_s_0001";
    private static String b = "1003";
    private static String c = "xiaoyuan";

    private static String a(String str) {
        Cursor query = Constant.getContext().getContentResolver().query(Uri.parse("content://com.yunos.lifecard/cards"), null, " arg1 like ? ", new String[]{"%" + str + "%"}, null);
        if (!query.moveToNext()) {
            return null;
        }
        String string = query.getString(query.getColumnIndex("card_id"));
        query.close();
        return string;
    }

    private static Map<String, Object> a(Map<String, Object> map, String str) {
        Uri parse = Uri.parse("content://com.yunos.lifecard/cards");
        if (map != null) {
            String jSONObject = ((JSONObject) map.get("content")).toString();
            if (jSONObject == null || jSONObject.equals("")) {
                map.put("Result", Boolean.valueOf(false));
            } else {
                Uri uri;
                ContentResolver contentResolver = Constant.getContext().getContentResolver();
                ContentValues contentValues = (ContentValues) map.get("contentValue");
                if (contentValues == null) {
                    uri = null;
                } else {
                    contentValues.put("card_id", str);
                    uri = contentResolver.insert(parse, contentValues);
                }
                if (uri != null) {
                    map.put("Result", Boolean.valueOf(true));
                } else {
                    map.put("Result", Boolean.valueOf(false));
                }
            }
            return map;
        }
        Map<String, Object> hashMap = new HashMap();
        hashMap.put("Result", Boolean.valueOf(false));
        return hashMap;
    }

    private static Map<String, Object> b(Map<String, Object> map, String str) {
        Uri parse = Uri.parse("content://com.yunos.lifecard/cards/");
        if (map != null) {
            String jSONObject = ((JSONObject) map.get("content")).toString();
            if (jSONObject == null || jSONObject.equals("")) {
                map.put("Result", Boolean.valueOf(false));
            } else {
                int i;
                ContentResolver contentResolver = Constant.getContext().getContentResolver();
                ContentValues contentValues = (ContentValues) map.get("contentValue");
                if (contentValues == null) {
                    i = -1;
                } else {
                    new StringBuilder("values=").append(contentValues);
                    contentValues.remove("card_tag");
                    i = contentResolver.update(parse, contentValues, "card_id = ?", new String[]{str});
                }
                map = new HashMap();
                if (i > 0) {
                    map.put("Result", Boolean.valueOf(true));
                } else {
                    map.put("Result", Boolean.valueOf(false));
                }
            }
            return map;
        }
        Map<String, Object> hashMap = new HashMap();
        hashMap.put("Result", Boolean.valueOf(false));
        return hashMap;
    }

    public static boolean checkStationList(String str, String str2, String str3) {
        return a.a(str, str2, str3);
    }

    public static void expressage(String str, String str2, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str) || StringUtils.isNull(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
            return;
        }
        XyCallBack fVar = new f(sdkCallBack);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("express_name", str);
            jSONObject.put("express_no", str2);
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL2, jSONObject.toString(), fVar);
        } catch (JSONException e) {
        }
    }

    public static Map<String, Object> parseMsgForCard(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map hashMap = map != null ? map : new HashMap();
            if (!hashMap.containsKey("PARSE_TIME_OUT")) {
                hashMap.put("PARSE_TIME_OUT", "15000");
            }
            Map a = ParseManager.a(context, str, str2, str3, 0, hashMap);
            Map<String, Object> hashMap2;
            if (a == null || ParseBubbleManager.getParseStatu(a) == -1) {
                hashMap2 = new HashMap();
                hashMap2.put("Result", Boolean.valueOf(false));
                return hashMap2;
            }
            if (hashMap != null && hashMap.containsKey(SMS_DATE_TIME)) {
                a.put(SMS_DATE_TIME, hashMap.get(SMS_DATE_TIME));
            }
            hashMap2 = DexUtil.handerValueMap(a);
            if (hashMap2 != null) {
                hashMap2.put("Result", Boolean.valueOf(true));
                return hashMap2;
            }
            hashMap2 = new HashMap();
            hashMap2.put("Result", Boolean.valueOf(false));
            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", "parseMsgForCard handerValueMap end false", null);
            return hashMap2;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static String parseMsgForCardData(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            if (!n.a((byte) 8)) {
                PopupUtil.getResultMap(false, false);
            }
            Map a = ParseManager.a(context, str, str2, str3, 0, map);
            if (!(a == null || ParseBubbleManager.getParseStatu(a) == -1)) {
                Map handerValueMap = DexUtil.handerValueMap(a);
                if (handerValueMap == null) {
                    return null;
                }
                Object obj = handerValueMap.get("content");
                if (obj != null && (obj instanceof JSONObject)) {
                    JSONObject put = ((JSONObject) handerValueMap.get("theReturn")).put("content", handerValueMap.get("content"));
                    put.put("title", handerValueMap.get("title"));
                    put.put("card_tag", "xiaoyuan");
                    return put.toString();
                }
            }
            return null;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static void queryFlightData(String str, String str2, String str3, Map<String, Object> map, SdkCallBack sdkCallBack) {
        ParseManager.queryFlightData(str, str2, str3, map, sdkCallBack);
    }

    public static void queryTrainInfo(String str, String str2, String str3, String str4, Map<String, Object> map, SdkCallBack sdkCallBack) {
        a.a(str, str2, str3, str4, map, sdkCallBack);
    }
}
