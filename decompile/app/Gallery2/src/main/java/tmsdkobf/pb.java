package tmsdkobf;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.SparseArray;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.TMSDKContext;
import tmsdk.common.utils.f;

/* compiled from: Unknown */
public class pb implements tmsdkobf.pk.a {
    private final int FA = 1;
    private final int FB = 2;
    private oq FC;
    private ph FD;
    private ox FE;
    private ol FF;
    private String FG = "";
    private f FH;
    private SparseArray<qi> FI = null;
    private int FJ = 0;
    private d FK = null;
    private d FL = null;
    private ArrayList<d> FM = new ArrayList();
    private LinkedHashMap<Integer, d> FN = new LinkedHashMap();
    private ExecutorService FO;
    private boolean FP;
    private c FQ;
    private li FR;
    private tmsdkobf.ph.b FS = new tmsdkobf.ph.b(this) {
        final /* synthetic */ pb FU;

        {
            this.FU = r1;
        }

        public void a(boolean z, int i, byte[] bArr) {
            int bX;
            if (-160000 == i) {
                bX = oi.bX(i);
                tmsdk.common.utils.d.c("SharkNetwork", "spSend() ESharkCode.ERR_NEED_WIFIAPPROVEMENT == ret, doneRet: " + bX);
                this.FU.a(false, z, bX);
            } else if (i != 0) {
                bX = oi.bX(i);
                tmsdk.common.utils.d.c("SharkNetwork", "spSend() ESharkCode.ERR_NONE != ret, doneRet: " + bX);
                this.FU.a(false, z, bX);
            } else if (bArr != null) {
                tmsdk.common.utils.d.e("SharkNetwork", "spSend() retData.length: " + bArr.length);
                try {
                    br s = ot.s(bArr);
                    if (s != null) {
                        d a;
                        br brVar = s;
                        ArrayList arrayList = brVar.dV;
                        tmsdk.common.utils.d.e("SharkNetwork", "spSend() respSashimiList.size(): " + arrayList.size());
                        int i2 = brVar.dH;
                        if (this.FU.FK != null && this.FU.FK.Gg == i2) {
                            a = this.FU.FK;
                            this.FU.FK = null;
                        } else if (this.FU.FL != null && this.FU.FL.Gg == i2) {
                            a = this.FU.FL;
                            this.FU.FL = null;
                        } else {
                            a = (d) this.FU.FN.get(Integer.valueOf(i2));
                        }
                        tmsdk.common.utils.d.e("SharkNetwork", "spSend() seqNoTag: " + i2 + " ssTag: " + a);
                        boolean a2 = this.FU.A(arrayList);
                        if (arrayList != null) {
                            tmsdk.common.utils.d.d("SharkNetwork", "spSend() 收到shark回包，密钥是否过期：" + (!a2 ? "否" : "是"));
                            tmsdk.common.utils.d.e("SharkNetwork", "spSend() retShark.seqNo: " + brVar.dG + " respSashimiList.size(): " + arrayList.size());
                            if (a2) {
                                tmsdk.common.utils.d.e("SharkNetwork", "spSend() 密钥过期");
                                pa.b("ocean", "[ocean]密钥过期，自动交换密钥重发", null, null);
                                this.FU.FP = true;
                                if (a != null) {
                                    this.FU.b(a);
                                }
                                this.FU.yB.sendEmptyMessage(1);
                                return;
                            }
                            tmsdk.common.utils.d.e("SharkNetwork", "spSend() 收到shark回包");
                            this.FU.a(z, a, 0, brVar.dG, this.FU.a(a, z, brVar, arrayList));
                            return;
                        }
                        tmsdk.common.utils.d.c("SharkNetwork", "spSend() null == respSashimiList");
                        this.FU.a(z, a, -5, brVar.dG, null);
                        return;
                    }
                    tmsdk.common.utils.d.c("SharkNetwork", "spSend() null == obj");
                } catch (Exception e) {
                    tmsdk.common.utils.d.c("SharkNetwork", "spSend() e: " + e.toString());
                }
            } else {
                tmsdk.common.utils.d.c("SharkNetwork", "spSend() null == retData");
            }
        }
    };
    private Handler FT = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ pb FU;

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 0:
                    this.FU.a((d) message.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private final int Fz = 0;
    private final String TAG = "SharkNetwork";
    private Context mContext;
    private Handler yB = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ pb FU;

        private void b(final boolean z, final d dVar) {
            this.FU.FO.submit(new Runnable(this) {
                final /* synthetic */ AnonymousClass2 FX;

                public void run() {
                    if (this.FX.FU.FI != null) {
                        ArrayList arrayList = dVar.Gj;
                        if (arrayList != null && arrayList.size() > 0) {
                            Iterator it = arrayList.iterator();
                            while (it.hasNext()) {
                                bm bmVar = (bm) it.next();
                                if (bmVar != null) {
                                    qi qiVar;
                                    synchronized (this.FX.FU.FI) {
                                        qiVar = (qi) this.FX.FU.FI.get(bmVar.aZ);
                                    }
                                    if (qiVar != null) {
                                        if (qiVar.hK()) {
                                            qiVar.hL();
                                        } else {
                                            it.remove();
                                            pa.b("SharkNetwork", "network ctrl cmdid : " + bmVar.aZ, null, null);
                                            bq bqVar = new bq();
                                            bqVar.aZ = bmVar.aZ;
                                            bqVar.dJ = -7;
                                            this.FX.FU.a(true, false, dVar, -7, 0, bqVar);
                                        }
                                    }
                                }
                            }
                        }
                        if (arrayList != null && arrayList.size() > 0) {
                            synchronized (this.FX.FU.FI) {
                                qi qiVar2 = (qi) this.FX.FU.FI.get(997);
                                if (qiVar2 != null) {
                                    if (!qiVar2.hK()) {
                                        dVar.Ge = true;
                                    }
                                }
                            }
                        } else {
                            return;
                        }
                    }
                    try {
                        this.FX.FU.a(z, dVar);
                    } catch (Exception e) {
                        tmsdk.common.utils.d.c("SharkNetwork", e);
                    }
                }
            });
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 0:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND_VIP");
                    this.FU.yB.removeMessages(0);
                    if (this.FU.FK != null || this.FU.FL != null) {
                        if (this.FU.FK == null) {
                            if (this.FU.FL != null) {
                                tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND_VIP mSharkSendGuid");
                                b(true, this.FU.FL);
                                break;
                            }
                        }
                        tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND_VIP mSharkSendRsa");
                        b(false, this.FU.FK);
                        break;
                    }
                    tmsdk.common.utils.d.c("SharkNetwork", "MSG_SHARK_SEND_VIP null");
                    return;
                    break;
                case 1:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND");
                    this.FU.yB.removeMessages(1);
                    tmsdkobf.ox.b gh = this.FU.FE.gh();
                    if (TextUtils.isEmpty(gh.Fs) || TextUtils.isEmpty(gh.Ft)) {
                        if (2 != this.FU.FJ) {
                            this.FU.FJ = 1;
                            this.FU.yB.sendEmptyMessage(2);
                            return;
                        }
                        tmsdk.common.utils.d.e("SharkNetwork", "MSG_SHARK_SEND 正在交换密钥");
                        return;
                    } else if (this.FU.FP) {
                        tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND 密钥过期");
                        this.FU.yB.sendEmptyMessage(2);
                        return;
                    } else if (this.FU.FF.fL()) {
                        if (5 != this.FU.FJ) {
                            this.FU.FJ = 4;
                            this.FU.yB.sendEmptyMessage(3);
                            return;
                        }
                        tmsdk.common.utils.d.e("SharkNetwork", "MSG_SHARK_SEND 正在注册guid");
                        return;
                    } else if (this.FU.FM.size() > 0) {
                        ArrayList arrayList;
                        synchronized (this.FU.FM) {
                            arrayList = (ArrayList) this.FU.FM.clone();
                            this.FU.FM.clear();
                        }
                        if (this.FU.FK != null || this.FU.FL != null) {
                            tmsdk.common.utils.d.f("SharkNetwork", "MSG_SHARK_SEND  mSharkSendRsa: " + this.FU.FK + " mSharkSendGuid: " + this.FU.FL);
                        }
                        Iterator it = arrayList.iterator();
                        while (it.hasNext()) {
                            d dVar = (d) it.next();
                            if (dVar != null) {
                                if (this.FU.FP) {
                                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_SEND sending 密钥过期");
                                    this.FU.yB.sendEmptyMessage(2);
                                    return;
                                }
                                b(true, dVar);
                            }
                        }
                        break;
                    } else {
                        return;
                    }
                case 2:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_UPDATE_RSAKEY");
                    this.FU.yB.removeMessages(2);
                    this.FU.FO.submit(new Runnable(this) {
                        final /* synthetic */ AnonymousClass2 FX;

                        {
                            this.FX = r1;
                        }

                        public void run() {
                            try {
                                if (this.FX.FU.FJ != 2) {
                                    this.FX.FU.FJ = 2;
                                    this.FX.FU.FE.a(new tmsdkobf.ox.a(this) {
                                        final /* synthetic */ AnonymousClass2 FY;

                                        {
                                            this.FY = r1;
                                        }

                                        public void I(boolean z) {
                                            tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_UPDATE_RSAKEY " + (!z ? "失败" : "成功"));
                                            this.FY.FX.FU.FJ = 3;
                                            if (z) {
                                                if (this.FY.FX.FU.FN.size() > 0) {
                                                    synchronized (this.FY.FX.FU.FM) {
                                                        synchronized (this.FY.FX.FU.FN) {
                                                            if (this.FY.FX.FU.FN.values().size() > 0) {
                                                                this.FY.FX.FU.FM.addAll(0, this.FY.FX.FU.FN.values());
                                                                this.FY.FX.FU.FN.clear();
                                                            }
                                                        }
                                                    }
                                                }
                                                this.FY.FX.FU.FP = false;
                                                this.FY.FX.FU.yB.sendEmptyMessage(1);
                                                return;
                                            }
                                            this.FY.FX.FU.a(true, false, oi.bX(-100));
                                        }
                                    });
                                    return;
                                }
                                pa.x("SharkNetwork", "is updating rsa_key.ingore it.");
                            } catch (Exception e) {
                                tmsdk.common.utils.d.c("SharkNetwork", "MSG_SHARK_UPDATE_RSAKEY e: " + e.toString());
                            }
                        }
                    });
                    break;
                case 3:
                    tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_GET_GUID");
                    this.FU.yB.removeMessages(3);
                    this.FU.FO.submit(new Runnable(this) {
                        final /* synthetic */ AnonymousClass2 FX;

                        {
                            this.FX = r1;
                        }

                        public void run() {
                            try {
                                if (this.FX.FU.FJ != 5) {
                                    this.FX.FU.FJ = 5;
                                    this.FX.FU.FF.a(new tmsdkobf.ol.a(this) {
                                        final /* synthetic */ AnonymousClass3 FZ;

                                        {
                                            this.FZ = r1;
                                        }

                                        public void c(boolean z, String str) {
                                            tmsdk.common.utils.d.d("SharkNetwork", "MSG_SHARK_GET_GUID " + (!z ? "失败" : "成功"));
                                            this.FZ.FX.FU.FJ = 6;
                                            if (z) {
                                                this.FZ.FX.FU.yB.sendEmptyMessage(1);
                                            } else {
                                                this.FZ.FX.FU.a(true, false, oi.bX(-200));
                                            }
                                        }
                                    });
                                    return;
                                }
                                pa.x("SharkNetwork", "is geting guid.ingore it.");
                            } catch (Exception e) {
                                tmsdk.common.utils.d.c("SharkNetwork", "MSG_SHARK_GET_GUID e: " + e.toString());
                                this.FX.FU.a(true, false, oi.bX(-200));
                            }
                        }
                    });
                    break;
                case 4:
                    this.FU.FO.submit(new Runnable(this) {
                        final /* synthetic */ AnonymousClass2 FX;

                        {
                            this.FX = r1;
                        }

                        public void run() {
                            if (this.FX.FU.FF != null) {
                                this.FX.FU.FF.G(true);
                            }
                        }
                    });
                    break;
            }
        }
    };

    /* compiled from: Unknown */
    public interface b {
        void a(boolean z, int i, int i2, ArrayList<bq> arrayList);
    }

    /* compiled from: Unknown */
    public interface a {
        void a(long j, int i, fs fsVar, int i2, li liVar, boolean z);
    }

    /* compiled from: Unknown */
    public interface c {
        long a(boolean z, int i, bq bqVar);
    }

    /* compiled from: Unknown */
    public static class d {
        public boolean Ga = false;
        public boolean Gb = false;
        public boolean Gc = false;
        public boolean Gd = false;
        public boolean Ge = false;
        public boolean Gf = false;
        public int Gg;
        public tmsdkobf.ox.b Gh;
        public long Gi;
        public ArrayList<bm> Gj;
        public b Gk;

        public d(boolean z, boolean z2, boolean z3, long j, ArrayList<bm> arrayList, b bVar) {
            this.Ga = z;
            this.Gb = z2;
            this.Gc = z3;
            this.Gi = j;
            this.Gj = arrayList;
            this.Gk = bVar;
        }

        public boolean gr() {
            return this.Gb || this.Gc;
        }

        public boolean gs() {
            return this.Ga;
        }
    }

    public pb(Context context, oq oqVar, c cVar, a aVar, boolean z) {
        tmsdk.common.utils.d.d("SharkNetwork", "SharkNetwork() isTest: " + z);
        this.mContext = context;
        this.FC = oqVar;
        this.FQ = cVar;
        this.FE = new ox(context, this);
        this.FF = new ol(context, this);
        this.FO = Executors.newSingleThreadExecutor();
        this.FD = new ph(this.FC.an(), context, oqVar, z, this.FS, this, aVar);
        a(aVar);
        int myPid = Process.myPid();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(!this.FC.an() ? "f" : "b");
        stringBuilder.append(myPid);
        this.FG = stringBuilder.toString();
        pa.b("ocean", "ext: " + this.FG, null, null);
    }

    private boolean A(ArrayList<bq> arrayList) {
        if (arrayList == null || arrayList.size() != 1) {
            return false;
        }
        bq bqVar = (bq) arrayList.get(0);
        if (bqVar == null) {
            return false;
        }
        if (!(2 == bqVar.dJ)) {
            return false;
        }
        tmsdk.common.utils.d.d("SharkNetwork", "checkRsa() ERC_EXPIRE retCode: " + bqVar.dJ + " dataRetCode: " + bqVar.dK);
        return true;
    }

    private final ArrayList<bq> a(d dVar, boolean z, br brVar, ArrayList<bq> arrayList) {
        if (arrayList == null) {
            return null;
        }
        ArrayList<bq> arrayList2 = new ArrayList();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            bq bqVar = (bq) arrayList.get(i);
            if (bqVar != null) {
                tmsdk.common.utils.d.d("SharkNetwork", "checkFilteList() rs.seqNo: " + bqVar.dG + " rs.cmd: " + bqVar.aZ + " rs.retCode: " + bqVar.dJ + " rs.dataRetCode: " + bqVar.dK);
                if (bqVar.data != null) {
                    tmsdk.common.utils.d.d("SharkNetwork", "checkFilteList() rs.data.length: " + bqVar.data.length);
                }
                if (!a(z, brVar, bqVar)) {
                    arrayList2.add(bqVar);
                }
            }
        }
        return arrayList2;
    }

    private bn a(d dVar, boolean z, boolean z2, tmsdkobf.ox.b bVar, ArrayList<bm> arrayList) {
        bn gg = ot.gg();
        int fQ = oz.gk().fQ();
        gg.dG = fQ;
        dVar.Gg = fQ;
        dVar.Gh = bVar;
        gg.dO = 2;
        gg.dP = a(z, bVar);
        gg.dQ = arrayList;
        return gg;
    }

    private f a(boolean z, tmsdkobf.ox.b bVar) {
        if (this.FH == null) {
            this.FH = new f();
        }
        String str = bVar == null ? this.FH.t : bVar.Fs;
        f fVar = this.FH;
        if (z) {
            str = "";
        }
        fVar.t = str;
        this.FH.u = TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD);
        this.FH.q = go();
        this.FH.v = f.A(this.mContext);
        this.FH.authType = gp();
        this.FH.r = this.FF.c();
        this.FH.s = this.FG;
        if (bVar != null) {
            tmsdk.common.utils.d.d("SharkNetwork", "checkSharkfin() rsaKeyCode: " + bVar.hashCode() + " rsaKey: " + bVar + " mSharkfin.buildno: " + this.FH.u + " mSharkfin.apn: " + this.FH.q + " mSharkfin.netType: " + this.FH.v + " mSharkfin.authType: " + this.FH.authType + " mSharkfin.guid: " + this.FH.r + " mSharkfin.ext1: " + this.FH.s);
            tmsdk.common.utils.d.g("ocean", "[ocean]info: guid|" + this.FH.r);
            pa.a("ocean", "[ocean]info: SdKy: sessionId|" + this.FH.t + "|encodeKey|" + bVar.Ft, null, null);
            tmsdk.common.utils.d.e("ocean", "checkSharkfin() rsaKeyCode: " + bVar.hashCode() + " rsaKey: " + bVar + " mSharkfin.buildno: " + this.FH.u + " mSharkfin.apn: " + this.FH.q + " mSharkfin.netType: " + this.FH.v + " mSharkfin.authType: " + this.FH.authType + " mSharkfin.guid: " + this.FH.r + " mSharkfin.ext1: " + this.FH.s);
        }
        return this.FH;
    }

    private pl<Long, Integer, fs> a(long j, int i, d dVar) {
        os gI = this.FD.gI();
        if (gI != null) {
            gI.a(dVar.hash, i, dVar.c, dVar.d, dVar.e, dVar.f);
        }
        int W = this.FD.gI().W();
        tmsdk.common.utils.d.d("SharkNetwork", "handleIpList() 收到ip列表，hash: " + W + " hashSeqNo: " + this.FD.gI().X() + " pushId: " + j);
        if (W == 0) {
            return null;
        }
        this.FD.gI().t(0, 0);
        a aVar = new a();
        aVar.hash = W;
        tmsdk.common.utils.d.d("SharkNetwork", "handleIpList() 处理ip列表成功");
        return new pl(Long.valueOf(j), Integer.valueOf(151), aVar);
    }

    private void a(a aVar) {
        this.FR = new li(this) {
            final /* synthetic */ pb FU;

            {
                this.FU = r1;
            }

            public pl<Long, Integer, fs> a(int i, long j, int i2, fs fsVar) {
                if (fsVar != null) {
                    switch (i2) {
                        case 10150:
                            return this.FU.a(j, i, (d) fsVar);
                        default:
                            return null;
                    }
                }
                tmsdk.common.utils.d.c("SharkNetwork", "onRecvPush() null == push");
                return null;
            }
        };
        aVar.a(0, 10150, new d(), 0, this.FR, false);
    }

    private void a(final d dVar) {
        tmsdk.common.utils.d.e("SharkNetwork", "runTimeout() sharkSend.seqNoTag: " + dVar.Gg);
        this.FT.removeMessages(0, dVar);
        if (this.FN.containsKey(Integer.valueOf(dVar.Gg))) {
            jq.ct().a(new Runnable(this) {
                final /* synthetic */ pb FU;

                public void run() {
                    if (this.FU.cc(dVar.Gg) != null) {
                        tmsdk.common.utils.d.d("SharkNetwork", "runTimeout() really timeout, sharkSend.seqNoTag: " + dVar.Gg);
                        this.FU.a(dVar.Gd, dVar, oi.bX(-50000), 0, null);
                    }
                }
            }, "runTimeout");
        }
    }

    private void a(boolean z, d dVar, int i, int i2, ArrayList<bq> arrayList) {
        a(false, z, dVar, i, i2, (ArrayList) arrayList);
    }

    private void a(boolean z, boolean z2, int i) {
        tmsdk.common.utils.d.e("SharkNetwork", "runError() mSharkQueueSendingBySeqNo.size(): " + this.FN.size());
        ArrayList arrayList = new ArrayList();
        synchronized (this.FN) {
            if (z) {
                arrayList.addAll(this.FN.values());
                this.FN.clear();
            } else {
                for (d dVar : this.FN.values()) {
                    if (z2 == dVar.Gd || dVar.gr()) {
                        arrayList.add(dVar);
                    }
                }
                this.FN.values().removeAll(arrayList);
            }
        }
        synchronized (this.FM) {
            arrayList.addAll(this.FM);
            this.FM.clear();
        }
        tmsdk.common.utils.d.e("SharkNetwork", "runError() values.size(): " + arrayList.size());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            a(z2, (d) it.next(), i, 0, null);
        }
    }

    private void a(boolean z, boolean z2, d dVar, int i, int i2, ArrayList<bq> arrayList) {
        try {
            a(false, false, false, dVar, i, (ArrayList) arrayList);
            if (dVar != null) {
                if (z) {
                    if (dVar.Gj != null && dVar.Gj.size() > 0) {
                        dVar.Gk.a(z2, i, i2, arrayList);
                    }
                }
                cc(dVar.Gg);
                dVar.Gk.a(z2, i, i2, arrayList);
            }
        } catch (Throwable e) {
            tmsdk.common.utils.d.a("SharkNetwork", "runError() callback crash", e);
        }
        this.FC.ar(i);
    }

    private void a(boolean z, boolean z2, d dVar, int i, int i2, bq bqVar) {
        ArrayList arrayList = new ArrayList();
        arrayList.add(bqVar);
        a(z, z2, dVar, i, i2, arrayList);
    }

    private void a(boolean z, boolean z2, boolean z3, d dVar, int i, ArrayList<bq> arrayList) {
        if (dVar != null) {
            int size;
            int i2;
            bm bmVar;
            if (z2 && dVar.Gj != null) {
                size = dVar.Gj.size();
                for (i2 = 0; i2 < size; i2++) {
                    bmVar = (bm) dVar.Gj.get(i2);
                    if (bmVar != null) {
                        pa.a("ocean", "[ocean]info: Used: sessionId|" + (dVar.Gh == null ? "" : dVar.Gh.Fs) + "|encodeKey|" + (dVar.Gh == null ? "" : dVar.Gh.Ft), bmVar, null);
                        if (z) {
                            pa.a("ocean", "[ocean]guid|" + this.FF.c() + "|send|" + (!z3 ? "" : "转") + "通道|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bmVar.aZ + "|seqNo|" + bmVar.dG + "|refSeqNo|" + bmVar.dH + "|ret|" + i + "|ident|" + bmVar.dI + (bmVar.dL == null ? "" : "|pushId|" + bmVar.dL.dF), bmVar, null);
                        } else {
                            pa.b("ocean", "[ocean]guid|" + this.FF.c() + "|send|" + (!z3 ? "" : "转") + "通道|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bmVar.aZ + "|seqNo|" + bmVar.dG + "|refSeqNo|" + bmVar.dH + "|ret|" + i + "|ident|" + bmVar.dI + "|size|" + (bmVar.data == null ? 0 : bmVar.data.length) + (bmVar.dL == null ? "" : "|pushId|" + bmVar.dL.dF), bmVar, null);
                        }
                    }
                }
            }
            if (!z2 && arrayList != null && arrayList.size() > 0) {
                size = arrayList.size();
                for (i2 = 0; i2 < size; i2++) {
                    bq bqVar = (bq) arrayList.get(i2);
                    if (bqVar != null) {
                        if (i == 0 && bqVar.dJ == 0 && bqVar.dK == 0) {
                            pa.b("ocean", "[ocean]guid|" + this.FF.c() + "|recv|" + "通道|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bqVar.aZ + "|seqNo|" + bqVar.dG + "|refSeqNo|" + bqVar.dH + "|ret|" + i + "|retCode|" + bqVar.dJ + "|dataRetCode|" + bqVar.dK + "|size|" + (bqVar.data == null ? 0 : bqVar.data.length), null, bqVar);
                        } else {
                            pa.c("ocean", "[ocean]guid|" + this.FF.c() + "|recv|" + "通道|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + bqVar.aZ + "|seqNo|" + bqVar.dG + "|refSeqNo|" + bqVar.dH + "|ret|" + i + "|retCode|" + bqVar.dJ + "|dataRetCode|" + bqVar.dK, null, bqVar);
                        }
                    }
                }
            } else if (!(z2 || dVar == null || dVar.Gj == null || i == 0)) {
                size = dVar.Gj.size();
                for (i2 = 0; i2 < size; i2++) {
                    bmVar = (bm) dVar.Gj.get(i2);
                    if (bmVar != null && bmVar.aZ < 10000) {
                        pa.c("ocean", "[ocean]guid|" + this.FF.c() + "|recv|" + "通道|" + (!dVar.Gd ? "http|" : "tcp|") + "sharkSeqNo|" + dVar.Gg + "|ECmd|" + (bmVar.aZ >= 10000 ? bmVar.aZ : bmVar.aZ + 10000) + "|ret|" + i, bmVar, null);
                    }
                }
            }
        }
    }

    private boolean a(boolean z, br brVar, bq bqVar) {
        if (bqVar == null) {
            return false;
        }
        boolean z2 = bqVar.dH == 0;
        if (z2) {
            pa.b("ocean", "[ocean]guid|" + this.FF.c() + "|push|" + "通道|" + (!z ? "http|" : "tcp|") + "sharkSeqNo|" + brVar.dG + "|ECmd|" + bqVar.aZ + "|seqNo|" + bqVar.dG + "|refSeqNo|" + bqVar.dH + "|ret|" + 0 + "|ident|" + this.FQ.a(z, brVar.dG, bqVar) + (bqVar.dT == null ? "" : "|pushId|" + bqVar.dT.dF), null, bqVar);
        }
        return z2;
    }

    private void b(d dVar) {
        if (dVar == null || dVar.Gj == null || dVar.Gh == null || dVar.Gh.Ft == null) {
            tmsdk.common.utils.d.c("SharkNetwork", "revertClientSashimiData() something null");
            return;
        }
        Iterator it = dVar.Gj.iterator();
        while (it.hasNext()) {
            bm bmVar = (bm) it.next();
            if (!(bmVar == null || bmVar.data == null)) {
                bmVar.data = ok.decrypt(bmVar.data, dVar.Gh.Ft.getBytes());
            }
        }
    }

    private d cc(int i) {
        d dVar;
        tmsdk.common.utils.d.e("SharkNetwork", "removeSendingBySeqNoTag() seqNoTag: " + i);
        synchronized (this.FN) {
            dVar = (d) this.FN.remove(Integer.valueOf(i));
        }
        return dVar;
    }

    private int go() {
        if (!ml.Bc) {
            ml.Bc = false;
            ml.n(this.mContext);
        }
        if ((byte) 3 == ml.Bd) {
            return 1;
        }
        switch (ml.Bf) {
            case (byte) 0:
                return 2;
            case (byte) 1:
                return 3;
            case (byte) 2:
                return 4;
            case (byte) 3:
                return 5;
            case (byte) 4:
                return 6;
            case (byte) 5:
                return 7;
            case (byte) 6:
                return 8;
            case (byte) 7:
                return 9;
            case (byte) 8:
                return 10;
            default:
                return 0;
        }
    }

    private int gp() {
        switch (tmsdk.common.utils.c.ir()) {
            case 0:
                return 0;
            case 1:
                return 1;
            case 2:
                return 2;
            default:
                return 0;
        }
    }

    protected tmsdkobf.ox.b a(boolean z, d dVar) {
        tmsdkobf.ox.b bVar = null;
        tmsdk.common.utils.d.e("SharkNetwork", "spSend() 开始发shark包");
        if (dVar == null) {
            return null;
        }
        bm bmVar;
        if (z) {
            bVar = this.FE.gh();
            if (bVar == null) {
                tmsdk.common.utils.d.c("SharkNetwork", "spSend() RsaKey is null");
            }
            Iterator it = dVar.Gj.iterator();
            while (it.hasNext()) {
                bmVar = (bm) it.next();
                if (!(bmVar == null || bmVar.data == null)) {
                    bmVar.data = ok.encrypt(bmVar.data, bVar.Ft.getBytes());
                }
            }
        }
        fs a = a(dVar, !z, true, bVar, dVar.Gj);
        if (!(dVar == null || dVar.Gj == null || dVar.Gj.size() <= 0)) {
            Iterator it2 = dVar.Gj.iterator();
            while (it2.hasNext()) {
                bmVar = (bm) it2.next();
                if (bmVar != null && bmVar.dH == 0) {
                    dVar.Gf = true;
                }
            }
        }
        byte[] d = ot.d(a);
        tmsdk.common.utils.d.e("SharkNetwork", "spSend() sendData.length: " + d.length);
        synchronized (this.FN) {
            tmsdk.common.utils.d.e("SharkNetwork", "spSend() sharkSend.seqNoTag: " + dVar.Gg);
            this.FN.put(Integer.valueOf(dVar.Gg), dVar);
        }
        this.FT.sendMessageDelayed(Message.obtain(this.FT, 0, dVar), 180000);
        this.FD.a(dVar, d, new tmsdkobf.ph.a(this) {
            final /* synthetic */ pb FU;

            {
                this.FU = r1;
            }

            public void a(boolean z, int i, d dVar) {
                this.FU.a(false, true, z, dVar, i, null);
            }
        });
        return bVar;
    }

    public void a(int i, int i2, int i3) {
        if (i2 > 0) {
            if (this.FI == null) {
                synchronized (pb.class) {
                    if (this.FI == null) {
                        this.FI = new SparseArray();
                    }
                }
            }
            qi qiVar = new qi("network_control_" + i, (long) (i2 * 1000), i3);
            synchronized (this.FI) {
                this.FI.append(i, qiVar);
                tmsdk.common.utils.d.e("SharkNetwork", "handleNetworkControl : cmdid|" + i + "|timeSpan|" + i2 + "|maxTimes|" + i3);
            }
        }
    }

    public void a(long j, boolean z, ArrayList<bm> arrayList, b bVar) {
        a(false, false, j, (ArrayList) arrayList, bVar);
        if (z) {
            this.FF.G(false);
        }
    }

    protected void a(ArrayList<bm> arrayList, b bVar) {
        if (bVar == null) {
            throw new NullPointerException();
        } else if (arrayList.size() > 0) {
            this.FK = new d(true, false, false, 0, arrayList, bVar);
            this.yB.sendEmptyMessage(0);
        } else {
            tmsdk.common.utils.d.c("SharkNetwork", "asyncSendSharkRsa() empty list");
        }
    }

    public void a(boolean z, boolean z2, long j, ArrayList<bm> arrayList, b bVar) {
        if (bVar == null) {
            throw new NullPointerException();
        } else if (arrayList.size() > 0) {
            d dVar = new d(false, z, z2, j, arrayList, bVar);
            a(true, true, false, dVar, 0, null);
            synchronized (this.FM) {
                this.FM.add(dVar);
                tmsdk.common.utils.d.d("SharkNetwork", "asyncSendShark() mSharkQueueWaiting.size(): " + this.FM.size());
            }
            this.yB.sendEmptyMessage(1);
        } else {
            tmsdk.common.utils.d.c("SharkNetwork", "asyncSendShark() empty list");
        }
    }

    protected void b(ArrayList<bm> arrayList, b bVar) {
        if (bVar == null) {
            throw new NullPointerException();
        } else if (arrayList.size() > 0) {
            this.FL = new d(true, false, false, 0, arrayList, bVar);
            this.yB.sendEmptyMessage(0);
        } else {
            tmsdk.common.utils.d.c("SharkNetwork", "asyncSendSharkGuid() empty list");
        }
    }

    public String c() {
        return this.FF.c();
    }

    public void fN() {
        this.FF.fN();
    }

    public void fY() {
        os gI = this.FD.gI();
        if (gI != null) {
            gI.fY();
        }
    }

    protected tmsdkobf.ox.b gh() {
        return this.FE.gh();
    }

    public pk gl() {
        return this.FD.gl();
    }

    protected oq gm() {
        return this.FC;
    }

    public void gn() {
        boolean z = false;
        String av = this.FC.av();
        String ax = this.FC.ax();
        if (ax != null) {
            if (TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_BUILD) == this.FC.at() && TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT) == this.FC.au() && ax.equals(av)) {
                this.FF.G(z);
            }
        } else if (av != null) {
            this.FF.G(z);
        }
        z = true;
        this.FF.G(z);
    }

    public void gq() {
        if (this.yB != null) {
            tmsdk.common.utils.d.e("SharkNetwork", "onGuidInfoChange()");
            this.yB.removeMessages(4);
            this.yB.sendEmptyMessageDelayed(4, 15000);
        }
    }

    public void n(boolean z) {
        this.FD.n(z);
    }

    public void refresh() {
        tmsdk.common.utils.d.d("SharkNetwork", "refresh()");
        this.FF.fM();
    }
}
