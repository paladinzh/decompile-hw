package cn.com.xy.sms.sdk.iccid;

import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfo;

/* compiled from: Unknown */
final class a implements Runnable {
    private final /* synthetic */ IccidInfo a;

    a(IccidInfo iccidInfo) {
        this.a = iccidInfo;
    }

    public final void run() {
        try {
            IccidLocationUtil.a(Constant.getContext(), this.a, false);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
