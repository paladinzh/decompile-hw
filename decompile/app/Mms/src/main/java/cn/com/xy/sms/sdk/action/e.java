package cn.com.xy.sms.sdk.action;

import android.os.Bundle;
import android.os.Message;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/* compiled from: Unknown */
final class e extends Thread {
    private /* synthetic */ NearbyPoint a;

    private e(NearbyPoint nearbyPoint) {
        this.a = nearbyPoint;
    }

    public final void run() {
        Object obj = 1;
        try {
            if ((this.a.d < 0.0d ? 1 : null) == null) {
                if (this.a.e >= 0.0d) {
                    obj = null;
                }
                if (!(obj != null || this.a.f == null || this.a.f.equalsIgnoreCase(""))) {
                    String a = NearbyPoint.a("6a0ddfcfdf1a1e7a1f38501fc5d218bf", this.a.f, this.a.d, this.a.e, 20000, "json", 2, this.a.g);
                    if (a != null) {
                        Object futureTask = new FutureTask(new d(a));
                        new Thread(futureTask).start();
                        a = (String) futureTask.get(5000, TimeUnit.MILLISECONDS);
                        Message obtainMessage = this.a.c.obtainMessage(NearbyPoint.QUERY_RESULT_RECEIVE);
                        Bundle bundle = new Bundle();
                        bundle.putString(NearbyPoint.QUERY_RESULT, a);
                        obtainMessage.setData(bundle);
                        obtainMessage.sendToTarget();
                        return;
                    }
                    this.a.c.obtainMessage(NearbyPoint.GET_QUERY_URL_FAILURE).sendToTarget();
                    return;
                }
            }
            this.a.c.obtainMessage(NearbyPoint.QUERY_PARAM_ERROR).sendToTarget();
        } catch (Throwable th) {
            th.getMessage();
        }
    }
}
