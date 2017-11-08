package cn.com.xy.sms.util;

import android.os.Process;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import org.json.JSONObject;

/* compiled from: Unknown */
final class n implements Runnable {
    private final /* synthetic */ D a;
    private final /* synthetic */ String b;
    private final /* synthetic */ SdkCallBack c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ long f;
    private final /* synthetic */ String g;
    private final /* synthetic */ HashMap h;

    n(D d, String str, SdkCallBack sdkCallBack, String str2, String str3, long j, String str4, HashMap hashMap) {
        this.a = d;
        this.b = str;
        this.c = sdkCallBack;
        this.d = str2;
        this.e = str3;
        this.f = j;
        this.g = str4;
        this.h = hashMap;
    }

    public final void run() {
        try {
            a.a("xy_richpool_1", 10);
            Process.setThreadPriority(-16);
            if (ParseManager.isInitData()) {
                JSONObject jSONObject;
                JSONObject a = ParseSmsMessage.a(this.b, this.d, this.e, this.f);
                if (a != null) {
                    if (!a.has("need_parse_recognise")) {
                        jSONObject = (JSONObject) JsonUtil.getValueFromJsonObject(a, "value_recognise_result");
                        if (jSONObject != null) {
                            if (!a.has("need_parse_recognise")) {
                                synchronized (this.a.l) {
                                    this.a.l.put(this.b, jSONObject);
                                }
                                this.a.m.remove(this.b);
                                XyUtil.doXycallBackResult(this.c, Integer.valueOf(1), jSONObject, this.b, Integer.valueOf(8));
                                this.a.n.remove(this.b);
                                return;
                            }
                        }
                        this.a.m.add(this.b);
                        XyUtil.doXycallBackResult(this.c, Integer.valueOf(-3), " invalid data need_parse_simple", this.b, Integer.valueOf(8));
                        this.a.n.remove(this.b);
                        return;
                    }
                }
                jSONObject = ParseSmsMessage.queryRecognisedValueFromApi(this.b, this.d, this.g, this.e, this.f, this.h, this.c);
                if (jSONObject == null) {
                    this.a.m.add(this.b);
                    XyUtil.doXycallBackResult(this.c, Integer.valueOf(-3), jSONObject, this.b, Integer.valueOf(8));
                } else {
                    synchronized (this.a.l) {
                        this.a.l.put(this.b, jSONObject);
                    }
                    XyUtil.doXycallBackResult(this.c, Integer.valueOf(1), jSONObject, this.b, Integer.valueOf(8));
                }
                this.a.n.remove(this.b);
                return;
            }
            XyUtil.doXycallBackResult(this.c, Integer.valueOf(-3), "sdk is still in init", this.b, Integer.valueOf(8));
            this.a.n.remove(this.b);
        } catch (Throwable th) {
            this.a.n.remove(this.b);
        }
    }
}
