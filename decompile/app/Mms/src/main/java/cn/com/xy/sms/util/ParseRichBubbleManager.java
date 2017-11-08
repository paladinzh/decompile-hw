package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.g;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseRichBubbleManager {
    private static long a = 21600000;
    private static long b = 1296000000;
    public static boolean isBusy = false;

    public static void addEffectiveBubbleData(String str, String str2, JSONObject jSONObject) {
        if (jSONObject != null) {
            D a = D.a(str);
            if (a != null) {
                synchronized (a.g) {
                    a.g.put(str2, jSONObject);
                    try {
                        if (jSONObject.has("msg_num_md5")) {
                            jSONObject.remove("msg_num_md5");
                        }
                    } catch (Throwable th) {
                    }
                }
                synchronized (a.f) {
                    a.f.remove(str2);
                }
                synchronized (a.h) {
                    a.h.remove(str2);
                }
                synchronized (a.i) {
                    a.i.remove(str2);
                }
                try {
                    jSONObject.remove("viewPartParam");
                } catch (Throwable th2) {
                }
            }
        }
    }

    public static void addInvalidBubbleData(String str, String str2) {
        if (str2 == null) {
            D a = D.a(str);
            if (a != null) {
                synchronized (a.h) {
                    a.h.add(str2);
                }
                synchronized (a.f) {
                    a.f.remove(str2);
                }
                synchronized (a.g) {
                    a.g.remove(str2);
                }
                synchronized (a.i) {
                    a.i.remove(str2);
                }
            }
        }
    }

    public static void addToFavorite(String str, String str2, String str3, long j, boolean z, JSONObject jSONObject) {
        if (z && jSONObject != null) {
            try {
                if (!"1".equalsIgnoreCase(jSONObject.optString("is_mark"))) {
                    jSONObject.put("is_mark", "1");
                    jSONObject.put(Constant.IS_FAVORITE, "1");
                    g.a(str, str2, str3);
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void clearCacheBubbleData(String str) {
        D.c(str);
        BusinessSmsMessage.emptyObj = null;
    }

    public static void deleteBubbleDataFromCache(String str, String str2) {
        try {
            if (StringUtils.isNull(str)) {
                D.e(str2);
            }
            D a = D.a(str);
            if (a != null) {
                a.d(str2);
            }
        } catch (Throwable th) {
        }
    }

    public static void deleteParseDataFromCache(String str) {
        try {
            D.g(str);
        } catch (Throwable th) {
        }
    }

    public static void loadBubbleDataByPhoneNum(String str) {
        synchronized (D.b(str).g) {
            clearCacheBubbleData(str);
            D.b(StringUtils.getPhoneNumberNo86(str), MatchCacheManager.loadDataByParam("phoneNum=?", new String[]{StringUtils.getPhoneNumberNo86(str)}));
        }
    }

    public static Map<String, Object> parseMsgToSimpleBubbleResult(Context context, String str, String str2, String str3, String str4, byte b, Map<String, String> map) {
        if (!n.a((byte) Constant.POWER_TOSIMPLEBUBBLE_VIEW)) {
            return null;
        }
        Map<String, Object> hashMap;
        String parseMsgToBubble = ParseManager.parseMsgToBubble(context, str2, str3, str4, map);
        if (b == (byte) 1) {
            long j = 0;
            String md5 = MatchCacheManager.getMD5(str2, str4);
            if (!StringUtils.isNull(md5)) {
                String[] strArr = new String[12];
                strArr[0] = "msg_num_md5";
                strArr[1] = md5;
                strArr[2] = NetUtil.REQ_QUERY_NUM;
                strArr[3] = StringUtils.getPhoneNumberNo86(str2);
                strArr[4] = "msg_id";
                strArr[5] = str;
                strArr[6] = "session_reuslt";
                strArr[7] = parseMsgToBubble != null ? parseMsgToBubble : "";
                strArr[8] = "save_time";
                strArr[9] = String.valueOf(System.currentTimeMillis());
                strArr[10] = "session_lasttime";
                strArr[11] = String.valueOf(System.currentTimeMillis());
                j = MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, strArr), 1);
            }
            if (parseMsgToBubble != null) {
                hashMap = new HashMap();
                hashMap.put("CACHE_SDK_MSG_ID", Long.valueOf(j));
                hashMap.put("CACHE_SDK_MSG_RESULT", parseMsgToBubble);
                return hashMap;
            }
        } else if (parseMsgToBubble != null) {
            hashMap = new HashMap();
            hashMap.put("CACHE_SDK_MSG_RESULT", parseMsgToBubble);
            return hashMap;
        }
        hashMap = null;
        return hashMap;
    }

    public static void pubBubbleData(String str, Map<String, JSONObject> map, boolean z) {
        D b = D.b(str);
        synchronized (b.f) {
            if (z) {
                b.a();
            }
            if (map != null) {
                b.f.putAll(map);
                map.size();
            }
        }
    }

    public static JSONObject queryBubbleDataFromApi(String str, String str2, String str3, String str4, long j, HashMap<String, Object> hashMap) {
        Map parseSmsToBubbleResult = ParseSmsToBubbleUtil.parseSmsToBubbleResult(str, str2, str3, str4, j, 3, true, true, hashMap);
        return parseSmsToBubbleResult == null ? null : (JSONObject) parseSmsToBubbleResult.get("BUBBLE_JSON_RESULT");
    }

    public static JSONObject queryBubbleDataFromDb(String str, String str2, String str3, long j) {
        JSONObject dataByParam = MatchCacheManager.getDataByParam(str);
        if (dataByParam != null) {
            String str4 = (String) JsonUtil.getValueFromJsonObject(dataByParam, "msg_num_md5");
            return (str4 == null || !str4.equals(MatchCacheManager.getMD5(str2, str3))) ? null : dataByParam;
        }
    }

    public static JSONObject queryDataByMsgItem(String str, String str2, String str3, String str4, int i, long j) {
        if (str == null || str2 == null || str3 == null) {
            throw new Exception("msgid or phoneNum or smsContent is null but they need value.");
        } else if (StringUtils.isPhoneNumber(str2)) {
            return null;
        } else {
            D b = D.b(str2);
            Map map = b.g;
            JSONObject jSONObject = (JSONObject) b.g.get(str);
            if (jSONObject != null) {
                return jSONObject;
            }
            if (jSONObject == null && b.h.contains(str)) {
                return null;
            }
            JSONObject jSONObject2;
            int i2;
            if (jSONObject == null) {
                map = b.f;
                jSONObject = (JSONObject) b.f.get(str);
                if (jSONObject != null) {
                    int i3;
                    String str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_num_md5");
                    String md5 = MatchCacheManager.getMD5(str2, str3);
                    if (md5 == null || str5 == null || !md5.equals(str5)) {
                        i3 = 3;
                        b.h.remove(str);
                    } else {
                        if (i == 2) {
                            str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "bubble_result");
                            try {
                                long longValue = Long.valueOf(JsonUtil.getValueFromJsonObject(jSONObject, "bubble_lasttime").toString()).longValue();
                                if (StringUtils.isNull(str5)) {
                                    if ((System.currentTimeMillis() - longValue >= DexUtil.getUpdateCycleByType(23, a) ? 1 : null) == null) {
                                        b.h.add(str);
                                        i3 = 2;
                                    }
                                } else {
                                    if ((System.currentTimeMillis() - longValue >= DexUtil.getUpdateCycleByType(22, b) ? 1 : null) == null) {
                                        b.g.put(str, new JSONObject(str5));
                                        i3 = 1;
                                    }
                                }
                            } catch (Throwable th) {
                                b.h.add(str);
                            }
                        }
                        i3 = 0;
                    }
                    b.f.remove(str);
                    jSONObject2 = jSONObject;
                    i2 = i3;
                    if (i2 != 1) {
                        if (!b.i.contains(str)) {
                            return null;
                        }
                        b.i.add(str);
                        g.a(i2, str, str2, str3, str4, i, j, jSONObject2);
                    }
                    return jSONObject2;
                }
            }
            jSONObject2 = jSONObject;
            i2 = 0;
            if (i2 != 1) {
                if (!b.i.contains(str)) {
                    return null;
                }
                b.i.add(str);
                g.a(i2, str, str2, str3, str4, i, j, jSONObject2);
            }
            return jSONObject2;
        }
    }

    public static void queryDataByMsgItem(String str, String str2, String str3, long j, String str4, int i, SdkCallBack sdkCallBack, boolean z, HashMap<String, Object> hashMap) {
        if (str == null || str2 == null || str3 == null) {
            throw new Exception("msgid or phoneNum or smsContent is null but they need value.");
        } else if (StringUtils.isPhoneNumber(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), "phonenum is null", str, Integer.valueOf(2));
        } else {
            boolean z2 = false;
            if (hashMap != null) {
                if ("1".equalsIgnoreCase((String) hashMap.get(Constant.IS_FAVORITE))) {
                    z2 = true;
                }
            }
            D b = D.b(str2);
            Map map = b.g;
            JSONObject jSONObject = (JSONObject) b.g.get(str);
            if (jSONObject != null) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), jSONObject, str, Integer.valueOf(2));
                addToFavorite(str, str2, str3, j, z2, jSONObject);
            } else if (jSONObject == null && b.h.contains(str)) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), new StringBuilder(String.valueOf(str)).append(" invalidBubbleData ").toString(), str, Integer.valueOf(2));
            } else {
                int i2 = 0;
                if (jSONObject == null) {
                    map = b.f;
                    jSONObject = (JSONObject) b.f.get(str);
                    if (!(jSONObject == null || jSONObject.has("need_parse_bubble"))) {
                        int i3;
                        addToFavorite(str, str2, str3, j, z2, jSONObject);
                        String str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_num_md5");
                        String md5 = MatchCacheManager.getMD5(str2, str3);
                        if (md5 == null || str5 == null || !md5.equals(str5)) {
                            i3 = 3;
                        } else if (i != 2) {
                            i3 = 3;
                        } else {
                            try {
                                JSONObject jSONObject2 = (JSONObject) JsonUtil.getValueFromJsonObject(jSONObject, "bubble_result");
                                if (jSONObject2 == null) {
                                    b.h.add(str);
                                    i3 = 2;
                                } else {
                                    b.g.put(str, jSONObject2);
                                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), jSONObject2, str, Integer.valueOf(2));
                                    return;
                                }
                            } catch (Throwable th) {
                                b.h.add(str);
                            }
                        }
                        b.f.remove(str);
                        i2 = i3;
                    }
                }
                if (i2 != 0 && i2 != 3) {
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " invalidBubbleData dataStatu: " + i2, str, Integer.valueOf(2));
                    addToFavorite(str, str2, str3, j, z2, jSONObject);
                } else if (b.i.contains(str)) {
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " inQueueBubbleData2 ", str, Integer.valueOf(2));
                } else if (z) {
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-4), " is scrolling", str, Integer.valueOf(2));
                } else {
                    b.i.add(str);
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-2), " need parse ", str, Integer.valueOf(2));
                    a.c.execute(new m(b, str, i2, str2, str3, j, sdkCallBack, z2, str4, hashMap));
                }
            }
        }
    }
}
