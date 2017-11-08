package cn.com.xy.sms.sdk.db;

import cn.com.xy.sms.sdk.constant.Constant;

/* compiled from: Unknown */
final class a extends Thread {
    a() {
    }

    public final void run() {
        try {
            AirManager.importAirData(Constant.getContext());
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
