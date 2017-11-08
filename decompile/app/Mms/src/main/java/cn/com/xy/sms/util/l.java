package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.db.entity.a.f;

/* compiled from: Unknown */
final class l implements SdkCallBack {
    private final /* synthetic */ SdkCallBack a;
    private final /* synthetic */ String b;

    l(SdkCallBack sdkCallBack, String str) {
        this.a = sdkCallBack;
        this.b = str;
    }

    public final void execute(Object... objArr) {
        if (objArr == null || objArr.length == 0 || objArr[0] == null || !objArr[0].toString().contains("action_data")) {
            try {
                this.a.execute(Integer.valueOf(0), "menu json data error");
                return;
            } catch (Throwable th) {
                this.a.execute(Integer.valueOf(0), "error:" + th.getMessage());
                return;
            }
        }
        this.a.execute(Integer.valueOf(1), this.b, f.e((String) objArr[0]));
    }
}
