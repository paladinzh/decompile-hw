package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.util.SdkCallBack;
import java.util.HashMap;

/* compiled from: Unknown */
final class g implements SdkCallBack {
    private final /* synthetic */ SdkCallBack a;
    private final /* synthetic */ HashMap b;

    g(SdkCallBack sdkCallBack, HashMap hashMap) {
        this.a = sdkCallBack;
        this.b = hashMap;
    }

    public final void execute(Object... objArr) {
        String str = null;
        if (this.a != null && objArr != null && objArr.length == 2) {
            if (objArr[1] != null) {
                str = objArr[1].toString();
            }
            if (str != null && !str.equals(this.b.get("pubId"))) {
                this.a.execute(objArr[0]);
            }
        }
    }
}
