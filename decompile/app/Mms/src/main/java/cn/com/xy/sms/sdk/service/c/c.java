package cn.com.xy.sms.sdk.service.c;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.p;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.NewXyHttpRunnable;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.HashMap;
import org.json.JSONObject;

/* compiled from: Unknown */
final class c implements SdkCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ HashMap b;
    private final /* synthetic */ String c;
    private final /* synthetic */ SdkCallBack d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;

    c(String str, HashMap hashMap, String str2, SdkCallBack sdkCallBack, String str3, String str4) {
        this.a = str;
        this.b = hashMap;
        this.c = str2;
        this.d = sdkCallBack;
        this.e = str3;
        this.f = str4;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length > 0) {
            String obj = objArr[0].toString();
            if (obj.equals("1")) {
                NetUtil.requestNewTokenAndPostRequestAgain(NetUtil.REQ_QUERY_OPERATOR_MSG, this.a, Constant.FIVE_MINUTES, a.b(this.b), false, true, this.b, this.c, this);
                return;
            } else if (obj.equals("2") || obj.equals(NewXyHttpRunnable.ERROR_CODE_SERVICE_ERR)) {
                XyUtil.doXycallBackResult(this.d, Integer.valueOf(-10), "server error");
                return;
            } else if (obj.equals("0") && objArr.length == 2) {
                obj = objArr[1].toString();
                p.a(this.e, this.f, obj);
                try {
                    JSONObject jSONObject = new JSONObject(obj);
                    XyUtil.doXycallBackResult(this.d, "0", jSONObject);
                    return;
                } catch (Throwable th) {
                    XyUtil.doXycallBackResult(this.d, Integer.valueOf(-10), "response content error");
                    return;
                }
            } else {
                XyUtil.doXycallBackResult(this.d, Integer.valueOf(-10), "response code wrong, code=" + obj);
                return;
            }
        }
        XyUtil.doXycallBackResult(this.d, Integer.valueOf(-10), "no result");
    }
}
