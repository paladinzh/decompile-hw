package cn.com.xy.sms.sdk.service.f;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.util.SdkCallBack;
import java.util.Map;

/* compiled from: Unknown */
final class c implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ Map f;
    private final /* synthetic */ String g;
    private final /* synthetic */ String h;
    private final /* synthetic */ String i;
    private final /* synthetic */ String j;
    private final /* synthetic */ String k;
    private final /* synthetic */ SdkCallBack l;

    c(String str, String str2, String str3, String str4, String str5, Map map, String str6, String str7, String str8, String str9, String str10, SdkCallBack sdkCallBack) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
        this.e = str5;
        this.f = map;
        this.g = str6;
        this.h = str7;
        this.i = str8;
        this.j = str9;
        this.k = str10;
        this.l = sdkCallBack;
    }

    public final void run() {
        try {
            a.a("xy_service_data_query", 10);
            a.a(this.a, this.b, this.c, this.d, this.e, new d(this, this.g, this.h, this.i, this.j, this.k, this.l), this.f);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
