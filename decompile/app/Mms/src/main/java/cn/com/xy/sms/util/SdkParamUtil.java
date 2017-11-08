package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;

/* compiled from: Unknown */
public class SdkParamUtil {
    public static String getParamValue(Context context, String str) {
        return SysParamEntityManager.getStringParam(context, str);
    }

    public static boolean setParamValue(Context context, String str, String str2) {
        return setParamValue(context, str, str2, null);
    }

    public static boolean setParamValue(Context context, String str, String str2, String str3) {
        boolean z = true;
        try {
            if (SysParamEntityManager.insertOrUpdateKeyValue(context, str, str2, str3) <= 0) {
                z = false;
            }
            if (z) {
                SysParamEntityManager.cacheMap.put(str, str2);
            }
        } catch (Throwable th) {
        }
        return false;
    }
}
