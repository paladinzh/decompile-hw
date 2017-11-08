package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.io.File;

/* compiled from: Unknown */
final class h implements XyCallBack {
    private final /* synthetic */ XyCallBack a;

    h(XyCallBack xyCallBack) {
        this.a = xyCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr.length > 0 && ((Integer) objArr[0]).intValue() == 5) {
                    f.a((File) objArr[2]);
                }
            } catch (Throwable th) {
            }
        }
        XyUtil.doXycallBackResult(this.a, objArr);
    }
}
