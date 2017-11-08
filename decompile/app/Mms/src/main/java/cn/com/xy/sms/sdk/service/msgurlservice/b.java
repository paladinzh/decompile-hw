package cn.com.xy.sms.sdk.service.msgurlservice;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
final class b implements Runnable {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String[] e;
    private final /* synthetic */ Map f;
    private final /* synthetic */ HashMap g;
    private final /* synthetic */ XyCallBack h;

    b(String str, String str2, String str3, String str4, String[] strArr, Map map, HashMap hashMap, XyCallBack xyCallBack) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = str4;
        this.e = strArr;
        this.f = map;
        this.g = hashMap;
        this.h = xyCallBack;
    }

    public final void run() {
        MsgUrlService.checkValidUrlNet(this.a, this.b, this.c, this.d, this.e, this.f, false, this.g, this.h);
    }
}
