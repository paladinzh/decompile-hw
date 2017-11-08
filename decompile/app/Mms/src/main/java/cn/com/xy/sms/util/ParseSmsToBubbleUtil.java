package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.PhoneSmsParseManager;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class ParseSmsToBubbleUtil {
    public static final byte RETURN_CACHE_SDK_MSG_ID = (byte) 1;
    public static final byte RETURN_CACHE_SDK_MSG_VALUE = (byte) 2;

    private static Map<String, Object> a(String str, String str2, String str3, String str4, long j, Map<String, Object> map, boolean z) {
        Map<String, Object> map2 = null;
        try {
            map2 = PopupUtil.parseMsgToBubbleCardResult(Constant.getContext(), str, str2, str4, str3, j, (byte) 1, map, z);
        } catch (Throwable th) {
        }
        return map2;
    }

    private static Map<String, Object> a(String str, String str2, String str3, String str4, long j, Map<String, Object> map, boolean z, Map<String, String> map2) {
        Map<String, Object> map3 = null;
        try {
            map3 = PopupUtil.parseMsgToSimpleBubbleResult(Constant.getContext(), str, str2, str4, str3, j, (byte) 1, map, z, map2);
        } catch (Throwable th) {
        }
        return map3;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Map<String, Object> b(String str, String str2, String str3, String str4, long j, int i, boolean z, boolean z2, Map<String, Object> map, Map<String, String> map2) {
        Map<String, Object> a;
        Map<String, Object> map3 = null;
        if (map != null) {
            Map<String, Object> map4;
            switch (i) {
                case 1:
                    try {
                        a = a(str, str2, str3, str4, j, map, z2, map2);
                        break;
                    } catch (Throwable th) {
                    }
                case 2:
                    a = a(str, str2, str3, str4, j, map, z2);
                    break;
                case 3:
                    a = a(str, str2, str3, str4, j, map, z2, map2);
                    try {
                        map3 = a(str, str2, str3, str4, j, map, z2);
                        if (a != null) {
                            if (!(a == null || map3 == null || map3.isEmpty())) {
                                a.putAll(map3);
                                map4 = a;
                                break;
                            }
                        }
                    } catch (Throwable th2) {
                        map3 = a;
                    }
                    a = map3;
                    break;
                default:
                    map4 = null;
                    break;
            }
        }
        a = null;
        String str5 = null;
        if (a != null) {
            str5 = (String) a.get("View_fdes");
        }
        PhoneSmsParseManager.addInsertQueue(str2, !z ? j : 0, str5, null, null);
        return a;
    }

    protected static void backGroundHandleMapByType(Map<String, String> map, Map<String, Object> map2) {
        if (map != null) {
            String str = (String) map.get("handle_type");
            if (!StringUtils.isNull(str)) {
                new s(str, map2).start();
            }
        }
    }

    protected static void backGroundParseSmsBubble(String str, String str2, String str3, String str4, long j, boolean z, boolean z2, Map<String, Object> map, Map<String, String> map2) {
        new r(str, str2, str3, str4, j, z, z2, map, map2).start();
    }

    public static void beforeHandParseReceiveSms(int i, int i2) {
        beforeHandParseReceiveSms(null, i, i2, false);
    }

    public static void beforeHandParseReceiveSms(String str, int i, int i2, boolean z) {
        boolean z2 = false;
        if (StringUtils.isNull(str)) {
            z2 = true;
        }
        a.a(z2, str, i, i2, z);
    }

    public static Map<String, Object> parseSmsToBubbleResult(String str, String str2, String str3, String str4, long j, int i, boolean z, boolean z2, HashMap<String, Object> hashMap) {
        return parseSmsToBubbleResultMap(str, str2, str3, str4, j, i, z, z2, XyUtil.changeObjMapToStrMap(hashMap));
    }

    public static Map<String, Object> parseSmsToBubbleResultMap(String str, String str2, String str3, String str4, long j, int i, boolean z, boolean z2, Map<String, String> map) {
        try {
            Map putValueToMap = ParseManager.putValueToMap(map, "msgId", str);
            if (putValueToMap != null) {
                String str5 = null;
                if (putValueToMap.containsKey("msgTime")) {
                    str5 = (String) putValueToMap.get("msgTime");
                }
                if (StringUtils.isNull(str5)) {
                    putValueToMap.put("msgTime", String.valueOf(j));
                }
                if (DuoquUtils.getSdkDoAction().needRecognisedValue().booleanValue()) {
                    putValueToMap.put("parse_recognise_value", "true");
                }
            }
            Map a = ParseManager.a(Constant.getContext(), str2, str4, str3, j, putValueToMap);
            if (ParseBubbleManager.getParseStatu(a) == -1) {
                return null;
            }
            if (i != 4) {
                return b(str, str2, str3, str4, j, i, z, z2, a, putValueToMap);
            }
            if (a == null) {
                return null;
            }
            Map<String, Object> hashMap = new HashMap();
            hashMap.putAll(a);
            backGroundParseSmsBubble(str, str2, str3, str4, j, z, z2, a, putValueToMap);
            return hashMap;
        } catch (Throwable th) {
            return null;
        }
    }
}
