package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;

/* compiled from: Unknown */
final class f implements XyCallBack {
    private final /* synthetic */ SdkCallBack a;

    f(SdkCallBack sdkCallBack) {
        this.a = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length >= 2) {
            XyUtil.doXycallBackResult(this.a, objArr);
        } else {
            XyUtil.doXycallBackResult(this.a, new Object[0]);
        }
    }
}
