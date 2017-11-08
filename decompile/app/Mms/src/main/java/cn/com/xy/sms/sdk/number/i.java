package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;

/* compiled from: Unknown */
final class i implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ XyCallBack b;

    i(String str, XyCallBack xyCallBack) {
        this.a = str;
        this.b = xyCallBack;
    }

    public final void execute(Object... objArr) {
        try {
            objArr[1] = this.a;
        } catch (Exception e) {
        }
        XyUtil.doXycallBackResult(this.b, objArr);
    }
}
