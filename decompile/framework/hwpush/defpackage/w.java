package defpackage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.pushmessage.HeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr;
import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import java.net.InetSocketAddress;

/* renamed from: w */
public class w extends ConnectEntity {
    private boolean ar = false;

    public w(j jVar, Context context) {
        super(jVar, context, new y(context), w.class.getSimpleName());
        bm();
    }

    public void a(SocketEvent socketEvent, Bundle bundle) {
        int a = ag.a(this.context, "tryConnectPushSevTimes", 0);
        int a2 = ag.a(this.context, "lastConnectPushSrvMethodIdx", 0);
        aw.d("PushLog2841", "enter PushConnectEntity. notifyEvent is " + socketEvent + ", " + " tryConnectPushSevTimes:" + a + " lastConnctIdx:" + a2);
        switch (x.ap[socketEvent.ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                PushService.a(new Intent("com.huawei.android.push.intent.CONNECTING"));
                return;
            case Reporter.ACTIVITY_RESUME /*2*/:
                this.T.bc();
                this.T.h(System.currentTimeMillis());
                ReconnectMgr.s(this.context).a(this.context, RECONNECTEVENT.SOCKET_CONNECTED, new Bundle());
                ag.a(this.context, new g("lastcontectsucc_time", Long.class, Long.valueOf(System.currentTimeMillis())));
                Intent intent = new Intent("com.huawei.android.push.intent.CONNECTED");
                if (bundle != null) {
                    intent.putExtras(bundle);
                }
                PushService.a(intent);
                return;
            case Reporter.ACTIVITY_PAUSE /*3*/:
                bundle.putInt("connect_mode", bb().ordinal());
                PushService.a(new Intent("com.huawei.android.push.intent.CHANNEL_CLOSED").putExtras(bundle));
                if (ChannelMgr.aV() == bb()) {
                    bq.w(this.context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
                    ReconnectMgr.s(this.context).a(this.context, RECONNECTEVENT.SOCKET_CLOSE, bundle);
                }
                if (!this.ar) {
                    int i = a + 1;
                    aw.i("PushLog2841", "channel is not Regist, tryConnectPushSevTimes add to " + i);
                    ag.a(this.context, new g("tryConnectPushSevTimes", Integer.class, Integer.valueOf(i)));
                    ag.a(this.context, new g("lastConnectPushSrvMethodIdx", Integer.class, Integer.valueOf(a2)));
                    return;
                }
                return;
            case Reporter.ACTIVITY_DESTROY /*4*/:
                bq.w(this.context, "com.huawei.android.push.intent.RESPONSE_FAIL");
                IPushMessage iPushMessage = (IPushMessage) bundle.getSerializable("push_msg");
                if (iPushMessage == null) {
                    aw.i("PushLog2841", "push_msg is null");
                    return;
                }
                aw.d("PushLog2841", "received pushSrv Msg:" + au.e(iPushMessage.k()));
                if (iPushMessage.k() == (byte) -45 || iPushMessage.k() == (byte) -33) {
                    this.ar = true;
                    ag.a(this.context, new g("lastConnectPushSrvMethodIdx", Integer.class, Integer.valueOf(b(a, a2))));
                    ag.a(this.context, new g("tryConnectPushSevTimes", Integer.class, Integer.valueOf(0)));
                } else if ((iPushMessage instanceof HeartBeatRspMessage) || (iPushMessage instanceof NewHeartBeatRspMessage)) {
                    bq.w(this.context, "com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT");
                    this.T.f(false);
                }
                this.T.bc();
                Intent intent2 = new Intent("com.huawei.android.push.intent.MSG_RECEIVED");
                intent2.putExtra("push_msg", iPushMessage);
                PushService.a(intent2);
                return;
            default:
                return;
        }
    }

    public synchronized void a(boolean z) {
        a(z, false);
    }

    public synchronized void a(boolean z, boolean z2) {
        try {
            if (PushBastet.ac(this.context).ca()) {
                aw.i("PushLog2841", "enter connect, bastetProxy is started");
                if (hasConnection()) {
                    aw.i("PushLog2841", "enter connect, has Connection, do not reconnect");
                } else if (au.G(this.context) == -1) {
                    aw.e("PushLog2841", "no network, so cannot connect");
                } else {
                    aw.i("PushLog2841", "enter connect, hasResetBastetAlarm " + this.W);
                    if (!this.W) {
                        c(true);
                        bq.b(this.context, new Intent("com.huawei.android.push.intent.RESET_BASTET").setPackage(this.context.getPackageName()), ae.l(this.context).aw());
                        aw.i("PushLog2841", "bastetProxyStarted, setDelayAlarm");
                    }
                }
            } else {
                aw.d("PushLog2841", "enter PushConnectEntity:connect(isForceToConnPushSrv:" + z + ")");
                this.T.bd();
                if (ae.l(this.context).isValid()) {
                    if (au.G(this.context) == -1) {
                        aw.e("PushLog2841", "no network, so cannot connect");
                    } else {
                        if (ag.a(this.context, "cloudpush_isNoDelayConnect", false)) {
                            z = true;
                        }
                        if (!hasConnection()) {
                            int a = ag.a(this.context, "tryConnectPushSevTimes", 0);
                            long v = ReconnectMgr.s(this.context).v(this.context);
                            if (v <= 0) {
                                aw.i("PushLog2841", "no limit to connect pushsvr");
                            } else if (this.V) {
                                aw.i("PushLog2841", "no limit to connect pushsvr, skipControl");
                                b(false);
                            } else {
                                ChannelMgr.g(this.context).g(v);
                            }
                            if (this.R == null || !this.R.isAlive()) {
                                aw.d("PushLog2841", "begin to create new socket, so close socket");
                                ba();
                                close();
                                aw.d("PushLog2841", "IS_NODELAY_CONNECT:" + ag.a(this.context, "cloudpush_isNoDelayConnect", false) + " hasMsg:" + z2);
                                if (ag.a(this.context, "cloudpush_isNoDelayConnect", false) || z2 || ai.a(this.context, 1)) {
                                    this.ar = false;
                                    int a2 = ag.a(this.context, "lastConnectPushSrvMethodIdx", 0);
                                    InetSocketAddress g = ae.l(this.context).g(z);
                                    if (g != null) {
                                        aw.d("PushLog2841", "get pushSrvAddr:" + g);
                                        this.Q.E = g.getAddress().getHostAddress();
                                        this.Q.port = g.getPort();
                                        this.Q.F = ag.o(this.context);
                                        this.Q = a(a2, a);
                                        this.R = new z(this);
                                        this.R.start();
                                    } else {
                                        aw.d("PushLog2841", "no valid pushSrvAddr, just wait!!");
                                    }
                                } else {
                                    ChannelMgr.g(this.context).a(ConnectEntityMode.ConnectEntity_Polling);
                                    ChannelMgr.g(this.context).a(ConnectEntityMode.ConnectEntity_Polling, false);
                                }
                            } else {
                                aw.i("PushLog2841", "It is in connecting...");
                            }
                        } else if (z) {
                            aw.d("PushLog2841", "hasConnect, but isForceToConnPushSrv:" + z + ", so send heartBeat");
                            this.T.bi();
                        } else {
                            aw.d("PushLog2841", "aready connect, need not connect more");
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new PushException(e);
        }
    }

    public ConnectEntityMode bb() {
        return ConnectEntityMode.ConnectEntity_Push;
    }

    public boolean bm() {
        if (this.Q == null) {
            this.Q = new j("", -1, false, ag.o(this.context));
        }
        return true;
    }
}
