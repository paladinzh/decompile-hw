package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;

/* compiled from: Unknown */
final class l implements XyCallBack {
    l() {
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr.length >= 2 && ((Integer) objArr[0]).intValue() == 0) {
                    k.a(objArr[1].toString());
                }
            } catch (Throwable th) {
            }
        }
    }
}
