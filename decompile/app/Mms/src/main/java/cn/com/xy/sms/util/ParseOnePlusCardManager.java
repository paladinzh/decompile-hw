package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.db.ParseItemManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.net.util.n;
import cn.com.xy.sms.sdk.queue.i;
import cn.com.xy.sms.sdk.queue.k;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseOnePlusCardManager {
    public static Map<String, Object> parseMsgForCard(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map<String, Object> map2;
            Map hashMap = map != null ? map : new HashMap();
            if (!hashMap.containsKey("PARSE_TIME_OUT")) {
                hashMap.put("PARSE_TIME_OUT", "15000");
            }
            if (n.a((byte) 8)) {
                Map a = ParseManager.a(context, str, str2, str3, 0, hashMap);
                if (!(a == null || ParseBubbleManager.getParseStatu(a) == -1)) {
                    a.put("msgTime", hashMap.get("msgTime"));
                    Map<String, Object> handerValueMap = DexUtil.handerValueMap(a);
                    if (handerValueMap != null) {
                        i.a(new k(12, ParseItemManager.STATE, "32"));
                        i.a(new k(12, ParseItemManager.STATE, "32"));
                        JSONObject jSONObject = (JSONObject) handerValueMap.get("card_content");
                        if (jSONObject == null) {
                            map2 = handerValueMap;
                            if (map2 == null) {
                                map2 = new HashMap();
                                map2.put("Result", Boolean.valueOf(false));
                            }
                            return map2;
                        }
                        Integer num = (Integer) jSONObject.get("card_type");
                        JSONObject jSONObject2 = new JSONObject();
                        jSONObject2.put("msgid", hashMap.get("msgid"));
                        jSONObject2.put("phone", hashMap.get("phone"));
                        jSONObject2.put("content", hashMap.get("content"));
                        jSONObject2.put("msgTime", hashMap.get("msgTime"));
                        jSONObject2.put("smsdate", hashMap.get("smsdate"));
                        jSONObject.put("msgid", hashMap.get("msgid"));
                        jSONObject.put("smsdate", hashMap.get("smsdate"));
                        jSONObject2.put("result", jSONObject.toString());
                        long createCard = DuoquUtils.getSdkDoAction().createCard(jSONObject2, num.intValue(), hashMap);
                        handerValueMap.put("Result", Boolean.valueOf(((createCard > 0 ? 1 : (createCard == 0 ? 0 : -1)) <= 0 ? 1 : null) == null));
                        handerValueMap.put("id", new StringBuilder(String.valueOf(createCard)).toString());
                        return handerValueMap;
                    }
                    map2 = new HashMap();
                    map2.put("Result", Boolean.valueOf(false));
                    return map2;
                }
            }
            map2 = null;
            if (map2 == null) {
                map2 = new HashMap();
                map2.put("Result", Boolean.valueOf(false));
            }
            return map2;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static Map<String, Object> parseMsgForCardNew(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            Map hashMap = map != null ? map : new HashMap();
            if (!hashMap.containsKey("PARSE_TIME_OUT")) {
                hashMap.put("PARSE_TIME_OUT", "15000");
            }
            if (n.a((byte) 8)) {
                Map a = ParseManager.a(context, str, str2, str3, 0, hashMap);
                if (!(a == null || ParseBubbleManager.getParseStatu(a) == -1)) {
                    a.put("msgTime", hashMap.get("msgTime"));
                    Map<String, Object> handerValueMap = DexUtil.handerValueMap(a);
                    if (handerValueMap != null) {
                        i.a(new k(12, ParseItemManager.STATE, "32"));
                        i.a(new k(12, ParseItemManager.STATE, "32"));
                        String substring = handerValueMap.get("title_num").toString().substring(0, 5);
                        Integer valueOf = !(handerValueMap.get("card_type") instanceof Integer) ? Integer.valueOf(Integer.parseInt((String) handerValueMap.get("card_type"))) : (Integer) handerValueMap.get("card_type");
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("msgid", hashMap.get("msgid"));
                        jSONObject.put("phone", hashMap.get("phone"));
                        jSONObject.put("content", hashMap.get("content"));
                        jSONObject.put("msgTime", hashMap.get("msgTime"));
                        jSONObject.put("smsdate", hashMap.get("smsdate"));
                        handerValueMap.put("msgid", hashMap.get("msgid"));
                        handerValueMap.put("smsdate", hashMap.get("smsdate"));
                        jSONObject.put("result", new JSONObject(handerValueMap).toString());
                        jSONObject.put("title_num", substring);
                        jSONObject.put("card_key_index1", handerValueMap.get("card_key_index1"));
                        jSONObject.put("card_key_index2", handerValueMap.get("card_key_index2"));
                        long createCard = DuoquUtils.getSdkDoAction().createCard(jSONObject, valueOf.intValue(), hashMap);
                        handerValueMap.put("Result", Boolean.valueOf(((createCard > 0 ? 1 : (createCard == 0 ? 0 : -1)) <= 0 ? 1 : null) == null));
                        handerValueMap.put("id", new StringBuilder(String.valueOf(createCard)).toString());
                        return handerValueMap;
                    }
                    Map<String, Object> hashMap2 = new HashMap();
                    hashMap2.put("Result", Boolean.valueOf(false));
                    return hashMap2;
                }
            }
            return null;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }
}
