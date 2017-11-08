package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.n;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class m {
    private static String a = "phonenum";
    private static String b = "tagType";
    private static String c = "content";

    private static String a(Map<String, String[]> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        JSONArray jSONArray = new JSONArray();
        try {
            for (Entry entry : map.entrySet()) {
                String str = (String) entry.getKey();
                if (!StringUtils.isNull(str)) {
                    String[] strArr = (String[]) entry.getValue();
                    if (strArr != null && strArr.length >= 2) {
                        JSONObject jSONObject = new JSONObject();
                        Object obj = strArr[0];
                        Object obj2 = strArr[1];
                        jSONObject.put("content", obj);
                        jSONObject.put("tagType", obj2);
                        jSONObject.put(NetUtil.REQ_QUERY_NUM, str);
                        jSONArray.put(jSONObject);
                    }
                }
            }
        } catch (Throwable th) {
        }
        return jSONArray.length() != 0 ? jSONArray.toString() : null;
    }

    static /* synthetic */ JSONArray a(Object obj) {
        JSONObject jSONObject = new JSONObject((String) obj);
        if (!Constant.FIND_CMD_STATUS.equals(jSONObject.optString("status"))) {
            return null;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("items");
        if (optJSONArray == null) {
            return null;
        }
        int length = optJSONArray.length();
        for (int i = 0; i < length; i++) {
            n.a(optJSONArray.getString(i), 1);
        }
        return optJSONArray;
    }

    public static void a(Map<String, String[]> map, Map<String, String> map2, XyCallBack xyCallBack) {
        Set set = null;
        if (map == null || map.isEmpty()) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), null, "no number");
            return;
        }
        if (map != null) {
            set = map.keySet();
        }
        String a = a((Map) map);
        if (StringUtils.isNull(a)) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), set, "reqeustContent == null");
            return;
        }
        try {
            NetUtil.executeServiceHttpRequest(NetUtil.REQ_NUM_MARK, a, map2, new n(xyCallBack, set));
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), set, th.getMessage());
        }
    }

    private static JSONArray b(Object obj) {
        JSONObject jSONObject = new JSONObject((String) obj);
        if (!Constant.FIND_CMD_STATUS.equals(jSONObject.optString("status"))) {
            return null;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("items");
        if (optJSONArray == null) {
            return null;
        }
        int length = optJSONArray.length();
        for (int i = 0; i < length; i++) {
            n.a(optJSONArray.getString(i), 1);
        }
        return optJSONArray;
    }
}
