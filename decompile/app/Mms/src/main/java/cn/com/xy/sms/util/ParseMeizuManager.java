package cn.com.xy.sms.util;

import android.content.Context;
import android.text.TextUtils;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseMeizuManager {
    public static final String SMS_BALANCE = "1";
    public static final String SMS_FLOW = "2";
    public static final String SMS_FLOW_FOUR = "23";
    public static final String SMS_FLOW_THREE = "22";
    public static final String SMS_FLOW_TWO = "21";
    private static final String a = "查询余额";
    private static final String b = "查询流量";
    private static final String c = "查2/3G流量";
    private static final String d = "查3/4G流量";
    private static final String e = "查询2G流量";
    private static final String f = "查询3G流量";
    private static final String g = "查询4G流量";
    private static final String h = "查询充值";
    private static final String i = "查询业务";

    private static String a(String str, JSONObject jSONObject) {
        if ("1".equals(str) && a.equals(jSONObject.getString("name"))) {
            return jSONObject.getString("action_data");
        }
        if ("2".equals(str) || SMS_FLOW_TWO.equals(str) || SMS_FLOW_THREE.equals(str) || SMS_FLOW_FOUR.equals(str)) {
            if (b.equals(jSONObject.getString("name"))) {
                return jSONObject.getString("action_data");
            }
        }
        if (SMS_FLOW_TWO.equals(str)) {
            if (e.equals(jSONObject.getString("name")) || c.equals(jSONObject.getString("name"))) {
                return jSONObject.getString("action_data");
            }
        }
        if (SMS_FLOW_THREE.equals(str)) {
            if (f.equals(jSONObject.getString("name")) || c.equals(jSONObject.getString("name")) || d.equals(jSONObject.getString("name"))) {
                return jSONObject.getString("action_data");
            }
        }
        if (SMS_FLOW_FOUR.equals(str)) {
            if (g.equals(jSONObject.getString("name")) || d.equals(jSONObject.getString("name"))) {
                return jSONObject.getString("action_data");
            }
        }
        return null;
    }

    public static String querySmsOrderActionData(Context context, String str, String str2, int i, String str3, Map<String, String> map) {
        Object queryMenuByPhoneNum = ParseManager.queryMenuByPhoneNum(context, str, i, str3, map);
        if (TextUtils.isEmpty(queryMenuByPhoneNum)) {
            return null;
        }
        JSONArray jSONArray = new JSONArray(queryMenuByPhoneNum);
        for (int i2 = 0; i2 < jSONArray.length(); i2++) {
            JSONObject jSONObject = jSONArray.getJSONObject(i2);
            if (h.equals(jSONObject.getString("name")) || i.equals(jSONObject.getString("name"))) {
                JSONArray jSONArray2 = jSONObject.getJSONArray("secondmenu");
                for (int i3 = 0; i3 < jSONArray2.length(); i3++) {
                    Object a = a(str2, jSONArray2.getJSONObject(i3));
                    if (!TextUtils.isEmpty(a)) {
                        return a;
                    }
                }
                continue;
            } else {
                queryMenuByPhoneNum = a(str2, jSONObject);
                if (!TextUtils.isEmpty(queryMenuByPhoneNum)) {
                    return queryMenuByPhoneNum;
                }
            }
        }
        return null;
    }
}
