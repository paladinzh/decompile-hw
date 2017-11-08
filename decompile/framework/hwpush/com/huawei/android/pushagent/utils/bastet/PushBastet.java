package com.huawei.android.pushagent.utils.bastet;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import com.huawei.android.bastet.HwBastet;
import com.huawei.android.pushagent.PushService;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;
import defpackage.aa;
import defpackage.ae;
import defpackage.au;
import defpackage.aw;
import defpackage.bc;
import defpackage.bq;
import java.lang.reflect.Method;
import java.net.Socket;

public class PushBastet {
    private static PushBastet bR = null;
    private HwBastet bS = null;
    private BasteProxyStatus bT = BasteProxyStatus.Stoped;
    private Context mContext;
    private Handler mHandler = null;

    enum BasteProxyStatus {
        Started,
        Stoped
    }

    private PushBastet(Context context) {
        this.mContext = context;
    }

    private boolean a(int i, long j) {
        aw.d("PushLog2841", "initPushHeartBeatDataContent");
        try {
            NewHeartBeatReqMessage newHeartBeatReqMessage = new NewHeartBeatReqMessage();
            newHeartBeatReqMessage.d((byte) ((int) Math.ceil((1.0d * ((double) j)) / 60000.0d)));
            byte[] b = aa.b(newHeartBeatReqMessage.encode());
            aw.d("PushLog2841", "heartbeat fixedSendContent is : " + au.f(b));
            byte[] b2 = aa.b(new NewHeartBeatRspMessage().encode());
            aw.d("PushLog2841", "heartbeat fixedReplyContent is : " + au.f(b2));
            this.bS.setAolHeartbeat(i, b, b2);
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2841", "initPushHeartBeatDataContent error :" + e.toString(), e);
            return false;
        }
    }

    public static synchronized PushBastet ac(Context context) {
        PushBastet pushBastet;
        synchronized (PushBastet.class) {
            if (bR != null) {
                pushBastet = bR;
            } else {
                bR = new PushBastet(context);
                pushBastet = bR;
            }
        }
        return pushBastet;
    }

    private boolean bU() {
        aw.d("PushLog2841", "initPushBastet");
        if (!bW() || !bV()) {
            return false;
        }
        if (ConnectEntityMode.ConnectEntity_Polling == ChannelMgr.aV()) {
            aw.i("PushLog2841", "initPushBastet, getCurConnectMode is Polling");
            return false;
        } else if (!bX()) {
            return false;
        } else {
            Socket socket = ChannelMgr.aX().getSocket();
            if (socket == null || !cc()) {
                return false;
            }
            try {
                this.bS = new HwBastet("PUSH_BASTET", socket, this.mHandler, this.mContext);
                if (this.bS.isBastetAvailable()) {
                    this.bS.reconnectSwitch(true);
                    return true;
                }
                aw.i("PushLog2841", "isBastetAvailable false, can't use bastet.");
                cb();
                return false;
            } catch (Throwable e) {
                aw.c("PushLog2841", "init bastet error", e);
                cb();
                return false;
            }
        }
    }

    private boolean bV() {
        boolean at = ae.l(this.mContext).at();
        aw.d("PushLog2841", "isPushServerAllowBastet: " + at);
        return at;
    }

    private boolean bW() {
        try {
            Class.forName("com.huawei.android.bastet.HwBastet");
            return true;
        } catch (ClassNotFoundException e) {
            aw.e("PushLog2841", "bastet not exist");
            return false;
        }
    }

    private boolean bX() {
        if (ChannelType.ChannelType_Secure.equals(ChannelMgr.g(this.mContext).aZ())) {
            return true;
        }
        aw.d("PushLog2841", "only ChannelType_Secure support bastet");
        return false;
    }

    private synchronized void bY() {
        try {
            if (!(this.mHandler == null || this.mHandler.getLooper() == null)) {
                this.mHandler.getLooper().quitSafely();
            }
            this.mHandler = null;
        } catch (Throwable e) {
            aw.d("PushLog2841", "PushBastetListener release error", e);
        }
    }

    private void bZ() {
        aw.i("PushLog2841", "reConnectPush");
        cb();
        ChannelMgr.g(this.mContext).aW().close();
        PushService.a(new Intent("com.huawei.action.CONNECT_PUSHSRV").setPackage(this.mContext.getPackageName()));
    }

    private synchronized boolean cc() {
        boolean z = false;
        synchronized (this) {
            aw.d("PushLog2841", "initMsgHandler");
            try {
                if (!(this.mHandler == null || this.mHandler.getLooper() == null)) {
                    this.mHandler.getLooper().quitSafely();
                }
                HandlerThread handlerThread = new HandlerThread("bastetRspHandlerThread");
                handlerThread.start();
                int i = 0;
                while (!handlerThread.isAlive()) {
                    int i2 = i + 1;
                    try {
                        wait(10);
                        if (i2 % 100 == 0) {
                            aw.e("PushLog2841", "wait bastetRspHandlerThread start take time: " + (i2 * 10) + " ms");
                        }
                        if (i2 > 500) {
                            aw.e("PushLog2841", "reached the max retry times:500");
                            break;
                        }
                        i = i2;
                    } catch (Throwable e) {
                        aw.d("PushLog2841", "InterruptedException error", e);
                        i = i2;
                    }
                }
                if (handlerThread.getLooper() == null) {
                    aw.e("PushLog2841", "looper is null when initMsgHandler");
                } else {
                    this.mHandler = new bc(this, handlerThread.getLooper());
                    z = true;
                }
            } catch (Throwable e2) {
                aw.d("PushLog2841", "initMsgHandler error:" + e2.getMessage(), e2);
                cb();
            }
        }
        return z;
    }

    private void ce() {
        aw.d("PushLog2841", "enter clearBastetProxy!");
        if (this.bS == null) {
            aw.i("PushLog2841", "enter clearBastetProxy, mHwBastet is null");
            return;
        }
        try {
            Method declaredMethod = this.bS.getClass().getDeclaredMethod("clearBastetProxy", new Class[0]);
            declaredMethod.setAccessible(true);
            declaredMethod.invoke(this.bS, new Object[0]);
            aw.d("PushLog2841", "clearBastetProxy success!");
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
        } catch (Throwable e2) {
            aw.d("PushLog2841", e2.toString(), e2);
        } catch (Throwable e22) {
            aw.d("PushLog2841", e22.toString(), e22);
        } catch (Throwable e222) {
            aw.d("PushLog2841", e222.toString(), e222);
        } catch (Throwable e2222) {
            aw.c("PushLog2841", e2222.toString(), e2222);
        }
    }

    public void a(BasteProxyStatus basteProxyStatus) {
        this.bT = basteProxyStatus;
    }

    public synchronized boolean bT() {
        boolean z = true;
        synchronized (this) {
            try {
                if (ca()) {
                    aw.i("PushLog2841", "bastet has started, need not restart");
                } else if (bU()) {
                    if (a(3, 900000)) {
                        aw.i("PushLog2841", "startPushBastetProxy success");
                        a(BasteProxyStatus.Started);
                    }
                    aw.i("PushLog2841", "startPushBastetProxy failed");
                    z = false;
                } else {
                    aw.i("PushLog2841", "init push bastet failed!");
                    z = false;
                }
            } catch (Throwable e) {
                aw.d("PushLog2841", "startPushBastetProxy failed:" + e.getMessage(), e);
            }
        }
        return z;
    }

    public boolean ca() {
        return this.bS != null && BasteProxyStatus.Started == this.bT;
    }

    public void cb() {
        aw.i("PushLog2841", "resetBastet");
        ce();
        this.bS = null;
        a(BasteProxyStatus.Stoped);
        bY();
        ChannelMgr.aX().c(false);
        bq.w(this.mContext, "com.huawei.android.push.intent.RESET_BASTET");
    }

    public long cd() {
        return 900000;
    }
}
