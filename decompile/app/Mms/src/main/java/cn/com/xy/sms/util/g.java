package cn.com.xy.sms.util;

import android.app.Activity;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class g {
    private static HashSet<String> a = new HashSet();

    private static void a() {
        if (a != null && !a.isEmpty()) {
            try {
                Iterator it = a.iterator();
                while (it.hasNext()) {
                    ParseRichBubbleManager.clearCacheBubbleData((String) it.next());
                }
            } catch (Throwable th) {
            }
        }
    }

    private static void a(Activity activity) {
        try {
            Map loadDataByParam = MatchCacheManager.loadDataByParam("is_favorite=?", new String[]{"1"});
            if (loadDataByParam != null && !loadDataByParam.isEmpty()) {
                Map hashMap = new HashMap();
                a.clear();
                for (Entry entry : loadDataByParam.entrySet()) {
                    JSONObject jSONObject = (JSONObject) entry.getValue();
                    if (!(jSONObject == null || StringUtils.isNull(jSONObject.optString(NetUtil.REQ_QUERY_NUM)))) {
                        String optString = jSONObject.optString(NetUtil.REQ_QUERY_NUM);
                        a.add(optString);
                        HashMap hashMap2 = (HashMap) hashMap.get(optString);
                        if (hashMap2 != null) {
                            hashMap2.put((String) entry.getKey(), jSONObject);
                        } else {
                            hashMap2 = new HashMap();
                            hashMap2.put((String) entry.getKey(), jSONObject);
                            hashMap.put(optString, hashMap2);
                        }
                    }
                }
                for (Entry entry2 : hashMap.entrySet()) {
                    String str = (String) entry2.getKey();
                    if (!StringUtils.isNull(str)) {
                        ParseRichBubbleManager.pubBubbleData(str, (Map) entry2.getValue(), true);
                    }
                }
                DuoquUtils.getSdkDoAction().beforeInitBubbleView(activity, a);
            }
        } catch (Throwable th) {
        }
    }

    public static void a(String str, String str2, String str3) {
        MatchCacheManager.updateMarkAndFavoriteAsy(str, str2, str3, 1, 1);
    }

    private static void b(String str, String str2, String str3) {
        MatchCacheManager.updateMarkAndFavoriteAsy(str, str2, str3, 0, 0);
    }
}
