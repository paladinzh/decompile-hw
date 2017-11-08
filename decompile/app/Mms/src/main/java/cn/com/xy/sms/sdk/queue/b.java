package cn.com.xy.sms.sdk.queue;

import android.os.Process;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.DBManager;
import cn.com.xy.sms.sdk.db.entity.e;
import cn.com.xy.sms.sdk.net.util.g;
import cn.com.xy.sms.sdk.util.StringUtils;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class b extends Thread {
    private static boolean a = false;

    public static synchronized void a() {
        Object obj = 1;
        synchronized (b.class) {
            if (System.currentTimeMillis() >= Constant.lastEmergencyUpdateTime + 600000) {
                obj = null;
            }
            if (obj == null) {
                if (!a) {
                    a = true;
                    new b().start();
                }
                Constant.lastEmergencyUpdateTime = System.currentTimeMillis();
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(JSONObject jSONObject) {
        if (jSONObject == null) {
            a = false;
            return;
        }
        try {
            g.a(jSONObject.optString("emContent"));
            if (jSONObject != null) {
                String optString = jSONObject.optString("emVersion");
                if (StringUtils.isNull(optString)) {
                    optString = "";
                }
                DBManager.delete("tb_emergency_queue", "emVersion = ?", new String[]{optString});
            }
            try {
                Thread.sleep(2000);
            } catch (Throwable th) {
            }
            a(e.b());
            return;
        } catch (Throwable th2) {
        }
        a(e.b());
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void run() {
        try {
            setName("xiaoyuan_EmergencyQueue");
            Process.setThreadPriority(i.b);
            Thread.sleep(1000);
            a(e.b());
            a = false;
            a = false;
        } catch (Throwable th) {
            a = false;
        }
    }
}
