package cn.com.xy.sms.util;

import android.content.Context;
import cn.com.xy.sms.sdk.a.a;

/* compiled from: Unknown */
final class j implements Runnable {
    private final /* synthetic */ Context a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ int f;
    private final /* synthetic */ int g;
    private final /* synthetic */ SdkCallBack h;

    j(Context context, String str, String str2, String str3, String str4, int i, int i2, SdkCallBack sdkCallBack) {
        this.a = context;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = i;
        this.g = i2;
        this.h = sdkCallBack;
    }

    public final void run() {
        try {
            a.a("xy_logo_1", 10);
            ParseManager.b(this.a, this.b, this.c, this.d, this.e, this.f, this.g, this.h);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
