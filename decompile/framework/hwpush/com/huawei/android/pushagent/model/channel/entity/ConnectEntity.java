package com.huawei.android.pushagent.model.channel.entity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.huawei.android.pushagent.datatype.IPushMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.ChannelMgr.ConnectEntityMode;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import com.huawei.bd.Reporter;
import defpackage.ae;
import defpackage.au;
import defpackage.aw;
import defpackage.bq;
import defpackage.j;
import defpackage.p;
import defpackage.q;
import java.net.Socket;

public abstract class ConnectEntity {
    public j Q;
    public SocketReadThread R;
    public IPushChannel S;
    public q T;
    private PowerManager U;
    public boolean V;
    public boolean W = false;
    private WakeLock X = null;
    public Context context;

    public enum CONNECT_METHOD {
        CONNECT_METHOD_DIRECT_TrsPort,
        CONNECT_METHOD_DIRECT_DefaultPort,
        CONNECT_METHOD_Proxy_TrsPort,
        CONNECT_METHOD_Proxy_DefaultPort
    }

    public ConnectEntity(j jVar, Context context, q qVar, String str) {
        this.context = context;
        this.Q = jVar;
        this.T = qVar;
        this.U = (PowerManager) context.getSystemService("power");
    }

    public j a(int i, int i2) {
        switch (p.Y[CONNECT_METHOD.values()[b(i, i2)].ordinal()]) {
            case Reporter.ACTIVITY_CREATE /*1*/:
                return new j(this.Q.E, this.Q.port, false, this.Q.F);
            case Reporter.ACTIVITY_RESUME /*2*/:
                return new j(this.Q.E, 443, false, this.Q.F);
            case Reporter.ACTIVITY_PAUSE /*3*/:
                return new j(this.Q.E, 443, true, this.Q.F);
            case Reporter.ACTIVITY_DESTROY /*4*/:
                return new j(this.Q.E, this.Q.port, true, this.Q.F);
            default:
                return null;
        }
    }

    public abstract void a(SocketEvent socketEvent, Bundle bundle);

    public abstract void a(boolean z);

    public abstract void a(boolean z, boolean z2);

    public synchronized boolean a(IPushMessage iPushMessage) {
        boolean z = false;
        synchronized (this) {
            if (this.S == null || this.S.getSocket() == null) {
                aw.e("PushLog2841", "when send pushMsg, channel is null， curCls:" + getClass().getSimpleName());
            } else {
                if (ChannelMgr.aV() == bb()) {
                    this.S.getSocket().setSoTimeout(0);
                } else {
                    this.S.getSocket().setSoTimeout((int) (this.T.e(false) + ae.l(this.context).ae()));
                }
                byte[] bArr = null;
                if (iPushMessage != null) {
                    bArr = iPushMessage.encode();
                } else {
                    aw.e("PushLog2841", "pushMsg = null, send fail");
                }
                if (bArr == null || bArr.length == 0) {
                    aw.i("PushLog2841", "when send PushMsg, encode Len is null");
                } else {
                    aw.i("PushLog2841", "read to Send:" + au.e(iPushMessage.k()));
                    if (this.S.a(bArr)) {
                        aw.i("PushLog2841", "send msg to remote srv success");
                        if ((byte) -34 == iPushMessage.k() || (byte) -36 == iPushMessage.k() || (byte) -92 == iPushMessage.k()) {
                            bq.b(this.context, new Intent("com.huawei.android.push.intent.RESPONSE_FAIL").setPackage(this.context.getPackageName()), ae.l(this.context).ax());
                        }
                        z = true;
                    } else {
                        aw.e("PushLog2841", "call channel.send false!!");
                    }
                }
            }
        }
        return z;
    }

    public int b(int i, int i2) {
        return Math.abs(i + i2) % CONNECT_METHOD.values().length;
    }

    public synchronized void b(boolean z) {
        this.V = z;
    }

    public synchronized void ba() {
        this.X = this.U.newWakeLock(1, "mWakeLockForThread");
        this.X.setReferenceCounted(false);
        this.X.acquire(1000);
    }

    public abstract ConnectEntityMode bb();

    public synchronized void c(boolean z) {
        this.W = z;
    }

    public void close() {
        if (this.S != null) {
            try {
                this.S.close();
                this.S = null;
            } catch (Throwable e) {
                aw.d("PushLog2841", "call channel.close() cause:" + e.toString(), e);
            }
            if (this.R != null) {
                this.R.interrupt();
                this.R = null;
            }
        }
    }

    public Socket getSocket() {
        return this.S != null ? this.S.getSocket() : null;
    }

    public boolean hasConnection() {
        return this.S != null && this.S.hasConnection();
    }

    public String toString() {
        return this.Q.toString() + " " + this.T.toString();
    }
}
