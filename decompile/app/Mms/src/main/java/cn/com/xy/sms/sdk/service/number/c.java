package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.net.util.j;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.List;

/* compiled from: Unknown */
final class c implements Runnable {
    private final /* synthetic */ List a;
    private final /* synthetic */ String b;
    private final /* synthetic */ SdkCallBack c;

    c(List list, String str, SdkCallBack sdkCallBack) {
        this.a = list;
        this.b = str;
        this.c = sdkCallBack;
    }

    public final void run() {
        XyCallBack dVar = new d(this, this.c);
        NetUtil.executeServiceHttpRequest(NetUtil.GET_PHONE_MENU, j.a(this.a, this.b), null, dVar);
    }
}
