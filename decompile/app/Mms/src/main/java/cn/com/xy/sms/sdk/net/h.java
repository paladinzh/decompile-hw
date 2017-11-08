package cn.com.xy.sms.sdk.net;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;

/* compiled from: Unknown */
final class h implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ XyCallBack c;

    h(String str, String str2, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = xyCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length == 2) {
            NetUtil.b(this.a, this.b, this.c, true, false, false, null);
            return;
        }
        XyUtil.doXycallBackResult(this.c, Integer.valueOf(-10), "Error get token");
    }
}
