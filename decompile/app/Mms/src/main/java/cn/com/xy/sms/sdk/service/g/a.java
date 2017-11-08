package cn.com.xy.sms.sdk.service.g;

import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.dex.DexUtil;
import cn.com.xy.sms.sdk.service.a.b;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseBubbleManager;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class a {
    public static Map<String, Object> a(String str, String str2, long j) {
        if (StringUtils.isNull(str)) {
            return a(false, -1, "error:number is null or empty");
        }
        if (StringUtils.isNull(str2)) {
            return a(false, -1, "error:smsBody is null or empty");
        }
        if (!(j > 0)) {
            return a(false, -1, "error:receiveTime <= 0");
        }
        try {
            Map hashMap = new HashMap();
            hashMap.put("popup_type", "2");
            hashMap.put("parseVerifyCode", "true");
            Map b = b.b(Constant.getContext(), str, null, str2, j, hashMap);
            if (b == null || ParseBubbleManager.getParseStatu(b) == -1) {
                return a(false, -1, "error:parse sms failure");
            }
            if (DexUtil.getSmsTypeByMap(b, 1) != 1) {
                return a(false, -1, "not a valid code sms");
            }
            b.put("smsReceiveTime", Long.valueOf(j));
            b = DexUtil.handleValidTime(b);
            return (b == null || b.get("validTime") == null || ThemeUtil.SET_NULL_STR.equals(String.valueOf(b.get("validTime")))) ? a(true, -1, "error:get valid time failure") : a(true, ((Long) b.get("validTime")).longValue(), "succeed");
        } catch (Throwable th) {
            return a(false, -1, "error:" + th.getMessage());
        }
    }

    private static Map<String, Object> a(boolean z, long j, String str) {
        Map<String, Object> hashMap = new HashMap();
        hashMap.put("isValidCodeSms", Boolean.valueOf(z));
        hashMap.put("validTime", Long.valueOf(j));
        hashMap.put("parseResult", str);
        return hashMap;
    }

    public static boolean a(Context context, String str, String str2, String str3, Map<String, String> map) {
        Map hashMap = map != null ? map : new HashMap();
        try {
            hashMap.put("popup_type", "2");
            hashMap.put("parseVerifyCode", "true");
            Map b = b.b(context, str, str3, str2, 0, hashMap);
            return (b == null || ParseBubbleManager.getParseStatu(b) == -1 || DexUtil.getSmsTypeByMap(b, 1) != 1) ? false : true;
        } catch (Throwable th) {
        }
        return false;
    }
}
