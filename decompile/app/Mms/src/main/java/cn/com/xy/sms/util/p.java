package cn.com.xy.sms.util;

import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
final class p implements SdkCallBack {
    private final /* synthetic */ HashMap a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ long f;
    private final /* synthetic */ boolean g;
    private final /* synthetic */ SdkCallBack h;

    p(HashMap hashMap, String str, String str2, String str3, String str4, long j, boolean z, SdkCallBack sdkCallBack) {
        this.a = hashMap;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = j;
        this.g = z;
        this.h = sdkCallBack;
    }

    public final void execute(Object... objArr) {
        try {
            Map hashMap = new HashMap();
            if (this.a != null && this.a.containsKey("isUseNewAction")) {
                hashMap.put("isUseNewAction", this.a.get("isUseNewAction").toString());
            }
            ParseBubbleManager.queryDataByMsgItem(this.b, this.c, this.d, this.e, 1, this.f, new q(this, objArr, this.h, this.b), this.g, hashMap);
        } catch (Throwable th) {
        }
    }
}
