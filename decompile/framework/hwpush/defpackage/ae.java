package defpackage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT;
import java.net.InetSocketAddress;
import java.util.Date;

/* renamed from: ae */
public class ae extends k {
    private static ae aP = null;
    private Thread aN = null;
    public boolean aO = false;

    private ae(Context context) {
        super(context);
        i();
    }

    private boolean b(k kVar) {
        if (kVar == null || !kVar.isValid()) {
            aw.e("PushLog2841", "in PushSrvInfo:trsRetInfo, trsRetInfo is null or invalid:" + kVar);
            return false;
        }
        aw.d("PushLog2841", "queryTrs success!");
        if (!a(kVar)) {
            aw.d("PushLog2841", "heart beat range change.");
            PushService.a(new Intent("com.huawei.android.push.intent.HEARTBEAT_RANGE_CHANGE"));
        }
        if (kVar.u.containsKey("USE_SSL")) {
            ag.a(null, new g("USE_SSL", Integer.class, Integer.valueOf(((Integer) kVar.u.get("USE_SSL")).intValue())));
        }
        if (!f(kVar.p())) {
            aw.i("PushLog2841", "belongId changed, need to reRegisterDeviceToken");
            ao.B(this.context);
        }
        this.u.putAll(kVar.u);
        a("pushSrvValidTime", (Object) Long.valueOf((t() * 1000) + System.currentTimeMillis()));
        ag.a(this.context, new g("queryTrsTimes", Integer.class, Integer.valueOf(0)));
        aw.d("PushLog2841", "write the lastQueryTRSsucc_time to the pushConfig.xml file ");
        ag.a(this.context, new g("lastQueryTRSsucc_time", Long.class, Long.valueOf(System.currentTimeMillis())));
        this.aO = false;
        this.u.remove("PushID");
        j();
        ReconnectMgr.s(this.context).a(this.context, RECONNECTEVENT.TRS_QUERIED, new Bundle());
        PushService.a(new Intent("com.huawei.android.push.intent.TRS_QUERY_SUCCESS").putExtra("trs_result", kVar.toString()));
        return true;
    }

    private synchronized boolean bt() {
        boolean z;
        if (bu()) {
            aw.d("PushLog2841", " trsQuery thread already running, just wait!!");
            z = false;
        } else {
            this.aN = new af(this, "PushTRSQuery");
            this.aN.start();
            ag.a(this.context, new g("lastQueryTRSTime", Long.class, Long.valueOf(System.currentTimeMillis())));
            ag.a(this.context, new g("queryTrsTimes", Long.class, Long.valueOf(ag.a(this.context, "queryTrsTimes", 0) + 1)));
            z = true;
        }
        return z;
    }

    private synchronized boolean bu() {
        boolean z;
        z = this.aN != null && this.aN.isAlive();
        return z;
    }

    public static synchronized ae l(Context context) {
        ae aeVar;
        synchronized (ae.class) {
            if (aP == null) {
                aP = new ae(context);
            }
            aeVar = aP;
        }
        return aeVar;
    }

    public static void m(Context context) {
        if (aP != null) {
            aP.a("pushSrvValidTime", (Object) Integer.valueOf(0));
            aP.aO = true;
        }
    }

    public InetSocketAddress g(boolean z) {
        boolean i = i(z);
        if (!isValid() || i) {
            aw.i("PushLog2841", "in getPushSrvAddr, have no invalid addr");
            return null;
        }
        aw.d("PushLog2841", "return valid PushSrvAddr");
        return new InetSocketAddress(q(), r());
    }

    public InetSocketAddress h(boolean z) {
        boolean i = i(z);
        if (!an() || i) {
            aw.i("PushLog2841", "in getPollingAddr, have no invalid addr");
            return null;
        }
        aw.d("PushLog2841", "return valid PollingSrvAddr");
        return new InetSocketAddress(Q(), R());
    }

    public boolean i(boolean z) {
        if (bu()) {
            aw.i("PushLog2841", "trsQuery thread is running");
            return true;
        }
        long a = ag.a(this.context, "lastQueryTRSTime", 0);
        long a2 = ag.a(this.context, "lastQueryTRSsucc_time", 0);
        aw.i("PushLog2841", "isvalid:" + isValid() + " srvValidBefore:" + (getLong("pushSrvValidTime", Long.MAX_VALUE) - System.currentTimeMillis()) + " pushSrvNeedQueryTRS:" + this.aO);
        if (isValid()) {
            if (!this.aO && getLong("pushSrvValidTime", Long.MAX_VALUE) >= System.currentTimeMillis() && System.currentTimeMillis() > a2) {
                aw.i("PushLog2841", " need not query TRS");
                return false;
            } else if (this.aO && System.currentTimeMillis() - a < s() * 1000 && System.currentTimeMillis() > a) {
                aw.i("PushLog2841", " cannot query TRS in trsValid_min, pushSrvNeedQueryTRS, info:" + toString());
                return false;
            }
        }
        if (-1 == au.G(this.context)) {
            aw.i("PushLog2841", "in queryTRSInfo no network");
            return false;
        }
        if (z) {
            aw.i("PushLog2841", "Force to Connect TRS");
        } else {
            long currentTimeMillis = System.currentTimeMillis() - a2;
            if (currentTimeMillis <= 0 || currentTimeMillis >= s() * 1000) {
                a2 = E() * 1000;
                if (ag.a(this.context, "queryTrsTimes", 0) > H()) {
                    a2 = F() * 1000;
                }
                currentTimeMillis = System.currentTimeMillis() - a;
                if (currentTimeMillis > 0 && currentTimeMillis < a2) {
                    aw.i("PushLog2841", "can't connect TRS Service when the connectting time more later " + (a2 / 1000) + "sec than  last contectting time,lastQueryTRSTime =" + new Date(a));
                    return false;
                }
            }
            aw.i("PushLog2841", "can not contect TRS Service when  the connect more than " + s() + " sec last contected success time," + "lastQueryTRSsucc_time = " + new Date(a2));
            return false;
        }
        if (ag.a(this.context, "cloudpush_isNoDelayConnect", false) || ai.p(this.context)) {
            return bt();
        }
        aw.i("PushLog2841", "ConnectControlMgr.canQueryTRS is false");
        return false;
    }
}
