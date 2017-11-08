package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.ChannelMgr;

/* renamed from: u */
class u extends q {
    private long aq = -1;

    public u(Context context) {
        super(context);
        bh();
    }

    public String be() {
        return "Push_PollingHBeat";
    }

    public q bh() {
        this.ae = new bt(this.context, be()).getLong("lastHeartBeatTime");
        return this;
    }

    public void bi() {
        try {
            ChannelMgr.aY().a(false);
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
        }
    }

    protected boolean bj() {
        return false;
    }

    public long e(boolean z) {
        if (-1 == au.G(this.context)) {
            return ae.l(this.context).D() * 1000;
        }
        if (bj()) {
            bh();
        }
        if (this.aq > 0) {
            return this.aq;
        }
        long P = ae.l(this.context).P() * 1000;
        long currentTimeMillis = System.currentTimeMillis();
        if (bf() >= currentTimeMillis) {
            h(0);
        }
        return bf() <= currentTimeMillis - (ae.l(this.context).P() * 1000) ? ae.l(this.context).P() * 1000 : (bf() > currentTimeMillis || currentTimeMillis > bf() + (ae.l(this.context).P() * 1000)) ? P : (bf() + (ae.l(this.context).P() * 1000)) - currentTimeMillis;
    }

    public void f(boolean z) {
    }

    public boolean i(long j) {
        this.aq = j;
        return true;
    }
}
