package cn.com.xy.sms.sdk.db.entity.a;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.util.ParseManager;
import java.util.HashMap;

/* compiled from: Unknown */
final class e implements Runnable {
    private final /* synthetic */ HashMap a;

    e(HashMap hashMap) {
        this.a = hashMap;
    }

    public final void run() {
        try {
            a.a("xy_query_pubinfo_1", 10);
            ParseManager.queryPublicInfo(Constant.getContext(), (String) this.a.get(IccidInfoManager.NUM), 1, "", null, null);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
