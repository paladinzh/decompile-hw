package defpackage;

import android.content.Context;
import android.os.Bundle;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.datatype.pollingmessage.PollingDataReqMessage;
import com.huawei.android.pushagent.datatype.pollingmessage.PollingDataRspMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import com.huawei.android.pushagent.utils.bastet.PushBastet;
import com.huawei.bd.Reporter;
import java.net.InetSocketAddress;
import java.util.Date;

/* renamed from: s */
public class s extends ConnectEntity {
    public s(j jVar, Context context) {
        super(jVar, context, new u(context), s.class.getSimpleName());
        bm();
    }

    public void a(SocketEvent socketEvent, Bundle bundle) {
        aw.d("PushLog2841", "enter PollingConnectEntity:notifyEvent(" + socketEvent + ",bd:" + bundle + ")");
        switch (t.ap[socketEvent.ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                this.T.bc();
                this.T.h(System.currentTimeMillis());
                try {
                    a(new PollingDataReqMessage(ae.l(this.context).S()));
                    this.S.getSocket().setSoTimeout((int) (ae.l(this.context).J() * 1000));
                    return;
                } catch (Throwable e) {
                    aw.d("PushLog2841", "call send cause:" + e.toString(), e);
                    return;
                }
            case Reporter.ACTIVITY_RESUME /*2*/:
                IPushMessage iPushMessage = (IPushMessage) bundle.getSerializable("push_msg");
                if (iPushMessage == null) {
                    aw.i("PushLog2841", "push_msg is null");
                    return;
                }
                aw.i("PushLog2841", "received polling Msg:" + iPushMessage.getClass().getSimpleName());
                if (iPushMessage instanceof PollingDataRspMessage) {
                    PollingDataRspMessage pollingDataRspMessage = (PollingDataRspMessage) iPushMessage;
                    if (pollingDataRspMessage.az() < (byte) 0 || pollingDataRspMessage.az() > ConnectEntityMode.values().length) {
                        aw.e("PushLog2841", "received mode:" + pollingDataRspMessage.az() + " cannot be recongnized");
                        return;
                    }
                    ConnectEntityMode connectEntityMode = ConnectEntityMode.values()[pollingDataRspMessage.az()];
                    ChannelMgr.g(this.context).a(connectEntityMode);
                    if (ConnectEntityMode.ConnectEntity_Polling == connectEntityMode && PushBastet.ac(this.context).ca()) {
                        aw.i("PushLog2841", "bastet has started, but now is polling mode, close pushchannel to stop bastet");
                        ChannelMgr.aX().close();
                        PushBastet.ac(this.context).cb();
                    }
                    this.T.i((long) (pollingDataRspMessage.aB() * 1000));
                    if (pollingDataRspMessage.aA() || connectEntityMode == ConnectEntityMode.ConnectEntity_Push) {
                        try {
                            ChannelMgr.aX().a(true, pollingDataRspMessage.aA());
                        } catch (Throwable e2) {
                            aw.d("PushLog2841", e2.toString(), e2);
                        }
                    }
                    try {
                        this.S.close();
                        return;
                    } catch (Throwable e22) {
                        aw.d("PushLog2841", "call channel close cause:" + e22.toString(), e22);
                        return;
                    }
                }
                return;
            default:
                return;
        }
    }

    public synchronized void a(boolean z) {
        aw.d("PushLog2841", "enter PollingConnectEntity:connect(forceCon:" + z + ")");
        this.T.bd();
        if (ae.l(this.context).an()) {
            if (hasConnection()) {
                aw.i("PushLog2841", "Polling aready connect, just wait Rsp!");
            } else {
                if (!z) {
                    if (System.currentTimeMillis() < this.T.bf() + this.T.e(false) && System.currentTimeMillis() > this.T.bf()) {
                        aw.i("PushLog2841", "cannot connect, heartBeatInterval:" + this.T.e(false) + " lastCntTime:" + new Date(this.T.bf()));
                    }
                }
                if (au.G(this.context) == -1) {
                    aw.i("PushLog2841", "no network, so cannot connect Polling");
                } else if (this.R == null || !this.R.isAlive()) {
                    aw.d("PushLog2841", "begin to create new socket, so close socket");
                    ba();
                    close();
                    InetSocketAddress h = ae.l(this.context).h(false);
                    if (h != null) {
                        aw.d("PushLog2841", "get pollingSrvAddr:" + h);
                        this.Q.E = h.getAddress().getHostAddress();
                        this.Q.port = h.getPort();
                        this.R = new v(this);
                        this.R.start();
                    } else {
                        aw.e("PushLog2841", "no valid pollingSrvAddr, just wait!!");
                    }
                } else {
                    aw.i("PushLog2841", "aready in connect, just wait!! srvSocket:" + this.R.toString());
                }
            }
        }
    }

    public synchronized void a(boolean z, boolean z2) {
        a(z);
    }

    public ConnectEntityMode bb() {
        return ConnectEntityMode.ConnectEntity_Polling;
    }

    public boolean bm() {
        if (this.Q == null) {
            this.Q = new j("", -1, false, ChannelType.ChannelType_Normal);
        }
        return true;
    }
}
