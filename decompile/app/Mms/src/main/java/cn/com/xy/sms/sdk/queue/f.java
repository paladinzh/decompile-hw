package cn.com.xy.sms.sdk.queue;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.SysParamEntityManager;
import cn.com.xy.sms.sdk.db.entity.y;
import cn.com.xy.sms.sdk.net.util.i;

/* compiled from: Unknown */
final class f implements XyCallBack {
    private final /* synthetic */ int a;

    f(int i) {
        this.a = i;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                if (objArr[0].toString().equals("0") && objArr.length == 2) {
                    String obj = objArr[1].toString();
                    SysParamEntityManager.setParam("LastCheckResourseTime_" + this.a, new StringBuilder(String.valueOf(System.currentTimeMillis())).toString());
                    y.a(i.f(obj));
                }
            } catch (Throwable th) {
            }
        }
    }
}
