package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.MatchCacheManager;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;

/* compiled from: Unknown */
final class o implements XyCallBack {
    private final /* synthetic */ D a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ SdkCallBack d;

    o(D d, String str, String str2, SdkCallBack sdkCallBack) {
        this.a = d;
        this.b = str;
        this.c = str2;
        this.d = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        if (objArr != null) {
            try {
                this.a.j.put(this.b, (Map) objArr[0]);
                this.a.l.remove(this.c);
                this.a.n.remove(this.c);
                MatchCacheManager.resetRecognisedResult(this.c);
                XyUtil.doXycallBackResult(this.d, Integer.valueOf(2), "refresh list", this.c, Integer.valueOf(8));
            } catch (Throwable th) {
            }
        }
    }
}
