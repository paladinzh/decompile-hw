package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import tmsdk.common.TMSDKContext;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.d;
import tmsdk.fg.module.qscanner.AmScannerStatic;

/* compiled from: Unknown */
public class pd extends BaseManagerC implements tmsdkobf.pb.a, tmsdkobf.pb.c {
    private static pd GN;
    private boolean EA = false;
    private pb Ew;
    private ExecutorService FO;
    private boolean GO = false;
    private ArrayList<b> GP = new ArrayList();
    private pn<Long> GQ = new pn(1000);
    private oq GR;
    private boolean GS = false;
    private boolean GT = false;
    private boolean GU = false;
    private boolean GV = false;
    private boolean GW = false;
    private AtomicInteger GX = null;
    private AtomicInteger GY = null;
    private boolean GZ = false;
    private TreeMap<Integer, pl<fs, li, a>> Gq = new TreeMap();
    private boolean Ha = false;
    private LinkedList<ou> Hb = null;
    private final String TAG = "SharkProtocolQueue";
    private Context mContext;
    private Object mLock = new Object();
    private Handler yB = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ pd Hc;

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.Hc.yB.removeMessages(1);
                    Runnable cVar = new c();
                    synchronized (this.Hc.GP) {
                        Iterator it = this.Hc.GP.iterator();
                        while (it.hasNext()) {
                            b bVar = (b) it.next();
                            if ((bVar.GJ & 1073741824) != 0) {
                                cVar.Hk.add(bVar);
                            } else if (!bVar.Hi.dC()) {
                                cVar.a(Integer.valueOf(bVar.Fx), bVar);
                            }
                        }
                        this.Hc.GP.clear();
                    }
                    this.Hc.FO.submit(cVar);
                    return;
                case 2:
                    Object[] objArr = (Object[]) message.obj;
                    b bVar2 = (b) objArr[0];
                    if (bVar2.GC <= 0) {
                        bVar2.GK.onFinish(bVar2.Fx, ((Integer) objArr[1]).intValue(), ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), bVar2.GI);
                        return;
                    } else if (bVar2.Hg != null) {
                        bVar2.Hg.a(bVar2.Gm, bVar2.GC, bVar2.Fx, ((Integer) objArr[1]).intValue(), ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), bVar2.Hf);
                        return;
                    } else {
                        return;
                    }
                case 3:
                    int size;
                    int i;
                    synchronized (this.Hc.GP) {
                        size = this.Hc.GP.size();
                    }
                    if (size > 0) {
                        this.Hc.yB.sendEmptyMessage(1);
                    }
                    lk.wD = true;
                    if (this.Hc.GS) {
                        this.Hc.n(this.Hc.EA);
                    }
                    if (this.Hc.GT) {
                        this.Hc.refresh();
                    }
                    if (this.Hc.GU) {
                        this.Hc.fY();
                    }
                    if (this.Hc.GV) {
                        this.Hc.gn();
                    }
                    if (this.Hc.GW) {
                        this.Hc.gw();
                    }
                    if (this.Hc.GX != null) {
                        i = this.Hc.GX.get();
                        for (size = 0; size < i; size++) {
                            this.Hc.cd(2);
                        }
                        this.Hc.GX = null;
                    }
                    if (this.Hc.GY != null) {
                        i = this.Hc.GY.get();
                        for (size = 0; size < i; size++) {
                            this.Hc.ce(2);
                        }
                        this.Hc.GY = null;
                    }
                    if (this.Hc.GZ) {
                        this.Hc.gx();
                    }
                    if (this.Hc.Ha) {
                        this.Hc.gq();
                    }
                    if (this.Hc.Hb != null) {
                        Iterator it2 = this.Hc.Hb.iterator();
                        while (it2.hasNext()) {
                            ou ouVar = (ou) it2.next();
                            if (ouVar != null) {
                                this.Hc.a(ouVar.gc, ouVar.ET, ouVar.EU);
                            }
                        }
                        this.Hc.Hb = null;
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    /* compiled from: Unknown */
    public static class a {
        public boolean Hd;
        public long lU;

        public a(boolean z, long j) {
            this.Hd = z;
            this.lU = j;
        }
    }

    /* compiled from: Unknown */
    private class b {
        public int Fx;
        public int GC;
        public int GF;
        public long GG;
        public fs GH;
        public fs GI;
        public int GJ;
        public lg GK;
        public int Gm;
        final /* synthetic */ pd Hc;
        public byte[] He;
        public byte[] Hf;
        public lh Hg;
        public int Hh;
        public ll Hi;
        public long dF;
        public int dJ;
        public long qP = -1;
        public long qQ = 0;

        b(pd pdVar, int i, int i2, long j, int i3, fs fsVar, byte[] bArr, fs fsVar2, int i4, lg lgVar, lh lhVar) {
            this.Hc = pdVar;
            this.Gm = i;
            this.GC = i2;
            this.GG = j;
            this.GF = i3;
            this.GH = fsVar;
            this.He = bArr;
            this.GI = fsVar2;
            this.GJ = i4;
            this.GK = lgVar;
            this.Hg = lhVar;
            this.Hi = new ll();
        }
    }

    /* compiled from: Unknown */
    private class c implements Runnable {
        final /* synthetic */ pd Hc;
        private TreeMap<Integer, b> Hj;
        public ArrayList<b> Hk;
        private Handler Hl;

        private c(pd pdVar) {
            this.Hc = pdVar;
            this.Hj = new TreeMap();
            this.Hk = new ArrayList();
            this.Hl = new Handler(this, Looper.getMainLooper()) {
                final /* synthetic */ c Hm;

                public void handleMessage(Message message) {
                    super.handleMessage(message);
                    bq bqVar = new bq();
                    bqVar.dJ = -50004;
                    bqVar.dH = message.what;
                    d.e("SharkProtocolQueue", "seq : " + message.what + "超时");
                    this.Hm.a(bqVar);
                }
            };
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void a(bq bqVar) {
            byte[] bArr = null;
            this.Hl.removeMessages(bqVar.dH);
            synchronized (this.Hj) {
                b bVar = (b) this.Hj.get(Integer.valueOf(bqVar.dH));
                if (bVar != null) {
                    this.Hj.remove(Integer.valueOf(bqVar.dH));
                }
            }
        }

        private void a(b bVar, Integer num, Integer num2, Integer num3) {
            bVar.Hi.setState(2);
            if (bVar.GK != null) {
                switch (lk.bf(bVar.GJ)) {
                    case 8:
                        this.Hc.yB.sendMessage(this.Hc.yB.obtainMessage(2, new Object[]{bVar, num, num2, num3}));
                        break;
                    case 16:
                        if (bVar.Hg == null || bVar.GC <= 0) {
                            bVar.GK.onFinish(bVar.Fx, num.intValue(), num2.intValue(), num3.intValue(), bVar.GI);
                            break;
                        } else {
                            bVar.Hg.a(bVar.Gm, bVar.GC, bVar.Fx, num.intValue(), num2.intValue(), num3.intValue(), bVar.Hf);
                            break;
                        }
                        break;
                    default:
                        final b bVar2 = bVar;
                        final Integer num4 = num;
                        final Integer num5 = num2;
                        final Integer num6 = num3;
                        jq.ct().a(new Runnable(this) {
                            final /* synthetic */ c Hm;

                            public void run() {
                                if (bVar2.Hg != null && bVar2.GC > 0) {
                                    bVar2.Hg.a(bVar2.Gm, bVar2.GC, bVar2.Fx, num4.intValue(), num5.intValue(), num6.intValue(), bVar2.Hf);
                                } else {
                                    bVar2.GK.onFinish(bVar2.Fx, num4.intValue(), num5.intValue(), num6.intValue(), bVar2.GI);
                                }
                            }
                        }, "shark callback");
                        break;
                }
            }
        }

        private void b(boolean z, int i, int i2, ArrayList<bq> arrayList) {
            if (i != 0) {
                ci(pd.bX(i));
                return;
            }
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                bq bqVar = (bq) it.next();
                if (ch(bqVar.dH)) {
                    a(bqVar);
                } else {
                    if ((this.Hc.a(z, i2, bqVar) > 0 ? 1 : null) == null) {
                        d.f("SharkProtocolQueue", "No callback cmd : " + bqVar.aZ + " seqNo : " + bqVar.dG + " refSeqNo : " + bqVar.dH);
                    }
                }
            }
            ci(pd.bX(-500));
        }

        private void ci(int i) {
            Set<Entry> gu = gu();
            synchronized (this.Hj) {
                this.Hj.clear();
            }
            for (Entry entry : gu) {
                try {
                    a((b) entry.getValue(), Integer.valueOf(((b) entry.getValue()).GF), Integer.valueOf(i), Integer.valueOf(-1));
                } catch (Throwable e) {
                    d.b("SharkProtocolQueue", "callback crash", e);
                }
            }
        }

        public void a(Integer num, b bVar) {
            this.Hj.put(num, bVar);
        }

        public boolean ch(int i) {
            boolean containsKey;
            synchronized (this.Hj) {
                containsKey = this.Hj.containsKey(Integer.valueOf(i));
            }
            return containsKey;
        }

        public Set<Entry<Integer, b>> gu() {
            TreeMap treeMap;
            synchronized (this.Hj) {
                treeMap = (TreeMap) this.Hj.clone();
            }
            return treeMap.entrySet();
        }

        public void run() {
            long j = 0;
            ArrayList arrayList = new ArrayList();
            tmsdkobf.ox.b gh = this.Hc.Ew.gh();
            for (Entry entry : gu()) {
                if (!((b) entry.getValue()).Hi.dC()) {
                    ((b) entry.getValue()).Hi.setState(1);
                    bm bmVar = new bm();
                    bmVar.aZ = ((b) entry.getValue()).GF;
                    bmVar.dG = ((b) entry.getValue()).Fx;
                    bmVar.dI = ((b) entry.getValue()).GG;
                    bmVar.dH = 0;
                    bmVar.data = null;
                    if (TextUtils.isEmpty(gh.Ft)) {
                        d.c("SharkProtocolQueue", "mEncodeKey is empty");
                    }
                    if (((b) entry.getValue()).He == null) {
                        bmVar.data = ok.c(((b) entry.getValue()).GH);
                    } else {
                        try {
                            bmVar.data = ((b) entry.getValue()).He;
                        } catch (Exception e) {
                            d.c("SharkProtocolQueue", "run shark task e: " + e.toString());
                            ci(-10);
                            return;
                        }
                    }
                    if ((((b) entry.getValue()).qP <= 0 ? 1 : null) == null) {
                        d.g("SharkProtocolQueue", "对seq : " + bmVar.dG + "计时 : " + ((b) entry.getValue()).qP + "ms");
                        this.Hl.sendEmptyMessageDelayed(bmVar.dG, ((b) entry.getValue()).qP);
                    }
                    arrayList.add(bmVar);
                    if (this.Hc.cf(((b) entry.getValue()).GJ)) {
                        if ((((b) entry.getValue()).qQ <= j ? 1 : null) == null) {
                            j = ((b) entry.getValue()).qQ;
                        }
                    }
                }
            }
            Iterator it = this.Hk.iterator();
            while (it.hasNext()) {
                b bVar = (b) it.next();
                bm bmVar2 = new bm();
                bmVar2.aZ = bVar.GF;
                bmVar2.dG = oz.gj().fQ();
                bmVar2.dH = bVar.Fx;
                bmVar2.data = null;
                bmVar2.dJ = bVar.dJ;
                bmVar2.dK = bVar.Hh;
                bl blVar = new bl();
                blVar.dF = bVar.dF;
                bmVar2.dL = blVar;
                d.d("SharkProtocolQueue", "push.pushId: " + bVar.dF);
                if (TextUtils.isEmpty(gh.Ft)) {
                    d.c("SharkProtocolQueue", "mEncodeKey is empty");
                }
                try {
                    if (bVar.He == null) {
                        bmVar2.data = ok.c(bVar.GH);
                    } else {
                        bmVar2.data = bVar.He;
                    }
                } catch (Exception e2) {
                }
                arrayList.add(bmVar2);
            }
            this.Hc.Ew.a(j, true, arrayList, new tmsdkobf.pb.b(this) {
                final /* synthetic */ c Hm;

                {
                    this.Hm = r1;
                }

                public void a(boolean z, int i, int i2, ArrayList<bq> arrayList) {
                    this.Hm.b(z, i, i2, arrayList);
                }
            });
        }
    }

    public static int bX(int i) {
        switch (i) {
            case -500:
                return i - 8;
            case -400:
                return i - 5;
            case -300:
                return i - 5;
            case -200:
                return i - 9;
            case AmScannerStatic.ERR_EXPIRED /*-100*/:
                return i - 9;
            default:
                return i;
        }
    }

    private boolean cf(int i) {
        return v(i, 2);
    }

    private boolean cg(int i) {
        return v(i, 8);
    }

    private boolean v(int i, int i2) {
        return (i & i2) != 0;
    }

    public void J(boolean z) {
        d.d("gjj", "init is Test server : " + z);
        this.Ew = new pb(TMSDKContext.getApplicaionContext(), this.GR, this, this, z);
        this.FO = Executors.newSingleThreadExecutor();
        this.yB.sendEmptyMessage(3);
    }

    public long a(boolean z, int i, bq bqVar) {
        if (bqVar == null) {
            return -1;
        }
        if ((bqVar.dH != 0 ? null : 1) == null) {
            return -1;
        }
        long j = 0;
        if (bqVar.dT != null) {
            j = bqVar.dT.dF;
        }
        pa.a("ocean", "[ocean]push shark got it, ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z, null, null);
        synchronized (this.Gq) {
            pl plVar = (pl) this.Gq.get(Integer.valueOf(bqVar.aZ));
        }
        a(j, bqVar.aZ, i, bqVar.dG, -1000000001);
        if (plVar == null) {
            pa.c("ocean", "[ocean]push nobody handle, ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " isTcpChannel: " + z, null, bqVar);
            a(bqVar.dG, j, bqVar.aZ, null, -2, -1000000001);
            return -1;
        } else if (this.GQ.c(Long.valueOf(j))) {
            pa.c("ocean", "[ocean]push duplicate, ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG, null, bqVar);
            return -1;
        } else {
            byte[] a;
            fs fsVar;
            pl a2;
            this.GQ.push(Long.valueOf(j));
            pa.a("ocean", "[ocean]push before handle ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z, null, null);
            if (bqVar.data != null) {
                if (((a) plVar.In).Hd) {
                    a = ok.a(this.mContext, this.Ew.gh().Ft.getBytes(), bqVar.data);
                    fsVar = null;
                } else if (plVar.first != null) {
                    pa.a("ocean", "[ocean]push doing handle ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z + " pushName: " + ((fs) plVar.first).getClass().getName(), null, null);
                    try {
                        a = null;
                        fsVar = ok.a(this.mContext, this.Ew.gh().Ft.getBytes(), bqVar.data, (fs) plVar.first, true);
                    } catch (Exception e) {
                        pa.c("ocean", "[ocean]ERR decode: " + e.toString(), null, null);
                        a(bqVar.dG, j, bqVar.aZ, null, -1);
                    }
                }
                if (fsVar != null) {
                    pa.c("ocean", "[ocean]push after handle ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z + " req: " + fsVar, null, bqVar);
                } else {
                    pa.a("ocean", "[ocean]push after handle ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z + " req: " + fsVar, null, bqVar);
                }
                a2 = ((a) plVar.In).Hd ? ((li) plVar.second).a(bqVar.dG, j, bqVar.aZ, fsVar) : ((lj) plVar.second).a(bqVar.dG, j, bqVar.aZ, a);
                if (a2 != null) {
                    a(bqVar.dG, j, ((Integer) a2.second).intValue(), (fs) a2.In, 1);
                    pa.b("ocean", "[ocean]guid|" + this.Ew.c() + "|" + 1103 + "|ECmd|" + a2.second + "|pushId|" + j, null, null);
                }
                return ((a) plVar.In).lU;
            }
            a = null;
            fsVar = null;
            if (fsVar != null) {
                pa.a("ocean", "[ocean]push after handle ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z + " req: " + fsVar, null, bqVar);
            } else {
                pa.c("ocean", "[ocean]push after handle ECmd: " + bqVar.aZ + " seqNo: " + bqVar.dG + " pushId: " + j + " isTcpChannel: " + z + " req: " + fsVar, null, bqVar);
            }
            try {
                if (((a) plVar.In).Hd) {
                }
                a2 = ((a) plVar.In).Hd ? ((li) plVar.second).a(bqVar.dG, j, bqVar.aZ, fsVar) : ((lj) plVar.second).a(bqVar.dG, j, bqVar.aZ, a);
            } catch (Exception e2) {
                pa.c("ocean", "[ocean]ERR mode: " + e2.toString(), null, null);
                a2 = null;
            }
            if (a2 != null) {
                a(bqVar.dG, j, ((Integer) a2.second).intValue(), (fs) a2.In, 1);
                pa.b("ocean", "[ocean]guid|" + this.Ew.c() + "|" + 1103 + "|ECmd|" + a2.second + "|pushId|" + j, null, null);
            }
            return ((a) plVar.In).lU;
        }
    }

    public WeakReference<ll> a(int i, long j, int i2, fs fsVar, int i3) {
        return a(i, j, i2, fsVar, i3, 0);
    }

    public WeakReference<ll> a(int i, long j, int i2, fs fsVar, int i3, int i4) {
        d.d("SharkProtocolQueue", "sendPushResp() pushSeqNo: " + i + " pushId: " + j + " cmdId: " + i2 + " result: " + i3 + " retCode: " + i4);
        fs aiVar = new ai();
        aiVar.aZ = i2;
        aiVar.status = i3;
        if (fsVar != null) {
            aiVar.ba = ok.b(fsVar);
        }
        b bVar = new b(this, 0, 0, -1, 1103, fsVar, ok.c(aiVar), null, 1073741824, null, null);
        bVar.Fx = i;
        bVar.dF = j;
        bVar.dJ = i4;
        synchronized (this.GP) {
            this.GP.add(bVar);
        }
        if (lk.wD) {
            this.yB.sendEmptyMessage(1);
        }
        return new WeakReference(bVar.Hi);
    }

    public void a(int i, int i2, int i3) {
        if (lk.wD) {
            this.Ew.a(i, i2, i3);
            return;
        }
        if (this.Hb == null) {
            this.Hb = new LinkedList();
        }
        this.Hb.add(new ou(i, i2, i3));
    }

    public void a(long j, int i, int i2, int i3, int i4) {
        b bVar = new b(this, Process.myPid(), 0, 0, i, null, new byte[0], null, 1073741824, null, null);
        bVar.dJ = i4;
        bVar.Fx = i3;
        bVar.dF = j;
        synchronized (this.GP) {
            this.GP.add(bVar);
        }
        if (lk.wD) {
            this.yB.sendEmptyMessage(1);
        }
    }

    public void a(long j, int i, fs fsVar, int i2, li liVar, boolean z) {
        if (liVar != null) {
            ClassCastException classCastException;
            synchronized (this.Gq) {
                if (this.Gq.containsKey(Integer.valueOf(i))) {
                    pl plVar = (pl) this.Gq.get(Integer.valueOf(i));
                    if (plVar == null || plVar.In == null || !((a) plVar.In).Hd) {
                        classCastException = new ClassCastException("cmdId: " + i);
                    } else {
                        this.Gq.put(Integer.valueOf(i), new pl(fsVar, liVar, new a(z, j)));
                        classCastException = null;
                    }
                } else {
                    this.Gq.put(Integer.valueOf(i), new pl(fsVar, liVar, new a(z, j)));
                    classCastException = null;
                }
            }
            if (classCastException != null) {
                throw classCastException;
            }
            return;
        }
        throw new NullPointerException();
    }

    public void a(oq oqVar) {
        this.GR = oqVar;
        this.GO = this.GR.an();
    }

    public String c() {
        return this.Ew != null ? this.Ew.c() : "";
    }

    public WeakReference<ll> c(int i, int i2, int i3, long j, long j2, int i4, fs fsVar, byte[] bArr, fs fsVar2, int i5, lg lgVar, lh lhVar, long j3, long j4) {
        d.e("SharkProtocolQueue", "sendShark() cmdId: " + i4 + " pushSeqNo: " + i3);
        if (i3 > 0) {
            return a(i3, j, i4, fsVar, 1);
        }
        b bVar = new b(this, i, i2, j2, i4, fsVar, bArr, fsVar2, i5, lgVar, lhVar);
        bVar.Fx = oz.gj().fQ();
        bVar.qP = j3;
        bVar.qQ = j4;
        synchronized (this.GP) {
            this.GP.add(bVar);
        }
        if (lk.wD) {
            this.yB.sendEmptyMessage(1);
        }
        return new WeakReference(bVar.Hi);
    }

    public void cd(int i) {
        if (cf(i)) {
            if (lk.wD) {
                this.Ew.gl().gU();
                gx();
                if (cg(i)) {
                    this.Ew.gl().gT();
                }
            } else {
                if (this.GX == null) {
                    synchronized (this.mLock) {
                        if (this.GX == null) {
                            this.GX = new AtomicInteger(0);
                        }
                    }
                }
                this.GX.incrementAndGet();
            }
        }
    }

    public void ce(int i) {
        if (cf(i)) {
            if (lk.wD) {
                this.Ew.gl().closeConnection();
            } else {
                if (this.GY == null) {
                    synchronized (this.mLock) {
                        if (this.GY == null) {
                            this.GY = new AtomicInteger(0);
                        }
                    }
                }
                this.GY.incrementAndGet();
            }
        }
    }

    public void fY() {
        if (!lk.wD) {
            this.GU = true;
        } else if (this.Ew != null) {
            this.Ew.fY();
        }
    }

    public oq gm() {
        return this.GR;
    }

    public void gn() {
        if (!lk.wD) {
            this.GV = true;
        } else if (this.Ew != null) {
            this.Ew.gn();
        }
    }

    public void gq() {
        if (!lk.wD) {
            this.Ha = true;
        } else if (this.Ew != null) {
            this.Ew.gq();
        }
    }

    public boolean gv() {
        return this.GO;
    }

    public void gw() {
        if (lk.wD) {
            this.Ew.gl().gw();
        } else {
            this.GW = true;
        }
    }

    void gx() {
        if (lk.wD) {
            this.Ew.gl().hd();
        } else {
            this.GZ = true;
        }
    }

    public void n(boolean z) {
        if (lk.wD) {
            d.d("SharkProtocolQueue", "setIsTest is Test server : " + z);
            this.Ew.n(z);
            return;
        }
        this.GS = true;
        this.EA = z;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        GN = this;
        d.d("fuckShark", "SharkProtocolQueue()" + GN.hashCode());
    }

    public void onImsiChanged() {
        d.g("ImsiChecker", "onImsiChanged-mSharkNetwork:[" + this.Ew + "]");
        if (this.Ew != null) {
            this.Ew.fN();
        }
    }

    public void refresh() {
        if (lk.wD) {
            this.Ew.refresh();
        } else {
            this.GT = true;
        }
    }

    public li u(int i, int i2) {
        li liVar = null;
        synchronized (this.Gq) {
            if (this.Gq.containsKey(Integer.valueOf(i))) {
                liVar = (li) ((pl) this.Gq.remove(Integer.valueOf(i))).second;
            }
        }
        return liVar;
    }
}
