package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;

/* compiled from: Unknown */
final class h implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ boolean c;

    h(String str, String str2, boolean z) {
        this.a = str;
        this.b = str2;
        this.c = z;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            if (objArr.length > 0 && !ThemeUtil.SET_NULL_STR.equals(objArr[0].toString())) {
                g.b(this.a, this.b, this.c);
                return;
            }
        }
        try {
            synchronized (g.a) {
                g.b = false;
            }
        } catch (Throwable th) {
        }
    }
}
