package cn.com.xy.sms.util;

import android.os.Process;
import cn.com.xy.sms.sdk.a.a;
import cn.com.xy.sms.sdk.util.D;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.XyUtil;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: Unknown */
final class c implements Runnable {
    private final /* synthetic */ D a;
    private final /* synthetic */ String b;
    private final /* synthetic */ String c;
    private final /* synthetic */ String d;
    private final /* synthetic */ long e;
    private final /* synthetic */ SdkCallBack f;
    private final /* synthetic */ String g;
    private final /* synthetic */ Map h;
    private final /* synthetic */ int i;

    c(D d, String str, String str2, String str3, long j, SdkCallBack sdkCallBack, String str4, Map map, int i) {
        this.a = d;
        this.b = str;
        this.c = str2;
        this.d = str3;
        this.e = j;
        this.f = sdkCallBack;
        this.g = str4;
        this.h = map;
        this.i = i;
    }

    public final void run() {
        try {
            JSONArray jSONArray;
            a.a("xy_richpool_1", 10);
            Process.setThreadPriority(-16);
            JSONObject queryBubbleDataFromDb = ParseRichBubbleManager.queryBubbleDataFromDb(this.b, this.c, this.d, this.e);
            if (queryBubbleDataFromDb != null) {
                if (!queryBubbleDataFromDb.has("need_parse_simple")) {
                    jSONArray = (JSONArray) JsonUtil.getValueFromJsonObject(queryBubbleDataFromDb, "session_reuslt");
                    if (jSONArray == null) {
                        this.a.d.add(this.b);
                        XyUtil.doXycallBackResult(this.f, Integer.valueOf(-3), " invalid data need_parse_simple", this.b, Integer.valueOf(1));
                        this.a.e.remove(this.b);
                        return;
                    }
                    synchronized (this.a.c) {
                        this.a.c.put(this.b, jSONArray);
                    }
                    this.a.d.remove(this.b);
                    XyUtil.doXycallBackResult(this.f, Integer.valueOf(1), jSONArray, this.b, Integer.valueOf(1));
                    this.a.e.remove(this.b);
                    return;
                }
            }
            jSONArray = ParseBubbleManager.b(this.b, this.c, this.g, this.d, this.e, this.h);
            if (jSONArray == null) {
                this.a.d.add(this.b);
                XyUtil.doXycallBackResult(this.f, Integer.valueOf(-3), "$$$$$$$$$$ dataType: " + this.i, this.b, Integer.valueOf(1));
            } else {
                synchronized (this.a.c) {
                    this.a.c.put(this.b, jSONArray);
                }
                XyUtil.doXycallBackResult(this.f, Integer.valueOf(1), jSONArray, this.b, Integer.valueOf(1));
            }
            this.a.e.remove(this.b);
        } catch (Throwable th) {
            try {
                th.getLocalizedMessage();
                XyUtil.doXycallBackResult(this.f, Integer.valueOf(-3), "result is null: error: " + th.getMessage(), this.b, Integer.valueOf(1));
            } finally {
                this.a.e.remove(this.b);
            }
        }
    }
}
