package tmsdkobf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import tmsdk.common.ErrorCode;
import tmsdkobf.lb.b;

/* compiled from: Unknown */
public class fc {
    private static fc lK;
    private byte[] lL = new byte[0];
    private SparseArray<b> lM = new SparseArray();
    private Handler lN;
    private List<o> lO = new ArrayList();
    private pf lP;

    /* compiled from: Unknown */
    class a extends Handler {
        final /* synthetic */ fc lS;

        public a(fc fcVar, Looper looper) {
            this.lS = fcVar;
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    tmsdkobf.lb.a aVar = message.obj == null ? null : (tmsdkobf.lb.a) message.obj;
                    int i = message.arg1;
                    if (aVar != null) {
                        i = aVar.wx.H;
                    }
                    SparseArray b = this.lS.lM;
                    if (!(aVar == null || aVar.wy == null)) {
                        i = aVar.wy.wA;
                    }
                    b bVar = (b) b.get(i);
                    if (bVar != null) {
                        try {
                            bVar.a(aVar);
                            return;
                        } catch (Throwable th) {
                            return;
                        }
                    }
                    return;
                case 1:
                    fs fsVar;
                    synchronized (this.lS.lL) {
                        if (this.lS.lO.size() <= 0) {
                            fsVar = null;
                        } else {
                            fsVar = new j();
                            fsVar.E = new ArrayList(this.lS.lO);
                            this.lS.lO.clear();
                        }
                    }
                    if (fsVar != null && fsVar.E != null) {
                        if (na.isEnable()) {
                            StringBuilder stringBuilder = new StringBuilder(fsVar.E.size());
                            Iterator it = fsVar.E.iterator();
                            while (it.hasNext()) {
                                o oVar = (o) it.next();
                                stringBuilder.append(oVar.U + "|" + oVar.H + "|" + oVar.V + "|");
                            }
                        }
                        this.lS.lP.a(21, fsVar, new s(), 2, new lg(this) {
                            final /* synthetic */ a lT;

                            {
                                this.lT = r1;
                            }

                            public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
                            }
                        });
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private fc() {
        HandlerThread d = ((lq) fe.ad(4)).d("ConchHandler", 1);
        d.start();
        this.lN = new a(this, d.getLooper());
        this.lP = jq.cu();
    }

    private n a(long j, long j2, m mVar, int i) {
        n nVar = new n();
        nVar.U = j;
        nVar.V = j2;
        if (mVar != null) {
            nVar.H = mVar.H;
            nVar.O = mVar.O;
        }
        nVar.result = i;
        return nVar;
    }

    public static fc k() {
        if (lK == null) {
            synchronized (fc.class) {
                if (lK == null) {
                    lK = new fc();
                }
            }
        }
        return lK;
    }

    public void a(long j, final int i) {
        final b bVar = (b) this.lM.get(i);
        if (bVar != null) {
            fs kVar = new k();
            kVar.H = i;
            this.lP.a(11, kVar, new t(), 2, new lg(this) {
                final /* synthetic */ fc lS;

                public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
                    if (i3 != 0) {
                        bVar.wz = i3 - 65;
                    } else if (i4 != 0) {
                        bVar.wz = i4 - 65;
                    } else if (i2 != 10011) {
                        bVar.wz = ErrorCode.ERR_RESPONSE;
                    }
                    if (bVar.wz != 0) {
                        this.lS.lN.sendMessage(this.lS.lN.obtainMessage(0, i, 0, null));
                    } else if (fsVar == null || ((t) fsVar).ac == null || ((t) fsVar).ac.size() == 0) {
                        this.lS.lN.sendMessage(this.lS.lN.obtainMessage(0, i, 0, null));
                    } else {
                        fs iVar = new i();
                        iVar.E = new ArrayList();
                        Iterator it = ((t) fsVar).ac.iterator();
                        Object obj = null;
                        while (it.hasNext()) {
                            p pVar = (p) it.next();
                            if (pVar.Y == null || pVar.Y.size() == 0) {
                                iVar.E.add(this.lS.a(pVar.U, pVar.V, null, 3));
                            } else {
                                Iterator it2 = pVar.Y.iterator();
                                while (it2.hasNext()) {
                                    m mVar = (m) it2.next();
                                    if (mVar.H != i && this.lS.lM.get(mVar.H) == null) {
                                        iVar.E.add(this.lS.a(pVar.U, pVar.V, mVar, 5));
                                    } else {
                                        obj = 1;
                                        this.lS.lN.sendMessage(this.lS.lN.obtainMessage(0, new tmsdkobf.lb.a(pVar.U, pVar.V, mVar)));
                                        iVar.E.add(this.lS.a(pVar.U, pVar.V, mVar, 1));
                                    }
                                }
                            }
                        }
                        if (obj == null) {
                            this.lS.lN.sendMessage(this.lS.lN.obtainMessage(0, i, 0, null));
                        }
                        if (iVar.E.size() > 0) {
                            this.lS.lP.a(13, iVar, new r(), 2, null);
                        }
                    }
                }
            });
        }
    }

    public void a(long j, int i, b bVar) {
        if (bVar != null) {
            synchronized (this.lL) {
                if (this.lM.get(i) == null) {
                    this.lM.put(i, bVar);
                }
            }
        }
    }

    public void a(long j, long j2, long j3, int i, int i2, int i3, int i4) {
        o oVar = new o();
        oVar.U = j2;
        oVar.V = j3;
        oVar.H = i;
        oVar.O = i2;
        oVar.W = i3;
        switch (i3) {
            case 1:
                oVar.action = i4;
                break;
            case 2:
                oVar.X = i4;
                break;
            default:
                oVar.result = i4;
                break;
        }
        synchronized (this.lL) {
            this.lO.add(oVar);
        }
        this.lN.removeMessages(1);
        this.lN.sendMessage(this.lN.obtainMessage(1));
    }
}
