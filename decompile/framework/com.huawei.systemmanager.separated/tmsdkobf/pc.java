package tmsdkobf;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import tmsdk.common.creator.ManagerCreatorC;
import tmsdk.common.utils.d;
import tmsdk.common.utils.f;

/* compiled from: Unknown */
public final class pc {
    private static pc Gr = null;
    private oq FC;
    private Handler FT = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ pc Gs;

        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case 0:
                    this.Gs.a((a) message.obj);
                    return;
                default:
                    return;
            }
        }
    };
    private tmsdkobf.oz.a Gl;
    private int Gm = Process.myPid();
    private ExecutorService Gn;
    private ArrayList<a> Go = new ArrayList();
    private TreeMap<Integer, a> Gp = new TreeMap();
    private TreeMap<Integer, Pair<fs, li>> Gq = new TreeMap();
    private Handler yB = new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ pc Gs;

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    this.Gs.yB.removeMessages(1);
                    Object bVar = new b();
                    synchronized (this.Gs.Go) {
                        Iterator it = this.Gs.Go.iterator();
                        while (it.hasNext()) {
                            a aVar = (a) it.next();
                            bVar.a(Integer.valueOf(aVar.GC), aVar);
                            if ((aVar.GJ & 1073741824) == 0) {
                                this.Gs.Gp.put(Integer.valueOf(aVar.GC), aVar);
                            }
                            d.e("SharkProcessProxy", this.Gs.Gm + " sendShark() MSG_SEND_PROXY_TASK task.mIpcSeqNo: " + aVar.GC);
                        }
                        d.e("SharkProcessProxy", this.Gs.Gm + " sendShark() MSG_SEND_PROXY_TASK send size: " + this.Gs.Go.size());
                        this.Gs.Go.clear();
                    }
                    this.Gs.Gn.submit(bVar);
                    d.e("SharkProcessProxy", "taskrun.mProxyTaskQueue.size() : " + bVar.GM.size());
                    d.e("SharkProcessProxy", this.Gs.Gm + " sendShark() MSG_SEND_PROXY_TASK all cache size: " + this.Gs.Gp.size());
                    return;
                case 2:
                    Object[] objArr = (Object[]) message.obj;
                    a aVar2 = (a) objArr[0];
                    if (aVar2.GK != null) {
                        aVar2.GK.onFinish(((Integer) objArr[1]).intValue(), aVar2.GF, ((Integer) objArr[2]).intValue(), ((Integer) objArr[3]).intValue(), aVar2.GI);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    /* compiled from: Unknown */
    private class a {
        public int GC;
        public int GD;
        public long GE;
        public int GF;
        public long GG;
        public fs GH;
        public fs GI;
        public int GJ;
        public lg GK;
        public long GL = -1;
        public int Gm;
        final /* synthetic */ pc Gs;
        public long mTimeout = -1;

        a(pc pcVar, int i, int i2, int i3, long j, long j2, int i4, fs fsVar, fs fsVar2, int i5, lg lgVar, long j3, long j4) {
            this.Gs = pcVar;
            this.Gm = i;
            this.GC = i2;
            this.GD = i3;
            this.GE = j;
            this.GF = i4;
            this.GG = j2;
            this.GH = fsVar;
            this.GI = fsVar2;
            this.GJ = i5;
            this.GK = lgVar;
            this.mTimeout = j3;
            this.GL = j4;
        }
    }

    /* compiled from: Unknown */
    private class b implements Runnable {
        private TreeMap<Integer, a> GM;
        final /* synthetic */ pc Gs;

        private b(pc pcVar) {
            this.Gs = pcVar;
            this.GM = new TreeMap();
        }

        public void a(Integer num, a aVar) {
            this.GM.put(num, aVar);
        }

        public Set<Entry<Integer, a>> gu() {
            TreeMap treeMap;
            synchronized (this.GM) {
                treeMap = (TreeMap) this.GM.clone();
            }
            return treeMap.entrySet();
        }

        public void run() {
            boolean hv = f.hv();
            for (Entry entry : gu()) {
                if (hv) {
                    d.d("SharkProcessProxy", this.Gs.Gm + " onPostToSendingProcess() mPid: " + ((a) entry.getValue()).Gm + " mCallerIdent: " + ((a) entry.getValue()).GG + " mIpcSeqNo: " + ((a) entry.getValue()).GC + " mPushSeqNo: " + ((a) entry.getValue()).GD + " mPushId: " + ((a) entry.getValue()).GE + " mCmdId: " + ((a) entry.getValue()).GF + " mFlag: " + ((a) entry.getValue()).GJ + " mTimeout: " + ((a) entry.getValue()).mTimeout);
                    this.Gs.FC.a(((a) entry.getValue()).Gm, ((a) entry.getValue()).GG, ((a) entry.getValue()).GC, ((a) entry.getValue()).GD, ((a) entry.getValue()).GE, ((a) entry.getValue()).GF, ok.c(((a) entry.getValue()).GH), ((a) entry.getValue()).GJ, ((a) entry.getValue()).mTimeout, ((a) entry.getValue()).GL);
                    this.Gs.FT.sendMessageDelayed(Message.obtain(this.Gs.FT, 0, entry.getValue()), 185000);
                } else {
                    d.d("SharkProcessProxy", this.Gs.Gm + " run, 无物理网络");
                    this.Gs.a(Process.myPid(), ((a) entry.getValue()).GC, 0, ((a) entry.getValue()).GF, null, -2, 0);
                }
            }
        }
    }

    private pc(oq oqVar) {
        this.FC = oqVar;
        this.Gl = new tmsdkobf.oz.a();
        this.Gn = Executors.newSingleThreadExecutor();
    }

    private void a(final a aVar) {
        d.e("SharkProcessProxy", "runTimeout() sharkProxyTask: " + aVar.GC);
        this.FT.removeMessages(0, aVar);
        if (this.Gp.containsKey(Integer.valueOf(aVar.GC))) {
            jq.ct().a(new Runnable(this) {
                final /* synthetic */ pc Gs;

                public void run() {
                    this.Gs.a(Process.myPid(), aVar.GC, 0, aVar.GF, null, oi.bY(-50000), 0);
                }
            }, "sharkProcessProxyTimeout");
        }
    }

    public static synchronized pc gt() {
        pc pcVar;
        synchronized (pc.class) {
            if (Gr == null) {
                Gr = new pc(((pd) ManagerCreatorC.getManager(pd.class)).gm());
            }
            pcVar = Gr;
        }
        return pcVar;
    }

    public void a(int i, int i2, int i3, int i4, byte[] bArr, int i5, int i6) {
        if (this.Gm == i) {
            final int i7 = i2;
            final byte[] bArr2 = bArr;
            final int i8 = i4;
            final int i9 = i3;
            final int i10 = i5;
            final int i11 = i6;
            jq.ct().a(new Runnable(this) {
                final /* synthetic */ pc Gs;

                public void run() {
                    synchronized (this.Gs.Gp) {
                        a aVar = (a) this.Gs.Gp.get(Integer.valueOf(i7));
                        if (aVar != null) {
                            fs c = ok.c(bArr2, aVar.GI);
                            if (aVar.GI != c) {
                                aVar.GI = c;
                            }
                            aVar.GF = i8;
                            d.d("SharkProcessProxy", this.Gs.Gm + " callBack() ipcSeqNo: " + i7 + " seqNo: " + i9 + " cmdId: " + i8 + " retCode: " + i10 + " dataRetCode: " + i11);
                            this.Gs.a(aVar, Integer.valueOf(i9), Integer.valueOf(i10), Integer.valueOf(i11));
                            this.Gs.Gp.remove(Integer.valueOf(i7));
                            return;
                        }
                        d.c("SharkProcessProxy", this.Gs.Gm + " callBack() empty callback by ipcSeqNo: " + i7);
                    }
                }
            }, "shark callback");
            return;
        }
        d.f("SharkProcessProxy", this.Gm + " callBack() not my pid's response, its pid is: " + i);
    }

    public void a(int i, long j, int i2, long j2, int i3, fs fsVar, fs fsVar2, int i4, lg lgVar, long j3, long j4) {
        d.e("SharkProcessProxy", this.Gm + " sendShark()");
        a aVar = new a(this, i, this.Gl.fP(), i2, j2, j, i3, fsVar, fsVar2, i4, lgVar, j3, j4);
        synchronized (this.Go) {
            this.Go.add(aVar);
        }
        this.yB.sendEmptyMessage(1);
    }

    public void a(long j, int i, fs fsVar, int i2, li liVar) {
        ClassCastException classCastException;
        synchronized (this.Gq) {
            d.e("SharkProcessProxy", this.Gm + " registerSharkPush() callIdent: " + j + " cmdId: " + i + " flag: " + i2);
            if (this.Gq.containsKey(Integer.valueOf(i))) {
                classCastException = new ClassCastException();
            } else {
                this.Gq.put(Integer.valueOf(i), new Pair(fsVar, liVar));
                final long j2 = j;
                final int i3 = i;
                final int i4 = i2;
                jq.ct().a(new Runnable(this) {
                    final /* synthetic */ pc Gs;

                    public void run() {
                        this.Gs.FC.a(j2, i3, i4);
                    }
                }, "shark regist push");
                classCastException = null;
            }
        }
        if (classCastException != null) {
            throw classCastException;
        }
    }

    protected void a(a aVar, Integer num, Integer num2, Integer num3) {
        if (aVar.GK != null) {
            pa.a("ocean", "[ocean]procallback: ECmd|" + aVar.GF + "|ipcSeqNo|" + aVar.GC + "|seqNo|" + num + "|ret|" + num2 + "|dataRetCode|" + num3 + "|ident|" + aVar.GG, null, null);
            switch (lk.bf(aVar.GJ)) {
                case 8:
                    this.yB.sendMessage(this.yB.obtainMessage(2, new Object[]{aVar, num, num2, num3}));
                    break;
                case 16:
                    aVar.GK.onFinish(num.intValue(), aVar.GF, num2.intValue(), num3.intValue(), aVar.GI);
                    break;
                default:
                    aVar.GK.onFinish(num.intValue(), aVar.GF, num2.intValue(), num3.intValue(), aVar.GI);
                    break;
            }
        }
    }

    public li v(final int i, final int i2) {
        li liVar = null;
        synchronized (this.Gq) {
            d.e("SharkProcessProxy", this.Gm + "unregisterSharkPush() cmdId: " + i + " flag: " + i2);
            if (this.Gq.containsKey(Integer.valueOf(i))) {
                liVar = (li) ((Pair) this.Gq.remove(Integer.valueOf(i))).second;
                jq.ct().a(new Runnable(this) {
                    final /* synthetic */ pc Gs;

                    public void run() {
                        this.Gs.FC.t(i, i2);
                    }
                }, "shark unregist push");
            }
        }
        return liVar;
    }
}
