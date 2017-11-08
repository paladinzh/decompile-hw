package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.db.entity.l;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import java.util.Map;

/* compiled from: Unknown */
final class e implements Runnable {
    private final /* synthetic */ int a;
    private final /* synthetic */ String[] b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ Map f;

    e(int i, String[] strArr, String str, String str2, String str3, Map map) {
        this.a = i;
        this.b = strArr;
        this.c = str;
        this.d = str2;
        this.e = str3;
        this.f = map;
    }

    public final void run() {
        try {
            for (int i = this.a; i < this.b.length; i++) {
                l.a(this.b[i], DuoquUtils.getSdkDoAction().checkValidUrl(this.c, this.d, this.e, this.b[i], this.f));
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
