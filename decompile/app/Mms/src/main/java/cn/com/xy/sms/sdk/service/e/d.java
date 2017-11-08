package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.util.ConversationManager;
import java.util.List;

/* compiled from: Unknown */
final class d implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ List b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ XyCallBack f;
    private final /* synthetic */ boolean g;

    d(String str, List list, String str2, String str3, String str4, XyCallBack xyCallBack, boolean z) {
        this.a = str;
        this.b = list;
        this.c = str2;
        this.d = str3;
        this.e = str4;
        this.f = xyCallBack;
        this.g = z;
    }

    public final void execute(Object... objArr) {
        ConversationManager.saveLogOut(this.a, "cn.com.xy.sms.sdk.service.pubInfo.PubInfoNetService", "queryPubInfoByList", this.b, this.c, this.d, this.e, this.f, Boolean.valueOf(this.g), objArr);
        List list = this.b;
        String str = this.c;
        b.a(list, false, null, this.d, this.f, 2, objArr);
    }
}
