package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseMsgUrlManager {
    public static void checkUrlFromMsg(String str, String str2, String str3, String str4, long j, SdkCallBack sdkCallBack, boolean z, Map<String, String> map) {
        MsgUrlService.checkUrlFromMsg(str, str2, str3, str4, j, map, sdkCallBack, z);
    }

    public static int checkValidUrl(String str, String str2, String str3, String str4, Map<String, String> map) {
        return MsgUrlService.checkValidUrl(str, str2, str3, str4, map, null);
    }

    public static int checkValidUrl(String str, String str2, String str3, String str4, Map<String, String> map, SdkCallBack sdkCallBack) {
        return MsgUrlService.checkValidUrl(str, str2, str3, str4, map, sdkCallBack);
    }

    public static String pickUrlFromMsg(String str, String str2, String str3, String str4, Map<String, String> map) {
        return MsgUrlService.pickUrlFromMsg(str, str2, str3, str4, map);
    }

    public static void putUrlsResultData(String str, Map<String, JSONObject> map, boolean z) {
        MsgUrlService.putUrlsResultData(str, map, z);
    }
}
