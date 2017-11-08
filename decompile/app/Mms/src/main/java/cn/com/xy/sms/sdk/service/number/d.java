package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.SdkCallBack;

/* compiled from: Unknown */
final class d implements SdkCallBack {
    private /* synthetic */ c a;
    private final /* synthetic */ SdkCallBack b;

    d(c cVar, SdkCallBack sdkCallBack) {
        this.b = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        String str = (objArr != null && objArr.length > 1 && ((Integer) objArr[0]).intValue() == 3) ? (String) objArr[1] : null;
        try {
            if (StringUtils.isNull(str)) {
                XyUtil.doXycallBackResult(this.b, Integer.valueOf(100), "net error");
            } else {
                XyUtil.doXycallBackResult(this.b, Integer.valueOf(102), str);
            }
        } catch (Throwable th) {
            XyUtil.doXycallBackResult(this.b, Integer.valueOf(100), "exception error");
        }
    }
}
