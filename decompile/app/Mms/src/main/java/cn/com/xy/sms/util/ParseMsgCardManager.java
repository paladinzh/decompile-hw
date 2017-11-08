package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.b.a;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.util.PopupUtil;
import cn.com.xy.sms.sdk.util.y;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseMsgCardManager {
    private static JSONObject a(Map<String, Object> map, Map<String, String> map2) {
        boolean z = true;
        boolean z2 = false;
        if (map != null) {
            JSONObject b;
            try {
                String str = (String) map.get("title_num");
                Object c = a.c(str);
                if (c != null) {
                    if (!c.isEmpty()) {
                        z2 = true;
                    }
                }
                if (map2 != null && map2.containsKey("isNeedRes")) {
                    z = "true".equalsIgnoreCase((String) map2.get("isNeedRes"));
                }
                if (z) {
                    if (z2) {
                        if (!PopupUtil.isPopupAble(map, str)) {
                        }
                    }
                }
                if (c != null) {
                    map.putAll(c);
                }
                b = b(map);
                return b;
            } catch (Throwable th) {
                b = th;
            } finally {
                y.a();
            }
        }
        y.a();
        return null;
    }

    private static boolean a(Map<String, String> map) {
        if (map == null || !map.containsKey("isNeedRes")) {
            return true;
        }
        return "true".equalsIgnoreCase((String) map.get("isNeedRes"));
    }

    private static JSONObject b(Map<String, Object> map) {
        JSONObject jSONObject = new JSONObject();
        for (Entry entry : map.entrySet()) {
            try {
                jSONObject.put((String) entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                return null;
            }
        }
        MatchCacheManager.removeUselessKey(jSONObject);
        return jSONObject;
    }

    public static JSONObject parseMsgForCard(Context context, String str, String str2, String str3, Map<String, String> map) {
        if (context == null) {
            throw new Exception(" Context is null.");
        } else if (str == null) {
            throw new Exception(" phoneNumber is null.");
        } else if (str3 != null) {
            JSONObject a;
            try {
                Map a2 = ParseManager.a(context, str, str2, str3, 0, map);
                if (a2 != null) {
                    if (ParseBubbleManager.getParseStatu(a2) != -1) {
                        Map handerBubbleValueMap = DexUtil.handerBubbleValueMap(a2);
                        if (handerBubbleValueMap == null || handerBubbleValueMap.isEmpty()) {
                            return null;
                        }
                        a = a(a2, map);
                        return a;
                    }
                }
            } catch (Throwable th) {
            }
            a = null;
            return a;
        } else {
            throw new Exception(" smsContent is null.");
        }
    }

    public static JSONArray parseMsgForCardArray(Context context, String str, String str2, String str3, Map<String, String> map) {
        JSONObject parseMsgForCard = parseMsgForCard(context, str, str2, str3, map);
        return parseMsgForCard != null ? DexUtil.parseMsgForCardArray(parseMsgForCard, map) : null;
    }
}
