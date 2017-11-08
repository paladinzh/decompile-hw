package cn.com.xy.sms.sdk.queue;

import android.os.Process;
import org.json.JSONObject;

/* compiled from: Unknown */
final class a extends Thread {
    a() {
    }

    public final void run() {
        try {
            setName("xiaoyuan_taskbubblequeue");
            Process.setThreadPriority(i.b);
            while (true) {
                JSONObject jSONObject = (JSONObject) BubbleTaskQueue.a.take();
                if (jSONObject != null) {
                    BubbleTaskQueue.a(jSONObject);
                }
            }
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
