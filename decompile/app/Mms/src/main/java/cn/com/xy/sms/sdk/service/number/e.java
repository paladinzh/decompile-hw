package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.q;
import cn.com.xy.sms.sdk.db.entity.r;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONObject;

/* compiled from: Unknown */
final class e implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ SdkCallBack d;

    e(String str, String str2, String str3, SdkCallBack sdkCallBack) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = sdkCallBack;
    }

    public final void run() {
        try {
            q a = r.a(this.a, this.b);
            if (a != null && a.c.length() > 2) {
                JSONObject jSONObject = new JSONObject(a.c);
                PhoneNumServeService.a.put(this.c, jSONObject);
                new StringBuilder("get result from db:").append(jSONObject.toString());
                XyUtil.doXycallBackResult(this.d, Integer.valueOf(102), jSONObject);
                return;
            }
            XyCallBack fVar = new f(this, this.a, this.c, this.b, this.d);
            NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.a(this.a, this.b), null, fVar);
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.d, Integer.valueOf(101), null);
        }
    }
}
