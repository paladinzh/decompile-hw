package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;
import org.json.JSONObject;

/* compiled from: Unknown */
final class i implements XyCallBack {
    private /* synthetic */ h a;
    private final /* synthetic */ D b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ long f;
    private final /* synthetic */ SdkCallBack g;

    i(h hVar, D d, String str, String str2, String str3, long j, SdkCallBack sdkCallBack) {
        this.b = d;
        this.c = str;
        this.d = str2;
        this.e = str3;
        this.f = j;
        this.g = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        try {
            JSONObject jSONObject = (JSONObject) objArr[0];
            this.b.q.remove(this.c);
            if (MsgUrlService.b(jSONObject)) {
                this.b.p.put(this.c, jSONObject);
            }
            MsgUrlService.a(this.c, this.d, this.e, this.f, jSONObject.toString());
            XyUtil.doXycallBackResult(this.g, Integer.valueOf(1), jSONObject, this.c, Integer.valueOf(16));
        } catch (Throwable th) {
        }
    }
}
