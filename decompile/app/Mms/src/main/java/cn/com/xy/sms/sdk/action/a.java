package cn.com.xy.sms.sdk.action;

import cn.com.xy.sms.sdk.iccid.IccidLocationUtil;

/* compiled from: Unknown */
final class a implements Runnable {
    private /* synthetic */ AbsSdkDoAction a;

    a(AbsSdkDoAction absSdkDoAction) {
    }

    public final void run() {
        try {
            cn.com.xy.sms.sdk.a.a.a("xy_local_bg_1", 10);
            IccidLocationUtil.changeIccidAreaCode(true);
        } catch (Throwable th) {
        }
    }
}
