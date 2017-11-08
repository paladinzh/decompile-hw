package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.db.entity.l;
import cn.com.xy.sms.sdk.util.StringUtils;

/* compiled from: Unknown */
final class a implements Runnable {
    private final /* synthetic */ String a;

    a(String str) {
        this.a = str;
    }

    public final void run() {
        try {
            if (!StringUtils.isNull(this.a)) {
                String str = this.a;
                if (!StringUtils.isNull(str)) {
                    try {
                        String[] split = str.split("_ARR_");
                        if (split != null) {
                            for (String b : split) {
                                l.b(b, 0);
                            }
                        }
                    } catch (Throwable th) {
                    }
                }
            }
        } catch (Throwable th2) {
            th2.getMessage();
        }
    }
}
