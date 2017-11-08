package tmsdkobf;

import tmsdkobf.lb.a;
import tmsdkobf.lb.b;

/* compiled from: Unknown */
public class me {
    private static me AA = null;
    private b AB = new b(this) {
        final /* synthetic */ me AC;

        {
            this.AC = r1;
        }

        public void a(a aVar) {
            boolean z = false;
            if (aVar != null && aVar.wx != null) {
                lb lbVar = (lb) fe.ad(17);
                if (1039 == aVar.wx.H) {
                    l lVar = (l) ot.a(aVar.wx.N, new l(), false);
                    if (com.tencent.tcuser.util.a.bt(lVar.I) != (byte) 1) {
                        fw.w().ai(0);
                    } else {
                        int bu = com.tencent.tcuser.util.a.bu(lVar.J);
                        fw.w().e((long) aVar.wx.P.A);
                        fw.w().ai(bu);
                    }
                } else if (1427 == aVar.wx.H) {
                    r1 = (q) ot.a(aVar.wx.N, new q(), false);
                    if (r1 == null || r1.aa == null || r1.aa.size() <= 0) {
                        lbVar.a(aVar, 3, 2);
                        return;
                    }
                    byte bt = com.tencent.tcuser.util.a.bt((String) r1.aa.get(0));
                    fw.w().f(Boolean.valueOf(bt == (byte) 1));
                    ma.p(29989, bt);
                } else if (615 == aVar.wx.H) {
                    if (com.tencent.tcuser.util.a.bt(((l) ot.a(aVar.wx.N, new l(), false)).I) == (byte) 1) {
                        z = true;
                    }
                    fw.w().k(Boolean.valueOf(z));
                    mf.eL();
                } else if (1445 == aVar.wx.H) {
                    r1 = (q) ot.a(aVar.wx.N, new q(), false);
                    if (r1 == null || r1.aa == null || r1.aa.size() <= 0) {
                        lbVar.a(aVar, 3, 2);
                        return;
                    }
                    if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1) {
                        z = true;
                    }
                    fw.w().i(Boolean.valueOf(z));
                } else if (1446 == aVar.wx.H) {
                    r1 = (q) ot.a(aVar.wx.N, new q(), false);
                    if (r1 == null || r1.aa == null || r1.aa.size() <= 0) {
                        lbVar.a(aVar, 3, 2);
                        return;
                    }
                    if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1) {
                        z = true;
                    }
                    fw.w().j(Boolean.valueOf(z));
                    new Thread(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 AE;

                        public void run() {
                            if (z) {
                                pg.gB().start();
                            } else {
                                pg.stop();
                            }
                        }
                    }).start();
                } else if (1463 == aVar.wx.H) {
                    r1 = (q) ot.a(aVar.wx.N, new q(), false);
                    if (r1 == null || r1.aa == null || r1.aa.size() <= 1) {
                        lbVar.a(aVar, 3, 2);
                        return;
                    }
                    boolean z2 = com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1;
                    fw.w().g(Boolean.valueOf(z2));
                    if (z2) {
                        ma.bx(1320011);
                    }
                    if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(1)) == (byte) 1) {
                        z = true;
                    }
                    fw.w().h(Boolean.valueOf(z));
                } else if (1466 == aVar.wx.H) {
                    r1 = (q) ot.a(aVar.wx.N, new q(), false);
                    if (r1 == null || r1.aa == null || r1.aa.size() <= 4) {
                        lbVar.a(aVar, 3, 2);
                        return;
                    }
                    fw.w().a(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(0)) == (byte) 1));
                    fw.w().b(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(1)) == (byte) 1));
                    fw.w().c(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(2)) == (byte) 1));
                    fw.w().d(Boolean.valueOf(com.tencent.tcuser.util.a.bt((String) r1.aa.get(3)) == (byte) 1));
                    if (com.tencent.tcuser.util.a.bt((String) r1.aa.get(4)) == (byte) 1) {
                        z = true;
                    }
                    fw.w().e(Boolean.valueOf(z));
                }
                lbVar.a(aVar, 3, 1);
            }
        }
    };

    public me() {
        lb lbVar = (lb) fe.ad(17);
        lbVar.a(1427, this.AB);
        lbVar.a(1039, this.AB);
        lbVar.a(615, this.AB);
        lbVar.a(-1, this.AB);
        lbVar.a(1445, this.AB);
        lbVar.a(1446, this.AB);
        lbVar.a(1463, this.AB);
        lbVar.a(1466, this.AB);
    }

    public static me eK() {
        if (AA == null) {
            synchronized (me.class) {
                if (AA == null) {
                    AA = new me();
                }
            }
        }
        return AA;
    }

    public void ac(int i) {
        ((lb) fe.ad(17)).ac(i);
    }
}
