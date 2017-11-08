package cn.com.xy.sms.sdk.number;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;

/* compiled from: Unknown */
final class s implements Runnable {
    private final /* synthetic */ XyCallBack a;

    s(XyCallBack xyCallBack) {
        this.a = xyCallBack;
    }

    public final void run() {
        r.b(this.a);
    }
}
