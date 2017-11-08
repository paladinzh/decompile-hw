package defpackage;

import android.content.Context;
import com.huawei.android.pushagent.model.channel.ChannelMgr;

/* renamed from: av */
final class av implements Runnable {
    final /* synthetic */ Context val$context;

    av(Context context) {
        this.val$context = context;
    }

    public void run() {
        try {
            Thread.sleep(ae.l(this.val$context).ac() * 1000);
            au.J(this.val$context);
            aw.d("PushLog2841", "start to handle clone event");
            au.P(this.val$context);
            au.K(this.val$context);
            au.n(this.val$context, "pushConfig");
            ag.n(this.val$context).init();
            au.Q(this.val$context);
            ChannelMgr.g(this.val$context).aW().close();
        } catch (Throwable e) {
            aw.d("PushLog2841", "handle backup error:" + e.getMessage(), e);
        }
    }
}
