package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.BubbleTaskQueue;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseBubbleManager {
    private static String a = null;

    private static int a() {
        try {
            String config = DuoquUtils.getSdkDoAction().getConfig(4, null);
            if (StringUtils.isNull(config)) {
                return 50;
            }
            int parseInt = Integer.parseInt(config);
            if (parseInt < 0) {
                parseInt = 0;
            } else if (parseInt > 1000) {
                parseInt = 1000;
            }
            return parseInt;
        } catch (Throwable th) {
            return 50;
        }
    }

    private static void a(D d, Map<String, JSONObject> map) {
        if (d != null && map != null) {
            synchronized (d.a) {
                d.a.putAll(map);
            }
            synchronized (d.k) {
                d.k.putAll(map);
            }
        }
    }

    private static void a(String str, String str2, String str3, long j, String str4) {
        a.e.execute(new d(str2, str3, str, str4, j));
    }

    private static void a(boolean z, String str) {
        D.c(str);
        if (z) {
            ParseSmsToBubbleUtil.beforeHandParseReceiveSms(SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, 3);
        }
        a = null;
    }

    public static void addEffectiveBubbleData(String str, String str2, JSONArray jSONArray) {
        if (str2 != null && jSONArray != null) {
            D a = D.a(str);
            if (a != null) {
                synchronized (a.c) {
                    a.c.put(str2, jSONArray);
                }
                synchronized (a.a) {
                    a.a.remove(str2);
                }
                synchronized (a.d) {
                    a.d.remove(str2);
                }
                synchronized (a.e) {
                    a.e.remove(str2);
                }
            }
        }
    }

    public static void addInvalidBubbleData(String str, String str2) {
        if (str2 != null) {
            D a = D.a(str);
            if (a != null) {
                synchronized (a.d) {
                    a.d.add(str2);
                }
                synchronized (a.a) {
                    a.a.remove(str2);
                }
                synchronized (a.e) {
                    a.e.remove(str2);
                }
                synchronized (a.c) {
                    a.c.remove(str2);
                }
            }
        }
    }

    private static JSONArray b(String str, String str2, String str3, String str4, long j, Map<String, String> map) {
        try {
            String parseMsgToSimpleBubbleResultKuai = parseMsgToSimpleBubbleResultKuai(Constant.getContext(), str, str2, str3, str4, j, (byte) 1, map);
            return parseMsgToSimpleBubbleResultKuai == null ? null : new JSONArray(parseMsgToSimpleBubbleResultKuai);
        } catch (Throwable th) {
            return null;
        }
    }

    public static void clearAllCache(String str) {
        a(true, str);
    }

    public static void clearCacheBubbleData(String str) {
        a(false, str);
    }

    public static int deleteBubbleData(String str) {
        try {
            D a = D.a(str);
            if (a != null) {
                a.c();
            }
            return MatchCacheManager.deleteDataByPhoneNum(str);
        } catch (Throwable th) {
            return -1;
        }
    }

    public static void deleteBubbleData(Set<Integer> set) {
        if (set != null) {
            try {
                if (!set.isEmpty()) {
                    a.e.execute(new e(set));
                }
            } catch (Throwable th) {
            }
        }
    }

    public static boolean deleteBubbleData(String str, String str2, String str3) {
        String str4 = null;
        if (str2 != null) {
            str4 = MatchCacheManager.getMD5(str2, str3);
        }
        deleteBubbleDataFromCache(str2, str);
        ParseRichBubbleManager.deleteBubbleDataFromCache(str2, str);
        return MatchCacheManager.deleteBubbleData(str, str4);
    }

    public static void deleteBubbleDataFromCache(String str, String str2) {
        try {
            D a = D.a(str);
            if (a != null) {
                a.a.remove(str2);
                a.c.remove(str2);
                a.d.remove(str2);
                a.e.remove(str2);
            }
        } catch (Throwable th) {
        }
    }

    public static boolean equalPhoneNum(String str) {
        if (str == null || a == null) {
            return false;
        }
        return a.equals(StringUtils.getPhoneNumberNo86(str));
    }

    public static int getParseStatu(Map<String, Object> map) {
        if (map != null) {
            Integer num = (Integer) map.get("parseStatu");
            if (num != null && num.intValue() == -1) {
                return -1;
            }
        }
        return 0;
    }

    public static void loadBubbleDataByPhoneNum(String str, boolean z) {
        loadBubbleDataByPhoneNum(str, z, false);
    }

    public static void loadBubbleDataByPhoneNum(String str, boolean z, boolean z2) {
        a.a();
        String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
        String str2 = "phoneNum=?";
        String[] strArr = new String[]{phoneNumberNo86};
        D b = D.b(phoneNumberNo86);
        synchronized (b.c) {
            clearCacheBubbleData(phoneNumberNo86);
            a = phoneNumberNo86;
        }
        Map loadDataByParam = MatchCacheManager.loadDataByParam(str2, strArr, "save_time desc", "15");
        a(b, loadDataByParam);
        if (z) {
            ParseRichBubbleManager.pubBubbleData(phoneNumberNo86, loadDataByParam, true);
        }
        if (z2) {
            ParseMsgUrlManager.putUrlsResultData(phoneNumberNo86, loadDataByParam, true);
        }
        loadBubbleDataByPhoneNumSecond(str2, strArr, z, b, phoneNumberNo86);
        ParseSmsToBubbleUtil.beforeHandParseReceiveSms(phoneNumberNo86, a(), 3, true);
    }

    public static void loadBubbleDataByPhoneNumSecond(String str, String[] strArr, boolean z, D d, String str2) {
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
        Map loadDataByParam = MatchCacheManager.loadDataByParam(str, strArr, "save_time desc", "500");
        a(d, loadDataByParam);
        if (z) {
            ParseRichBubbleManager.pubBubbleData(str2, loadDataByParam, false);
        }
    }

    public static String parseMsgToSimpleBubbleResultKuai(Context context, String str, String str2, String str3, String str4, long j, byte b, Map<String, String> map) {
        if (!n.a((byte) Constant.POWER_TOSIMPLEBUBBLE_VIEW)) {
            return null;
        }
        if (map == null) {
            map = new HashMap();
        }
        map.put("msgTime", String.valueOf(j));
        String parseMsgToBubble = ParseManager.parseMsgToBubble(context, str2, str3, str4, map);
        a.e.execute(new d(str2, str4, str, parseMsgToBubble, j));
        return parseMsgToBubble;
    }

    public static JSONObject queryDataByMsgItem(String str, String str2, String str3, String str4, int i, long j) {
        if (str == null || str2 == null || str3 == null) {
            throw new Exception("msgid or phoneNum or smsContent is null but they need value.");
        } else if (StringUtils.isPhoneNumber(str2)) {
            return null;
        } else {
            JSONObject jSONObject = null;
            D b = D.b(str2);
            if (b.b != null) {
                jSONObject = (JSONObject) b.b.get(str);
                if (jSONObject != null) {
                    return jSONObject;
                }
            }
            if (jSONObject == null && b.d.contains(str)) {
                return null;
            }
            int i2;
            JSONObject jSONObject2;
            if (jSONObject == null && b.a != null) {
                jSONObject = (JSONObject) b.a.get(str);
                if (!(jSONObject == null || jSONObject.has("need_parse_simple"))) {
                    String str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_num_md5");
                    String md5 = MatchCacheManager.getMD5(str2, str3);
                    if (md5 == null || str5 == null || !md5.equals(str5)) {
                        i2 = 3;
                    } else if (i != 1) {
                        i2 = 0;
                    } else {
                        str5 = jSONObject.optString("session_reuslt");
                        try {
                            if (StringUtils.isNull(str5)) {
                                b.d.add(str);
                                i2 = 2;
                            } else {
                                jSONObject.put("session_reuslt", new JSONArray(str5));
                                b.b.put(str, jSONObject);
                                i2 = 1;
                            }
                        } catch (Throwable th) {
                            b.d.add(str);
                        }
                    }
                    b.a.remove(str);
                    jSONObject2 = jSONObject;
                    if (i2 != 1) {
                        if (!b.e.contains(str)) {
                            return null;
                        }
                        b.e.add(str);
                        BubbleTaskQueue.addDataToQueue(i2, str, str2, str3, str4, j, i, jSONObject2);
                    }
                    return jSONObject2;
                }
            }
            i2 = 0;
            jSONObject2 = jSONObject;
            if (i2 != 1) {
                if (!b.e.contains(str)) {
                    return null;
                }
                b.e.add(str);
                BubbleTaskQueue.addDataToQueue(i2, str, str2, str3, str4, j, i, jSONObject2);
            }
            return jSONObject2;
        }
    }

    public static void queryDataByMsgItem(String str, String str2, String str3, String str4, int i, long j, SdkCallBack sdkCallBack, boolean z) {
        queryDataByMsgItem(str, str2, str3, str4, i, j, sdkCallBack, z, null);
    }

    public static void queryDataByMsgItem(String str, String str2, String str3, String str4, int i, long j, SdkCallBack sdkCallBack, boolean z, Map<String, String> map) {
        if (str == null || str2 == null || StringUtils.isNull(str3)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), "phonenum is null", str, Integer.valueOf(1));
        } else if (StringUtils.isPhoneNumber(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), "phonenum is null", str, Integer.valueOf(1));
        } else {
            JSONArray jSONArray = null;
            D b = D.b(str2);
            if (b.c != null) {
                jSONArray = (JSONArray) b.c.get(str);
                if (jSONArray != null) {
                    XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), jSONArray, str, Integer.valueOf(1));
                    return;
                }
            }
            if (jSONArray == null && b.d.contains(str)) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " invalidBubbleData ", str, Integer.valueOf(1));
                return;
            }
            int i2;
            if (jSONArray == null && b.a != null) {
                JSONObject jSONObject = (JSONObject) b.a.get(str);
                if (!(jSONObject == null || jSONObject.has("need_parse_simple"))) {
                    String str5 = (String) JsonUtil.getValueFromJsonObject(jSONObject, "msg_num_md5");
                    String md5 = MatchCacheManager.getMD5(str2, str3);
                    if (md5 == null || str5 == null || !md5.equals(str5)) {
                        i2 = 3;
                    } else {
                        try {
                            jSONArray = (JSONArray) JsonUtil.getValueFromJsonObject(jSONObject, "session_reuslt");
                            if (jSONArray == null) {
                                b.d.add(str);
                                i2 = 2;
                            } else {
                                b.c.put(str, jSONArray);
                                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(0), jSONArray, str, Integer.valueOf(1));
                                return;
                            }
                        } catch (Throwable th) {
                            b.d.add(str);
                        }
                    }
                    b.a.remove(str);
                    if (i2 != 0 && i2 != 3) {
                        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " invalidBubbleData ", str, Integer.valueOf(1));
                        return;
                    } else if (!b.e.contains(str)) {
                        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " inQueueBubbleData2 ", str, Integer.valueOf(1));
                    } else if (z) {
                        b.e.add(str);
                        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-2), " need parse", str, Integer.valueOf(1));
                        a.c.execute(new c(b, str, str2, str3, j, sdkCallBack, str4, map, i));
                    } else {
                        XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-4), " is scrolling", str, Integer.valueOf(1));
                    }
                }
            }
            i2 = 0;
            if (i2 != 0) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " invalidBubbleData ", str, Integer.valueOf(1));
                return;
            }
            if (!b.e.contains(str)) {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-1), " inQueueBubbleData2 ", str, Integer.valueOf(1));
            } else if (z) {
                b.e.add(str);
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-2), " need parse", str, Integer.valueOf(1));
                a.c.execute(new c(b, str, str2, str3, j, sdkCallBack, str4, map, i));
            } else {
                XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-4), " is scrolling", str, Integer.valueOf(1));
            }
        }
    }
}
