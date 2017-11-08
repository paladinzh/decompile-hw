package defpackage;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.huawei.android.pushagent.PushService;

/* renamed from: d */
public class d extends Thread {
    private static long n = 2000;
    private MessageQueue l;
    private WakeLock m = ((PowerManager) this.mContext.getSystemService("power")).newWakeLock(1, "eventloop");
    private Context mContext;
    public Handler mHandler;

    public d(Context context) {
        super("ReceiverDispatcher");
        this.mContext = context;
    }

    private void h() {
        try {
            if (this.m != null && this.m.isHeld()) {
                this.m.release();
            }
        } catch (Exception e) {
            aw.e("PushLog2841", e.toString());
        }
    }

    public void a(o oVar, Intent intent) {
        if (this.mHandler == null) {
            aw.e("PushLog2841", "ReceiverDispatcher: the handler is null");
            PushService.c().stopService();
            return;
        }
        try {
            if (!this.m.isHeld()) {
                this.m.acquire(n);
            }
            if (!this.mHandler.postDelayed(new f(this, oVar, intent, null), 1)) {
                aw.w("PushLog2841", "postDelayed runnable error");
                throw new Exception("postDelayed runnable error");
            }
        } catch (Exception e) {
            h();
        }
    }

    public void run() {
        try {
            Looper.prepare();
            this.mHandler = new Handler();
            this.l = Looper.myQueue();
            this.l.addIdleHandler(new e(this));
            Looper.loop();
            aw.i("PushLog2841", "ReceiverDispatcher thread exit!");
        } catch (Throwable th) {
            aw.e("PushLog2841", aw.getStackTraceString(th));
        } finally {
            h();
        }
    }
}
