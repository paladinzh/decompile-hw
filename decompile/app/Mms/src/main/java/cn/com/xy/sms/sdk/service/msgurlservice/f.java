package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.db.entity.l;

/* compiled from: Unknown */
final class f implements Runnable {
    f() {
    }

    public final void run() {
        try {
            MsgUrlService.checkValidUrlNetBatch(l.b(), true);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
