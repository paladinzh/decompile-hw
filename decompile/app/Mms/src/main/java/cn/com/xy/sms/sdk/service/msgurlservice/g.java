package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.db.entity.l;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONArray;

/* compiled from: Unknown */
final class g implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ boolean b;
    private final /* synthetic */ String c;

    g(String str, boolean z, String str2) {
        this.a = str;
        this.b = z;
        this.c = str2;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length > 0) {
            String obj = objArr[0].toString();
            if (obj.equals("1")) {
                NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.URL_VALIDITY, this.a, Constant.FIVE_MINUTES, this.b, false, true, null, this.c, this);
            } else if (obj.equals("0") && objArr.length >= 2) {
                obj = objArr[1].toString();
                if (!StringUtils.isNull(obj)) {
                    try {
                        JSONArray jSONArray = new JSONArray(obj);
                        l.a(jSONArray);
                        MatchCacheManager.updateCheckStatu(jSONArray);
                    } catch (Throwable th) {
                    }
                }
            }
        }
    }
}
