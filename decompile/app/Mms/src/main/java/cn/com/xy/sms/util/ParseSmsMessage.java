package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.service.a.b;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseSmsMessage {
    public static final int DUOQU_CALLBACK_BACKTHREAD_FAIL = -3;
    public static final int DUOQU_CALLBACK_BACKTHREAD_HASDATA = 1;
    public static final int DUOQU_CALLBACK_REFRESH_LIST = 2;
    public static final int DUOQU_CALLBACK_UITHREAD_HASDATA = 0;
    public static final int DUOQU_CALLBACK_UITHREAD_NEEDPARSE = -2;
    public static final int DUOQU_CALLBACK_UITHREAD_NODATA = -1;
    public static final int DUOQU_CALLBACK_UITHREAD_PHONENUM_NULL = -1;
    public static final int DUOQU_CALLBACK_UITHREAD_SCOLLING = -4;
    public static final int DUOQU_CALLBACK_UITHREAD_UNKOWN = -5;
    public static final int DUOQU_SMARTSMS_SHOW_BUBBLE_RICH_FLAG = 2;
    public static final int DUOQU_SMARTSMS_SHOW_BUBBLE_SIMPLE_FLAG = 1;
    public static final int DUOQU_SMARTSMS_SHOW_RECOGNISE_VALUE_FLAG = 8;
    public static final int DUOQU_SMARTSMS_SHOW_URL_VALUE_FLAG = 16;

    private static JSONObject a(String str, String str2, String str3) {
        JSONObject dataByParam = MatchCacheManager.getDataByParam(str);
        if (dataByParam != null) {
            String str4 = (String) JsonUtil.getValueFromJsonObject(dataByParam, "msg_num_md5");
            return (str4 == null || !str4.equals(MatchCacheManager.getMD5(str2, str3))) ? null : dataByParam;
        }
    }

    static /* synthetic */ JSONObject a(String str, String str2, String str3, long j) {
        JSONObject dataByParam = MatchCacheManager.getDataByParam(str);
        if (dataByParam != null) {
            String str4 = (String) JsonUtil.getValueFromJsonObject(dataByParam, "msg_num_md5");
            return (str4 == null || !str4.equals(MatchCacheManager.getMD5(str2, str3))) ? null : dataByParam;
        }
    }

    private static void a(String str, String str2, String str3, String str4) {
        try {
            String md5 = MatchCacheManager.getMD5(str2, str3);
            if (!StringUtils.isNull(md5)) {
                String[] strArr = new String[10];
                strArr[0] = "msg_num_md5";
                strArr[1] = md5;
                strArr[2] = NetUtil.REQ_QUERY_NUM;
                strArr[3] = StringUtils.getPhoneNumberNo86(str2);
                strArr[4] = "msg_id";
                strArr[5] = str;
                strArr[6] = "value_recognise_result";
                if (str4 == null) {
                    str4 = "";
                }
                strArr[7] = str4;
                strArr[8] = "recognise_lasttime";
                strArr[9] = String.valueOf(System.currentTimeMillis());
                MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, strArr), 4);
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }

    private static void b(String str) {
        MatchCacheManager.resetRecognisedResult(str);
    }

    public static void parseMessage(String str, String str2, String str3, String str4, long j, int i, HashMap<String, Object> hashMap, SdkCallBack sdkCallBack, boolean z) {
        if (i == 2) {
            try {
                ParseRichBubbleManager.queryDataByMsgItem(str, str2, str4, j, str3, 2, sdkCallBack, z, hashMap);
            } catch (Throwable th) {
                DexUtil.saveExceptionLog(th);
            }
        } else if (i == 1) {
            Map hashMap2 = new HashMap();
            if (hashMap != null) {
                if (hashMap.containsKey("isUseNewAction")) {
                    hashMap2.put("isUseNewAction", hashMap.get("isUseNewAction").toString());
                }
            }
            ParseBubbleManager.queryDataByMsgItem(str, str2, str4, str3, 1, j, sdkCallBack, z, hashMap2);
        } else if (i == 8) {
            if (XyUtil.isFlagOn("isCombineAction", hashMap)) {
                parseRecognisedValueCombineAction(str, str2, str3, str4, j, hashMap, sdkCallBack, z);
            } else {
                parseRecognisedValue(str, str2, str3, str4, j, hashMap, sdkCallBack, z);
            }
        }
    }

    public static void parseRecognisedValue(String str, String str2, String str3, String str4, long j, HashMap<String, Object> hashMap, SdkCallBack sdkCallBack, boolean z) {
        if (str4 != null) {
            D b = D.b(str2);
            if (((JSONObject) b.l.get(str)) != null) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), (JSONObject) b.l.get(str), str, Integer.valueOf(8));
                return;
            } else if (b.m.contains(str)) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " invalidRecognisedValue", str, Integer.valueOf(8));
                return;
            } else if (b.n.contains(str)) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " inQueueRecognisedValue", str, Integer.valueOf(8));
                return;
            } else {
                JSONObject jSONObject = (JSONObject) b.k.remove(str);
                JSONObject jSONObject2 = (JSONObject) JsonUtil.getValueFromJsonObject(jSONObject, "value_recognise_result");
                if (!(jSONObject == null || jSONObject2 == null)) {
                    String str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_num_md5");
                    String md5 = MatchCacheManager.getMD5(str2, str4);
                    if (!(md5 == null || str5 == null || !md5.equals(str5))) {
                        synchronized (b.l) {
                            b.l.put(str, jSONObject2);
                            b.m.remove(str);
                        }
                        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), jSONObject2, str, Integer.valueOf(8));
                        return;
                    }
                }
                if (z) {
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-4), " is scrolling", str, Integer.valueOf(8));
                    return;
                }
                b.n.add(str);
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-2), " need parse ", str, Integer.valueOf(8));
                a.c.execute(new n(b, str, sdkCallBack, str2, str4, j, str3, hashMap));
                return;
            }
        }
        throw new Exception("msgid or phoneNum or smsContent is null but they need value.");
    }

    public static void parseRecognisedValueCombineAction(String str, String str2, String str3, String str4, long j, HashMap<String, Object> hashMap, SdkCallBack sdkCallBack, boolean z) {
        parseRecognisedValue(str, str2, str3, str4, j, hashMap, new p(hashMap, str, str2, str4, str3, j, z, sdkCallBack), z);
    }

    public static JSONObject queryRecognisedValueFromApi(String str, String str2, String str3, String str4, long j, Map map, SdkCallBack sdkCallBack) {
        boolean z = false;
        Map hashMap = new HashMap();
        if (map != null) {
            try {
                Object obj = map.get("ref_basevalue");
                map.remove("parse_recognise_value");
                if (obj != null) {
                    z = Boolean.valueOf(obj.toString()).booleanValue();
                }
                hashMap.putAll(map);
            } catch (Throwable e) {
                DexUtil.saveExceptionLog(e);
            }
        }
        if (z) {
            String valueOf = String.valueOf(new StringBuilder(String.valueOf(str)).append(str4).append(str2).toString().hashCode());
            D b = D.b(str2);
            if (b.j.containsKey(valueOf)) {
                hashMap.putAll((Map) b.j.remove(valueOf));
            } else {
                b.a(Constant.getContext(), str2, str3, str4, j, map, new o(b, valueOf, str, sdkCallBack));
            }
        }
        JSONObject parseRecogniseValue = ParseManager.parseRecogniseValue(str2, str4, j, hashMap);
        if (parseRecogniseValue == null) {
            a(str, str2, str4, "");
        } else {
            a(str, str2, str4, parseRecogniseValue.toString());
        }
        return parseRecogniseValue;
    }
}
