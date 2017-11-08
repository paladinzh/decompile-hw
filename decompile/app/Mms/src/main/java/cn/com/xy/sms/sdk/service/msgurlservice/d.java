package cn.com.xy.sms.sdk.service.msgurlservice;

import org.json.JSONArray;

/* compiled from: Unknown */
final class d implements Runnable {
    private final /* synthetic */ JSONArray a;
    private final /* synthetic */ String b;
    private final /* synthetic */ int c;

    d(JSONArray jSONArray, String str, int i) {
        this.a = jSONArray;
        this.b = str;
        this.c = i;
    }

    public final void run() {
        try {
            MsgUrlService.saveUrlResult(this.a, this.b, this.c);
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
