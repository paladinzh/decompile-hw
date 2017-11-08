package cn.com.xy.sms.sdk.service.number;

import cn.com.xy.sms.sdk.dex.DexUtil;

/* compiled from: Unknown */
final class i implements Runnable {
    i() {
    }

    public final void run() {
        PhoneNumServeService.a(System.currentTimeMillis() - DexUtil.getUpdateCycleByType(43, 86400000), 1);
        PhoneNumServeService.a(System.currentTimeMillis() - DexUtil.getUpdateCycleByType(44, 604800000), -1);
    }
}
