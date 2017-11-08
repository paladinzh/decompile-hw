package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.z;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.net.util.i;
import java.util.List;

/* compiled from: Unknown */
final class B implements XyCallBack {
    private final /* synthetic */ List a;
    private final /* synthetic */ int b;

    B(List list, int i) {
        this.a = list;
        this.b = i;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr.length == 2 && "0".equals(String.valueOf(objArr[0]))) {
                    String obj = objArr[1].toString();
                    boolean z = LogManager.debug;
                    SceneconfigUtil.c(i.b(obj), this.b);
                    return;
                }
            } catch (Throwable th) {
                return;
            } finally {
                z.a(this.a);
            }
        }
        z.a(this.a);
    }
}
