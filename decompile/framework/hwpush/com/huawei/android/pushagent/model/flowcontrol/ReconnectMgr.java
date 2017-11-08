package com.huawei.android.pushagent.model.flowcontrol;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import defpackage.ae;
import defpackage.ag;
import defpackage.aj;
import defpackage.ak;
import defpackage.aw;
import defpackage.bt;
import defpackage.g;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class ReconnectMgr {
    private static int bc = 3;
    private static long bd = 600000;
    private static long be = 300000;
    private static long bf = 300000;
    private static ReconnectMgr bg = null;
    private int bh = 0;
    private ArrayList bi = new ArrayList();

    public enum RECONNECTEVENT {
        SOCKET_CLOSE,
        SOCKET_CONNECTED,
        TRS_QUERIED,
        NETWORK_CHANGE
    }

    private ReconnectMgr() {
    }

    private void a(Context context, boolean z) {
        ak akVar;
        aw.d("PushLog2841", "save connection info " + z);
        long currentTimeMillis = System.currentTimeMillis();
        Collection arrayList = new ArrayList();
        Iterator it = this.bi.iterator();
        while (it.hasNext()) {
            akVar = (ak) it.next();
            if (currentTimeMillis < akVar.bF() || currentTimeMillis - akVar.bF() > bd) {
                arrayList.add(akVar);
            }
        }
        if (!arrayList.isEmpty()) {
            aw.d("PushLog2841", "some connection info is expired:" + arrayList.size());
            this.bi.removeAll(arrayList);
        }
        akVar = new ak();
        akVar.j(z);
        akVar.j(System.currentTimeMillis());
        if (this.bi.size() < bc) {
            this.bi.add(akVar);
        } else {
            this.bi.remove(0);
            this.bi.add(akVar);
        }
        String str = "|";
        StringBuffer stringBuffer = new StringBuffer();
        Iterator it2 = this.bi.iterator();
        while (it2.hasNext()) {
            stringBuffer.append(((ak) it2.next()).toString());
            stringBuffer.append(str);
        }
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        new bt(context, "PushConnectControl").f("connectPushSvrInfos", stringBuffer.toString());
    }

    private void b(Context context, boolean z) {
        aw.d("PushLog2841", "set bad network mode " + z);
        ag.a(context, new g("isBadNetworkMode", Boolean.class, Boolean.valueOf(z)));
    }

    private boolean bC() {
        if (this.bi.size() < bc) {
            aw.d("PushLog2841", "total connect times is less than " + bc);
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        Iterator it = this.bi.iterator();
        int i = 0;
        while (it.hasNext()) {
            ak akVar = (ak) it.next();
            int i2 = (currentTimeMillis <= akVar.bF() || currentTimeMillis - akVar.bF() >= bd) ? i : i + 1;
            i = i2;
        }
        aw.d("PushLog2841", "connect times in last " + bd + " is " + i + ", limits is " + bc);
        return i >= bc;
    }

    private void bD() {
        this.bh = 0;
    }

    private void bE() {
        this.bh++;
    }

    public static synchronized ReconnectMgr s(Context context) {
        ReconnectMgr reconnectMgr;
        synchronized (ReconnectMgr.class) {
            if (bg == null) {
                bg = new ReconnectMgr();
            }
            if (bg.bi.isEmpty()) {
                bg.t(context);
            }
            reconnectMgr = bg;
        }
        return reconnectMgr;
    }

    private void t(Context context) {
        int i = 0;
        bc = ae.l(context).ah();
        bd = ae.l(context).ag();
        be = ae.l(context).ar();
        bf = ae.l(context).aq();
        String string = new bt(context, "PushConnectControl").getString("connectPushSvrInfos");
        if (!TextUtils.isEmpty(string)) {
            aw.d("PushLog2841", "connectPushSvrInfos is " + string);
            for (String str : string.split("\\|")) {
                ak akVar = new ak();
                if (akVar.load(str)) {
                    this.bi.add(akVar);
                }
            }
        }
        Collections.sort(this.bi);
        if (this.bi.size() > bc) {
            Collection arrayList = new ArrayList();
            int size = this.bi.size() - bc;
            while (i < size) {
                arrayList.add(this.bi.get(i));
                i++;
            }
            this.bi.removeAll(arrayList);
        }
    }

    private void u(Context context) {
        if (!y(context)) {
            aw.d("PushLog2841", "It is not bad network mode, do nothing");
        } else if (this.bi.isEmpty()) {
            b(context, false);
        } else {
            ak akVar = (ak) this.bi.get(this.bi.size() - 1);
            if (akVar.bG()) {
                aw.d("PushLog2841", "last connection is success");
                long currentTimeMillis = System.currentTimeMillis();
                long bF = akVar.bF();
                if (currentTimeMillis - bF > be || currentTimeMillis < bF) {
                    aw.d("PushLog2841", be + " has passed since last connect");
                    b(context, false);
                    return;
                }
                aw.d("PushLog2841", "connection keep too short , still in bad network mode");
                return;
            }
            aw.d("PushLog2841", "last connection result is false , still in bad network mode");
        }
    }

    private long w(Context context) {
        if (this.bi.isEmpty()) {
            aw.d("PushLog2841", "first connection, return 0");
            return 0;
        }
        long y;
        long C;
        if (!ag.a(context, "cloudpush_isNoDelayConnect", false)) {
            if (((long) this.bh) != ae.l(context).G()) {
                switch (this.bh) {
                    case 0:
                        y = 1000 * ae.l(context).y();
                        break;
                    case Reporter.ACTIVITY_CREATE /*1*/:
                        y = 1000 * ae.l(context).z();
                        break;
                    case Reporter.ACTIVITY_RESUME /*2*/:
                        y = 1000 * ae.l(context).A();
                        break;
                    case Reporter.ACTIVITY_PAUSE /*3*/:
                        y = 1000 * ae.l(context).B();
                        break;
                    default:
                        C = 1000 * ae.l(context).C();
                        ae.l(context).aO = true;
                        y = C;
                        break;
                }
            }
            ae.l(context).aO = true;
            y = 1000 * ae.l(context).C();
        } else {
            y = 1000;
        }
        long currentTimeMillis = System.currentTimeMillis();
        C = ((ak) this.bi.get(this.bi.size() - 1)).bk;
        if (currentTimeMillis < C) {
            aw.d("PushLog2841", "now is less than last connect time");
            C = 0;
        } else {
            C = Math.max((C + y) - currentTimeMillis, 0);
        }
        aw.i("PushLog2841", "after getConnectPushSrvInterval:" + C + " ms, connectTimes:" + this.bh);
        return C;
    }

    private long x(Context context) {
        if (bC()) {
            b(context, true);
        }
        boolean y = y(context);
        aw.d("PushLog2841", "bad network mode is " + y);
        if (!y || this.bi.isEmpty()) {
            return 0;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long b = ((ak) this.bi.get(this.bi.size() - 1)).bk;
        if (currentTimeMillis < b) {
            aw.d("PushLog2841", "now is less than last connect time");
            b = 0;
        } else {
            b = Math.max((b + bf) - currentTimeMillis, 0);
        }
        aw.d("PushLog2841", "It is in bad network mode, connect limit interval is " + b);
        return b;
    }

    private boolean y(Context context) {
        return ag.a(context, "isBadNetworkMode", false);
    }

    public void a(Context context, RECONNECTEVENT reconnectevent, Bundle bundle) {
        aw.d("PushLog2841", "receive reconnectevent:" + reconnectevent);
        switch (aj.bj[reconnectevent.ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                bD();
                return;
            case Reporter.ACTIVITY_RESUME /*2*/:
                bD();
                return;
            case Reporter.ACTIVITY_PAUSE /*3*/:
                ErrorType errorType = ErrorType.Err_unKnown;
                u(context);
                if (bundle.containsKey("errorType")) {
                    errorType = (ErrorType) bundle.getSerializable("errorType");
                    if (ErrorType.Err_Connect == errorType) {
                        a(context, false);
                    } else {
                        aw.d("PushLog2841", "socket close not caused by connect error, do not need save connection info");
                    }
                } else {
                    aw.d("PushLog2841", "socket close not caused by pushException");
                }
                bE();
                boolean ca = PushBastet.ac(context).ca();
                if (!ca || (ca && ErrorType.Err_Read != r0)) {
                    aw.i("PushLog2841", "socket closed, set alarm to reconnect srv");
                    ChannelMgr.g(context).g(v(context));
                    return;
                }
                return;
            case Reporter.ACTIVITY_DESTROY /*4*/:
                bD();
                a(context, true);
                return;
            default:
                return;
        }
    }

    public long v(Context context) {
        return Math.max(w(context), x(context));
    }
}
