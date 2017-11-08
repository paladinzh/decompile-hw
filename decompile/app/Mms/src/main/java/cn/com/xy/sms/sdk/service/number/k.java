package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class k {
    private static final String a = "dataType";
    private static final String b = "dataVersion";
    private static final int c = 1;

    private static String a(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(a, 1);
            jSONObject.put(b, map.get(NumberInfo.VERSION_KEY));
            return jSONObject.toString();
        } catch (Throwable th) {
            return null;
        }
    }

    public static void a(Map<String, String> map, Map<String, String> map2, XyCallBack xyCallBack) {
        if (map.isEmpty()) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), "no version");
            return;
        }
        String a = a(map);
        if (StringUtils.isNull(a)) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), "reqeustContent null");
            return;
        }
        try {
            NetUtil.executeServiceHttpRequest(NetUtil.REQ_QUERY_UPGRADE, a, null, new l(xyCallBack));
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(xyCallBack, Integer.valueOf(-10), th.getMessage());
        }
    }
}
