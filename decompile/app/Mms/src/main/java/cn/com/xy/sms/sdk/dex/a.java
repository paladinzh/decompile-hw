package cn.com.xy.sms.sdk.dex;

import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.h;
import cn.com.xy.sms.sdk.ui.popu.util.ThemeUtil;
import cn.com.xy.sms.sdk.util.k;

/* compiled from: Unknown */
final class a implements Runnable {
    private final /* synthetic */ String a;

    a(String str) {
        this.a = str;
    }

    public final void run() {
        long j = 600000;
        try {
            Process.setThreadPriority(10);
            Thread.currentThread().setPriority(1);
            Thread.currentThread().setName("xiaoyuan-ipool" + Thread.currentThread().hashCode());
            if (this.a != null && this.a.length() > 7) {
                long j2 = 0;
                Long l = (Long) Constant.checkJarMap.get(this.a);
                if (l != null) {
                    j2 = l.longValue();
                }
                if (DexUtil.d != null) {
                    j = DexUtil.getUpdateCycleByType(6, 600000);
                }
                if ((System.currentTimeMillis() < j + j2 ? 1 : null) == null) {
                    h.a(this.a, ThemeUtil.SET_NULL_STR, 1);
                    Constant.checkJarMap.put(this.a, Long.valueOf(System.currentTimeMillis()));
                    k.a();
                }
            }
        } catch (Throwable e) {
            DexUtil.saveExceptionLog(e);
        }
    }
}
