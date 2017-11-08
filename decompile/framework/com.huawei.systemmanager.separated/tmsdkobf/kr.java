package tmsdkobf;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;
import tmsdkobf.kp.a;
import tmsdkobf.kp.b;

/* compiled from: Unknown */
public class kr {
    private static Object lock = new Object();
    private static kr wb;
    Handler handler;
    private SparseArray<kp> vY;
    HandlerThread vZ;
    Handler wa;

    private kr() {
        this.vY = new SparseArray();
        this.vZ = null;
        this.handler = null;
        this.wa = null;
        this.vZ = ((lq) fe.ad(4)).bF("ProfileServiceManager");
        this.vZ.start();
        this.wa = new Handler(this, this.vZ.getLooper()) {
            final /* synthetic */ kr wc;

            public void handleMessage(Message message) {
                int i = message.what;
                this.wc.wa.removeMessages(i);
                final kp kpVar = (kp) this.wc.vY.get(i);
                if (kpVar != null) {
                    this.wc.a(kpVar, new b(this) {
                        final /* synthetic */ AnonymousClass1 we;

                        public void q(ArrayList<a> arrayList) {
                            if (arrayList != null && arrayList.size() > 0) {
                                kpVar.p(arrayList);
                            }
                            if (!kpVar.dk()) {
                                this.we.wc.b(kpVar);
                            }
                        }
                    });
                    if (!kpVar.dk()) {
                        this.wc.b(kpVar);
                    }
                }
            }
        };
        this.handler = new Handler(this, this.vZ.getLooper()) {
            final /* synthetic */ kr wc;

            public void handleMessage(Message message) {
                kp kpVar;
                switch (message.what) {
                    case 1:
                        kq kqVar = (kq) message.obj;
                        ArrayList arrayList = kqVar.vX;
                        kpVar = kqVar.vW;
                        if (!(arrayList == null || arrayList.size() <= 0 || kpVar == null)) {
                            kpVar.p(arrayList);
                            break;
                        }
                    case 2:
                        Object obj = message.obj;
                        if (obj != null && (obj instanceof kp)) {
                            kpVar = (kp) obj;
                            this.wc.vY.remove(kpVar.dl());
                            this.wc.vY.append(kpVar.dl(), kpVar);
                            kpVar.a(new hw(this) {
                                final /* synthetic */ AnonymousClass2 wf;

                                public void a(int i, ArrayList<fs> arrayList, int i2, int i3) {
                                    if (i2 == 0) {
                                        kpVar.o(this.wf.wc.a(i, (ArrayList) arrayList));
                                        if (i == 0) {
                                            kpVar.aZ(i3);
                                        }
                                    }
                                }
                            });
                            break;
                        }
                        return;
                        break;
                    case 3:
                        Integer num = (Integer) message.obj;
                        if (num != null) {
                            kpVar = (kp) this.wc.vY.get(num.intValue());
                            if (kpVar != null) {
                                kpVar.dm();
                                break;
                            }
                            return;
                        }
                        return;
                    case 4:
                        this.wc.b((kp) this.wc.vY.get(((Integer) message.obj).intValue()));
                        break;
                }
            }
        };
    }

    private void a(kp kpVar, b bVar) {
        if (kpVar != null) {
            hu.h("ProfileServiceManager", "MSG_FULL_CHECK id : " + kpVar.dl());
            kpVar.a(bVar);
        }
    }

    private void b(final kp kpVar) {
        if (kpVar != null) {
            kpVar.b(new b(this) {
                final /* synthetic */ kr wc;

                public void q(ArrayList<a> arrayList) {
                    if (arrayList != null && arrayList.size() > 0) {
                        Message.obtain(this.wc.handler, 1, new kq(kpVar, arrayList)).sendToTarget();
                    }
                }
            });
        }
    }

    public static kr do() {
        if (wb == null) {
            synchronized (lock) {
                if (wb == null) {
                    wb = new kr();
                }
            }
        }
        return wb;
    }

    protected ArrayList<a> a(int i, ArrayList<fs> arrayList) {
        ArrayList<a> arrayList2 = new ArrayList();
        if (arrayList == null || arrayList.size() == 0) {
            return arrayList2;
        }
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            fs fsVar = (fs) it.next();
            if (fsVar != null) {
                a aVar = new a();
                aVar.vV = fsVar;
                aVar.action = i;
                arrayList2.add(aVar);
            }
        }
        return arrayList2;
    }

    public void a(kp kpVar) {
        if (kpVar != null && this.handler != null) {
            Message.obtain(this.handler, 2, kpVar).sendToTarget();
        }
    }

    public void ba(int i) {
        Message.obtain(this.wa, i).sendToTarget();
    }

    public void bb(int i) {
        Message.obtain(this.handler, 4, Integer.valueOf(i)).sendToTarget();
    }
}
