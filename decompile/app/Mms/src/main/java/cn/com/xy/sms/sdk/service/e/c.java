package cn.com.xy.sms.sdk.service.e;

import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.db.entity.a.f;
import cn.com.xy.sms.sdk.util.ConversationManager;
import cn.com.xy.sms.sdk.util.JsonUtil;
import org.json.JSONObject;

/* compiled from: Unknown */
final class c implements XyCallBack {
    private final /* synthetic */ String a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ boolean d;
    private final /* synthetic */ String e;
    private final /* synthetic */ String f;
    private final /* synthetic */ String g;
    private final /* synthetic */ XyCallBack h;
    private final /* synthetic */ int i;
    private final /* synthetic */ boolean j;
    private final /* synthetic */ boolean k;
    private final /* synthetic */ boolean l;

    c(String str, String str2, String str3, boolean z, String str4, String str5, String str6, XyCallBack xyCallBack, int i, boolean z2, boolean z3, boolean z4) {
        this.a = str;
        this.b = str2;
        this.c = str3;
        this.d = z;
        this.e = str4;
        this.f = str5;
        this.g = str6;
        this.h = xyCallBack;
        this.i = i;
        this.j = z2;
        this.k = z3;
        this.l = z4;
    }

    public final void execute(Object... objArr) {
        b.a(this.a, this.b, objArr);
        ConversationManager.saveLogIn(this.c, "cn.com.xy.sms.sdk.service.pubInfo.PubInfoNetService", "queryPubInfoRequest", Boolean.valueOf(this.d), this.a, this.e, this.b, this.f, this.g, this.h, Integer.valueOf(this.i), Boolean.valueOf(this.j), Boolean.valueOf(this.k), Boolean.valueOf(this.l), objArr);
        boolean z = this.d;
        String str = this.a;
        String str2 = this.e;
        str2 = this.b;
        if (b.a(null, z, str, this.f, this.h, this.i, objArr) != null && this.i == 0) {
            JSONObject a = f.a(this.a, this.b, Integer.valueOf(this.g).intValue());
            if (this.h != null) {
                String pubInfoToJson = JsonUtil.pubInfoToJson(a, this.a, this.b);
                this.h.execute(pubInfoToJson);
            }
        }
    }
}
