package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.dex.DexUtil;

/* compiled from: Unknown */
final class k implements Runnable {
    private final /* synthetic */ String a;

    k(String str) {
        this.a = str;
    }

    public final void run() {
        try {
            ParseManager.a(this.a, DexUtil.getBubbleViewVersion(null));
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
