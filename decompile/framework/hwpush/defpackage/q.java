package defpackage;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import java.util.Date;

/* renamed from: q */
public abstract class q {
    public long ae = 0;
    public boolean af = false;
    protected PushBastet ag;
    public int batteryStatus = 1;
    public Context context = null;

    public q(Context context) {
        this.context = context;
        this.ag = PushBastet.ac(context);
    }

    private void a(long j, long j2, boolean z) {
        if (this.ag.ca()) {
            aw.d("PushLog2841", "support bastet, need not to send delayed heartbeat");
            return;
        }
        Intent intent = new Intent("com.huawei.intent.action.PUSH");
        intent.putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ");
        intent.putExtra("heartbeat_interval", j);
        intent.putExtra("isHeartbeatReq", z);
        intent.setPackage(this.context.getPackageName());
        bq.a(this.context, intent, j2);
    }

    public void a(int i) {
        this.batteryStatus = i;
    }

    public void bc() {
        if (ChannelMgr.h(this.context) == this) {
            long e = e(false);
            aw.d("PushLog2841", "after delayHeartBeatReq, nextHeartBeatTime, will be " + e + "ms later");
            a(e, e, true);
        }
    }

    public void bd() {
        if (ChannelMgr.h(this.context) == this) {
            long bg = bg() - System.currentTimeMillis();
            aw.d("PushLog2841", "after updateHeartBeatReq, nextHeartBeatTime, will be " + bg + "ms later");
            a(bg, bg, true);
        }
    }

    public String be() {
        return getClass().getSimpleName();
    }

    public long bf() {
        return this.ae;
    }

    public long bg() {
        long currentTimeMillis = System.currentTimeMillis();
        long e = e(false);
        return (bf() > currentTimeMillis || bf() + e <= currentTimeMillis) ? currentTimeMillis + e : bf() + e;
    }

    public abstract q bh();

    public abstract void bi();

    protected abstract boolean bj();

    public void d(boolean z) {
        this.af = z;
    }

    public abstract long e(boolean z);

    public abstract void f(boolean z);

    public void h(long j) {
        this.ae = j;
        new bt(this.context, be()).a("lastHeartBeatTime", Long.valueOf(j));
    }

    public abstract boolean i(long j);

    public void j(Context context) {
        int G = au.G(context);
        long j = 0;
        if (G == 0) {
            j = ae.l(context).ao();
        } else if (1 == G) {
            j = ae.l(context).ap();
        }
        a(e(false), j, false);
    }

    public String toString() {
        return new StringBuffer().append("lastHeartBeatTime").append(new Date(this.ae)).append(" heartBeatInterval").append(e(false)).toString();
    }
}
