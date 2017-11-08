package com.huawei.android.pushagent.model.channel;

import android.content.Context;
import android.content.Intent;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import defpackage.ae;
import defpackage.ag;
import defpackage.au;
import defpackage.aw;
import defpackage.bq;
import defpackage.g;
import defpackage.q;
import defpackage.s;
import defpackage.w;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ChannelMgr {
    private static ChannelMgr M = null;
    private ConnectEntityMode K = ConnectEntityMode.ConnectEntity_Polling;
    private ConnectEntity[] L = new ConnectEntity[ConnectEntityMode.values().length];
    private Context context;

    public enum ConnectEntityMode {
        ConnectEntity_Push,
        ConnectEntity_Polling
    }

    private ChannelMgr(Context context) {
        this.context = context;
    }

    public static ConnectEntityMode aV() {
        return g(null).K;
    }

    public static ConnectEntity aX() {
        return g(null).L[ConnectEntityMode.ConnectEntity_Push.ordinal()];
    }

    public static ConnectEntity aY() {
        return g(null).L[ConnectEntityMode.ConnectEntity_Polling.ordinal()];
    }

    public static synchronized ChannelMgr g(Context context) {
        ChannelMgr channelMgr;
        synchronized (ChannelMgr.class) {
            if (M != null) {
                channelMgr = M;
            } else if (context == null) {
                aw.e("PushLog2841", "when init ChannelMgr g_channelMgr and context all null!!");
                channelMgr = null;
            } else {
                M = new ChannelMgr(context);
                M.init();
                channelMgr = M;
            }
        }
        return channelMgr;
    }

    public static q h(Context context) {
        return g(context).aW().T;
    }

    private static void i(Context context) {
        aw.d("PushLog2841", "enter ConnectMgrProcessor:cancelDelayAlarm");
        bq.w(context, "com.huawei.action.CONNECT_PUSHSRV");
        bq.w(context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
        bq.h(context, new Intent("com.huawei.intent.action.PUSH").putExtra("EXTRA_INTENT_TYPE", "com.huawei.android.push.intent.HEARTBEAT_REQ").putExtra("heartbeat_interval", 2592000000L).setPackage(context.getPackageName()));
    }

    private boolean init() {
        aw.d("PushLog2841", "begin to init ChannelMgr");
        int a = ag.a(this.context, "curConnectEntity", ConnectEntityMode.ConnectEntity_Polling.ordinal());
        aw.d("PushLog2841", "in cfg curConEntity:" + a);
        if (a >= 0 && a < ConnectEntityMode.values().length) {
            this.K = ConnectEntityMode.values()[a];
        }
        if (ConnectEntityMode.ConnectEntity_Polling == this.K && !ae.l(this.context).an() && ae.l(this.context).am()) {
            this.K = ConnectEntityMode.ConnectEntity_Push;
        }
        this.L[ConnectEntityMode.ConnectEntity_Push.ordinal()] = new w(null, this.context);
        this.L[ConnectEntityMode.ConnectEntity_Polling.ordinal()] = new s(null, this.context);
        return true;
    }

    public void a(ConnectEntityMode connectEntityMode) {
        this.K = connectEntityMode;
        if (ConnectEntityMode.ConnectEntity_Polling == connectEntityMode && !ae.l(this.context).an() && ae.l(this.context).am()) {
            connectEntityMode = ConnectEntityMode.ConnectEntity_Push;
        }
        ag.a(this.context, new g("curConnectEntity", Integer.class, Integer.valueOf(connectEntityMode.ordinal())));
    }

    public void a(ConnectEntityMode connectEntityMode, boolean z) {
        aw.e("PushLog2841", "enter ChannelMgr:connect(entity" + connectEntityMode + ", forceCon" + z + ")");
        if (connectEntityMode != null) {
            try {
                this.L[connectEntityMode.ordinal()].a(z);
                return;
            } catch (Throwable e) {
                aw.d("PushLog2841", e.toString(), e);
                return;
            }
        }
        aw.e("PushLog2841", "entityMode is invalid!!");
    }

    public List aT() {
        List linkedList = new LinkedList();
        for (ConnectEntity connectEntity : this.L) {
            if (connectEntity.T.be() != null) {
                linkedList.add(connectEntity.T.be());
            }
        }
        return linkedList;
    }

    public void aU() {
        i(this.context);
        for (ConnectEntity close : this.L) {
            close.close();
        }
    }

    public ConnectEntity aW() {
        aw.d("PushLog2841", "enter getCurConnetEntity(curConnectType:" + this.K + ", ordinal:" + this.K.ordinal() + " curConnect:" + this.L[this.K.ordinal()].getClass().getSimpleName() + ")");
        if (ConnectEntityMode.ConnectEntity_Polling == this.K && !ae.l(this.context).an() && ae.l(this.context).am()) {
            aw.d("PushLog2841", "polling srv is not ready, push is ok, so change it to Push");
            this.K = ConnectEntityMode.ConnectEntity_Push;
        }
        return this.L[this.K.ordinal()];
    }

    public ChannelType aZ() {
        ConnectEntity aW = aW();
        if (aW == null) {
            aw.e("PushLog2841", "getCurrentChannelType:currentConnectEntity is null");
            return null;
        } else if (aW.S != null) {
            return aW().S.br();
        } else {
            aw.e("PushLog2841", "channel is null");
            return null;
        }
    }

    public void c(Intent intent) {
        String action = intent.getAction();
        String stringExtra = intent.getStringExtra("EXTRA_INTENT_TYPE");
        if ("com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT".equals(action)) {
            aw.i("PushLog2841", "time out for wait heartbeat so reconnect");
            h(this.context).f(true);
            Socket socket = aW().getSocket();
            boolean au = ae.l(this.context).au();
            if (socket != null && au) {
                try {
                    aw.d("PushLog2841", "setSoLinger 0 when close socket after heartbeat timeout");
                    socket.setSoLinger(true, 0);
                } catch (Throwable e) {
                    aw.d("PushLog2841", e.toString(), e);
                }
            }
            aW().close();
            if (-1 != au.G(this.context) && aV() == ConnectEntityMode.ConnectEntity_Push) {
                try {
                    aW().a(false);
                } catch (Throwable e2) {
                    aw.d("PushLog2841", e2.toString(), e2);
                }
            }
        } else if ("com.huawei.intent.action.PUSH".equals(action) && "com.huawei.android.push.intent.HEARTBEAT_REQ".equals(stringExtra)) {
            if (-1 != au.G(this.context)) {
                ConnectEntity aW = aW();
                if (aW.hasConnection()) {
                    aW.T.d(intent.getBooleanExtra("isHeartbeatReq", true));
                    aW.T.bi();
                    return;
                }
                PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.context.getPackageName()));
                return;
            }
            aw.e("PushLog2841", "when send heart beat, not net work");
            h(this.context).bd();
        } else if (!"android.intent.action.TIME_SET".equals(action) && !"android.intent.action.TIMEZONE_CHANGED".equals(action)) {
        } else {
            if (aW().hasConnection()) {
                h(this.context).d(false);
                h(this.context).bi();
            } else if (-1 != au.G(this.context)) {
                aw.d("PushLog2841", "received " + action + ", but not Connect, go to connect!");
                PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV"));
            } else {
                aw.i("PushLog2841", "no net work, when recevice :" + action + ", do nothing");
            }
        }
    }

    public void g(long j) {
        aw.i("PushLog2841", "next connect pushsvr will be after " + j);
        Intent intent = new Intent("com.huawei.action.CONNECT_PUSHSRV");
        intent.setPackage(this.context.getPackageName());
        bq.b(this.context, intent, j);
    }
}
