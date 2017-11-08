package cn.com.xy.sms.sdk.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class g {
    public static BlockingQueue<JSONObject> a = new LinkedBlockingQueue();
    private static Thread b = null;

    private static synchronized void a() {
        synchronized (g.class) {
            if (b == null) {
                Thread hVar = new h();
                b = hVar;
                hVar.start();
            }
        }
    }

    public static void a(int i, String str, String str2, String str3, String str4, int i2, long j, JSONObject jSONObject) {
        if (jSONObject == null) {
            jSONObject = new JSONObject();
        }
        try {
            jSONObject.put("dataStatu", i);
            jSONObject.put("msg_id", str);
            jSONObject.put("phoneNum", str2);
            jSONObject.put("smsContent", str3);
            jSONObject.put("smsReceiveTime", j);
            if (str4 != null) {
                jSONObject.put("centerNum", str4);
            }
            jSONObject.put("dataType", i2);
            a.put(jSONObject);
            a();
        } catch (Throwable th) {
        }
    }
}
