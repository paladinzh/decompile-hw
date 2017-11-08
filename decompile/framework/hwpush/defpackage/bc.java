package defpackage;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;

/* renamed from: bc */
public class bc extends Handler {
    final /* synthetic */ PushBastet bU;

    public bc(PushBastet pushBastet, Looper looper) {
        this.bU = pushBastet;
        super(looper);
    }

    public void handleMessage(Message message) {
        try {
            super.handleMessage(message);
            int i = message.what;
            switch (i) {
                case Reporter.ACTIVITY_DESTROY /*4*/:
                    i = message.arg1;
                    aw.i("PushLog2841", "receive handler message BASTET_HEARTBEAT_CYCLE " + i);
                    this.bU.bS.pauseHeartbeat();
                    this.bU.a(i, ((long) (i * 5)) * 60000);
                    return;
                case Reporter.PRI_LOW /*5*/:
                    aw.i("PushLog2841", "receive handler message BASTET_HB_NOT_AVAILABLE");
                    this.bU.bZ();
                    return;
                case 7:
                    aw.i("PushLog2841", "receive handler message BASTET_RECONNECTION_BEST_POINT");
                    this.bU.bZ();
                    return;
                case 8:
                    aw.i("PushLog2841", "receive handler message BASTET_RECONNECTION_BREAK");
                    this.bU.bZ();
                    return;
                default:
                    aw.i("PushLog2841", "receive handler message default, what is " + i);
                    return;
            }
        } catch (Throwable e) {
            aw.d("PushLog2841", "handle bastetMessage error:" + e.getMessage(), e);
            this.bU.cb();
        }
        aw.d("PushLog2841", "handle bastetMessage error:" + e.getMessage(), e);
        this.bU.cb();
    }
}
