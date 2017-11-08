package tmsdkobf;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.f;
import tmsdk.common.utils.h;

/* compiled from: Unknown */
public class ly {
    static a Ak = null;
    static volatile boolean Al = false;

    /* compiled from: Unknown */
    static class a extends jj implements tmsdkobf.ps.a {
        public static boolean Ao = false;

        a() {
        }

        public void cn() {
        }

        public void co() {
            if (jq.cq()) {
                ly.eo();
            }
            mi.bA(2);
        }

        public void doOnRecv(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                int i = ("android.intent.action.TIME_SET".equals(action) || "android.intent.action.TIMEZONE_CHANGED".equals(action)) ? 4 : !"android.intent.action.USER_PRESENT".equals(action) ? !"tmsdk.common.ccrreport".equals(action) ? -1 : 1 : 3;
                if (i != -1) {
                    if (jq.cq()) {
                        ly.ep();
                    }
                    mi.bA(i);
                }
            }
        }

        public synchronized void h(Context context) {
            if (!Ao) {
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction("android.intent.action.USER_PRESENT");
                intentFilter.setPriority(Integer.MAX_VALUE);
                context.registerReceiver(this, intentFilter);
                ps t = ps.t(context);
                if (t != null) {
                    t.b(this);
                }
                Ao = true;
            }
        }
    }

    public static void bw(int i) {
        fw.w().ah(i);
    }

    static boolean co(String str) {
        long currentTimeMillis = System.currentTimeMillis();
        long A = fw.w().A();
        if (currentTimeMillis <= A) {
            if (!(Math.abs(currentTimeMillis - A) < 86400000)) {
                return true;
            }
        }
        if (!(currentTimeMillis - A < 86400000)) {
            return true;
        }
        Calendar instance = Calendar.getInstance();
        instance.set(11, 0);
        instance.set(12, 0);
        instance.set(13, 0);
        long es = (((long) es()) * 1000) + instance.getTimeInMillis();
        if (!(es <= currentTimeMillis)) {
            es -= 86400000;
        }
        if (!(A > es)) {
            return true;
        }
        return false;
    }

    public static void en() {
        if (Ak == null) {
            Ak = new a();
            Ak.h(TMSDKContext.getApplicaionContext());
        }
        na.s("ccrService", "initAtBg()");
    }

    public static void eo() {
        if (!Al) {
            if (!co("FirstCheck")) {
                if (f.B(TMSDKContext.getApplicaionContext())) {
                    ((lq) fe.ad(4)).a(new Runnable() {
                        public void run() {
                            if (!ly.Al) {
                                md.eH().eI();
                            }
                        }
                    }, "withcheck");
                }
            } else if (f.iu()) {
                ((lq) fe.ad(4)).a(new Runnable() {
                    public void run() {
                        if (!ly.Al) {
                            ly.Al = true;
                            ly.er();
                            me.eK().ac(-1);
                            mc.eF();
                            md.eH().eI();
                            mb.eE();
                            ly.Al = false;
                        }
                    }
                }, "withcheck");
            }
        }
    }

    public static void ep() {
        if (!Al && co("FirstCheck") && f.iu()) {
            ((lq) fe.ad(4)).a(new Runnable() {
                public void run() {
                    if (!ly.Al) {
                        ly.Al = true;
                        ly.er();
                        me.eK().ac(-1);
                        mc.eF();
                        md.eH().eI();
                        mb.eE();
                        ly.Al = false;
                    }
                }
            }, "WithoutCheck");
        }
    }

    public static void eq() {
        if (jq.cq()) {
            ma.bx(29987);
        } else {
            ma.bx(29988);
        }
    }

    private static synchronized void er() {
        synchronized (ly.class) {
            try {
                final ma et = ma.et();
                if (fw.w().C().booleanValue()) {
                    if (!mi.k(TMSDKContext.getApplicaionContext())) {
                        ma.bx(1320026);
                    } else if (mi.l(TMSDKContext.getApplicaionContext())) {
                        ma.bx(1320024);
                    } else {
                        ma.bx(1320025);
                    }
                    ArrayList arrayList = new ArrayList();
                    Collection ex = et.ex();
                    if (ex != null) {
                        arrayList.addAll(ex);
                    }
                    ex = et.eB();
                    if (ex != null) {
                        arrayList.addAll(ex);
                    }
                    final long currentTimeMillis = System.currentTimeMillis();
                    ex = et.ez();
                    if (ex != null) {
                        arrayList.addAll(ex);
                    }
                    if (arrayList.size() > 0) {
                        jq.cu().a(v(arrayList), new tmsdkobf.lm.a() {
                            public void a(int i, qs qsVar) {
                                if (i == 0) {
                                    et.ey();
                                    et.eC();
                                    et.eA();
                                    fw.w().b(currentTimeMillis);
                                }
                            }
                        });
                    } else {
                        return;
                    }
                }
                et.ey();
                et.eC();
                et.eA();
                return;
            } catch (Throwable th) {
            }
        }
    }

    static int es() {
        int z = fw.w().z();
        if (z > 0) {
            return z;
        }
        z = m(1, 20);
        bw(z);
        return z;
    }

    static int m(int i, int i2) {
        int i3 = i2 - i;
        if (i3 < 0) {
            return -1;
        }
        long j = 0;
        try {
            j = Long.parseLong(h.C(TMSDKContext.getApplicaionContext()));
        } catch (Throwable th) {
        }
        Random random = new Random();
        random.setSeed(j + ((System.currentTimeMillis() + ((long) System.identityHashCode(random))) + ((long) System.identityHashCode(fw.w()))));
        return (((((int) (random.nextDouble() * ((double) i3))) + i) * 3600) + (((int) (random.nextDouble() * 60.0d)) * 60)) + ((int) (random.nextDouble() * 60.0d));
    }

    static qs v(ArrayList<dx> arrayList) {
        qo ic = ((qt) ManagerCreatorC.getManager(qt.class)).ic();
        qs qsVar = new qs(17, new qq("report", "reportSoftUsageInfo"));
        HashMap hashMap = new HashMap(2);
        hashMap.put("suikey", ic.hP());
        hashMap.put("vecsui", arrayList);
        qsVar.Ky = hashMap;
        return qsVar;
    }
}
