package cn.com.xy.sms.sdk.service.b;

import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class a {
    public static void a(int i, String str, SdkCallBack sdkCallBack) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("count", i);
            jSONObject.put("sdkVersion", NetUtil.APPVERSION);
            jSONObject.put("channel", KeyManager.getAppKey());
            jSONObject.put(NumberInfo.VERSION_KEY, str);
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_COMMING_MOVIE, jSONObject.toString(), sdkCallBack);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
        }
    }

    public static void a(String str, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str)) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
            return;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("movieName", str);
            jSONObject.put("sdkVersion", NetUtil.APPVERSION);
            jSONObject.put("channel", KeyManager.getAppKey());
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_MOVIE_POSTERS, jSONObject.toString(), sdkCallBack);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
        }
    }

    public static void a(String str, String str2, String str3, SdkCallBack sdkCallBack) {
        if (StringUtils.isNull(str2)) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
            return;
        }
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("city", str);
            jSONObject.put("sceneId", str2);
            jSONObject.put("sdkVersion", NetUtil.APPVERSION);
            jSONObject.put("channel", KeyManager.getAppKey());
            jSONObject.put(NumberInfo.VERSION_KEY, str3);
            NetWebUtil.sendPostRequest(NetWebUtil.WEB_SERVER_URL_DISCOVER, jSONObject.toString(), sdkCallBack);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(sdkCallBack, new Object[0]);
        }
    }
}
