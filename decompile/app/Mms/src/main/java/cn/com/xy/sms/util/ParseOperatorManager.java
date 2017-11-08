package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.service.c.a;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import org.json.JSONObject;

/* compiled from: Unknown */
public class ParseOperatorManager {
    private static String a = "ParseOperatorManager";

    public static Boolean isOperatorMsgPhone(String str) {
        try {
            return DexUtil.isOperatorsPhoneType(str);
        } catch (Exception e) {
            return Boolean.valueOf(false);
        }
    }

    public static JSONObject parseOperatorMsg(Context context, String str, String str2, String str3, String str4, long j, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        try {
            if (StringUtils.allValuesIsNotNull(str2, str4)) {
                return a.b(str2, str4, hashMap, sdkCallBack);
            }
            XyUtil.doXycallBackResult(sdkCallBack, Integer.valueOf(-10), "phone num or sms content is null");
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    public static JSONObject queryOperatorCmd(Context context, String str, String str2, HashMap<String, String> hashMap, SdkCallBack sdkCallBack) {
        try {
            return a.a(str, str2, hashMap, sdkCallBack);
        } catch (Throwable th) {
            return null;
        }
    }
}
