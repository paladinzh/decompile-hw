package cn.com.xy.sms.util;

import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.HashMap;
import org.json.JSONObject;

/* compiled from: Unknown */
final class m implements Runnable {
    private final /* synthetic */ D a;
    private final /* synthetic */ String b;
    private final /* synthetic */ int c;
    private final /* synthetic */ String d;
    private final /* synthetic */ String e;
    private final /* synthetic */ long f;
    private final /* synthetic */ SdkCallBack g;
    private final /* synthetic */ boolean h;
    private final /* synthetic */ String i;
    private final /* synthetic */ HashMap j;

    m(D d, String str, int i, String str2, String str3, long j, SdkCallBack sdkCallBack, boolean z, String str4, HashMap hashMap) {
        this.a = d;
        this.b = str;
        this.c = i;
        this.d = str2;
        this.e = str3;
        this.f = j;
        this.g = sdkCallBack;
        this.h = z;
        this.i = str4;
        this.j = hashMap;
    }

    public final void run() {
        try {
            a.a("xy_richpool_1", 10);
            Object obj = null;
            if (this.c == 0) {
                JSONObject queryBubbleDataFromDb = ParseRichBubbleManager.queryBubbleDataFromDb(this.b, this.d, this.e, this.f);
                if (!(queryBubbleDataFromDb == null || queryBubbleDataFromDb.has("need_parse_bubble"))) {
                    JSONObject jSONObject = (JSONObject) JsonUtil.getValueFromJsonObject(queryBubbleDataFromDb, "bubble_result");
                    if (jSONObject == null) {
                        this.a.h.add(this.b);
                        this.a.f.remove(this.b);
                        XyUtil.doXycallBackResult(this.g, Integer.valueOf(-3), " invalid data ", this.b, Integer.valueOf(2));
                        ParseRichBubbleManager.addToFavorite(this.b, this.d, this.e, this.f, this.h, queryBubbleDataFromDb);
                        this.a.i.remove(this.b);
                        return;
                    }
                    synchronized (this.a.g) {
                        this.a.g.put(this.b, jSONObject);
                    }
                    this.a.f.remove(this.b);
                    this.a.h.remove(this.b);
                    XyUtil.doXycallBackResult(this.g, Integer.valueOf(1), jSONObject, this.b, Integer.valueOf(2));
                    ParseRichBubbleManager.addToFavorite(this.b, this.d, this.e, this.f, this.h, queryBubbleDataFromDb);
                    this.a.i.remove(this.b);
                    return;
                }
            }
            if (!this.h) {
                obj = ParseRichBubbleManager.queryBubbleDataFromApi(this.b, this.d, this.e, this.i, this.f, this.j);
            }
            if (obj == null) {
                this.a.h.add(this.b);
                this.a.f.remove(this.b);
                String str = this.b;
                XyUtil.doXycallBackResult(this.g, Integer.valueOf(-3), " parse failed ", this.b, Integer.valueOf(2));
            } else {
                synchronized (this.a.g) {
                    this.a.g.put(this.b, obj);
                }
                this.a.h.remove(this.b);
                this.a.f.remove(this.b);
                XyUtil.doXycallBackResult(this.g, Integer.valueOf(1), obj, this.b, Integer.valueOf(2));
            }
            this.a.i.remove(this.b);
        } catch (Throwable th) {
            try {
                XyUtil.doXycallBackResult(this.g, Integer.valueOf(-3), "error: " + th.getLocalizedMessage(), this.b, Integer.valueOf(2));
            } finally {
                this.a.i.remove(this.b);
            }
        }
    }
}
