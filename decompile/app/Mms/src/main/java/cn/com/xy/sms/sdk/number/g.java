package cn.com.xy.sms.sdk.number;

import android.content.Context;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;

/* compiled from: Unknown */
final class g implements Runnable {
    private final /* synthetic */ Context a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ XyCallBack d;

    g(Context context, String str, String str2, XyCallBack xyCallBack) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = xyCallBack;
    }

    public final void run() {
        f.b(this.a, this.b, this.c, this.d);
    }
}
