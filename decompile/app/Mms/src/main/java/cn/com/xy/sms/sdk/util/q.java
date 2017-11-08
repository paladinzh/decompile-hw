package cn.com.xy.sms.sdk.util;

import android.content.Context;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.L;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.net.NetUtil;

/* compiled from: Unknown */
public final class q {
    private static String a(Context context) {
        return SysParamEntityManager.getStringParam(context, "LastMenuActionCountActionUpdate");
    }

    public static void a() {
        int i = 1;
        String currentTimeString = DateUtils.getCurrentTimeString("yyyyMMdd");
        String stringParam = SysParamEntityManager.getStringParam(Constant.getContext(), "LastMenuActionCountActionUpdate");
        if (stringParam != null) {
            i = DateUtils.compareDateString(currentTimeString, DateUtils.addDays(stringParam, "yyyyMMdd", 1), "yyyyMMdd");
        }
        NetUtil.requestNewTokenIfNeed(null);
        if (i != 0) {
            try {
                stringParam = L.a(currentTimeString).toString();
                if (!StringUtils.isNull(stringParam)) {
                    NetUtil.executeNewServiceHttpRequest(NetUtil.URL_MENU_CLICKED, stringParam, new r(stringParam, NetUtil.getToken(), currentTimeString), true, false, true, null);
                }
            } catch (Throwable th) {
            }
        }
    }

    public static void a(String str) {
        SysParamEntityManager.setParam("LastMenuActionCountActionUpdate", str);
    }
}
