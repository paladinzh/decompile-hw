package cn.com.xy.sms.sdk.util;

import android.content.Context;
import android.content.Intent;
import cn.com.xy.sms.sdk.action.AbsSdkDoAction;
import cn.com.xy.sms.sdk.b.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.db.base.BaseManager;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.SceneRule;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.service.a.b;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.util.ParseBubbleManager;
import cn.com.xy.sms.util.ParseRichBubbleManager;
import cn.com.xy.sms.util.w;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class PopupUtil {
    private static Pattern a = Pattern.compile("(?<![\\-0-9])0\\d{2,3}-?\\d{7,8}(?!\\d)");

    private static boolean a(String str) {
        try {
            if (a.matcher(str).find()) {
                return true;
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public static int getActionCode(String str) {
        try {
            if (StringUtils.isNull(str)) {
                return -1;
            }
            if (!str.equalsIgnoreCase(Constant.URLS)) {
                if (!(str.equalsIgnoreCase("reply_sms") || str.equalsIgnoreCase("reply_sms_fwd"))) {
                    if (str.equalsIgnoreCase("call_phone") || "call".equalsIgnoreCase(str)) {
                        return 2;
                    }
                    if (!str.equalsIgnoreCase("reply_sms_open")) {
                        if (!(str.equalsIgnoreCase("access_url") || str.equalsIgnoreCase("down_url") || "download".equalsIgnoreCase(str) || str.equalsIgnoreCase("send_email") || str.equalsIgnoreCase("weibo_url"))) {
                            return (str.equalsIgnoreCase("map_site") || "open_map".equalsIgnoreCase(str) || "open_map_list".equalsIgnoreCase(str) || "open_map_browser".equalsIgnoreCase(str)) ? 4 : (str.equalsIgnoreCase("chong_zhi") || str.equalsIgnoreCase("recharge") || "zfb_recharge".equalsIgnoreCase(str) || "WEB_CHONG_ZHI".equalsIgnoreCase(str) || "WEB_RECHARGE_CHOOSE".equalsIgnoreCase(str)) ? 5 : ("repayment".equalsIgnoreCase(str) || "zfb_repayment".equals(str) || "WEB_REPAYMENT".equalsIgnoreCase(str) || "WEB_REPAYMENT_CHOOSE".equalsIgnoreCase(str)) ? 6 : !str.equalsIgnoreCase("copy_code") ? !"open_app".equalsIgnoreCase(str) ? ("time_remind".equalsIgnoreCase(str) || "sdk_time_remind".equalsIgnoreCase(str)) ? 10 : !"pay_water_gas".equalsIgnoreCase(str) ? ("WEB_TRAFFIC_ORDER".equalsIgnoreCase(str) || "WEB_TRAFFIC_CHOOSE".equalsIgnoreCase(str) || "WEB_PURCHASE".equalsIgnoreCase(str)) ? 12 : !"WEB_QUERY_EXPRESS_FLOW".equalsIgnoreCase(str) ? !"WEB_QUERY_FLIGHT_TREND".equalsIgnoreCase(str) ? !"WEB_INSTALMENT_PLAN".equalsIgnoreCase(str) ? !"WEB_TRAIN_STATION".equalsIgnoreCase(str) ? ("WEB_NEAR_SITE".equalsIgnoreCase(str) || "near_site".equalsIgnoreCase(str)) ? 17 : !"WEB_LIVE_CHOOSE".equalsIgnoreCase(str) ? 7 : 18 : 16 : 15 : 14 : 13 : 11 : 9 : 8;
                        }
                    }
                }
                return 1;
            }
            return 3;
        } catch (Throwable th) {
            return -1;
        }
    }

    public static BusinessSmsMessage getMsg(String str, String str2) {
        if (StringUtils.isNull(str) || StringUtils.isNull(str2)) {
            return null;
        }
        BusinessSmsMessage businessSmsMessage = new BusinessSmsMessage();
        businessSmsMessage.setOriginatingAddress(str);
        businessSmsMessage.setMessageBody(str2);
        businessSmsMessage.isBgVis = true;
        return businessSmsMessage;
    }

    public static Map<String, Object> getResultMap(Map<String, Object> map, String str, String str2, Map<String, Object> map2, Context context) {
        if (!(ViewUtil.getChannelType() != 3 || ((Boolean) map.get("Result")).booleanValue() || str == null || map2 == null)) {
            Object obj = map2.get("msgId");
            Object obj2 = map2.get("simIndex");
            Object obj3 = map2.get("simName");
            Object obj4 = map2.get("msgTime");
            Object obj5 = map2.get("uri");
            map2.clear();
            map2.put("msgId", obj);
            map2.put("simIndex", obj2);
            map2.put("simName", obj3);
            map2.put("msgTime", obj4);
            map2.put("phoneNumber", str);
            map2.put("smsContent", str2);
            map2.put("uri", obj5);
            startBusinessReceiveSmsActivity(context, null, map2);
            map.put("Result", Boolean.valueOf(true));
        }
        return map;
    }

    public static Map<String, Object> getResultMap(boolean z, boolean z2) {
        Map<String, Object> hashMap = new HashMap();
        hashMap.put("Result", Boolean.valueOf(z));
        hashMap.put("recogResult", Boolean.valueOf(z2));
        return hashMap;
    }

    public static String getValue() {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                stringBuilder.append(DuoquUtils.getCode(i));
            }
            return StringUtils.decode(StringUtils.handlerAssemble(stringBuilder.toString()));
        } catch (Throwable th) {
            return "";
        }
    }

    public static boolean isEnterpriseSms(Context context, String str, String str2, Map<String, String> map) {
        String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
        if (StringUtils.isNull(phoneNumberNo86)) {
            return false;
        }
        int i;
        String replace = phoneNumberNo86.replace(" ", "").replace("-", "");
        String[] strArr = new String[]{"10088", "10198", "101901", "123662", "12306", "12110110", "121100020", "11888", "11868", "1186666", "118388", "118200", "118114", "118100", "118067", "11803080", "11185", "11183", "13800138000", "095583", "1252004411", StringUtils.phoneFiled12520, "12520029", "12520035", "125200353", "125200352", "125200304", "125200351", "12520010", "12520021", "125200303", "1252003300000", "12520032", "125200302", "12520028", "12520038", "12520024", "12520036", "125200301", "12520027", "125200354", "1252003300000", "053287003810"};
        for (i = 0; i < 43; i++) {
            if (strArr[i].equals(replace)) {
                return true;
            }
        }
        strArr = new String[]{"96", "95", "106", "10178", "10086", "1006", "1001", "1000", "116114"};
        for (i = 0; i < 9; i++) {
            if (replace.startsWith(strArr[i])) {
                return true;
            }
        }
        return (replace.startsWith("12520030") && replace.length() <= 10) ? true : (replace.startsWith("12520036") && replace.length() == 19) ? StringUtils.sj(replace.replace("12520036", "")) : (map == null || !map.containsKey("FIXED_PHONE") || "false".equalsIgnoreCase((String) map.get("FIXED_PHONE"))) ? false : a(replace);
    }

    public static boolean isFixedPhone(String str) {
        try {
            ClassLoader dexClassLoader = DexUtil.getDexClassLoader();
            if (dexClassLoader != null) {
                Class loadClass = dexClassLoader.loadClass("cn.com.xy.sms.sdk.Iservice.OnlineParseImpl");
                return ((Boolean) loadClass.getMethod("isFixedPhone", new Class[]{String.class}).invoke(loadClass, new Object[]{str})).booleanValue();
            }
        } catch (Throwable th) {
        }
        return a(str);
    }

    public static boolean isPopupAble(Map<String, Object> map, String str) {
        try {
            Class cls = Class.forName(DuoquUtils.getSdkDoAction().getConfig(1, null));
            Method method = cls.getMethod("isPopupAble", new Class[]{Map.class, String.class});
            if (method != null) {
                return ((Boolean) method.invoke(cls, new Object[]{map, str})).booleanValue();
            }
        } catch (Throwable th) {
        }
        return false;
    }

    public static void isUseDefaultPopup(BusinessSmsMessage businessSmsMessage, Map<String, Object> map, String str) {
        if (!(businessSmsMessage == null || map == null)) {
            try {
                if (Constant.isDefaultImageExist() && map.containsKey("view_forceToDefault_popup")) {
                    Constant.getContext();
                    HashMap a = a.a(str);
                    if (a != null) {
                        businessSmsMessage.imagePathMap = a;
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Map<String, Object> parseMsgToBubbleCardResult(Context context, String str, String str2, String str3, String str4, long j, byte b, Map<String, Object> map, boolean z) {
        if (map != null) {
            try {
                map.remove("viewPartParam");
                Map<String, Object> handerBubbleValueMap = DexUtil.handerBubbleValueMap(map);
                long currentTimeMillis = System.currentTimeMillis();
                int i = 0;
                if (handerBubbleValueMap == null) {
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " parseMsgToBubbleCardResult is null", null);
                } else {
                    Object obj;
                    String phoneNumberNo86;
                    JSONObject jSONObject;
                    String md5;
                    long insertOrUpdate;
                    if (map.containsKey(Constant.KEY_IS_SAFE_VERIFY_CODE)) {
                        handerBubbleValueMap.put(Constant.KEY_IS_SAFE_VERIFY_CODE, String.valueOf(map.get(Constant.KEY_IS_SAFE_VERIFY_CODE)));
                    }
                    String str5 = (String) handerBubbleValueMap.get("title_num");
                    Object c = a.c(str5);
                    if (c != null) {
                        if (!c.isEmpty()) {
                            obj = 1;
                            if (ViewUtil.getChannelType() == 7 || ViewUtil.getChannelType() == 17 || obj != null) {
                                if (obj != null) {
                                    handerBubbleValueMap.putAll(c);
                                }
                                if (isPopupAble(handerBubbleValueMap, str5)) {
                                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " parseMsgToBubbleCardResult  isPopupAble is faild", null);
                                    i = -3;
                                } else {
                                    phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str2);
                                    handerBubbleValueMap.remove("viewPartParam");
                                    jSONObject = new JSONObject();
                                    for (Entry entry : handerBubbleValueMap.entrySet()) {
                                        try {
                                            jSONObject.put((String) entry.getKey(), entry.getValue());
                                        } catch (JSONException e) {
                                            y.a();
                                            return null;
                                        }
                                    }
                                    md5 = MatchCacheManager.getMD5(phoneNumberNo86, str4);
                                    MatchCacheManager.removeUselessKey(jSONObject);
                                    insertOrUpdate = StringUtils.isNull(md5) ? 0 : MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, "msg_num_md5", md5, NetUtil.REQ_QUERY_NUM, phoneNumberNo86, ParseItemManager.SCENE_ID, str5, "msg_id", str, "bubble_result", jSONObject.toString(), "save_time", String.valueOf(j), "bubble_lasttime", String.valueOf(System.currentTimeMillis()), "urls", jSONObject.optString(Constant.URLS)), 2);
                                    handerBubbleValueMap.clear();
                                    if (b == (byte) 1) {
                                        handerBubbleValueMap.put("CACHE_SDK_MSG_RESULT", jSONObject.toString());
                                    } else {
                                        handerBubbleValueMap.put("CACHE_SDK_MSG_ID", Long.valueOf(insertOrUpdate));
                                    }
                                    handerBubbleValueMap.put("View_fdes", JsonUtil.getValueFromJsonObject(jSONObject, "View_fdes"));
                                    if (z) {
                                        ParseRichBubbleManager.addEffectiveBubbleData(phoneNumberNo86, str, jSONObject);
                                    }
                                    handerBubbleValueMap.put("BUBBLE_JSON_RESULT", jSONObject);
                                    handerBubbleValueMap.put("parseStatu", Integer.valueOf(1));
                                    y.a();
                                    return handerBubbleValueMap;
                                }
                            }
                            DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " parseMsgToBubbleCardResult  BubleSmsTitle is null", null);
                            i = -2;
                            currentTimeMillis = MatchCacheManager.queryDataCount(str, MatchCacheManager.getMD5(str2, str4)) != 0 ? System.currentTimeMillis() - (DexUtil.getUpdateCycleByType(14, 21600000) - 420000) : System.currentTimeMillis() - (DexUtil.getUpdateCycleByType(14, 21600000) - 120000);
                        }
                    }
                    obj = null;
                    if (ViewUtil.getChannelType() == 7) {
                        DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " parseMsgToBubbleCardResult  BubleSmsTitle is null", null);
                        i = -2;
                        if (MatchCacheManager.queryDataCount(str, MatchCacheManager.getMD5(str2, str4)) != 0) {
                        }
                    }
                    if (obj != null) {
                        handerBubbleValueMap.putAll(c);
                    }
                    if (isPopupAble(handerBubbleValueMap, str5)) {
                        phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str2);
                        handerBubbleValueMap.remove("viewPartParam");
                        jSONObject = new JSONObject();
                        for (Entry entry2 : handerBubbleValueMap.entrySet()) {
                            jSONObject.put((String) entry2.getKey(), entry2.getValue());
                        }
                        md5 = MatchCacheManager.getMD5(phoneNumberNo86, str4);
                        MatchCacheManager.removeUselessKey(jSONObject);
                        if (StringUtils.isNull(md5)) {
                        }
                        handerBubbleValueMap.clear();
                        if (b == (byte) 1) {
                            handerBubbleValueMap.put("CACHE_SDK_MSG_ID", Long.valueOf(insertOrUpdate));
                        } else {
                            handerBubbleValueMap.put("CACHE_SDK_MSG_RESULT", jSONObject.toString());
                        }
                        handerBubbleValueMap.put("View_fdes", JsonUtil.getValueFromJsonObject(jSONObject, "View_fdes"));
                        if (z) {
                            ParseRichBubbleManager.addEffectiveBubbleData(phoneNumberNo86, str, jSONObject);
                        }
                        handerBubbleValueMap.put("BUBBLE_JSON_RESULT", jSONObject);
                        handerBubbleValueMap.put("parseStatu", Integer.valueOf(1));
                        y.a();
                        return handerBubbleValueMap;
                    }
                    DuoquUtils.getSdkDoAction().logInfo("XIAOYUAN", " parseMsgToBubbleCardResult  isPopupAble is faild", null);
                    i = -3;
                    if (MatchCacheManager.queryDataCount(str, MatchCacheManager.getMD5(str2, str4)) != 0) {
                    }
                }
                if (!StringUtils.isNull(MatchCacheManager.getMD5(str2, str4))) {
                    MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, "msg_num_md5", MatchCacheManager.getMD5(str2, str4), NetUtil.REQ_QUERY_NUM, StringUtils.getPhoneNumberNo86(str2), ParseItemManager.SCENE_ID, "", "msg_id", str, "bubble_result", "", "save_time", String.valueOf(j), "bubble_lasttime", String.valueOf(currentTimeMillis)), 2);
                }
                if (z) {
                    ParseRichBubbleManager.addInvalidBubbleData(str2, str);
                }
                if (i == 0 || handerBubbleValueMap == null) {
                    y.a();
                    return null;
                }
                handerBubbleValueMap.clear();
                handerBubbleValueMap.put("parseStatu", Integer.valueOf(i));
                y.a();
                return handerBubbleValueMap;
            } catch (Throwable th) {
                y.a();
            }
        } else {
            y.a();
            return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Map<String, Object> parseMsgToPopupWindow(Context context, String str, String str2, Map<String, Object> map) {
        if (map == null) {
            y.a();
        } else {
            try {
                String str3 = (String) map.get("title_num");
                Map<String, Object> resultMap;
                if (StringUtils.isNull(str3)) {
                    resultMap = getResultMap(false, true);
                    y.a();
                    return resultMap;
                }
                HashMap b;
                Constant.popupDefault = false;
                SceneRule sceneRule = SceneconfigUtil.getSceneRule(str3, 0);
                if (Constant.Test) {
                    Constant.getContext();
                    b = a.b(str3);
                    if (b == null) {
                        Constant.getContext();
                        b = a.a(str3);
                        Constant.popupDefault = true;
                    }
                } else if (sceneRule == null) {
                    Constant.getContext();
                    b = a.a(str3);
                    Constant.popupDefault = true;
                } else {
                    Constant.getContext();
                    b = a.a(str3, sceneRule.Scene_page_config, sceneRule.Func_config);
                    if (b == null) {
                        Constant.getContext();
                        b = a.a(str3);
                        Constant.popupDefault = true;
                    }
                }
                if (b == null) {
                    resultMap = getResultMap(false, true);
                    y.a();
                    return resultMap;
                }
                BusinessSmsMessage businessSmsMessage = new BusinessSmsMessage();
                if (Constant.popupDefault) {
                    if (Constant.isDefaultImageExist()) {
                        map.put("view_default_popup", "true");
                    } else {
                        resultMap = getResultMap(false, false);
                        y.a();
                        return resultMap;
                    }
                }
                businessSmsMessage.valueMap = map;
                if (businessSmsMessage.extendParamMap == null) {
                    businessSmsMessage.extendParamMap = new HashMap();
                }
                businessSmsMessage.extendParamMap.putAll(map);
                businessSmsMessage.setOriginatingAddress(str);
                businessSmsMessage.setMessageBody(str2);
                businessSmsMessage.isBgVis = true;
                businessSmsMessage.imagePathMap = b;
                businessSmsMessage.setTitleNo(str3);
                String str4 = (String) map.get("simIndex");
                if (!StringUtils.isNull(str4)) {
                    businessSmsMessage.simIndex = Integer.valueOf(str4).intValue();
                }
                str4 = (String) map.get("msgTime");
                if (!StringUtils.isNull(str4)) {
                    businessSmsMessage.msgTime = Long.valueOf(str4).longValue();
                }
                businessSmsMessage.simName = (String) map.get("simName");
                DexUtil.handerValueMap(map, str3);
                isUseDefaultPopup(businessSmsMessage, map, str3);
                if (isPopupAble(map, str3)) {
                    if (ViewUtil.getChannelType() == 5) {
                        if (!Constant.popupDefault) {
                            if (!map.containsKey("view_forceToDefault_popup")) {
                            }
                        }
                        resultMap = getResultMap(false, true);
                        y.a();
                        return resultMap;
                    }
                    i.a(new k(4, "titleNo", str3));
                    startBusinessReceiveSmsActivity(context, businessSmsMessage, map);
                    resultMap = getResultMap(true, true);
                    y.a();
                    return resultMap;
                }
                popupDefault(businessSmsMessage, map, str3);
                if (ViewUtil.getChannelType() == 5) {
                    if (!Constant.popupDefault) {
                        if (!map.containsKey("view_forceToDefault_popup")) {
                        }
                    }
                    resultMap = getResultMap(false, true);
                    y.a();
                    return resultMap;
                }
                if (isPopupAble(map, str3)) {
                    i.a(new k(4, "titleNo", str3));
                    startBusinessReceiveSmsActivity(context, businessSmsMessage, map);
                    resultMap = getResultMap(true, true);
                    y.a();
                    return resultMap;
                }
                resultMap = getResultMap(false, true);
                y.a();
                return resultMap;
            } catch (Throwable th) {
                y.a();
            }
        }
        return getResultMap(false, true);
    }

    public static Map<String, Object> parseMsgToSimpleBubbleResult(Context context, String str, String str2, String str3, String str4, long j, byte b, Map<String, Object> map, boolean z, Map<String, String> map2) {
        if (map == null) {
            return null;
        }
        Map<String, Object> hashMap;
        String a = b.a(map, map2);
        if (b == (byte) 1) {
            long j2 = -1;
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
                strArr[7] = a != null ? a : "";
                strArr[8] = "save_time";
                strArr[9] = String.valueOf(j);
                strArr[10] = "session_lasttime";
                strArr[11] = String.valueOf(System.currentTimeMillis());
                j2 = MatchCacheManager.insertOrUpdate(BaseManager.getContentValues(null, strArr), 1);
            }
            if (a != null) {
                hashMap = new HashMap();
                hashMap.put("CACHE_SDK_MSG_ID", Long.valueOf(j2));
                hashMap.put("CACHE_SDK_MSG_SIMPLE_RESULT", a);
                if (a == null) {
                    ParseBubbleManager.addEffectiveBubbleData(str2, str, new JSONArray(a));
                    return hashMap;
                }
                ParseBubbleManager.addInvalidBubbleData(str2, str);
                return hashMap;
            }
        } else if (a != null) {
            hashMap = new HashMap();
            hashMap.put("CACHE_SDK_MSG_SIMPLE_RESULT", a);
            if (z && ParseBubbleManager.equalPhoneNum(str2)) {
                if (a == null) {
                    try {
                        ParseBubbleManager.addInvalidBubbleData(str2, str);
                    } catch (Throwable th) {
                        ParseBubbleManager.addInvalidBubbleData(str2, str);
                    }
                } else {
                    ParseBubbleManager.addEffectiveBubbleData(str2, str, new JSONArray(a));
                }
            }
            return hashMap;
        }
        hashMap = null;
        if (a == null) {
            ParseBubbleManager.addInvalidBubbleData(str2, str);
            return hashMap;
        }
        ParseBubbleManager.addEffectiveBubbleData(str2, str, new JSONArray(a));
        return hashMap;
    }

    public static void popupDefault(BusinessSmsMessage businessSmsMessage, Map<String, Object> map, String str) {
        if (!(businessSmsMessage == null || map == null)) {
            try {
                map.put("view_forceToDefault_popup", "true");
                if (ViewUtil.getChannelType() == 2 || ViewUtil.getChannelType() == 8) {
                    map.put("View_fdes", "H103102;B502513,10236113;F904");
                } else if (ViewUtil.getChannelType() != 5) {
                    map.put("View_fdes", "H101;B502,11125213;F901");
                } else {
                    map.put("View_fdes", "H113;B502,10340013;F908906");
                }
                map.put("view_title_name", map.get("title_name"));
                map.put("View_viewid", "001");
                if (Constant.isDefaultImageExist()) {
                    Constant.getContext();
                    HashMap a = a.a(str);
                    if (a != null) {
                        businessSmsMessage.imagePathMap = a;
                    }
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void startBusinessReceiveSmsActivity(Context context, BusinessSmsMessage businessSmsMessage, Map<String, Object> map) {
        String a = w.a();
        try {
            ConversationManager.saveLogIn(a, "cn.com.xy.sms.sdk.util.PopupUtil", "startBusinessReceiveSmsActivity", context, businessSmsMessage, map);
            if (businessSmsMessage == null) {
                PopupMsgManager.addThirdPopupMsgData(map);
            } else {
                businessSmsMessage.valueMap = map;
                PopupMsgManager.businessSmsList.addLast(businessSmsMessage);
            }
            i.a(new k(12, ParseItemManager.STATE, "128"));
            Intent intent = new Intent();
            intent.setClassName(context, "cn.com.xy.sms.sdk.ui.popu.BusinessReceiveSmsActivity");
            intent.setFlags(268435456);
            context.startActivity(intent);
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.util.PopupUtil", "startBusinessReceiveSmsActivity", context, businessSmsMessage, map);
        } catch (Throwable th) {
            ConversationManager.saveLogOut(a, "cn.com.xy.sms.sdk.util.PopupUtil", "startBusinessReceiveSmsActivity", context, businessSmsMessage, map);
        }
    }

    public static boolean startWebActivity(Context context, JSONObject jSONObject, String str, String str2) {
        return AbsSdkDoAction.openStartWebActivity(context, jSONObject, str, str2, null);
    }
}
