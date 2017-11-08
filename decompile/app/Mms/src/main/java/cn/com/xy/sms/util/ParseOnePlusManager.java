package cn.com.xy.sms.util;

import android.content.Context;
import java.util.Map;

/* compiled from: Unknown */
public class ParseOnePlusManager extends ParseManager {
    public static boolean isPinSms(Context context, String str, String str2, String str3, Map<String, String> map) {
        return ParseManager.parseSmsType(context, str, str2, str3, map, 1) == 1;
    }
}
