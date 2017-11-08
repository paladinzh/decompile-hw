package cn.com.xy.sms.sdk.util;

import cn.com.xy.sms.sdk.a.a;

/* compiled from: Unknown */
final class h implements Runnable {
    h() {
    }

    public final void run() {
        a.a("xy-sdkinitpool-1", 10);
        g.g();
        g.e();
        g.f();
        g.a("train_data.txt", 0);
        g.a("air_data.txt", 1);
    }
}
