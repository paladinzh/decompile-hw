package cn.com.xy.sms.sdk.provider;

import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.SdkCallBack;

/* compiled from: Unknown */
final class c implements SdkCallBack {
    private /* synthetic */ b a;
    private final /* synthetic */ String[] b;

    c(b bVar, String[] strArr) {
        this.b = strArr;
    }

    public final void execute(Object... objArr) {
        if (objArr != null && objArr.length > 0 && !StringUtils.isNull((String) objArr[0])) {
            this.b[0] = (String) objArr[0];
        }
    }
}
